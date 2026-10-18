package za.co.maryhairsalon.ui.booking

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import za.co.maryhairsalon.data.model.Booking
import za.co.maryhairsalon.data.model.BookingRequest
import za.co.maryhairsalon.data.model.BookingRules
import za.co.maryhairsalon.data.model.BookingType
import za.co.maryhairsalon.data.model.Category
import za.co.maryhairsalon.data.model.Service
import za.co.maryhairsalon.data.model.User
import za.co.maryhairsalon.di.AppContainer
import za.co.maryhairsalon.ui.UiMessages
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

data class BookingForm(
    val name: String = "",
    val surname: String = "",
    val contact: String = "",
    val categoryId: String = "",
    val serviceId: String = "",
    val stylistId: String = "",
    val type: BookingType = BookingType.INCALL,
    val location: String = "",
    val date: LocalDate? = null,
    val time: String = "",
    val notes: String = "",
    val visibleMonth: YearMonth = YearMonth.now(),
    val takenTimes: Set<String> = emptySet(),
    val loadingTimes: Boolean = false,
    val submitting: Boolean = false,
    val errors: List<String> = emptyList(),
    /** Set once the booking is saved, so the screen can close itself. */
    val saved: Boolean = false,
)

/**
 * Booking form (spec FR-05) used for new bookings and reschedules (FR-06).
 * Prices are only displayed here: the database function reads the real price.
 */
class BookingFormViewModel(
    container: AppContainer,
    private val initialServiceId: String?,
    private val bookingId: String?,
) : ViewModel() {

    private val bookings = container.bookingRepository
    val messages = UiMessages()
    val isReschedule: Boolean = bookingId != null

    val categories: StateFlow<List<Category>> = container.catalogRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val services: StateFlow<List<Service>> = container.catalogRepository.observeServices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val stylists: StateFlow<List<User>> = container.userRepository.observeStylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _form = MutableStateFlow(BookingForm())
    val form: StateFlow<BookingForm> = _form.asStateFlow()

    /** Stylist/date/time of the booking being rescheduled — that slot isn't "taken" for this user. */
    private var originalSlot: Triple<String, String, String>? = null
    private var timesJob: Job? = null

    init {
        viewModelScope.launch {
            if (bookingId != null) {
                val booking = bookings.observeBooking(bookingId).filterNotNull().first()
                prefillFrom(booking)
            } else {
                initialServiceId?.let { selectService(it) }
                // Prefill personal details from the logged-in user's profile.
                val me = container.authRepository.observeCurrentUser().filterNotNull().first()
                _form.update { f ->
                    f.copy(
                        name = f.name.ifBlank { me.name },
                        surname = f.surname.ifBlank { me.surname },
                        contact = f.contact.ifBlank { me.contact },
                    )
                }
            }
        }
        // Default to the first stylist once the list has loaded.
        viewModelScope.launch {
            val first = stylists.map { it.firstOrNull() }.filterNotNull().first()
            if (_form.value.stylistId.isBlank()) selectStylist(first.id)
        }
        // Fill in the category once services have loaded (when opened from a service card).
        viewModelScope.launch {
            val list = services.map { it.takeIf { l -> l.isNotEmpty() } }.filterNotNull().first()
            val f = _form.value
            if (f.categoryId.isBlank() && f.serviceId.isNotBlank()) {
                list.firstOrNull { it.id == f.serviceId }?.let { svc -> _form.update { it.copy(categoryId = svc.categoryId) } }
            }
        }
    }

    private fun prefillFrom(b: Booking) {
        originalSlot = Triple(b.stylistId, b.date, b.time)
        val date = runCatching { LocalDate.parse(b.date) }.getOrNull()
        _form.update {
            it.copy(
                name = b.name, surname = b.surname, contact = b.contact,
                categoryId = b.categoryId, serviceId = b.serviceId, stylistId = b.stylistId,
                type = b.type, location = b.location, notes = b.notes,
                date = date, time = b.time,
                visibleMonth = date?.let { d -> YearMonth.from(d) } ?: YearMonth.now(),
            )
        }
        refreshTakenTimes()
    }

    fun onName(v: String) = _form.update { it.copy(name = v) }
    fun onSurname(v: String) = _form.update { it.copy(surname = v) }
    fun onContact(v: String) = _form.update { it.copy(contact = v) }
    fun onLocation(v: String) = _form.update { it.copy(location = v) }
    fun onNotes(v: String) = _form.update { it.copy(notes = v.take(500)) }
    fun onType(t: BookingType) = _form.update { it.copy(type = t) }

    fun selectCategory(id: String) = _form.update {
        if (it.categoryId == id) it else it.copy(categoryId = id, serviceId = "")
    }

    fun selectService(id: String) {
        val svc = services.value.firstOrNull { it.id == id }
        _form.update { it.copy(serviceId = id, categoryId = svc?.categoryId ?: it.categoryId) }
    }

    fun selectStylist(id: String) {
        _form.update { it.copy(stylistId = id) }
        refreshTakenTimes()
    }

    fun selectDate(date: LocalDate) {
        _form.update { it.copy(date = date) }
        refreshTakenTimes()
    }

    fun selectTime(time: String) = _form.update { it.copy(time = time) }

    fun previousMonth() = _form.update {
        val prev = it.visibleMonth.minusMonths(1)
        if (prev.isBefore(YearMonth.now())) it else it.copy(visibleMonth = prev)
    }

    fun nextMonth() = _form.update { it.copy(visibleMonth = it.visibleMonth.plusMonths(1)) }

    /** True if a time can't be picked: already booked, or already passed today. */
    fun isUnavailable(f: BookingForm, time: String): Boolean {
        if (time in f.takenTimes) return true
        val today = LocalDate.now()
        return f.date == today && LocalTime.parse(time) <= LocalTime.now()
    }

    /** Asks the database which times this stylist already has on this date. */
    private fun refreshTakenTimes() {
        val f = _form.value
        val date = f.date ?: return
        if (f.stylistId.isBlank()) return
        timesJob?.cancel()
        timesJob = viewModelScope.launch {
            _form.update { it.copy(loadingTimes = true) }
            val iso = date.toString()
            val result = bookings.takenTimes(f.stylistId, iso)
            var taken = result.getOrDefault(emptyList()).toSet()
            originalSlot?.let { (stylist, d, t) -> if (stylist == f.stylistId && d == iso) taken = taken - t }
            result.onFailure { Log.w(TAG, "Couldn't load busy times", it) }
            _form.update { cur ->
                cur.copy(
                    takenTimes = taken,
                    loadingTimes = false,
                    // Drop the chosen time if it just became unavailable.
                    time = if (cur.time in taken) "" else cur.time,
                )
            }
        }
    }

    fun submit() {
        val f = _form.value
        if (f.submitting) return
        val request = BookingRequest(
            name = f.name.trim(),
            surname = f.surname.trim(),
            contact = f.contact.trim(),
            stylistId = f.stylistId,
            serviceId = f.serviceId,
            type = f.type,
            location = if (f.type == BookingType.OUTCALL) f.location.trim() else "",
            date = f.date?.toString() ?: "",
            time = f.time,
            notes = f.notes.trim(),
        )
        val errors = BookingRules.validate(request)
        if (errors.isNotEmpty()) {
            _form.update { it.copy(errors = errors) }
            messages.send(errors.first())
            return
        }
        _form.update { it.copy(submitting = true, errors = emptyList()) }
        viewModelScope.launch {
            val result = if (bookingId != null) bookings.reschedule(bookingId, request)
            else bookings.createBooking(request).map { }
            result
                .onSuccess {
                    // The screen shows a confirmation dialog and then closes.
                    _form.update { it.copy(submitting = false, saved = true) }
                }
                .onFailure { e ->
                    messages.send(e.message ?: "Couldn't save your booking.")
                    _form.update { it.copy(submitting = false) }
                    refreshTakenTimes() // someone may have just taken the slot
                }
        }
    }

    companion object {
        private const val TAG = "BookingForm"
    }
}
