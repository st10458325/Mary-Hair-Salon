package za.co.maryhairsalon.data.model

/**
 * Domain models used by the UI layer.
 *
 * The UI (Compose screens / ViewModels) should only ever see these classes, never Room
 * entities or Supabase DTOs. That keeps the screens independent of where the data
 * actually came from (local cache vs. the online database).
 *
 * They mirror the tables in supabase/migrations/001_schema.sql.
 */

enum class Role(val dbValue: String) {
    CUSTOMER("customer"),
    EMPLOYEE("employee"),
    MANAGER("manager");

    companion object {
        /** Unknown or missing roles fall back to CUSTOMER (least privilege). */
        fun from(value: String?): Role =
            entries.firstOrNull { it.dbValue.equals(value?.trim(), ignoreCase = true) } ?: CUSTOMER
    }
}

enum class BookingStatus(val dbValue: String) {
    PENDING("pending"),
    UPCOMING("upcoming"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    /** Customers may only reschedule/cancel bookings that have not happened yet. */
    val isActive: Boolean get() = this == PENDING || this == UPCOMING

    companion object {
        fun from(value: String?): BookingStatus =
            entries.firstOrNull { it.dbValue.equals(value?.trim(), ignoreCase = true) } ?: PENDING
    }
}

enum class BookingType(val dbValue: String) {
    INCALL("Incall"),
    OUTCALL("Outcall");

    companion object {
        fun from(value: String?): BookingType =
            entries.firstOrNull { it.dbValue.equals(value?.trim(), ignoreCase = true) } ?: INCALL
    }
}

/** A row of the profiles table. id = the Supabase Auth user id (UUID). */
data class User(
    val id: String,
    val name: String,
    val surname: String = "",
    val email: String,
    val contact: String = "",
    val role: Role = Role.CUSTOMER,
    /** Job title for stylists, e.g. "Senior Stylist". Empty for customers. */
    val title: String = "",
    val photoUrl: String = "",
    val notificationsEnabled: Boolean = true,
) {
    val displayName: String get() = listOf(name, surname).filter { it.isNotBlank() }.joinToString(" ")
}

data class Category(
    /** Readable slug, e.g. "braids". */
    val id: String,
    val label: String,
    val description: String = "",
    val image: String = "",
    val icon: String = "",
)

data class Service(
    val id: String,
    val title: String,
    val categoryId: String,
    /** Price in Rand before discount. */
    val price: Double,
    val description: String = "",
    val tags: List<String> = emptyList(),
    val image: String = "",
    /** Manager-controlled discount percentage (0–100). */
    val discount: Double = 0.0,
    val isActive: Boolean = true,
) {
    val discountedPrice: Double get() = Pricing.total(price, discount)
}

/** A booking as shown in the app (a row of the booking_details view). */
data class Booking(
    val id: String,
    val customerId: String,
    val stylistId: String,
    val stylistName: String,
    val serviceId: String,
    val serviceTitle: String,
    val categoryId: String,
    val categoryLabel: String,
    val name: String,
    val surname: String,
    val contact: String,
    val type: BookingType,
    val location: String = "",
    /** ISO date, yyyy-MM-dd. */
    val date: String,
    /** 24h time, HH:mm. */
    val time: String,
    val notes: String = "",
    val cost: Double,
    val discount: Double,
    val total: Double,
    val status: BookingStatus,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
) {
    val dateFormatted: String get() = BookingRules.formatDate(date)
}

/**
 * What the booking form hands to [za.co.maryhairsalon.data.repository.BookingRepository]
 * when creating or rescheduling a booking. Price is NOT part of it: the database reads
 * the price from the services table (create_booking in 003_api.sql).
 */
data class BookingRequest(
    val name: String,
    val surname: String,
    val contact: String,
    val stylistId: String,
    val serviceId: String,
    val type: BookingType,
    val location: String = "",
    val date: String,
    val time: String,
    val notes: String = "",
)

/** Pure pricing helpers, unit-tested in BookingRulesTest. Same formula as the database. */
object Pricing {
    fun total(cost: Double, discountPercent: Double): Double {
        val pct = discountPercent.coerceIn(0.0, 100.0)
        val raw = cost.coerceAtLeast(0.0) * (1 - pct / 100.0)
        return Math.round(raw * 100.0) / 100.0
    }
}
