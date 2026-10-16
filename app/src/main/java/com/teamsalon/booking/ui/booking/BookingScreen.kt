package com.teamsalon.booking.ui.booking

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamsalon.booking.data.BookingRepository

@Composable
fun BookingScreen(
    repository: BookingRepository,
    userId: Int,
    serviceId: Int,
    onDone: () -> Unit,
    viewModel: BookingViewModel = viewModel(factory = BookingViewModelFactory(repository, userId, serviceId))
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.submitted) {
        if (uiState.submitted) onDone()
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Book Appointment") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(24.dp).fillMaxSize()) {
            OutlinedTextField(
                value = uiState.date,
                onValueChange = viewModel::onDateChange,
                label = { Text("Date (yyyy-MM-dd)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.time,
                onValueChange = viewModel::onTimeChange,
                label = { Text("Time (HH:mm)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            uiState.errorMessage?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = viewModel::submit, enabled = !uiState.isSubmitting, modifier = Modifier.fillMaxWidth()) {
                if (uiState.isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else Text("Confirm Booking")
            }
        }
    }
}
