package com.example.gpstracker.feature.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpstracker.core.common.EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConsentViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ConsentUiState())
    val uiState: StateFlow<ConsentUiState> = _uiState.asStateFlow()

    private val effectBus = EventBus<ConsentEffect>()
    val effects = effectBus.events

    fun onEvent(event: ConsentEvent) {
        when (event) {
            ConsentEvent.ToggleTerms -> {
                _uiState.update { it.copy(termsAccepted = !it.termsAccepted) }
            }
            ConsentEvent.Continue -> {
                if (_uiState.value.canContinue) {
                    viewModelScope.launch { effectBus.emit(ConsentEffect.NavigateNext) }
                }
            }
            ConsentEvent.OpenTerms -> {
                viewModelScope.launch { effectBus.emit(ConsentEffect.OpenTermsUrl) }
            }
            ConsentEvent.OpenPrivacy -> {
                viewModelScope.launch { effectBus.emit(ConsentEffect.OpenPrivacyUrl) }
            }
        }
    }
}
