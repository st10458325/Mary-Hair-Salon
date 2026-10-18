package za.co.maryhairsalon.data.repository

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import za.co.maryhairsalon.data.local.AppDatabase
import za.co.maryhairsalon.data.local.toDomain
import za.co.maryhairsalon.data.local.toEntity
import za.co.maryhairsalon.data.model.Booking
import za.co.maryhairsalon.data.model.BookingRequest
import za.co.maryhairsalon.data.model.BookingRules
import za.co.maryhairsalon.data.model.BookingStatus
import za.co.maryhairsalon.data.remote.BookingDetailsDto
import za.co.maryhairsalon.data.remote.Db
import za.co.maryhairsalon.data.remote.TakenTimeDto
import za.co.maryhairsalon.data.remote.offlineFirst

/**
 * Bookings for all three roles.
 *
 * Reads come from the booking_details view (Row Level Security limits each user to the
 * bookings they may see). Writes go through the database API functions in 003_api.sql,
 * which read the price from the services table, validate the slot, and stop double
 * bookings with a unique index (spec FR-05 / NFR-04).
 */
class BookingRepository(
    private val supabase: SupabaseClient,
    private val db: AppDatabase,
    private val currentUid: () -> String?,
) {
    // ---------------- Reads (offline-first) ----------------

    /** Customer "My Bookings". */
    fun observeMyBookings(uid: String): Flow<List<Booking>> = offlineFirst(
        name = "myBookings",
        refresh = { refreshMyBookings(uid) },
        fromRoom = db.bookingDao().observeForCustomer(uid).map { list -> list.map { it.toDomain() } },
    )

    /** Live counts per status for the filter chips on "My Bookings" (spec FR-06). */
    fun observeMyStatusCounts(uid: String): Flow<Map<BookingStatus, Int>> =
        db.bookingDao().observeStatusCountsForCustomer(uid).map { rows ->
            BookingStatus.entries.associateWith { status ->
                rows.filter { BookingStatus.from(it.status) == status }.sumOf { it.count }
            }
        }

    /** Stylist schedule (spec FR-07). */
    fun observeSchedule(stylistId: String): Flow<List<Booking>> = offlineFirst(
        name = "schedule",
        refresh = { refreshSchedule(stylistId) },
        fromRoom = db.bookingDao().observeForStylist(stylistId).map { list -> list.map { it.toDomain() } },
    )

    /** Manager oversight (spec FR-08). */
    fun observeAllBookings(): Flow<List<Booking>> = offlineFirst(
        name = "allBookings",
        refresh = ::refreshAllBookings,
        fromRoom = db.bookingDao().observeAll().map { list -> list.map { it.toDomain() } },
    )

    fun observeBooking(id: String): Flow<Booking?> = db.bookingDao().observe(id).map { it?.toDomain() }

    /** Pull-to-refresh for whichever list the screen shows. */
    suspend fun refreshMine(): Result<Unit> = safeCall<Unit>(TAG, "refreshMine") {
        refreshMyBookings(currentUid() ?: throw UserFacingException("Please log in first."))
    }

    suspend fun refreshScheduleNow(): Result<Unit> = safeCall<Unit>(TAG, "refreshSchedule") {
        refreshSchedule(currentUid() ?: throw UserFacingException("Please log in first."))
    }

    suspend fun refreshAll(): Result<Unit> = safeCall<Unit>(TAG, "refreshAll") { refreshAllBookings() }

    /**
     * Times already taken for a stylist on a date (e.g. ["09:00", "14:00"]), so the booking
     * form can grey them out. Comes straight from the database; returns only times, no names.
     */
    suspend fun takenTimes(stylistId: String, date: String): Result<List<String>> = safeCall(TAG, "takenTimes") {
        supabase.postgrest.rpc(Db.RPC_TAKEN_TIMES, buildJsonObject {
            put("p_stylist_id", stylistId)
            put("p_date", date)
        }).decodeList<TakenTimeDto>().map { it.bookingTime }
    }

    private suspend fun fetchBookings(column: String?, value: String?): List<Booking> =
        supabase.from(Db.BOOKING_DETAILS).select {
            if (column != null && value != null) filter { eq(column, value) }
        }.decodeList<BookingDetailsDto>().map { it.toDomain() }

    private suspend fun refreshMyBookings(uid: String) =
        db.bookingDao().replaceForCustomer(uid, fetchBookings("customer_id", uid).map { it.toEntity() })

    private suspend fun refreshSchedule(stylistId: String) =
        db.bookingDao().replaceForStylist(stylistId, fetchBookings("stylist_id", stylistId).map { it.toEntity() })

    private suspend fun refreshAllBookings() =
        db.bookingDao().replaceAll(fetchBookings(null, null).map { it.toEntity() })

    /** After a write, re-download just that booking so every screen updates. */
    private suspend fun refreshOne(id: String) {
        val row = supabase.from(Db.BOOKING_DETAILS)
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull<BookingDetailsDto>()
        row?.let { db.bookingDao().upsert(it.toDomain().toEntity()) }
    }

    // ---------------- Writes (database API functions) ----------------

    /** Creates a booking with status "pending". Returns the new booking id. */
    suspend fun createBooking(request: BookingRequest): Result<String> = safeCall(TAG, "createBooking") {
        currentUid() ?: throw UserFacingException("Please log in to book an appointment.")
        validateOrThrow(request)
        val id = supabase.postgrest.rpc(Db.RPC_CREATE_BOOKING, buildJsonObject {
            putBookingFields(request)
        }).decodeAs<String>()
        refreshOne(id)
        Log.i(TAG, "Created booking $id for ${request.date} ${request.time}")
        id
    }

    /**
     * Customer reschedule (spec FR-06): moves the booking to a new stylist/date/time and
     * back to "pending" so the stylist re-confirms.
     */
    suspend fun reschedule(bookingId: String, request: BookingRequest): Result<Unit> = safeCall<Unit>(TAG, "reschedule") {
        validateOrThrow(request)
        supabase.postgrest.rpc(Db.RPC_RESCHEDULE_BOOKING, buildJsonObject {
            put("p_booking_id", bookingId)
            putBookingFields(request)
        })
        refreshOne(bookingId)
        Log.i(TAG, "Rescheduled booking $bookingId to ${request.date} ${request.time}")
    }

    /** Customer cancel. Frees the time slot for someone else. */
    suspend fun cancel(bookingId: String): Result<Unit> = changeStatus(bookingId, BookingStatus.CANCELLED)

    /** Stylist accept (pending -> upcoming). */
    suspend fun accept(bookingId: String): Result<Unit> = changeStatus(bookingId, BookingStatus.UPCOMING)

    /** Stylist decline (pending -> cancelled). */
    suspend fun decline(bookingId: String): Result<Unit> = changeStatus(bookingId, BookingStatus.CANCELLED)

    /** Stylist/manager: upcoming -> completed. */
    suspend fun complete(bookingId: String): Result<Unit> = changeStatus(bookingId, BookingStatus.COMPLETED)

    /**
     * Status change. The database decides who may do what (set_booking_status in 003_api.sql):
     * customers may only cancel, stylists follow pending -> upcoming -> completed, managers
     * may set any status (spec FR-08).
     */
    suspend fun changeStatus(bookingId: String, newStatus: BookingStatus): Result<Unit> = safeCall<Unit>(TAG, "changeStatus") {
        supabase.postgrest.rpc(Db.RPC_SET_BOOKING_STATUS, buildJsonObject {
            put("p_booking_id", bookingId)
            put("p_status", newStatus.dbValue)
        })
        refreshOne(bookingId)
        Log.i(TAG, "Booking $bookingId -> ${newStatus.dbValue}")
    }

    /** Manager: reassign the stylist; the database re-checks the slot (spec FR-08). */
    suspend fun reassignStylist(bookingId: String, newStylistId: String): Result<Unit> = safeCall<Unit>(TAG, "reassignStylist") {
        supabase.postgrest.rpc(Db.RPC_REASSIGN_STYLIST, buildJsonObject {
            put("p_booking_id", bookingId)
            put("p_stylist_id", newStylistId)
        })
        refreshOne(bookingId)
        Log.i(TAG, "Reassigned booking $bookingId to $newStylistId")
    }

    // ---------------- helpers ----------------

    private fun validateOrThrow(request: BookingRequest) {
        val errors = BookingRules.validate(request)
        if (errors.isNotEmpty()) throw UserFacingException(errors.joinToString("\n"))
    }

    /** Parameter names must match the function arguments in 003_api.sql exactly. */
    private fun JsonObjectBuilder.putBookingFields(r: BookingRequest) {
        put("p_service_id", r.serviceId)
        put("p_stylist_id", r.stylistId)
        put("p_type", r.type.dbValue)
        put("p_location", r.location.trim())
        put("p_date", r.date)
        put("p_time", r.time)
        put("p_notes", r.notes.trim())
        put("p_name", r.name.trim())
        put("p_surname", r.surname.trim())
        put("p_contact", r.contact.trim())
    }

    companion object {
        private const val TAG = "BookingRepository"
    }
}
