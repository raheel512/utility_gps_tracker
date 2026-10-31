package com.example.gpstracker.feature.onboarding.ui

data class ConsentUiState(
    val termsAccepted: Boolean = false,
) {
    val canContinue: Boolean get() = termsAccepted
}

sealed interface ConsentEvent {
    data object ToggleTerms : ConsentEvent
    data object Continue : ConsentEvent
    data object OpenTerms : ConsentEvent
    data object OpenPrivacy : ConsentEvent
}

sealed interface ConsentEffect {
    data object NavigateNext : ConsentEffect
    data object OpenTermsUrl : ConsentEffect
    data object OpenPrivacyUrl : ConsentEffect
}
