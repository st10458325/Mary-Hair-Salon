package za.co.maryhairsalon.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import za.co.maryhairsalon.data.model.BookingRules
import za.co.maryhairsalon.data.model.Role
import za.co.maryhairsalon.data.model.User
import za.co.maryhairsalon.data.repository.AuthRepository
import za.co.maryhairsalon.ui.CollectMessages
import za.co.maryhairsalon.ui.UiMessages
import za.co.maryhairsalon.ui.components.AppTextField
import za.co.maryhairsalon.ui.components.GhostButton
import za.co.maryhairsalon.ui.components.GlassCard
import za.co.maryhairsalon.ui.components.GradientButton
import za.co.maryhairsalon.ui.components.InitialsAvatar
import za.co.maryhairsalon.ui.components.SectionHeader
import za.co.maryhairsalon.ui.theme.Brand

/** Settings: edit your profile and preferences, sign out (Task 2: "change their settings"). */
class SettingsViewModel(private val auth: AuthRepository) : ViewModel() {
    val messages = UiMessages()
    val user: StateFlow<User?> = auth.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    fun save(name: String, surname: String, contact: String, notifications: Boolean) {
        if (contact.isNotBlank() && !BookingRules.isValidContact(contact)) {
            messages.send("Enter a valid SA cell number, e.g. 0821234567.")
            return
        }
        viewModelScope.launch {
            _saving.value = true
            auth.updateSettings(name, surname, contact, notifications)
                .onSuccess { messages.send("Settings saved.") }
                .onFailure { messages.send(it.message ?: "Couldn't save your settings.") }
            _saving.value = false
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            auth.sendPasswordReset(email)
                .onSuccess { messages.send("Password reset link sent to $email.") }
                .onFailure { messages.send(it.message ?: "Couldn't send the reset email.") }
        }
    }

    fun signOut() {
        viewModelScope.launch { auth.signOut() }
    }
}

@Composable
fun SettingsTab(vm: SettingsViewModel, snackbar: SnackbarHostState, onHelp: () -> Unit) {
    val user by vm.user.collectAsStateWithLifecycle()
    val saving by vm.saving.collectAsStateWithLifecycle()
    CollectMessages(vm.messages, snackbar)
    val u = user ?: return

    var name by remember(u.id) { mutableStateOf(u.name) }
    var surname by remember(u.id) { mutableStateOf(u.surname) }
    var contact by remember(u.id) { mutableStateOf(u.contact) }
    var notifications by remember(u.id) { mutableStateOf(u.notificationsEnabled) }
    var confirmSignOut by remember { mutableStateOf(false) }
    // Keep the form in sync if the profile is refreshed from the server.
    LaunchedEffect(u) {
        name = u.name; surname = u.surname; contact = u.contact; notifications = u.notificationsEnabled
    }

    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            title = { Text("Log out?") },
            text = { Text("You'll need to log in again to see your bookings.") },
            confirmButton = { TextButton(onClick = { confirmSignOut = false; vm.signOut() }) { Text("Log out", color = Brand.Danger) } },
            dismissButton = { TextButton(onClick = { confirmSignOut = false }) { Text("Stay", color = Brand.OrangeLight) } },
            containerColor = Brand.BgElevated,
        )
    }

    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionHeader("Your Account", "Profile & Settings")
        GlassCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                InitialsAvatar(u.displayName.ifBlank { u.email }, size = 56.dp)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(u.displayName.ifBlank { "Your name" }, style = MaterialTheme.typography.titleLarge)
                    Text(u.email, color = Brand.TextDim)
                    Text(
                        when (u.role) {
                            Role.CUSTOMER -> "Customer"
                            Role.EMPLOYEE -> "Stylist" + if (u.title.isNotBlank()) " · ${u.title}" else ""
                            Role.MANAGER -> "Manager"
                        },
                        color = Brand.OrangeLight,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }

        GlassCard(Modifier.fillMaxWidth()) {
            Text("Personal details", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTextField(name, { name = it }, "Name", Modifier.weight(1f))
                AppTextField(surname, { surname = it }, "Surname", Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            AppTextField(contact, { contact = it }, "Contact (Cell No. / WhatsApp)", keyboardType = KeyboardType.Phone, leadingIcon = Icons.Default.Phone)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Booking notifications", fontWeight = FontWeight.SemiBold)
                    Text("Updates when a booking is confirmed or changed.", style = MaterialTheme.typography.bodySmall, color = Brand.TextDim)
                }
                Switch(notifications, { notifications = it }, colors = SwitchDefaults.colors(checkedTrackColor = Brand.Orange))
            }
            Spacer(Modifier.height(14.dp))
            GradientButton("Save Changes", { vm.save(name, surname, contact, notifications) }, Modifier.fillMaxWidth(), loading = saving)
        }

        GlassCard(Modifier.fillMaxWidth(), contentPadding = 4.dp) {
            SettingsRow("Help & how to use the app", onHelp)
            HorizontalDivider(color = Brand.GlassBorder)
            SettingsRow("Send me a password reset email") { vm.resetPassword(u.email) }
        }

        GhostButton("Log Out", { confirmSignOut = true }, Modifier.fillMaxWidth())
        Text(
            "Mary Hair Salon · version ${za.co.maryhairsalon.BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodySmall,
            color = Brand.TextFaint,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SettingsRow(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(text, Modifier.weight(1f), color = Brand.Text)
        Text("›", color = Brand.TextFaint)
    }
}

/** In-app help (Task 2: "help system and user documentation"). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = Brand.Bg,
        topBar = {
            TopAppBar(
                title = { Text("Help") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Brand.Bg),
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionHeader("Support", "How to use Mary Hair Salon")
            HELP.forEach { (q, a) ->
                GlassCard(Modifier.fillMaxWidth()) {
                    Text(q, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(a, color = Brand.TextDim)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

private val HELP = listOf(
    "How do I book an appointment?" to
        "Open Services, pick a category and tap Book Now on a style. Choose your stylist, Incall (at the salon) or Outcall (we come to you), a date and a free time, then tap Confirm Booking.",
    "Why are some times crossed out?" to
        "That stylist is already booked at that time, or the time has already passed today. Try another time or another stylist.",
    "What does Pending mean?" to
        "Your request has been sent. It changes to Upcoming once your stylist accepts it. If it's declined it shows as Cancelled.",
    "Can I change or cancel a booking?" to
        "Yes. Under Bookings, tap Reschedule to pick a new stylist, date or time (it goes back to Pending), or Cancel to release the slot.",
    "How is the price worked out?" to
        "Prices and discounts are set by the salon. The amount shown in the summary is the price after any discount, and it's confirmed by the salon's system when you book.",
    "How do I sign in with Google?" to
        "On the login screen tap Continue with Google and choose your account. Your profile is created automatically the first time.",
    "I forgot my password" to
        "On the login screen tap Forgot password? and enter your email. You'll get a link to choose a new password.",
    "How do I update my details?" to
        "Go to the Profile tab, edit your name, surname or cell number and tap Save Changes. You can also turn booking notifications on or off there.",
    "I'm a stylist — how do I manage requests?" to
        "Your Schedule tab shows new requests under Pending. Tap Accept or Decline. After the appointment, tap Complete under Upcoming.",
    "I'm the manager — what can I do?" to
        "See every booking and change its status or stylist, promote customers to stylists under Staff, and add or edit services, prices and discounts under Catalogue.",
    "Does the app work offline?" to
        "Yes, you can see your last loaded services and bookings offline. Booking, cancelling and saving changes need an internet connection.",
)
