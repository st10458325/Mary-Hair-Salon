package za.co.maryhairsalon.data.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/**
 * Business rules shared by the repository and the UI. The database enforces the same
 * rules again (003_api.sql); checking here first gives instant feedback in the form.
 * Pure Kotlin (no Android / Supabase), so it is covered by plain JVM unit tests.
 */
object BookingRules {

    /** Bookable hours, 08:00–19:00 (same as the website and the bookings_time_slot check). */
    val TIME_SLOTS: List<String> = (8..19).map { String.format(Locale.US, "%02d:00", it) }

    private val DISPLAY_DATE = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH)

    /** e.g. "2026-10-14" -> "Wed, 14 Oct 2026". Falls back to the raw value if unparsable. */
    fun formatDate(date: String): String =
        try { LocalDate.parse(date).format(DISPLAY_DATE) } catch (e: DateTimeParseException) { date }

    /** South African cell number: 0XXXXXXXXX or +27XXXXXXXXX (spaces allowed). */
    fun isValidContact(contact: String): Boolean =
        contact.replace(" ", "").matches(Regex("^(0\\d{9}|\\+27\\d{9})$"))

    /**
     * Validates a booking before it is sent to Supabase.
     * Returns a list of user-friendly error messages; empty means valid.
     */
    fun validate(request: BookingRequest, today: LocalDate = LocalDate.now()): List<String> {
        val errors = mutableListOf<String>()
        if (request.name.isBlank()) errors += "Please enter your name."
        if (request.surname.isBlank()) errors += "Please enter your surname."
        if (!isValidContact(request.contact)) errors += "Please enter a valid cell number, e.g. 0821234567."
        if (request.stylistId.isBlank()) errors += "Please choose a stylist."
        if (request.serviceId.isBlank()) errors += "Please choose a service."
        if (request.type == BookingType.OUTCALL && request.location.isBlank()) {
            errors += "Please enter an address for an outcall booking."
        }
        if (request.notes.length > 500) errors += "Notes must be 500 characters or fewer."

        val date = try { LocalDate.parse(request.date) } catch (e: DateTimeParseException) { null }
        when {
            date == null -> errors += "Please choose a date."
            date.isBefore(today) -> errors += "You can't book a date in the past."
        }
        if (request.time !in TIME_SLOTS) {
            errors += "Please choose a time slot."
        } else if (date != null && date == today && LocalTime.parse(request.time) <= LocalTime.now()) {
            errors += "That time has already passed today."
        }
        return errors
    }

    /** Allowed status transitions (spec: pending -> upcoming -> completed/cancelled). */
    fun canTransition(from: BookingStatus, to: BookingStatus): Boolean = when (from) {
        BookingStatus.PENDING -> to == BookingStatus.UPCOMING || to == BookingStatus.CANCELLED
        BookingStatus.UPCOMING -> to == BookingStatus.COMPLETED || to == BookingStatus.CANCELLED
        BookingStatus.COMPLETED, BookingStatus.CANCELLED -> false
    }
}
