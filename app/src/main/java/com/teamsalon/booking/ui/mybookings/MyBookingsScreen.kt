package com.teamsalon.booking.ui.mybookings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamsalon.booking.data.BookingEntity
import com.teamsalon.booking.data.BookingRepository

@Composable
fun MyBookingsScreen(
    repository: BookingRepository,
    userId: Int,
    viewModel: MyBookingsViewModel = viewModel(factory = MyBookingsViewModelFactory(repository, userId))
) {
    val bookings by viewModel.bookings.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("My Bookings") }) }) { padding ->
        if (bookings.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), Alignment.Center) { Text("No bookings yet.") }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                items(bookings, key = { it.id }) { booking ->
                    BookingRow(booking, onCancel = { viewModel.cancel(booking.id) })
                }
            }
        }
    }
}

@Composable
private fun BookingRow(booking: BookingEntity, onCancel: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("${booking.date} at ${booking.time}", style = MaterialTheme.typography.titleMedium)
                Text("Status: ${booking.status}", style = MaterialTheme.typography.bodySmall)
            }
            if (booking.status != "cancelled") {
                TextButton(onClick = onCancel) { Text("Cancel") }
            }
        }
    }
}
