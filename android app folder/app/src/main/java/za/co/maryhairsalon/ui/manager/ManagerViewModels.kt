package za.co.maryhairsalon.ui.manager

import androidx.lifecycle.ViewModel
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
import za.co.maryhairsalon.data.model.Category
import za.co.maryhairsalon.data.model.Role
import za.co.maryhairsalon.data.model.Service
import za.co.maryhairsalon.data.model.User
import za.co.maryhairsalon.data.repository.BookingRepository
import za.co.maryhairsalon.data.repository.CatalogRepository
import za.co.maryhairsalon.data.repository.UserRepository
import za.co.maryhairsalon.ui.UiMessages

/** Manager: every booking, filter, change status, reassign stylist (spec FR-08). */
class ManagerBookingsViewModel(
    private val bookings: BookingRepository,
    users: UserRepository,
) : ViewModel() {
    val messages = UiMessages()

    private val _filter = MutableStateFlow<BookingStatus?>(null)
    val filter: StateFlow<BookingStatus?> = _filter.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val all = bookings.observeAllBookings()

    val visible: StateFlow<List<Booking>?> = combine(all, _filter) { list, f ->
        list.filter { f == null || it.status == f }.sortedBy { it.status.ordinal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val counts: StateFlow<Map<BookingStatus, Int>> = all
        .map { list -> list.groupingBy { it.status }.eachCount() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val stylists: StateFlow<List<User>> = users.observeStylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setFilter(s: BookingStatus?) {
        _filter.value = s
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            bookings.refreshAll().onFailure { messages.send(it.message ?: "Couldn't refresh.") }
            _refreshing.value = false
        }
    }

    fun setStatus(b: Booking, status: BookingStatus) {
        viewModelScope.launch {
            bookings.changeStatus(b.id, status)
                .onSuccess { messages.send("Status changed to ${status.dbValue}.") }
                .onFailure { messages.send(it.message ?: "Couldn't change the status.") }
        }
    }

    fun reassign(b: Booking, stylist: User) {
        viewModelScope.launch {
            bookings.reassignStylist(b.id, stylist.id)
                .onSuccess { messages.send("Reassigned to ${stylist.displayName}.") }
                .onFailure { messages.send(it.message ?: "Couldn't reassign this booking.") }
        }
    }
}

/** Manager: promote customers to stylists or revert them (spec FR-09). */
class StaffViewModel(private val users: UserRepository) : ViewModel() {
    val messages = UiMessages()

    val people: StateFlow<List<User>?> = users.observeAllUsers()
        .map<List<User>, List<User>?> { list -> list.sortedWith(compareBy({ it.role.ordinal * -1 }, { it.displayName })) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun makeStylist(u: User, title: String) = setRole(u, Role.EMPLOYEE, title, "${u.displayName} is now a stylist.")
    fun makeCustomer(u: User) = setRole(u, Role.CUSTOMER, "", "${u.displayName} is now a customer.")

    private fun setRole(u: User, role: Role, title: String, success: String) {
        viewModelScope.launch {
            users.setRole(u.id, role, title)
                .onSuccess { messages.send(success) }
                .onFailure { messages.send(it.message ?: "Couldn't change the role.") }
        }
    }
}

/** Manager: create, edit and delete categories and services (spec FR-03). */
class CatalogViewModel(private val catalog: CatalogRepository) : ViewModel() {
    val messages = UiMessages()

    val categories: StateFlow<List<Category>> = catalog.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val services: StateFlow<List<Service>?> = catalog.observeServices()
        .map<List<Service>, List<Service>?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    /** Returns true through [onDone] when saved, so the dialog can close. */
    fun saveService(s: Service, onDone: (Boolean) -> Unit) = perform(onDone, "Service saved.") { catalog.saveService(s) }
    fun deleteService(s: Service) = perform({}, "“${s.title}” removed.") { catalog.deleteService(s.id) }
    fun saveCategory(c: Category, onDone: (Boolean) -> Unit) = perform(onDone, "Category saved.") { catalog.saveCategory(c) }
    fun deleteCategory(c: Category) = perform({}, "Category “${c.label}” deleted.") { catalog.deleteCategory(c.id) }

    private fun perform(onDone: (Boolean) -> Unit, success: String, block: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _saving.value = true
            val result = block()
            _saving.value = false
            result.onSuccess { messages.send(success) }.onFailure { messages.send(it.message ?: "Couldn't save.") }
            onDone(result.isSuccess)
        }
    }
}
