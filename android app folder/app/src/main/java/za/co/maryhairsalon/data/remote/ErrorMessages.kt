package za.co.maryhairsalon.data.remote

/**
 * Turns database / auth errors into messages that can be shown to the user.
 * Pure Kotlin (no Supabase types) so it is unit-tested in MappersTest.
 */
object ErrorMessages {

    const val GENERIC = "Something went wrong. Please try again."
    const val OFFLINE = "You're offline. Please try again when connected."

    /**
     * The API functions in 003_api.sql raise errors like
     *   "USER: Sorry, that time slot has just been taken."
     * Returns the text after "USER:" (first line only), or null if it's not one of ours.
     */
    fun userMessage(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val idx = raw.indexOf("USER:")
        if (idx < 0) return null
        return raw.substring(idx + 5).lineSequence().first().trim().ifEmpty { null }
    }

    /**
     * Maps a PostgREST error (Postgres SQLSTATE code + message) to a friendly message.
     * Codes: https://www.postgresql.org/docs/current/errcodes-appendix.html
     */
    fun fromDatabase(code: String?, message: String?): String {
        userMessage(message)?.let { return it }
        return when (code) {
            "42501" -> "You don't have permission to do that."                 // insufficient_privilege / RLS
            "23503" -> "This item is still in use, so it can't be deleted."    // foreign_key_violation
            "23505" -> "That already exists."                                  // unique_violation
            "23514" -> "Some of the details aren't valid. Please check and try again." // check_violation
            "PGRST116" -> "That item no longer exists."
            else -> if (message?.contains("row-level security", ignoreCase = true) == true)
                "You don't have permission to do that." else GENERIC
        }
    }

    /** Supabase Auth error codes -> friendly text. */
    fun fromAuth(code: String?): String = when (code) {
        "invalid_credentials" -> "Incorrect email or password."
        "email_not_confirmed" -> "Please confirm your email address first (check your inbox)."
        "user_already_exists", "email_exists" -> "An account with this email already exists."
        "weak_password" -> "Please choose a stronger password (at least 8 characters)."
        "over_request_rate_limit", "over_email_send_rate_limit" -> "Too many attempts. Please wait a moment and try again."
        "user_banned" -> "This account has been disabled."
        "validation_failed", "email_address_invalid" -> "That email address doesn't look right."
        else -> "Sign-in failed. Please try again."
    }
}
