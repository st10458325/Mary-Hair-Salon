package com.teamsalon.booking.ui.mybookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teamsalon.booking.data.BookingEntity
import com.teamsalon.booking.data.BookingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyBookingsViewModel(
    private val repository: BookingRepository,
    userId: Int
) : ViewModel() {
    val bookings: StateFlow<List<BookingEntity>> = repository.observeBookingsForUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun cancel(bookingId: Int) {
        viewModelScope.launch { repository.cancelBooking(bookingId) }
    }
}

class MyBookingsViewModelFactory(
    private val repository: BookingRepository,
    private val userId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MyBookingsViewModel(repository, userId) as T
}
