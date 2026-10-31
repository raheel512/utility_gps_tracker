package com.example.gpstracker.feature.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpstracker.core.common.DispatcherProvider
import com.example.gpstracker.core.common.EventBus
import com.example.gpstracker.core.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LanguageUiState())
    val uiState: StateFlow<LanguageUiState> = _uiState.asStateFlow()

    private val effectBus = EventBus<LanguageEffect>()
    val effects = effectBus.events

    init {
        viewModelScope.launch {
            val saved = withContext(dispatchers.io) {
                settingsRepository.selectedLanguageCode.first()
            }
            _uiState.update { it.copy(selectedCode = saved) }
        }
    }

    fun onEvent(event: LanguageEvent) {
        when (event) {
            is LanguageEvent.Select -> {
                _uiState.update { it.copy(selectedCode = event.code) }
            }
            LanguageEvent.Continue -> persistAndEmit(LanguageEffect.NavigateNext)
            LanguageEvent.NavigateUp -> persistAndEmit(LanguageEffect.NavigateBack)
        }
    }

    private fun persistAndEmit(effect: LanguageEffect) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            withContext(dispatchers.io) {
                settingsRepository.setSelectedLanguageCode(_uiState.value.selectedCode)
            }
            _uiState.update { it.copy(isSaving = false) }
            effectBus.emit(effect)
        }
    }
}
