package com.teamsalon.booking.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Each booking links a user to a service — this is the foreign-key
 * relationship your ERD/schema diagram should show. Deleting a user or
 * service cascades to their bookings so the demo data never ends up with
 * orphaned rows.
 */
@Entity(
    tableName = "bookings",
    foreignKeys = [
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ServiceEntity::class, parentColumns = ["id"], childColumns = ["serviceId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("userId"), Index("serviceId")]
)
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val serviceId: Int,
    val date: String,   // "yyyy-MM-dd"
    val time: String,   // "HH:mm"
    val status: String = "pending" // pending, confirmed, completed, cancelled
)
