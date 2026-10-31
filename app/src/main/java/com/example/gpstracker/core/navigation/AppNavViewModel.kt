package com.example.gpstracker.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpstracker.core.common.DispatcherProvider
import com.example.gpstracker.core.common.EventBus
import com.example.gpstracker.core.data.SettingsRepository
import com.example.gpstracker.core.data.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface AppNavEvent {
    data object GoLanguage : AppNavEvent
}

/**
 * null = still loading settings (system splash stays up).
 * Non-null = first Compose destination after splash.
 */
sealed interface StartDestination {
    data object Home : StartDestination
    data object Consent : StartDestination
}

@HiltViewModel
class AppNavViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val eventBus = EventBus<AppNavEvent>()
    val events = eventBus.events

    private val _startDestination = MutableStateFlow<StartDestination?>(null)
    val startDestination: StateFlow<StartDestination?> = _startDestination.asStateFlow()

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeMode.Auto,
        )

    val isSettingsReady: Boolean
        get() = _startDestination.value != null

    init {
        resolveStartDestination()
    }

    fun resolveStartDestination() {
        viewModelScope.launch {
            val completed = withContext(dispatchers.io) {
                settingsRepository.onboardingCompleted.first()
            }
            _startDestination.value = if (completed) {
                StartDestination.Home
            } else {
                StartDestination.Consent
            }
        }
    }

    fun onConsentContinue() {
        viewModelScope.launch { eventBus.emit(AppNavEvent.GoLanguage) }
    }
}
