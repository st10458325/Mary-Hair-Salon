package com.teamsalon.booking.ui.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teamsalon.booking.data.BookingRepository
import com.teamsalon.booking.data.ServiceEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ServicesViewModel(repository: BookingRepository) : ViewModel() {
    val services: StateFlow<List<ServiceEntity>> = repository.observeServices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

class ServicesViewModelFactory(private val repository: BookingRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ServicesViewModel(repository) as T
}
