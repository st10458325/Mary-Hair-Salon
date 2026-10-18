package za.co.maryhairsalon.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import za.co.maryhairsalon.MaryHairSalonApp
import za.co.maryhairsalon.di.AppContainer

/**
 * Creates a ViewModel with access to the app's repositories:
 *   val vm: LoginViewModel = appViewModel { LoginViewModel(it.authRepository) }
 * `key` lets one screen keep separate ViewModels (e.g. one booking form per service).
 */
@Composable
inline fun <reified VM : ViewModel> appViewModel(key: String? = null, crossinline create: (AppContainer) -> VM): VM =
    viewModel(
        key = key,
        factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MaryHairSalonApp
                create(app.container)
            }
        },
    )

/**
 * One-off messages from a ViewModel to the screen (shown in a Snackbar).
 * A Channel makes sure each message is shown exactly once, even after rotation.
 */
class UiMessages {
    private val channel = Channel<String>(Channel.BUFFERED)
    val flow: Flow<String> = channel.receiveAsFlow()
    fun send(message: String) {
        channel.trySend(message)
    }
}

/** Shows every message from [messages] in [snackbar]. */
@Composable
fun CollectMessages(messages: UiMessages, snackbar: SnackbarHostState) {
    LaunchedEffect(messages) {
        messages.flow.collect { snackbar.showSnackbar(it) }
    }
}
