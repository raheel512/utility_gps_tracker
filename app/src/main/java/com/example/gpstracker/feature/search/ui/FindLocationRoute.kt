package com.example.gpstracker.feature.search.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gpstracker.core.common.ObserveAsEvents
import com.example.gpstracker.core.designsystem.component.NoInternetDialog
import com.example.gpstracker.core.navigation.Route
import com.example.gpstracker.core.navigation.routePreviewOf
import com.example.gpstracker.feature.voice.ui.VoiceSession

@Composable
fun FindLocationRoute(
    onNavigateUp: () -> Unit,
    onOpenRoutePreview: (Route.RoutePreview) -> Unit = {},
    onPickStop: () -> Unit = {},
    savedStateHandle: SavedStateHandle? = null,
    modifier: Modifier = Modifier,
    viewModel: FindLocationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val errorText = uiState.errorMessageRes?.let { stringResource(it) }
    var showVoice by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(errorText) {
        if (errorText != null) {
            snackbarHostState.showSnackbar(errorText)
            viewModel.onEvent(FindLocationEvent.DismissError)
        }
    }

    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            FindLocationEffect.OpenWirelessSettings -> {
                context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
            }
            is FindLocationEffect.OpenRoutePreview -> onOpenRoutePreview(
                routePreviewOf(
                    destName = effect.destName,
                    destLatitude = effect.destLatitude,
                    destLongitude = effect.destLongitude,
                    destAddress = effect.destAddress.ifBlank { null },
                    destPlaceId = effect.destPlaceId.ifBlank { null },
                ),
            )
            FindLocationEffect.OpenVoice -> showVoice = true
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        FindLocationScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onNavigateUp = onNavigateUp,
        )
        SnackbarHost(hostState = snackbarHostState)

        if (uiState.showNoInternetDialog) {
            NoInternetDialog(
                onConnect = { viewModel.onEvent(FindLocationEvent.ConnectInternet) },
                onDismiss = { viewModel.onEvent(FindLocationEvent.DismissNoInternet) },
            )
        }
        if (showVoice && savedStateHandle != null) {
            VoiceSession(
                onNavigateUp = { showVoice = false },
                onOpenRoutePreview = { preview ->
                    showVoice = false
                    onOpenRoutePreview(preview)
                },
                onPickStop = onPickStop,
                savedStateHandle = savedStateHandle,
                floating = true,
                onFillDestination = { fill ->
                    showVoice = false
                    val latitude = fill.latitude
                    val longitude = fill.longitude
                    if (latitude != null && longitude != null && fill.placeId.isNotBlank()) {
                        viewModel.onEvent(
                            FindLocationEvent.VoicePlaceChosen(
                                name = fill.label,
                                placeId = fill.placeId,
                                address = fill.address,
                                latitude = latitude,
                                longitude = longitude,
                            ),
                        )
                    } else if (fill.label.isNotBlank()) {
                        viewModel.onEvent(FindLocationEvent.DestinationQueryChanged(fill.label))
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
