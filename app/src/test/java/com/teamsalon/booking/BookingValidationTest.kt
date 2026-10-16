package com.teamsalon.booking

import com.teamsalon.booking.util.BookingValidation
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Person 3's unit test example — pure JVM, no Android dependency, so it
 * runs in milliseconds as part of the CI pipeline on every pull request.
 */
class BookingValidationTest {

    @Test
    fun validDateFormatIsAccepted() {
        assertTrue(BookingValidation.isValidDate("2026-10-10"))
    }

    @Test
    fun invalidDateFormatIsRejected() {
        assertFalse(BookingValidation.isValidDate("10-10-2026"))
    }

    @Test
    fun validTimeFormatIsAccepted() {
        assertTrue(BookingValidation.isValidTime("09:30"))
    }

    @Test
    fun invalidTimeFormatIsRejected() {
        assertFalse(BookingValidation.isValidTime("9:30am"))
    }
}
