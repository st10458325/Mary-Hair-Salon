package com.teamsalon.booking.ui.help

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * In-app help system — Person 3's "user support" deliverable. Keep this in
 * sync with the Word user guide; this is the quick in-app version a user
 * reaches by tapping "Help" from the Services screen.
 */
@Composable
fun HelpScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(24.dp).verticalScroll(rememberScrollState())) {
            HelpSection(
                "Logging in",
                "Use one of the demo accounts, e.g. customer1@demo.com / demo123. The login screen shows the exact credentials."
            )
            HelpSection(
                "Booking a service",
                "From the Services screen, tap \"Book\" on any service, then enter a date (yyyy-MM-dd) and time (HH:mm) and confirm."
            )
            HelpSection(
                "Managing bookings",
                "Open \"My Bookings\" from the Services screen to see everything you've booked, and cancel anything that's still pending or confirmed."
            )
            HelpSection(
                "Need more help?",
                "This is a demo project built for coursework — see the project README and the accompanying user guide document for full details."
            )
        }
    }
}

@Composable
private fun HelpSection(title: String, body: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(4.dp))
    Text(body, style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(20.dp))
}
