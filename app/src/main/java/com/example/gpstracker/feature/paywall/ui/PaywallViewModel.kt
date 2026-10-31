package com.example.gpstracker.feature.paywall.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpstracker.core.common.DispatcherProvider
import com.example.gpstracker.core.common.EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    @Suppress("unused") // reserved for TODO(billing) IO work
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaywallUiState())
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    private val effectBus = EventBus<PaywallEffect>()
    val effects = effectBus.events

    fun onEvent(event: PaywallEvent) {
        when (event) {
            PaywallEvent.Close -> {
                viewModelScope.launch { effectBus.emit(PaywallEffect.NavigateUp) }
            }
            PaywallEvent.PurchaseClicked -> {
                // TODO(billing): start Play Billing purchase / trial flow
            }
            PaywallEvent.RestorePurchases -> {
                // TODO(billing): restore purchases via Play Billing
            }
            PaywallEvent.OpenTerms -> {
                viewModelScope.launch { effectBus.emit(PaywallEffect.OpenTermsUrl) }
            }
            PaywallEvent.OpenPrivacy -> {
                viewModelScope.launch { effectBus.emit(PaywallEffect.OpenPrivacyUrl) }
            }
        }
    }
}
