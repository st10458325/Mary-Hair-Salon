package za.co.maryhairsalon.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import za.co.maryhairsalon.data.local.Converters
import za.co.maryhairsalon.data.local.toDomain
import za.co.maryhairsalon.data.local.toEntity
import za.co.maryhairsalon.data.model.BookingStatus
import za.co.maryhairsalon.data.model.BookingType
import za.co.maryhairsalon.data.model.Category
import za.co.maryhairsalon.data.model.Role
import za.co.maryhairsalon.data.model.Slug
import za.co.maryhairsalon.data.remote.BookingDetailsDto
import za.co.maryhairsalon.data.remote.CategoryDto
import za.co.maryhairsalon.data.remote.ErrorMessages
import za.co.maryhairsalon.data.remote.ProfileDto
import za.co.maryhairsalon.data.remote.ServiceDto
import za.co.maryhairsalon.data.remote.parseTimestamp

class MappersTest {

    @Test fun roleStatusType_fallBackSafely() {
        assertEquals(Role.CUSTOMER, Role.from(null))
        assertEquals(Role.CUSTOMER, Role.from("superadmin"))
        assertEquals(Role.MANAGER, Role.from(" Manager "))
        assertEquals(BookingStatus.PENDING, BookingStatus.from(null))
        assertEquals(BookingStatus.UPCOMING, BookingStatus.from("upcoming"))
        assertEquals(BookingType.OUTCALL, BookingType.from("outcall"))
        assertEquals(BookingType.INCALL, BookingType.from("???"))
    }

    @Test fun profileDto_mapsToUser() {
        val user = ProfileDto(id = "u1", name = "Thandi", surname = "Mokoena", role = "employee", title = "Senior Braider").toDomain()
        assertEquals(Role.EMPLOYEE, user.role)
        assertEquals("Thandi Mokoena", user.displayName)
        assertTrue(user.notificationsEnabled)
    }

    @Test fun bookingDetailsDto_mapsToBooking() {
        val booking = BookingDetailsDto(
            id = "b1", customerId = "c1", stylistId = "s1", stylistName = null,
            serviceId = "box-braids", serviceTitle = "Box Braids", categoryId = "braids", categoryLabel = "Braids",
            customerName = "Ayanda", customerSurname = "Zulu", contact = "0731000001",
            type = "Outcall", location = "12 Main Rd", bookingDate = "2026-10-14", bookingTime = "10:00:00",
            cost = 550.0, discount = 10.0, total = 495.0, status = "upcoming",
            createdAt = "2026-10-11T17:05:09.123456+00:00",
        ).toDomain()
        assertEquals("", booking.stylistName)
        assertEquals("10:00", booking.time)
        assertEquals(BookingType.OUTCALL, booking.type)
        assertEquals(BookingStatus.UPCOMING, booking.status)
        assertEquals("Wed, 14 Oct 2026", booking.dateFormatted)
        assertEquals(1791738309123L, booking.createdAt)
        assertEquals(0L, booking.updatedAt)
    }

    @Test fun booking_roundTripsThroughRoom() {
        val booking = BookingDetailsDto(id = "b2", bookingDate = "2026-10-14", bookingTime = "09:00", total = 200.0).toDomain()
        assertEquals(booking, booking.toEntity().toDomain())
    }

    @Test fun serviceDto_cleansTagsAndClampsDiscount() {
        val s = ServiceDto(id = "fade", categoryId = "cuts", title = "Fade", price = 150.0,
            tags = listOf("fade", " barber ", ""), discount = 250.0).toDomain()
        assertEquals(listOf("fade", "barber"), s.tags)
        assertEquals(100.0, s.discount, 0.001)
        assertEquals(s, s.toEntity().toDomain())
    }

    @Test fun categoryDto_blankLabelFallsBackToId() {
        assertEquals("braids", CategoryDto(id = "braids").toDomain().label)
        assertEquals("Braids", CategoryDto.from(Category("braids", " Braids ")).label)
    }

    @Test fun timestamps() {
        assertEquals(0L, parseTimestamp(null))
        assertEquals(0L, parseTimestamp("not a date"))
        assertEquals(parseTimestamp("2026-10-11T17:00:00+00:00"), parseTimestamp("2026-10-11T17:00:00+00"))
    }

    @Test fun userMessages_fromDatabaseFunctions() {
        val raw = "USER: Sorry, that time slot has just been taken. Please pick another time.\nURL: https://x"
        assertEquals("Sorry, that time slot has just been taken. Please pick another time.", ErrorMessages.userMessage(raw))
        assertNull(ErrorMessages.userMessage("duplicate key value"))
        assertEquals("Sorry, that time slot has just been taken. Please pick another time.", ErrorMessages.fromDatabase("P0001", raw))
        assertEquals("You don't have permission to do that.", ErrorMessages.fromDatabase("42501", "permission denied for table bookings"))
        assertEquals("You don't have permission to do that.", ErrorMessages.fromDatabase(null, "new row violates row-level security policy"))
        assertEquals("This item is still in use, so it can't be deleted.", ErrorMessages.fromDatabase("23503", "fk"))
        assertEquals(ErrorMessages.GENERIC, ErrorMessages.fromDatabase("XX000", "boom"))
        assertEquals("Incorrect email or password.", ErrorMessages.fromAuth("invalid_credentials"))
    }

    @Test fun slugs() {
        assertEquals("knotless-braids", Slug.from("Knotless Braids!"))
        assertEquals("weaves-wigs", Slug.from("Weaves & Wigs"))
        assertTrue(Slug.isValid("box-braids"))
        assertFalse(Slug.isValid("Box Braids"))
    }

    @Test fun tagConverter_roundTrip() {
        val c = Converters()
        val tags = listOf("knotless", "long, waist", "protective")
        assertEquals(tags, c.toTags(c.fromTags(tags)))
        assertEquals(emptyList<String>(), c.toTags(c.fromTags(emptyList())))
    }
}
