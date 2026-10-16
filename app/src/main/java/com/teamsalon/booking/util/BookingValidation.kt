package com.teamsalon.booking.util

/**
 * Pure validation logic, deliberately kept out of the ViewModel so it can
 * be unit-tested as plain JVM code (see BookingValidationTest) with no
 * Android framework or database involved.
 */
object BookingValidation {
    private val datePattern = Regex("""\d{4}-\d{2}-\d{2}""")
    private val timePattern = Regex("""\d{2}:\d{2}""")

    fun isValidDate(date: String): Boolean = datePattern.matches(date)

    fun isValidTime(time: String): Boolean = timePattern.matches(time)
}
