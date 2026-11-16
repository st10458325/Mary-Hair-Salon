package za.co.maryhairsalon.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import za.co.maryhairsalon.data.model.BookingRequest
import za.co.maryhairsalon.data.model.BookingRules
import za.co.maryhairsalon.data.model.BookingType
import za.co.maryhairsalon.data.model.Role
import za.co.maryhairsalon.data.model.Service
import za.co.maryhairsalon.data.model.Slug
import za.co.maryhairsalon.data.model.User
import za.co.maryhairsalon.data.remote.ErrorMessages
import java.time.LocalDate

/**
 * Extra local tests for the pure-Kotlin rules: booking form validation edge cases,
 * database / auth error messages, slugs and the small computed model properties.
 * Run with:  gradlew testDebugUnitTest
 */
class ValidationAndModelsTest {

    private val today = LocalDate.of(2026, 10, 6)

    private fun request(
        name: String = "Ayanda",
        surname: String = "Zulu",
        contact: String = "0821234567",
        stylistId: String = "stylist-1",
        serviceId: String = "knotless-braids",
        type: BookingType = BookingType.INCALL,
        location: String = "",
        date: String = "2026-10-10",
        time: String = "10:00",
        notes: String = "",
    ) = BookingRequest(name, surname, contact, stylistId, serviceId, type, location, date, time, notes)

    // ---- Booking form validation ------------------------------------------------------

    @Test fun blankName_andSurname_areRejected() {
        val errors = BookingRules.validate(request(name = " ", surname = ""), today)
        assertTrue(errors.contains("Please enter your name."))
        assertTrue(errors.contains("Please enter your surname."))
    }

    @Test fun missingStylistOrService_isRejected() {
        val errors = BookingRules.validate(request(stylistId = "", serviceId = ""), today)
        assertTrue(errors.contains("Please choose a stylist."))
        assertTrue(errors.contains("Please choose a service."))
    }

    @Test fun invalidContact_isRejectedWithHelpfulMessage() {
        val errors = BookingRules.validate(request(contact = "123"), today)
        assertTrue(errors.any { it.contains("cell number") })
    }

    @Test fun notesLongerThan500_areRejected_exactly500Allowed() {
        assertTrue(BookingRules.validate(request(notes = "x".repeat(500)), today).isEmpty())
        val errors = BookingRules.validate(request(notes = "x".repeat(501)), today)
        assertTrue(errors.any { it.contains("500") })
    }

    @Test fun emptyDate_isRejected() {
        val errors = BookingRules.validate(request(date = ""), today)
        assertTrue(errors.contains("Please choose a date."))
    }

    @Test fun yesterdayRejected_tomorrowAccepted() {
        assertTrue(BookingRules.validate(request(date = "2026-10-05"), today).any { it.contains("past") })
        assertTrue(BookingRules.validate(request(date = "2026-10-07"), today).isEmpty())
    }

    @Test fun allTwelveTimeSlots_areAccepted() {
        BookingRules.TIME_SLOTS.forEach { slot ->
            assertTrue("slot $slot should be valid", BookingRules.validate(request(time = slot), today).isEmpty())
        }
    }

    @Test fun halfHourTime_isRejected() {
        assertTrue(BookingRules.validate(request(time = "10:30"), today).contains("Please choose a time slot."))
    }

    @Test fun everythingWrong_reportsEveryProblem() {
        val errors = BookingRules.validate(
            request(name = "", surname = "", contact = "x", stylistId = "", serviceId = "",
                type = BookingType.OUTCALL, location = "", date = "bad", time = "03:00"),
            today,
        )
        // name, surname, contact, stylist, service, outcall address, date, time
        assertEquals(8, errors.size)
    }

    @Test fun contactAcceptsInternationalFormat() {
        assertTrue(BookingRules.isValidContact("+27821234567"))
        assertFalse(BookingRules.isValidContact("+2782123456"))
        assertFalse(BookingRules.isValidContact(""))
    }

    // ---- Error messages ---------------------------------------------------------------

    @Test fun userMessage_extractsTextAfterMarker_firstLineOnly() {
        assertEquals("Sorry, that time slot has just been taken.",
            ErrorMessages.userMessage("P0001: USER: Sorry, that time slot has just been taken.\nCONTEXT: ..."))
        assertNull(ErrorMessages.userMessage("duplicate key value"))
        assertNull(ErrorMessages.userMessage(null))
        assertNull(ErrorMessages.userMessage("   "))
        assertNull(ErrorMessages.userMessage("USER:   "))
    }

    @Test fun databaseErrors_mapToFriendlyText() {
        assertEquals("You don't have permission to do that.", ErrorMessages.fromDatabase("42501", null))
        assertEquals("This item is still in use, so it can't be deleted.", ErrorMessages.fromDatabase("23503", null))
        assertEquals("That already exists.", ErrorMessages.fromDatabase("23505", null))
        assertEquals("Some of the details aren't valid. Please check and try again.", ErrorMessages.fromDatabase("23514", null))
        assertEquals("That item no longer exists.", ErrorMessages.fromDatabase("PGRST116", null))
        assertEquals(ErrorMessages.GENERIC, ErrorMessages.fromDatabase("XX000", "boom"))
        assertEquals("You don't have permission to do that.",
            ErrorMessages.fromDatabase(null, "new row violates row-level security policy"))
    }

    @Test fun databaseErrors_prefersOurOwnMessage() {
        assertEquals("Pick another stylist.", ErrorMessages.fromDatabase("23505", "USER: Pick another stylist."))
    }

    @Test fun authErrors_mapToFriendlyText() {
        assertEquals("Incorrect email or password.", ErrorMessages.fromAuth("invalid_credentials"))
        assertEquals("An account with this email already exists.", ErrorMessages.fromAuth("user_already_exists"))
        assertEquals("An account with this email already exists.", ErrorMessages.fromAuth("email_exists"))
        assertTrue(ErrorMessages.fromAuth("weak_password").contains("8 characters"))
        assertTrue(ErrorMessages.fromAuth("over_request_rate_limit").contains("Too many"))
        assertEquals("Sign-in failed. Please try again.", ErrorMessages.fromAuth("something_new"))
        assertEquals("Sign-in failed. Please try again.", ErrorMessages.fromAuth(null))
    }

    // ---- Slugs and models -------------------------------------------------------------

    @Test fun slug_from_andIsValid() {
        assertEquals("knotless-braids", Slug.from("Knotless Braids!"))
        assertEquals("a-b-c", Slug.from("  A  b--C  "))
        assertTrue(Slug.isValid("knotless-braids"))
        assertFalse(Slug.isValid("Knotless Braids"))
        assertFalse(Slug.isValid(""))
        assertFalse(Slug.isValid("under_score"))
    }

    @Test fun user_displayName_skipsBlankParts() {
        assertEquals("Mary Smith", User(id = "1", name = "Mary", surname = "Smith", email = "m@x.com").displayName)
        assertEquals("Mary", User(id = "1", name = "Mary", email = "m@x.com").displayName)
        assertEquals(Role.CUSTOMER, User(id = "1", name = "Mary", email = "m@x.com").role)
    }

    @Test fun service_discountedPrice_usesPricingRule() {
        val service = Service(id = "s", title = "Braids", categoryId = "braids", price = 550.0, discount = 10.0)
        assertEquals(495.0, service.discountedPrice, 0.001)
        assertEquals(550.0, service.copy(discount = 0.0).discountedPrice, 0.001)
    }
}
