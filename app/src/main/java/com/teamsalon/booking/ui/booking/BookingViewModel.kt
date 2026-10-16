package com.teamsalon.booking.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teamsalon.booking.data.BookingRepository
import com.teamsalon.booking.util.BookingValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BookingUiState(
    val date: String = "",
    val time: String = "",
    val isSubmitting: Boolean = false,
    val submitted: Boolean = false,
    val errorMessage: String? = null
)

class BookingViewModel(
    private val repository: BookingRepository,
    private val userId: Int,
    private val serviceId: Int
) : ViewModel() {
    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    fun onDateChange(v: String) { _uiState.value = _uiState.value.copy(date = v, errorMessage = null) }
    fun onTimeChange(v: String) { _uiState.value = _uiState.value.copy(time = v, errorMessage = null) }

    fun submit() {
        val state = _uiState.value
        if (!BookingValidation.isValidDate(state.date.trim())) {
            _uiState.value = state.copy(errorMessage = "Enter the date as yyyy-MM-dd.")
            return
        }
        if (!BookingValidation.isValidTime(state.time.trim())) {
            _uiState.value = state.copy(errorMessage = "Enter the time as HH:mm.")
            return
        }
        _uiState.value = state.copy(isSubmitting = true, errorMessage = null)
        viewModelScope.launch {
            repository.createBooking(userId, serviceId, state.date.trim(), state.time.trim())
            _uiState.value = _uiState.value.copy(isSubmitting = false, submitted = true)
        }
    }
}

class BookingViewModelFactory(
    private val repository: BookingRepository,
    private val userId: Int,
    private val serviceId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        BookingViewModel(repository, userId, serviceId) as T
}
