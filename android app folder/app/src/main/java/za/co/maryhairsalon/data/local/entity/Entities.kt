package za.co.maryhairsalon.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/*
 * Room tables: the on-device cache of the Supabase tables.
 * Supabase is the source of truth; Room is what the UI observes, so screens load
 * instantly and keep working offline. Primary keys are the Supabase ids, so a
 * refresh is a simple upsert.
 */

@Entity(tableName = "users", indices = [Index("role")])
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val surname: String,
    val email: String,
    val contact: String,
    val role: String,
    val title: String,
    val photoUrl: String,
    val notificationsEnabled: Boolean,
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val label: String,
    val description: String,
    val image: String,
    val icon: String,
)

@Entity(tableName = "services", indices = [Index("categoryId")])
data class ServiceEntity(
    @PrimaryKey val id: String,
    val title: String,
    val categoryId: String,
    val price: Double,
    val description: String,
    /** Stored via [za.co.maryhairsalon.data.local.Converters]. */
    val tags: List<String>,
    val image: String,
    val discount: Double,
    val isActive: Boolean,
)

@Entity(
    tableName = "bookings",
    indices = [Index("customerId"), Index("stylistId"), Index("status"), Index("date")],
)
data class BookingEntity(
    @PrimaryKey val id: String,
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
    val type: String,
    val location: String,
    val date: String,
    val time: String,
    val notes: String,
    val cost: Double,
    val discount: Double,
    val total: Double,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
)
