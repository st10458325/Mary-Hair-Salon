package com.teamsalon.booking.ui.services

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamsalon.booking.data.BookingRepository
import com.teamsalon.booking.data.ServiceEntity

@Composable
fun ServicesScreen(
    repository: BookingRepository,
    onBookService: (Int) -> Unit,
    onViewBookings: () -> Unit,
    onHelp: () -> Unit,
    viewModel: ServicesViewModel = viewModel(factory = ServicesViewModelFactory(repository))
) {
    val services by viewModel.services.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Services") },
                actions = {
                    TextButton(onClick = onViewBookings) { Text("My Bookings") }
                    TextButton(onClick = onHelp) { Text("Help") }
                }
            )
        }
    ) { padding ->
        if (services.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                items(services, key = { it.id }) { service -> ServiceRow(service, onBookService) }
            }
        }
    }
}

@Composable
private fun ServiceRow(service: ServiceEntity, onBook: (Int) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(service.title, style = MaterialTheme.typography.titleMedium)
                Text("${service.category} \u00b7 ${service.durationMinutes} min", style = MaterialTheme.typography.bodySmall)
                Text("R%.2f".format(service.price), style = MaterialTheme.typography.bodyMedium)
            }
            Button(onClick = { onBook(service.id) }) { Text("Book") }
        }
    }
}
