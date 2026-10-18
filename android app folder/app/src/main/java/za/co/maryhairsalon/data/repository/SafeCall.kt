package za.co.maryhairsalon.data.repository

import android.util.Log
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CancellationException
import za.co.maryhairsalon.data.remote.ErrorMessages
import java.io.IOException

/** An error whose message is safe to show directly to the user (e.g. in a Snackbar). */
class UserFacingException(message: String) : Exception(message)

/**
 * Runs a repository call and turns every failure into a [UserFacingException], so the UI
 * can always do: result.onFailure { showSnackbar(it.message) } without crashing.
 */
internal suspend fun <T> safeCall(tag: String, action: String, block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(toUserFacing(tag, action, e))
}

internal fun toUserFacing(tag: String, action: String, e: Exception): UserFacingException = when (e) {
    is UserFacingException -> {
        Log.w(tag, "$action rejected: ${e.message}")
        e
    }
    is IllegalArgumentException -> {
        Log.w(tag, "$action invalid input: ${e.message}")
        UserFacingException(e.message ?: "Invalid input.")
    }
    is PostgrestRestException -> {
        Log.e(tag, "$action failed: code=${e.code} ${e.error}", e)
        UserFacingException(ErrorMessages.fromDatabase(e.code, e.error))
    }
    is AuthRestException -> {
        Log.w(tag, "$action auth error: ${e.error}", e)
        UserFacingException(ErrorMessages.fromAuth(e.error))
    }
    is RestException -> {
        Log.e(tag, "$action failed: ${e.error}", e)
        UserFacingException(ErrorMessages.userMessage(e.error) ?: ErrorMessages.GENERIC)
    }
    is HttpRequestException, is HttpRequestTimeoutException, is IOException -> {
        Log.w(tag, "$action network error", e)
        UserFacingException(ErrorMessages.OFFLINE)
    }
    else -> {
        Log.e(tag, "$action failed unexpectedly", e)
        UserFacingException(ErrorMessages.GENERIC)
    }
}
