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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val effectBus = EventBus<OnboardingEffect>()
    val effects = effectBus.events

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.PageChanged -> {
                _uiState.update { it.copy(currentPage = event.index.coerceIn(0, it.slides.lastIndex)) }
            }
            OnboardingEvent.Next -> {
                val state = _uiState.value
                if (state.isLastPage) {
                    completeOnboarding()
                } else {
                    _uiState.update { it.copy(currentPage = it.currentPage + 1) }
                }
            }
            OnboardingEvent.Skip -> completeOnboarding()
            OnboardingEvent.Back -> {
                val page = _uiState.value.currentPage
                if (page > 0) {
                    _uiState.update { it.copy(currentPage = page - 1) }
                } else {
                    viewModelScope.launch { effectBus.emit(OnboardingEffect.NavigateBack) }
                }
            }
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCompleting = true) }
            withContext(dispatchers.io) {
                settingsRepository.setOnboardingCompleted(true)
            }
            effectBus.emit(OnboardingEffect.Finished)
            _uiState.update { it.copy(isCompleting = false) }
        }
    }
}
