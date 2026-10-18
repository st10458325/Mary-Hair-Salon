package za.co.maryhairsalon.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import za.co.maryhairsalon.data.model.Booking
import za.co.maryhairsalon.data.model.BookingStatus
import za.co.maryhairsalon.data.model.BookingType
import za.co.maryhairsalon.data.model.Category
import za.co.maryhairsalon.data.model.Role
import za.co.maryhairsalon.data.model.Service
import za.co.maryhairsalon.data.model.User
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

/*
 * DTOs = the exact JSON shape Supabase (PostgREST) sends and receives.
 * @SerialName maps the database's snake_case columns to Kotlin camelCase.
 * Every field has a default so a missing column never crashes the app.
 */

/** Table names, view names and API function names in supabase/migrations. */
object Db {
    const val PROFILES = "profiles"
    const val CATEGORIES = "categories"
    const val SERVICES = "services"
    const val BOOKING_DETAILS = "booking_details"   // view (read-only)
    const val MESSAGES = "messages"

    const val RPC_CREATE_BOOKING = "create_booking"
    const val RPC_RESCHEDULE_BOOKING = "reschedule_booking"
    const val RPC_SET_BOOKING_STATUS = "set_booking_status"
    const val RPC_REASSIGN_STYLIST = "reassign_stylist"
    const val RPC_SET_USER_ROLE = "set_user_role"
    const val RPC_TAKEN_TIMES = "taken_times"
}

@Serializable
data class ProfileDto(
    val id: String,
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val contact: String = "",
    val role: String = "customer",
    val title: String = "",
    @SerialName("photo_url") val photoUrl: String = "",
    @SerialName("notifications_enabled") val notificationsEnabled: Boolean = true,
) {
    fun toDomain() = User(
        id = id, name = name, surname = surname, email = email, contact = contact,
        role = Role.from(role), title = title, photoUrl = photoUrl,
        notificationsEnabled = notificationsEnabled,
    )
}

@Serializable
data class CategoryDto(
    val id: String,
    val label: String = "",
    val description: String = "",
    val image: String = "",
    val icon: String = "",
) {
    fun toDomain() = Category(id = id, label = label.ifBlank { id }, description = description, image = image, icon = icon)

    companion object {
        fun from(c: Category) = CategoryDto(c.id, c.label.trim(), c.description.trim(), c.image.trim(), c.icon.trim())
    }
}

@Serializable
data class ServiceDto(
    val id: String,
    @SerialName("category_id") val categoryId: String = "",
    val title: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val tags: List<String> = emptyList(),
    val image: String = "",
    val discount: Double = 0.0,
    @SerialName("is_active") val isActive: Boolean = true,
) {
    fun toDomain() = Service(
        id = id, title = title, categoryId = categoryId, price = price,
        description = description, tags = tags.map { it.trim() }.filter { it.isNotEmpty() },
        image = image, discount = discount.coerceIn(0.0, 100.0), isActive = isActive,
    )

    companion object {
        fun from(s: Service) = ServiceDto(
            id = s.id, categoryId = s.categoryId, title = s.title.trim(), price = s.price,
            description = s.description.trim(), tags = s.tags, image = s.image.trim(),
            discount = s.discount, isActive = s.isActive,
        )
    }
}

/** A row of the booking_details view (bookings joined with service, category and stylist). */
@Serializable
data class BookingDetailsDto(
    val id: String,
    @SerialName("customer_id") val customerId: String = "",
    @SerialName("stylist_id") val stylistId: String = "",
    @SerialName("stylist_name") val stylistName: String? = null,
    @SerialName("service_id") val serviceId: String = "",
    @SerialName("service_title") val serviceTitle: String = "",
    @SerialName("category_id") val categoryId: String = "",
    @SerialName("category_label") val categoryLabel: String = "",
    @SerialName("customer_name") val customerName: String = "",
    @SerialName("customer_surname") val customerSurname: String = "",
    val contact: String = "",
    val type: String = "Incall",
    val location: String = "",
    @SerialName("booking_date") val bookingDate: String = "",
    @SerialName("booking_time") val bookingTime: String = "",
    val notes: String = "",
    val cost: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0,
    val status: String = "pending",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
) {
    fun toDomain() = Booking(
        id = id,
        customerId = customerId,
        stylistId = stylistId,
        stylistName = stylistName.orEmpty(),
        serviceId = serviceId,
        serviceTitle = serviceTitle,
        categoryId = categoryId,
        categoryLabel = categoryLabel,
        name = customerName,
        surname = customerSurname,
        contact = contact,
        type = BookingType.from(type),
        location = location,
        date = bookingDate,
        time = bookingTime.take(5),   // "10:00:00" -> "10:00" just in case
        notes = notes,
        cost = cost,
        discount = discount,
        total = total,
        status = BookingStatus.from(status),
        createdAt = parseTimestamp(createdAt),
        updatedAt = parseTimestamp(updatedAt),
    )
}

/** One row returned by the taken_times() function. */
@Serializable
data class TakenTimeDto(@SerialName("booking_time") val bookingTime: String)

/** Postgres timestamptz text, e.g. "2026-10-11T17:05:09.123456+00:00" -> epoch millis (0 if unreadable). */
fun parseTimestamp(value: String?): Long {
    if (value.isNullOrBlank()) return 0L
    return try {
        OffsetDateTime.parse(value).toInstant().toEpochMilli()
    } catch (e: DateTimeParseException) {
        // Postgres sometimes sends "+00" instead of "+00:00".
        try { OffsetDateTime.parse(value.replace(Regex("([+-]\\d{2})$"), "$1:00")).toInstant().toEpochMilli() }
        catch (e2: DateTimeParseException) { 0L }
    }
}
