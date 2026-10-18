package za.co.maryhairsalon.data.repository

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import za.co.maryhairsalon.data.local.AppDatabase
import za.co.maryhairsalon.data.local.toDomain
import za.co.maryhairsalon.data.local.toEntity
import za.co.maryhairsalon.data.model.User
import za.co.maryhairsalon.data.remote.Db
import za.co.maryhairsalon.data.remote.ProfileDto
import za.co.maryhairsalon.data.remote.offlineFirst

/** Result of sign-up: Supabase may require the user to confirm their email first. */
enum class SignUpOutcome { SIGNED_IN, CHECK_EMAIL }

/**
 * Login, sign-up, single sign-on (Google), password reset and the signed-in user's
 * profile/settings.
 *
 * Passwords: Supabase Auth never stores plain-text passwords. They are sent over HTTPS
 * and stored as salted bcrypt hashes in auth.users, so the app never handles or stores
 * a password itself. This is how the "encrypt the password" requirement is met.
 */
class AuthRepository(
    private val supabase: SupabaseClient,
    private val db: AppDatabase,
) {
    /** Emits the signed-in user's id, or null when signed out. */
    val currentUid: Flow<String?> = supabase.auth.sessionStatus
        .map { status ->
            when (status) {
                is SessionStatus.Authenticated -> status.session.user?.id ?: supabase.auth.currentUserOrNull()?.id
                else -> null
            }
        }
        .distinctUntilChanged()

    val currentUidOrNull: String? get() = supabase.auth.currentUserOrNull()?.id

    /** True while Supabase is still loading a saved session at app start (show a splash/loader). */
    val isInitializing: Flow<Boolean> = supabase.auth.sessionStatus.map { it is SessionStatus.Initializing }

    /**
     * The signed-in user's profile, including role, used for role-based routing (spec FR-02).
     * Emits null when signed out. Cached in Room so routing also works offline.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeCurrentUser(): Flow<User?> = currentUid.flatMapLatest { uid ->
        if (uid == null) flowOf(null)
        else offlineFirst(
            name = "currentUser",
            refresh = { refreshProfile(uid) },
            fromRoom = db.userDao().observe(uid).map { it?.toDomain() },
        )
    }

    private suspend fun refreshProfile(uid: String) {
        val profile = supabase.from(Db.PROFILES)
            .select { filter { eq("id", uid) } }
            .decodeSingleOrNull<ProfileDto>()
        profile?.let { db.userDao().upsert(it.toDomain().toEntity()) }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> = safeCall<Unit>(TAG, "signIn") {
        require(email.isNotBlank() && password.isNotBlank()) { "Please enter your email and password." }
        supabase.auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
        Log.i(TAG, "Signed in with email")
    }

    /**
     * Creates the account. The database trigger on_auth_user_created then creates the
     * profiles row (role is always "customer"; only a manager can promote someone).
     */
    suspend fun signUp(
        name: String,
        surname: String,
        contact: String,
        email: String,
        password: String,
    ): Result<SignUpOutcome> = safeCall(TAG, "signUp") {
        require(name.isNotBlank() && surname.isNotBlank()) { "Please enter your name and surname." }
        require(android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) { "Please enter a valid email address." }
        require(password.length >= 8) { "Password must be at least 8 characters." }

        supabase.auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password
            data = buildJsonObject {
                put("first_name", name.trim())
                put("surname", surname.trim())
                put("contact", contact.trim())
            }
        }
        // If "Confirm email" is on in Supabase, there is no session until the user clicks the link.
        if (supabase.auth.currentSessionOrNull() != null) {
            Log.i(TAG, "Signed up and signed in")
            SignUpOutcome.SIGNED_IN
        } else {
            Log.i(TAG, "Signed up; waiting for email confirmation")
            SignUpOutcome.CHECK_EMAIL
        }
    }

    /**
     * Single sign-on with Google. The UI gets a Google ID token using Credential Manager
     * (GetGoogleIdOption with your Web client id) and passes it here. If you set a nonce on
     * the Google request, pass the raw (un-hashed) nonce too.
     * The profile row is created automatically by the database trigger on first login.
     */
    suspend fun signInWithGoogle(idToken: String, rawNonce: String? = null): Result<Unit> = safeCall<Unit>(TAG, "signInWithGoogle") {
        require(idToken.isNotBlank()) { "Google sign-in was cancelled." }
        supabase.auth.signInWith(IDToken) {
            this.idToken = idToken
            provider = Google
            nonce = rawNonce
        }
        Log.i(TAG, "Signed in with Google")
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = safeCall<Unit>(TAG, "sendPasswordReset") {
        require(email.isNotBlank()) { "Please enter your email address." }
        supabase.auth.resetPasswordForEmail(email.trim())
    }

    /**
     * Settings screen save. The database only lets users change these columns on their own
     * row (see 002_security.sql), never their role or email.
     */
    suspend fun updateSettings(
        name: String,
        surname: String,
        contact: String,
        notificationsEnabled: Boolean,
        photoUrl: String? = null,
    ): Result<Unit> = safeCall<Unit>(TAG, "updateSettings") {
        val uid = currentUidOrNull ?: throw UserFacingException("You are not signed in.")
        require(name.isNotBlank()) { "Name can't be empty." }
        supabase.from(Db.PROFILES).update({
            set("name", name.trim())
            set("surname", surname.trim())
            set("contact", contact.trim())
            set("notifications_enabled", notificationsEnabled)
            photoUrl?.let { set("photo_url", it.trim()) }
        }) {
            filter { eq("id", uid) }
        }
        refreshProfile(uid)
        Log.i(TAG, "Updated settings for $uid")
    }

    /** Signs out and wipes the local cache so the next user never sees someone else's data. */
    suspend fun signOut(): Result<Unit> = safeCall<Unit>(TAG, "signOut") {
        try {
            supabase.auth.signOut()
        } finally {
            withContext(Dispatchers.IO) { db.clearAllTables() }
            Log.i(TAG, "Signed out and cleared local cache")
        }
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}
