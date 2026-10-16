package com.teamsalon.booking.data

/**
 * Populates all three tables with sample data the first time the app runs.
 * Person 2 owns this file — this is the "at least ten records per table"
 * evidence. The first two users (ids 1 and 2) ARE the fake login accounts
 * used to demo the login screen; the rest are realistic padding so the
 * table has a believable row count, same as the other two tables.
 */
object DatabaseSeeder {

    suspend fun seedIfEmpty(db: AppDatabase) {
        if (db.userDao().count() == 0) {
            db.userDao().insertAll(sampleUsers())
        }
        if (db.serviceDao().count() == 0) {
            db.serviceDao().insertAll(sampleServices())
        }
        if (db.bookingDao().count() == 0) {
            db.bookingDao().insertAll(sampleBookings())
        }
    }

    /** Rows 1 and 2 are the demo login accounts — use either to sign in. */
    private fun sampleUsers() = listOf(
        UserEntity(fullName = "Test Customer One", email = "customer1@demo.com", password = "demo123"),
        UserEntity(fullName = "Test Customer Two", email = "customer2@demo.com", password = "demo123"),
        UserEntity(fullName = "Naledi Khumalo", email = "naledi.k@example.com", password = "demo123"),
        UserEntity(fullName = "Sipho Mokoena", email = "sipho.m@example.com", password = "demo123"),
        UserEntity(fullName = "Thandiwe Nkosi", email = "thandiwe.n@example.com", password = "demo123"),
        UserEntity(fullName = "Johan van der Berg", email = "johan.vdb@example.com", password = "demo123"),
        UserEntity(fullName = "Amahle Dlamini", email = "amahle.d@example.com", password = "demo123"),
        UserEntity(fullName = "Lindiwe Zulu", email = "lindiwe.z@example.com", password = "demo123"),
        UserEntity(fullName = "Pieter Botha", email = "pieter.b@example.com", password = "demo123"),
        UserEntity(fullName = "Zanele Mahlangu", email = "zanele.m@example.com", password = "demo123")
    )

    private fun sampleServices() = listOf(
        ServiceEntity(title = "Classic Haircut", category = "Hair Cuts", price = 150.0, durationMinutes = 30),
        ServiceEntity(title = "Fade & Line-up", category = "Hair Cuts", price = 180.0, durationMinutes = 40),
        ServiceEntity(title = "Box Braids", category = "Braids", price = 450.0, durationMinutes = 180),
        ServiceEntity(title = "Cornrows", category = "Braids", price = 300.0, durationMinutes = 120),
        ServiceEntity(title = "Kids Trim", category = "Kids", price = 80.0, durationMinutes = 20),
        ServiceEntity(title = "Kids Braids", category = "Kids", price = 200.0, durationMinutes = 90),
        ServiceEntity(title = "Wig Install", category = "Wigs", price = 500.0, durationMinutes = 150),
        ServiceEntity(title = "Wig Styling", category = "Wigs", price = 250.0, durationMinutes = 60),
        ServiceEntity(title = "Beard Trim", category = "Hair Cuts", price = 60.0, durationMinutes = 15),
        ServiceEntity(title = "Hair Wash & Treatment", category = "Treatments", price = 120.0, durationMinutes = 45),
        ServiceEntity(title = "Scalp Treatment", category = "Treatments", price = 160.0, durationMinutes = 40),
        ServiceEntity(title = "Silk Press", category = "Treatments", price = 220.0, durationMinutes = 75)
    )

    private fun sampleBookings() = listOf(
        BookingEntity(userId = 1, serviceId = 1, date = "2026-10-10", time = "09:00", status = "confirmed"),
        BookingEntity(userId = 1, serviceId = 3, date = "2026-10-17", time = "11:00", status = "pending"),
        BookingEntity(userId = 2, serviceId = 2, date = "2026-10-11", time = "14:00", status = "confirmed"),
        BookingEntity(userId = 2, serviceId = 7, date = "2026-10-20", time = "10:00", status = "pending"),
        BookingEntity(userId = 3, serviceId = 4, date = "2026-10-12", time = "13:00", status = "completed"),
        BookingEntity(userId = 4, serviceId = 5, date = "2026-10-13", time = "15:30", status = "cancelled"),
        BookingEntity(userId = 5, serviceId = 6, date = "2026-10-14", time = "09:30", status = "confirmed"),
        BookingEntity(userId = 6, serviceId = 8, date = "2026-10-15", time = "16:00", status = "pending"),
        BookingEntity(userId = 7, serviceId = 9, date = "2026-10-16", time = "08:30", status = "confirmed"),
        BookingEntity(userId = 8, serviceId = 10, date = "2026-10-18", time = "12:00", status = "completed")
    )
}
