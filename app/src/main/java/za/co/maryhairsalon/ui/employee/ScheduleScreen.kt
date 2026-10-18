package za.co.maryhairsalon.ui.employee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import za.co.maryhairsalon.data.model.Booking
import za.co.maryhairsalon.data.model.BookingStatus
import za.co.maryhairsalon.data.repository.BookingRepository
import za.co.maryhairsalon.ui.CollectMessages
import za.co.maryhairsalon.ui.UiMessages
import za.co.maryhairsalon.ui.components.BookingCard
import za.co.maryhairsalon.ui.components.CardAction
import za.co.maryhairsalon.ui.components.EmptyState
import za.co.maryhairsalon.ui.components.LoadingBox
import za.co.maryhairsalon.ui.components.SectionHeader
import za.co.maryhairsalon.ui.customer.StatusFilterRow
import za.co.maryhairsalon.ui.theme.Brand

/** Stylist schedule (spec FR-07): accept/decline pending requests, complete upcoming ones. */
class ScheduleViewModel(private val bookings: BookingRepository, stylistId: String) : ViewModel() {
    val messages = UiMessages()

    /** Default view: what needs attention (pending). */
    private val _filter = MutableStateFlow<BookingStatus?>(BookingStatus.PENDING)
    val filter: StateFlow<BookingStatus?> = _filter.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val all = bookings.observeSchedule(stylistId)

    /** Soonest first. */
    val visible: StateFlow<List<Booking>?> = combine(all, _filter) { list, f ->
        list.filter { f == null || it.status == f }.sortedWith(compareBy({ it.date }, { it.time }))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val counts: StateFlow<Map<BookingStatus, Int>> = all
        .map { list -> list.groupingBy { it.status }.eachCount() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun setFilter(s: BookingStatus?) {
        _filter.value = s
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            bookings.refreshScheduleNow().onFailure { messages.send(it.message ?: "Couldn't refresh.") }
            _refreshing.value = false
        }
    }

    fun accept(b: Booking) = act(bookings::accept, b, "Booking accepted — the client will see it as upcoming.")
    fun decline(b: Booking) = act(bookings::decline, b, "Booking declined.")
    fun complete(b: Booking) = act(bookings::complete, b, "Marked as completed.")

    private fun act(action: suspend (String) -> Result<Unit>, b: Booking, success: String) {
        viewModelScope.launch {
            action(b.id)
                .onSuccess { messages.send(success) }
                .onFailure { messages.send(it.message ?: "Couldn't update this booking.") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleTab(vm: ScheduleViewModel, snackbar: SnackbarHostState) {
    val list by vm.visible.collectAsStateWithLifecycle()
    val counts by vm.counts.collectAsStateWithLifecycle()
    val filter by vm.filter.collectAsStateWithLifecycle()
    val refreshing by vm.refreshing.collectAsStateWithLifecycle()
    CollectMessages(vm.messages, snackbar)

    PullToRefreshBox(isRefreshing = refreshing, onRefresh = vm::refresh, modifier = Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader("Stylist", "My Schedule")
                    StatusFilterRow(filter, counts, vm::setFilter)
                }
            }
            val bookings = list
            when {
                bookings == null -> item { LoadingBox(Modifier.padding(top = 60.dp)) }
                bookings.isEmpty() -> item {
                    EmptyState("All clear", if (filter == BookingStatus.PENDING) "No new booking requests right now." else "No bookings in this list.")
                }
                else -> items(bookings, key = { it.id }) { b ->
                    BookingCard(
                        b,
                        showCustomer = true,
                        actions = when (b.status) {
                            BookingStatus.PENDING -> {
                                {
                                    CardAction("Decline", { vm.decline(b) }, danger = true)
                                    CardAction("Accept", { vm.accept(b) }, color = Brand.Success)
                                }
                            }
                            BookingStatus.UPCOMING -> {
                                {
                                    CardAction("Cancel", { vm.decline(b) }, danger = true)
                                    CardAction("Complete", { vm.complete(b) }, color = Brand.Success)
                                }
                            }
                            else -> null
                        },
                    )
                }
            }
        }
    }
}
