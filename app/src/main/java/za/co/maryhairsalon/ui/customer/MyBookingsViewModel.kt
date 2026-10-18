package za.co.maryhairsalon.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import za.co.maryhairsalon.data.model.Booking
import za.co.maryhairsalon.data.model.BookingStatus
import za.co.maryhairsalon.data.repository.BookingRepository
import za.co.maryhairsalon.ui.UiMessages

/** "My Bookings": status filter with live counts, cancel and reschedule (spec FR-06). */
class MyBookingsViewModel(private val bookings: BookingRepository, uid: String) : ViewModel() {

    val messages = UiMessages()

    /** null = "All". */
    private val _filter = MutableStateFlow<BookingStatus?>(null)
    val filter: StateFlow<BookingStatus?> = _filter.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val all: Flow<List<Booking>> =
        if (uid.isBlank()) flowOf(emptyList()) else bookings.observeMyBookings(uid)

    /** Pending first, then upcoming, completed, cancelled (same order as the website). */
    val visible: StateFlow<List<Booking>?> = combine(all, _filter) { list, f ->
        list.filter { f == null || it.status == f }.sortedBy { it.status.ordinal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val counts: StateFlow<Map<BookingStatus, Int>> =
        (if (uid.isBlank()) emptyFlow<Map<BookingStatus, Int>>() else bookings.observeMyStatusCounts(uid))
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun setFilter(status: BookingStatus?) {
        _filter.value = status
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            bookings.refreshMine().onFailure { messages.send(it.message ?: "Couldn't refresh.") }
            _refreshing.value = false
        }
    }

    fun cancel(id: String) {
        viewModelScope.launch {
            bookings.cancel(id)
                .onSuccess { messages.send("Booking cancelled.") }
                .onFailure { messages.send(it.message ?: "Couldn't cancel this booking.") }
        }
    }
}
