package za.co.maryhairsalon.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import za.co.maryhairsalon.data.model.User
import za.co.maryhairsalon.data.repository.AuthRepository

/** Where the app is in the login lifecycle; drives role-based routing (spec FR-02). */
sealed interface SessionState {
    /** Supabase is still restoring a saved session at start-up. */
    data object Loading : SessionState
    data object SignedOut : SessionState
    /** Logged in, but the profile row (with the role) hasn't arrived yet. */
    data object ProfileLoading : SessionState
    data class SignedIn(val user: User) : SessionState
}

class RootViewModel(private val auth: AuthRepository) : ViewModel() {

    val session: StateFlow<SessionState> = combine(
        auth.isInitializing,
        auth.currentUid,
        auth.observeCurrentUser(),
    ) { initializing, uid, user ->
        when {
            initializing -> SessionState.Loading
            uid == null -> SessionState.SignedOut
            user == null -> SessionState.ProfileLoading
            else -> SessionState.SignedIn(user)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)

    fun signOut() {
        viewModelScope.launch { auth.signOut() }
    }
}
