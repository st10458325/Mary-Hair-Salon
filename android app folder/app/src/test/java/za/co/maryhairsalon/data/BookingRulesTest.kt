package za.co.maryhairsalon.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import za.co.maryhairsalon.data.model.BookingRequest
import za.co.maryhairsalon.data.model.BookingRules
import za.co.maryhairsalon.data.model.BookingStatus
import za.co.maryhairsalon.data.model.BookingType
import za.co.maryhairsalon.data.model.Pricing
import java.time.LocalDate

class BookingRulesTest {

    private val today = LocalDate.of(2026, 10, 6)

    private fun request(
        date: String = "2026-10-10",
        time: String = "10:00",
        type: BookingType = BookingType.INCALL,
        location: String = "",
        contact: String = "0821234567",
    ) = BookingRequest(
        name = "Ayanda", surname = "Zulu", contact = contact, stylistId = "stylist-1",
        serviceId = "knotless-braids", type = type, location = location, date = date, time = time,
    )

    @Test fun validRequest_hasNoErrors() {
        assertTrue(BookingRules.validate(request(), today).isEmpty())
    }

    @Test fun pastDate_isRejected() {
        val errors = BookingRules.validate(request(date = "2026-10-01"), today)
        assertTrue(errors.any { it.contains("past") })
    }

    @Test fun garbageDate_isRejected() {
        assertTrue(BookingRules.validate(request(date = "not-a-date"), today).isNotEmpty())
    }

    @Test fun timeOutsideSlots_isRejected() {
        assertTrue(BookingRules.validate(request(time = "07:30"), today).isNotEmpty())
        assertTrue(BookingRules.validate(request(time = "20:00"), today).isNotEmpty())
    }

    @Test fun outcallWithoutAddress_isRejected() {
        val errors = BookingRules.validate(request(type = BookingType.OUTCALL), today)
        assertTrue(errors.any { it.contains("address") })
        assertTrue(BookingRules.validate(request(type = BookingType.OUTCALL, location = "12 Main Rd"), today).isEmpty())
    }

    @Test fun contactNumbers() {
        assertTrue(BookingRules.isValidContact("0821234567"))
        assertTrue(BookingRules.isValidContact("+27 82 123 4567"))
        assertFalse(BookingRules.isValidContact("12345"))
        assertFalse(BookingRules.isValidContact("08212345678"))
        assertFalse(BookingRules.isValidContact("abcdefghij"))
    }

    @Test fun timeSlots_matchDatabaseRule() {
        assertEquals(12, BookingRules.TIME_SLOTS.size)
        assertEquals("08:00", BookingRules.TIME_SLOTS.first())
        assertEquals("19:00", BookingRules.TIME_SLOTS.last())
    }

    @Test fun formatDate() {
        assertEquals("Sat, 10 Oct 2026", BookingRules.formatDate("2026-10-10"))
        assertEquals("bad", BookingRules.formatDate("bad"))
    }

    @Test fun statusTransitions() {
        assertTrue(BookingRules.canTransition(BookingStatus.PENDING, BookingStatus.UPCOMING))
        assertTrue(BookingRules.canTransition(BookingStatus.PENDING, BookingStatus.CANCELLED))
        assertTrue(BookingRules.canTransition(BookingStatus.UPCOMING, BookingStatus.COMPLETED))
        assertFalse(BookingRules.canTransition(BookingStatus.PENDING, BookingStatus.COMPLETED))
        assertFalse(BookingRules.canTransition(BookingStatus.COMPLETED, BookingStatus.CANCELLED))
        assertFalse(BookingRules.canTransition(BookingStatus.CANCELLED, BookingStatus.PENDING))
    }

    @Test fun pricing() {
        assertEquals(650.0, Pricing.total(650.0, 0.0), 0.001)
        assertEquals(495.0, Pricing.total(550.0, 10.0), 0.001)
        assertEquals(0.0, Pricing.total(300.0, 100.0), 0.001)
        // Out-of-range discounts are clamped rather than producing negative prices.
        assertEquals(0.0, Pricing.total(300.0, 150.0), 0.001)
        assertEquals(300.0, Pricing.total(300.0, -5.0), 0.001)
        assertEquals(212.5, Pricing.total(250.0, 15.0), 0.001)
    }
}
