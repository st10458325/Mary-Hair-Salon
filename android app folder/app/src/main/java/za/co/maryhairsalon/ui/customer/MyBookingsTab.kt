package za.co.maryhairsalon.ui.customer

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import za.co.maryhairsalon.data.model.Booking
import za.co.maryhairsalon.data.model.BookingStatus
import za.co.maryhairsalon.ui.CollectMessages
import za.co.maryhairsalon.ui.components.BookingCard
import za.co.maryhairsalon.ui.components.CardAction
import za.co.maryhairsalon.ui.components.EmptyState
import za.co.maryhairsalon.ui.components.GradientButton
import za.co.maryhairsalon.ui.components.LoadingBox
import za.co.maryhairsalon.ui.components.SectionHeader
import za.co.maryhairsalon.ui.theme.Brand

/** Status filter chips with live counts (shared by customer, stylist and manager lists). */
@Composable
fun StatusFilterRow(selected: BookingStatus?, counts: Map<BookingStatus, Int>, onSelect: (BookingStatus?) -> Unit) {
    val total = counts.values.sum()
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatusChip("All $total", selected == null) { onSelect(null) }
        BookingStatus.entries.forEach { status ->
            StatusChip("${status.dbValue.replaceFirstChar { it.uppercase() }} ${counts[status] ?: 0}", selected == status) { onSelect(status) }
        }
    }
}

@Composable
private fun StatusChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Brand.Orange,
            selectedLabelColor = androidx.compose.ui.graphics.Color.White,
            containerColor = Brand.Glass,
            labelColor = Brand.TextDim,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsTab(
    vm: MyBookingsViewModel,
    snackbar: SnackbarHostState,
    onReschedule: (Booking) -> Unit,
    onBrowse: () -> Unit,
) {
    val list by vm.visible.collectAsStateWithLifecycle()
    val counts by vm.counts.collectAsStateWithLifecycle()
    val filter by vm.filter.collectAsStateWithLifecycle()
    val refreshing by vm.refreshing.collectAsStateWithLifecycle()
    var confirmCancel by remember { mutableStateOf<Booking?>(null) }
    CollectMessages(vm.messages, snackbar)

    confirmCancel?.let { b ->
        AlertDialog(
            onDismissRequest = { confirmCancel = null },
            title = { Text("Cancel this booking?") },
            text = { Text("${b.serviceTitle} on ${b.dateFormatted} at ${b.time}. The time slot will be released for other clients.") },
            confirmButton = {
                TextButton(onClick = { vm.cancel(b.id); confirmCancel = null }) { Text("Yes, cancel", color = Brand.Danger) }
            },
            dismissButton = { TextButton(onClick = { confirmCancel = null }) { Text("Keep it", color = Brand.OrangeLight) } },
            containerColor = Brand.BgElevated,
        )
    }

    PullToRefreshBox(isRefreshing = refreshing, onRefresh = vm::refresh, modifier = Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader("Your Account", "My Bookings")
                    StatusFilterRow(filter, counts, vm::setFilter)
                }
            }
            val bookings = list
            when {
                bookings == null -> item { LoadingBox(Modifier.padding(top = 60.dp)) }
                bookings.isEmpty() -> item {
                    EmptyState(
                        if (filter == null) "No bookings yet" else "Nothing here",
                        if (filter == null) "Once you book a service, it'll show up here." else "You have no ${filter?.dbValue} bookings.",
                        action = if (filter == null) { { GradientButton("Browse Services", onBrowse) } } else null,
                    )
                }
                else -> items(bookings, key = { it.id }) { b ->
                    BookingCard(
                        b,
                        actions = if (b.status.isActive) {
                            {
                                CardAction("Reschedule", { onReschedule(b) })
                                CardAction("Cancel", { confirmCancel = b }, danger = true)
                            }
                        } else null,
                    )
                }
            }
        }
    }
}
