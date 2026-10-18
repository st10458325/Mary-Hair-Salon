package za.co.maryhairsalon.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import za.co.maryhairsalon.data.model.BookingRules
import za.co.maryhairsalon.data.repository.AuthRepository
import za.co.maryhairsalon.data.repository.SignUpOutcome

data class AuthUiState(
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val contact: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val acceptedTerms: Boolean = false,
    val loading: Boolean = false,
    /** Error shown above the button (red). */
    val error: String? = null,
    /** Information shown above the button (green), e.g. "check your email". */
    val info: String? = null,
    /** Field-level validation messages. */
    val fieldErrors: Map<String, String> = emptyMap(),
)

/**
 * Shared by the login, sign-up and forgot-password screens. On success nothing navigates
 * here: the app's root watches the session and moves to the right home screen by role.
 */
class AuthViewModel(private val auth: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun onName(v: String) = _state.update { it.copy(name = v, fieldErrors = it.fieldErrors - "name") }
    fun onSurname(v: String) = _state.update { it.copy(surname = v, fieldErrors = it.fieldErrors - "surname") }
    fun onEmail(v: String) = _state.update { it.copy(email = v.trim(), fieldErrors = it.fieldErrors - "email") }
    fun onContact(v: String) = _state.update { it.copy(contact = v, fieldErrors = it.fieldErrors - "contact") }
    fun onPassword(v: String) = _state.update { it.copy(password = v, fieldErrors = it.fieldErrors - "password") }
    fun onConfirm(v: String) = _state.update { it.copy(confirmPassword = v, fieldErrors = it.fieldErrors - "confirm") }
    fun onTerms(v: Boolean) = _state.update { it.copy(acceptedTerms = v) }
    fun clearMessages() = _state.update { it.copy(error = null, info = null, fieldErrors = emptyMap()) }

    private fun isEmail(v: String) = android.util.Patterns.EMAIL_ADDRESS.matcher(v).matches()

    fun login() {
        val s = _state.value
        val errors = buildMap {
            if (!isEmail(s.email)) put("email", "Enter a valid email address.")
            if (s.password.isEmpty()) put("password", "Enter your password.")
        }
        if (errors.isNotEmpty()) { _state.update { it.copy(fieldErrors = errors) }; return }
        submit { auth.signIn(s.email, s.password).map { null } }
    }

    fun signUp() {
        val s = _state.value
        val errors = buildMap {
            if (s.name.isBlank()) put("name", "Required.")
            if (s.surname.isBlank()) put("surname", "Required.")
            if (!isEmail(s.email)) put("email", "Enter a valid email address.")
            if (!BookingRules.isValidContact(s.contact)) put("contact", "Enter a valid SA cell number, e.g. 0821234567.")
            if (s.password.length < 8) put("password", "At least 8 characters.")
            if (s.confirmPassword != s.password) put("confirm", "Passwords don't match.")
        }
        if (errors.isNotEmpty()) { _state.update { it.copy(fieldErrors = errors) }; return }
        if (!s.acceptedTerms) { _state.update { it.copy(error = "Please accept the Terms of Service and Privacy Policy.") }; return }
        submit {
            auth.signUp(s.name, s.surname, s.contact, s.email, s.password).map { outcome ->
                if (outcome == SignUpOutcome.CHECK_EMAIL) "Account created! Check your email and tap the link to confirm it, then log in." else null
            }
        }
    }

    fun sendReset() {
        val s = _state.value
        if (!isEmail(s.email)) { _state.update { it.copy(fieldErrors = mapOf("email" to "Enter a valid email address.")) }; return }
        submit {
            auth.sendPasswordReset(s.email).map { "If an account exists for ${s.email}, a reset link is on its way. Check your inbox." }
        }
    }

    fun signInWithGoogle(activityContext: Context) {
        if (_state.value.loading) return
        _state.update { it.copy(loading = true, error = null, info = null) }
        viewModelScope.launch {
            val token = GoogleSignIn.getToken(activityContext)
            val result = token.fold(
                onSuccess = { auth.signInWithGoogle(it.idToken, it.rawNonce) },
                onFailure = { Result.failure(it) },
            )
            _state.update {
                it.copy(loading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    /** Runs an auth call; a non-null success value is shown as an info message. */
    private fun submit(block: suspend () -> Result<String?>) {
        if (_state.value.loading) return
        _state.update { it.copy(loading = true, error = null, info = null) }
        viewModelScope.launch {
            val result = block()
            _state.update {
                it.copy(
                    loading = false,
                    error = result.exceptionOrNull()?.message,
                    info = result.getOrNull(),
                )
            }
        }
    }
}
