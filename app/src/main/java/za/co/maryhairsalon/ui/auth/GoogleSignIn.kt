package za.co.maryhairsalon.ui.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import za.co.maryhairsalon.BuildConfig
import java.security.MessageDigest
import java.util.UUID

/** What Google returns: the ID token for Supabase, plus the raw nonce it was issued for. */
data class GoogleToken(val idToken: String, val rawNonce: String)

/**
 * Single sign-on with Google using Android's Credential Manager (the "Sign in with Google"
 * bottom sheet). The ID token is then exchanged with Supabase Auth in AuthRepository.
 *
 * Needs GOOGLE_WEB_CLIENT_ID in local.properties = the *Web* OAuth client id, the same one
 * configured in Supabase -> Authentication -> Providers -> Google.
 */
object GoogleSignIn {
    private const val TAG = "GoogleSignIn"

    val isConfigured: Boolean get() = BuildConfig.GOOGLE_WEB_CLIENT_ID.endsWith(".apps.googleusercontent.com")

    /** @param activityContext must be an Activity context (LocalContext inside setContent is fine). */
    suspend fun getToken(activityContext: Context): Result<GoogleToken> {
        if (!isConfigured) {
            return Result.failure(IllegalStateException("Google sign-in isn't set up yet (GOOGLE_WEB_CLIENT_ID is missing)."))
        }
        // A random nonce protects against replayed tokens. Google gets the SHA-256 hash,
        // Supabase gets the raw value and checks they match.
        val rawNonce = UUID.randomUUID().toString()
        val hashedNonce = MessageDigest.getInstance("SHA-256")
            .digest(rawNonce.toByteArray())
            .joinToString("") { "%02x".format(it) }

        val option = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setNonce(hashedNonce)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

        return try {
            val result = CredentialManager.create(activityContext).getCredential(activityContext, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val google = GoogleIdTokenCredential.createFrom(credential.data)
                Log.i(TAG, "Got Google ID token")
                Result.success(GoogleToken(google.idToken, rawNonce))
            } else {
                Result.failure(IllegalStateException("Unexpected credential type from Google."))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(IllegalStateException("Google sign-in was cancelled."))
        } catch (e: NoCredentialException) {
            Result.failure(IllegalStateException("No Google account found on this phone. Add one in Settings → Accounts."))
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Google sign-in failed", e)
            Result.failure(IllegalStateException("Google sign-in failed. Please try again."))
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in failed unexpectedly", e)
            Result.failure(IllegalStateException("Google sign-in failed. Please try again."))
        }
    }
}
