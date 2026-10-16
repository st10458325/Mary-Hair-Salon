package com.teamsalon.booking.data

import kotlinx.coroutines.flow.Flow

/**
 * Thin wrapper around the three DAOs so the UI layer (ViewModels) never
 * talks to Room directly — same repository pattern as the main salon app.
 */
class BookingRepository(private val db: AppDatabase) {

    suspend fun login(email: String, password: String): UserEntity? =
        db.userDao().login(email, password)

    fun observeServices(): Flow<List<ServiceEntity>> = db.serviceDao().observeAll()

    fun observeBookingsForUser(userId: Int): Flow<List<BookingEntity>> =
        db.bookingDao().observeForUser(userId)

    suspend fun createBooking(userId: Int, serviceId: Int, date: String, time: String) {
        db.bookingDao().insert(
            BookingEntity(userId = userId, serviceId = serviceId, date = date, time = time)
        )
    }

    suspend fun cancelBooking(bookingId: Int) = db.bookingDao().cancel(bookingId)
}
