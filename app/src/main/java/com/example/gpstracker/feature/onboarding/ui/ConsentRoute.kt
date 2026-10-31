package com.example.gpstracker.feature.onboarding.ui

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
fun ConsentRoute(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConsentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val termsUrl = stringResource(R.string.consent_terms_url)
    val privacyUrl = stringResource(R.string.consent_privacy_url)

    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            ConsentEffect.NavigateNext -> onContinue()
            ConsentEffect.OpenTermsUrl -> uriHandler.openUri(termsUrl)
            ConsentEffect.OpenPrivacyUrl -> uriHandler.openUri(privacyUrl)
        }
    }

    ConsentScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}
