package com.teamsalon.booking.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(bookings: List<BookingEntity>)

    @Insert
    suspend fun insert(booking: BookingEntity): Long

    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY date DESC")
    fun observeForUser(userId: Int): Flow<List<BookingEntity>>

    @Query("UPDATE bookings SET status = 'cancelled' WHERE id = :bookingId")
    suspend fun cancel(bookingId: Int)

    @Query("SELECT COUNT(*) FROM bookings")
    suspend fun count(): Int
}
