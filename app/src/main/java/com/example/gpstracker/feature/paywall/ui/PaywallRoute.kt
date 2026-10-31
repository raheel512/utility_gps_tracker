package com.example.gpstracker.feature.paywall.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gpstracker.R
import com.example.gpstracker.core.common.ObserveAsEvents

@Composable
fun PaywallRoute(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaywallViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val termsUrl = stringResource(R.string.consent_terms_url)
    val privacyUrl = stringResource(R.string.consent_privacy_url)

    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            PaywallEffect.NavigateUp -> onNavigateUp()
            PaywallEffect.OpenTermsUrl -> uriHandler.openUri(termsUrl)
            PaywallEffect.OpenPrivacyUrl -> uriHandler.openUri(privacyUrl)
        }
    }

    PaywallScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}
