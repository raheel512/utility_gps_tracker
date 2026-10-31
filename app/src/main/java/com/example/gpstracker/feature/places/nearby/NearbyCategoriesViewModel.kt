package com.example.gpstracker.feature.places.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpstracker.core.common.EventBus
import com.example.gpstracker.core.places.NearbyCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearbyCategoriesViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        NearbyCategoriesUiState(categories = NearbyCategory.seeMoreOrder),
    )
    val uiState: StateFlow<NearbyCategoriesUiState> = _uiState.asStateFlow()

    private val effectBus = EventBus<NearbyCategoriesEffect>()
    val effects: Flow<NearbyCategoriesEffect> = effectBus.events

    fun onEvent(event: NearbyCategoriesEvent) {
        when (event) {
            is NearbyCategoriesEvent.CategoryClicked -> {
                viewModelScope.launch {
                    effectBus.emit(NearbyCategoriesEffect.OpenCategory(event.category.id))
                }
            }
        }
    }
}
