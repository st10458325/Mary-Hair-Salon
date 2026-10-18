package za.co.maryhairsalon.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import za.co.maryhairsalon.data.model.BookingRules
import za.co.maryhairsalon.data.model.BookingType
import za.co.maryhairsalon.data.model.Pricing
import za.co.maryhairsalon.ui.CollectMessages
import za.co.maryhairsalon.ui.components.AppTextField
import za.co.maryhairsalon.ui.components.GlassCard
import za.co.maryhairsalon.ui.components.GradientButton
import za.co.maryhairsalon.ui.components.InitialsAvatar
import za.co.maryhairsalon.ui.components.SelectBox
import za.co.maryhairsalon.ui.components.formatZar
import za.co.maryhairsalon.ui.theme.Brand

/** The booking form, laid out like the website's booking modal (numbered sections + summary). */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookingFormScreen(vm: BookingFormViewModel, onDone: () -> Unit, onBack: () -> Unit) {
    val f by vm.form.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()
    val services by vm.services.collectAsStateWithLifecycle()
    val stylists by vm.stylists.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    CollectMessages(vm.messages, snackbar)

    val service = services.firstOrNull { it.id == f.serviceId }
    val categoryServices = services.filter { it.categoryId == f.categoryId }

    if (f.saved) {
        AlertDialog(
            onDismissRequest = onDone,
            confirmButton = { TextButton(onClick = onDone) { Text("OK", color = Brand.OrangeLight) } },
            title = { Text(if (vm.isReschedule) "Booking updated" else "Booking requested") },
            text = {
                Text(
                    if (vm.isReschedule) "Your new date and time are waiting for your stylist to confirm."
                    else "Your stylist will confirm it soon. You can follow it under Bookings."
                )
            },
            containerColor = Brand.BgElevated,
        )
    }

    Scaffold(
        containerColor = Brand.Bg,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(if (vm.isReschedule) "Reschedule Booking" else "Book Your Appointment") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Brand.Bg),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 1. Personal information
            Section("1.", "Personal Information")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTextField(f.name, vm::onName, "Name", Modifier.weight(1f), leadingIcon = Icons.Default.Person)
                AppTextField(f.surname, vm::onSurname, "Surname", Modifier.weight(1f))
            }
            AppTextField(f.contact, vm::onContact, "Contact (Cell No. / WhatsApp)", keyboardType = KeyboardType.Phone, leadingIcon = Icons.Default.Phone)

            // 2. Service
            Section("2.", "Service Details")
            Text("Service", style = MaterialTheme.typography.labelMedium, color = Brand.TextDim)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { cat ->
                    SelectBox(cat.label, selected = cat.id == f.categoryId, onClick = { vm.selectCategory(cat.id) })
                }
            }
            if (f.categoryId.isNotBlank()) {
                Text("Style", style = MaterialTheme.typography.labelMedium, color = Brand.TextDim)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    categoryServices.forEach { svc ->
                        SelectBox(
                            svc.title,
                            selected = svc.id == f.serviceId,
                            onClick = { vm.selectService(svc.id) },
                            subtitle = formatZar(svc.discountedPrice),
                        )
                    }
                }
            }

            // 2.1 Stylist
            Section("2.1", "Choose Your Stylist")
            if (stylists.isEmpty()) {
                Text("Loading stylists…", color = Brand.TextDim)
            } else {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    stylists.forEach { st ->
                        val selected = st.id == f.stylistId
                        val shape = RoundedCornerShape(14.dp)
                        Column(
                            Modifier
                                .width(130.dp)
                                .clip(shape)
                                .background(if (selected) Brand.OrangeSoft else Brand.Glass)
                                .border(1.dp, if (selected) Brand.Orange else Brand.GlassBorder, shape)
                                .clickable { vm.selectStylist(st.id) }
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            InitialsAvatar(st.displayName)
                            Spacer(Modifier.height(6.dp))
                            Text(st.displayName, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Text(st.title.ifBlank { "Stylist" }, style = MaterialTheme.typography.bodySmall, color = Brand.TextDim, maxLines = 1)
                        }
                    }
                }
            }

            // 3. Booking type
            Section("3.", "Booking Type")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SelectBox("Incall", f.type == BookingType.INCALL, { vm.onType(BookingType.INCALL) }, Modifier.weight(1f), subtitle = "You come to us")
                SelectBox("Outcall", f.type == BookingType.OUTCALL, { vm.onType(BookingType.OUTCALL) }, Modifier.weight(1f), subtitle = "We come to you")
            }
            if (f.type == BookingType.OUTCALL) {
                AppTextField(f.location, vm::onLocation, "Location (for Outcall)", leadingIcon = Icons.Default.LocationOn, placeholder = "Enter the full address")
            }

            // 4. Date
            Section("4.", "Select Date")
            GlassCard(Modifier.fillMaxWidth(), contentPadding = 10.dp) {
                MonthCalendar(f.visibleMonth, f.date, vm::selectDate, vm::previousMonth, vm::nextMonth)
            }

            // 5. Time
            Row(verticalAlignment = Alignment.CenterVertically) {
                Section("5.", "Select Time")
                if (f.loadingTimes) {
                    Spacer(Modifier.width(10.dp))
                    CircularProgressIndicator(Modifier.height(16.dp).width(16.dp), strokeWidth = 2.dp, color = Brand.Orange)
                }
            }
            if (f.date == null) {
                Text("Pick a date to see available times.", color = Brand.TextDim)
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BookingRules.TIME_SLOTS.forEach { t ->
                        SelectBox(
                            t,
                            selected = t == f.time,
                            onClick = { vm.selectTime(t) },
                            enabled = !vm.isUnavailable(f, t),
                            modifier = Modifier.width(76.dp),
                        )
                    }
                }
                Text("Crossed-out times are already booked or have passed.", style = MaterialTheme.typography.bodySmall, color = Brand.TextFaint)
            }

            // 6. Notes
            Section("6.", "Additional Information")
            AppTextField(f.notes, vm::onNotes, "Notes / Special Requests", singleLine = false, minLines = 3)

            // 7. Summary
            Section("7.", "Booking Summary")
            GlassCard(Modifier.fillMaxWidth()) {
                SummaryRow("Service", categories.firstOrNull { it.id == f.categoryId }?.label ?: "-")
                SummaryRow("Style", service?.title ?: "-")
                SummaryRow("Stylist", stylists.firstOrNull { it.id == f.stylistId }?.displayName ?: "-")
                SummaryRow("Booking Type", f.type.dbValue)
                SummaryRow("Date", f.date?.let { BookingRules.formatDate(it.toString()) } ?: "-")
                SummaryRow("Time", f.time.ifBlank { "-" })
                SummaryRow("Location", if (f.type == BookingType.OUTCALL) f.location.ifBlank { "-" } else "N/A")
                Spacer(Modifier.height(8.dp))
                val cost = service?.price ?: 0.0
                val discount = service?.discount ?: 0.0
                SummaryRow("Cost (ZAR)", formatZar(cost))
                SummaryRow("Discount", "${discount.toInt()}%")
                Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                    Text("Amount After Discount", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text(formatZar(Pricing.total(cost, discount)), color = Brand.OrangeLight, fontWeight = FontWeight.Bold)
                }
            }

            GradientButton(
                if (vm.isReschedule) "Save Changes" else "Confirm Booking",
                vm::submit,
                Modifier.fillMaxWidth(),
                loading = f.submitting,
            )
            Text(
                "The price is confirmed by the salon's system when you book.",
                style = MaterialTheme.typography.bodySmall,
                color = Brand.TextFaint,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Section(number: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
        Text(number, color = Brand.OrangeLight, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(6.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, Modifier.weight(1f), color = Brand.TextDim)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}
