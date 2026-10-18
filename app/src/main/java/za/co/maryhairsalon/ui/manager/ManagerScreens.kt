package za.co.maryhairsalon.ui.manager

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import za.co.maryhairsalon.data.model.Booking
import za.co.maryhairsalon.data.model.BookingStatus
import za.co.maryhairsalon.data.model.Category
import za.co.maryhairsalon.data.model.Role
import za.co.maryhairsalon.data.model.Service
import za.co.maryhairsalon.data.model.User
import za.co.maryhairsalon.ui.CollectMessages
import za.co.maryhairsalon.ui.components.AppTextField
import za.co.maryhairsalon.ui.components.BookingCard
import za.co.maryhairsalon.ui.components.CardAction
import za.co.maryhairsalon.ui.components.EmptyState
import za.co.maryhairsalon.ui.components.GlassCard
import za.co.maryhairsalon.ui.components.GradientButton
import za.co.maryhairsalon.ui.components.InitialsAvatar
import za.co.maryhairsalon.ui.components.LoadingBox
import za.co.maryhairsalon.ui.components.SectionHeader
import za.co.maryhairsalon.ui.components.SelectBox
import za.co.maryhairsalon.ui.components.formatZar
import za.co.maryhairsalon.ui.customer.StatusFilterRow
import za.co.maryhairsalon.ui.theme.Brand

/* ------------------------------ Bookings (FR-08) ------------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerBookingsTab(vm: ManagerBookingsViewModel, snackbar: SnackbarHostState) {
    val list by vm.visible.collectAsStateWithLifecycle()
    val counts by vm.counts.collectAsStateWithLifecycle()
    val filter by vm.filter.collectAsStateWithLifecycle()
    val refreshing by vm.refreshing.collectAsStateWithLifecycle()
    val stylists by vm.stylists.collectAsStateWithLifecycle()
    var reassigning by remember { mutableStateOf<Booking?>(null) }
    CollectMessages(vm.messages, snackbar)

    reassigning?.let { b ->
        AlertDialog(
            onDismissRequest = { reassigning = null },
            title = { Text("Reassign stylist") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text("${b.serviceTitle} · ${b.dateFormatted} ${b.time}", color = Brand.TextDim)
                    Spacer(Modifier.height(10.dp))
                    stylists.filter { it.id != b.stylistId }.forEach { st ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            InitialsAvatar(st.displayName, size = 36.dp)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(st.displayName, fontWeight = FontWeight.SemiBold)
                                Text(st.title.ifBlank { "Stylist" }, style = MaterialTheme.typography.bodySmall, color = Brand.TextDim)
                            }
                            TextButton(onClick = { vm.reassign(b, st); reassigning = null }) { Text("Choose", color = Brand.OrangeLight) }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { reassigning = null }) { Text("Close", color = Brand.TextDim) } },
            containerColor = Brand.BgElevated,
        )
    }

    PullToRefreshBox(isRefreshing = refreshing, onRefresh = vm::refresh, modifier = Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader("Manager", "All Bookings")
                    StatusFilterRow(filter, counts, vm::setFilter)
                }
            }
            val bookings = list
            when {
                bookings == null -> item { LoadingBox(Modifier.padding(top = 60.dp)) }
                bookings.isEmpty() -> item { EmptyState("No bookings", "Nothing matches this filter.") }
                else -> items(bookings, key = { it.id }) { b ->
                    BookingCard(b, showCustomer = true, actions = {
                        Text(b.stylistName, style = MaterialTheme.typography.bodySmall, color = Brand.TextFaint)
                        CardAction("Reassign", { reassigning = b })
                        StatusMenu(b) { vm.setStatus(b, it) }
                    })
                }
            }
        }
    }
}

/** "Status ▾" menu: a manager may set any status directly. */
@Composable
private fun StatusMenu(b: Booking, onPick: (BookingStatus) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        CardAction("Status ▾", { open = true })
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            BookingStatus.entries.filter { it != b.status }.forEach { s ->
                DropdownMenuItem(
                    text = { Text(s.dbValue.replaceFirstChar { it.uppercase() }) },
                    onClick = { open = false; onPick(s) },
                )
            }
        }
    }
}

/* ------------------------------ Staff (FR-09) ------------------------------ */

@Composable
fun StaffTab(vm: StaffViewModel, snackbar: SnackbarHostState) {
    val people by vm.people.collectAsStateWithLifecycle()
    var promoting by remember { mutableStateOf<User?>(null) }
    var title by remember { mutableStateOf("") }
    CollectMessages(vm.messages, snackbar)

    promoting?.let { u ->
        AlertDialog(
            onDismissRequest = { promoting = null },
            title = { Text("Make ${u.displayName} a stylist") },
            text = {
                Column {
                    Text("They'll appear in the booking form and can manage their own schedule.", color = Brand.TextDim)
                    Spacer(Modifier.height(10.dp))
                    AppTextField(title, { title = it }, "Job title", placeholder = "e.g. Senior Braider")
                }
            },
            confirmButton = {
                TextButton(onClick = { vm.makeStylist(u, title); promoting = null; title = "" }) { Text("Promote", color = Brand.OrangeLight) }
            },
            dismissButton = { TextButton(onClick = { promoting = null }) { Text("Cancel", color = Brand.TextDim) } },
            containerColor = Brand.BgElevated,
        )
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SectionHeader("Manager", "Staff & Accounts") }
        val list = people
        when {
            list == null -> item { LoadingBox(Modifier.padding(top = 60.dp)) }
            list.isEmpty() -> item { EmptyState("No accounts", "Accounts appear here once people sign up.") }
            else -> items(list, key = { it.id }) { u ->
                GlassCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InitialsAvatar(u.displayName.ifBlank { u.email }, size = 42.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(u.displayName.ifBlank { u.email }, fontWeight = FontWeight.SemiBold)
                            Text(u.email, style = MaterialTheme.typography.bodySmall, color = Brand.TextDim)
                            Text(
                                when (u.role) {
                                    Role.MANAGER -> "Manager"
                                    Role.EMPLOYEE -> "Stylist" + if (u.title.isNotBlank()) " · ${u.title}" else ""
                                    Role.CUSTOMER -> "Customer"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (u.role == Role.CUSTOMER) Brand.TextFaint else Brand.OrangeLight,
                            )
                        }
                        when (u.role) {
                            Role.CUSTOMER -> CardAction("Make stylist", { promoting = u })
                            Role.EMPLOYEE -> CardAction("Make customer", { vm.makeCustomer(u) }, danger = true)
                            Role.MANAGER -> {}
                        }
                    }
                }
            }
        }
    }
}

/* ------------------------------ Catalogue (FR-03) ------------------------------ */

@Composable
fun CatalogTab(vm: CatalogViewModel, snackbar: SnackbarHostState) {
    val categories by vm.categories.collectAsStateWithLifecycle()
    val services by vm.services.collectAsStateWithLifecycle()
    val saving by vm.saving.collectAsStateWithLifecycle()
    var editingService by remember { mutableStateOf<Service?>(null) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var confirmDelete by remember { mutableStateOf<Any?>(null) }
    CollectMessages(vm.messages, snackbar)

    editingService?.let { s ->
        ServiceDialog(s, categories, saving, onDismiss = { editingService = null }) { updated ->
            vm.saveService(updated) { ok -> if (ok) editingService = null }
        }
    }
    editingCategory?.let { c ->
        CategoryDialog(c, saving, onDismiss = { editingCategory = null }) { updated ->
            vm.saveCategory(updated) { ok -> if (ok) editingCategory = null }
        }
    }
    confirmDelete?.let { item ->
        val name = when (item) { is Service -> item.title; is Category -> item.label; else -> "" }
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("Delete “$name”?") },
            text = {
                Text(
                    if (item is Service) "If this service has bookings it will be hidden instead, so booking history stays intact."
                    else "Only empty categories can be deleted."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    when (item) { is Service -> vm.deleteService(item); is Category -> vm.deleteCategory(item) }
                    confirmDelete = null
                }) { Text("Delete", color = Brand.Danger) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text("Cancel", color = Brand.TextDim) } },
            containerColor = Brand.BgElevated,
        )
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader("Manager", "Services & Prices")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GradientButton("+ Service", {
                        editingService = Service(id = "", title = "", categoryId = categories.firstOrNull()?.id ?: "", price = 0.0)
                    }, Modifier.weight(1f))
                    GradientButton("+ Category", { editingCategory = Category(id = "", label = "") }, Modifier.weight(1f))
                }
            }
        }
        val all = services
        if (all == null) {
            item { LoadingBox(Modifier.padding(top = 60.dp)) }
        } else {
            categories.forEach { cat ->
                item(key = "cat-${cat.id}") {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
                        Text(cat.label, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        IconButton(onClick = { editingCategory = cat }) { Icon(Icons.Default.Edit, "Edit category", tint = Brand.TextDim) }
                        IconButton(onClick = { confirmDelete = cat }) { Icon(Icons.Default.Delete, "Delete category", tint = Brand.TextDim) }
                    }
                }
                val inCat = all.filter { it.categoryId == cat.id }
                if (inCat.isEmpty()) {
                    item(key = "empty-${cat.id}") { Text("No services in this category.", color = Brand.TextFaint) }
                }
                items(inCat, key = { it.id }) { s ->
                    GlassCard(Modifier.fillMaxWidth(), contentPadding = 12.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(s.title, fontWeight = FontWeight.SemiBold)
                                Text(
                                    formatZar(s.price) + if (s.discount > 0) "  ·  -${s.discount.toInt()}% → ${formatZar(s.discountedPrice)}" else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Brand.OrangeLight,
                                )
                            }
                            IconButton(onClick = { editingService = s }) { Icon(Icons.Default.Edit, "Edit service", tint = Brand.TextDim) }
                            IconButton(onClick = { confirmDelete = s }) { Icon(Icons.Default.Delete, "Delete service", tint = Brand.TextDim) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceDialog(
    initial: Service,
    categories: List<Category>,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (Service) -> Unit,
) {
    var title by remember { mutableStateOf(initial.title) }
    var categoryId by remember { mutableStateOf(initial.categoryId) }
    var price by remember { mutableStateOf(if (initial.price > 0) initial.price.toString() else "") }
    var discount by remember { mutableStateOf(initial.discount.toInt().toString()) }
    var description by remember { mutableStateOf(initial.description) }
    var tags by remember { mutableStateOf(initial.tags.joinToString(", ")) }
    var active by remember { mutableStateOf(initial.isActive) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.id.isBlank()) "New service" else "Edit service") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(title, { title = it }, "Title")
                Text("Category", style = MaterialTheme.typography.labelMedium, color = Brand.TextDim)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { c -> SelectBox(c.label, c.id == categoryId, { categoryId = c.id }) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTextField(price, { price = it }, "Price (R)", Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
                    AppTextField(discount, { discount = it }, "Discount %", Modifier.weight(1f), keyboardType = KeyboardType.Number)
                }
                AppTextField(description, { description = it }, "Description", singleLine = false, minLines = 2)
                AppTextField(tags, { tags = it }, "Tags (comma separated)")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Visible to customers", Modifier.weight(1f))
                    Switch(active, { active = it }, colors = SwitchDefaults.colors(checkedTrackColor = Brand.Orange))
                }
                error?.let { Text(it, color = Brand.Danger) }
            }
        },
        confirmButton = {
            TextButton(enabled = !saving, onClick = {
                val p = price.replace(",", ".").toDoubleOrNull()
                val d = discount.ifBlank { "0" }.toDoubleOrNull()
                error = when {
                    title.isBlank() -> "Enter a title."
                    categoryId.isBlank() -> "Choose a category."
                    p == null || p <= 0 -> "Enter a price above R0."
                    d == null || d < 0 || d > 100 -> "Discount must be 0–100."
                    else -> null
                }
                if (error == null) {
                    onSave(
                        initial.copy(
                            title = title.trim(), categoryId = categoryId, price = p!!, discount = d!!,
                            description = description.trim(),
                            tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                            isActive = active,
                        )
                    )
                }
            }) { Text(if (saving) "Saving…" else "Save", color = Brand.OrangeLight) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Brand.TextDim) } },
        containerColor = Brand.BgElevated,
    )
}

@Composable
private fun CategoryDialog(initial: Category, saving: Boolean, onDismiss: () -> Unit, onSave: (Category) -> Unit) {
    var label by remember { mutableStateOf(initial.label) }
    var description by remember { mutableStateOf(initial.description) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.id.isBlank()) "New category" else "Edit category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(label, { label = it }, "Name")
                AppTextField(description, { description = it }, "Short description")
                error?.let { Text(it, color = Brand.Danger) }
            }
        },
        confirmButton = {
            TextButton(enabled = !saving, onClick = {
                error = if (label.isBlank()) "Enter a name." else null
                if (error == null) onSave(initial.copy(label = label.trim(), description = description.trim()))
            }) { Text(if (saving) "Saving…" else "Save", color = Brand.OrangeLight) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Brand.TextDim) } },
        containerColor = Brand.BgElevated,
    )
}
