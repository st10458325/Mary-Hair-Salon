package za.co.maryhairsalon.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import za.co.maryhairsalon.data.model.Category
import za.co.maryhairsalon.data.model.Service
import za.co.maryhairsalon.data.repository.CatalogRepository
import za.co.maryhairsalon.data.repository.CategoryWithCount
import za.co.maryhairsalon.ui.UiMessages

/** Services tab: category grid, keyword search (spec FR-04) and current deals. */
class ServicesViewModel(private val catalog: CatalogRepository) : ViewModel() {

    val messages = UiMessages()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    val categories: StateFlow<List<CategoryWithCount>?> = catalog.observeCategoriesWithCounts()
        .map<List<CategoryWithCount>, List<CategoryWithCount>?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Services that currently have a manager-set discount. */
    val deals: StateFlow<List<Service>> = catalog.observeServices()
        .map { list -> list.filter { it.discount > 0 }.sortedByDescending { it.discount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val results: StateFlow<List<Service>> = _query
        .flatMapLatest { q -> if (q.isBlank()) flowOf(emptyList()) else catalog.search(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQuery(q: String) {
        _query.value = q
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            catalog.refresh().onFailure { messages.send(it.message ?: "Couldn't refresh.") }
            _refreshing.value = false
        }
    }
}

/** One category's services (spec FR-04 "drill into a category"). */
class CategoryViewModel(catalog: CatalogRepository, categoryId: String) : ViewModel() {
    val category: StateFlow<Category?> = catalog.observeCategories()
        .map { list -> list.firstOrNull { it.id == categoryId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val services: StateFlow<List<Service>?> = catalog.observeServicesInCategory(categoryId)
        .map<List<Service>, List<Service>?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
