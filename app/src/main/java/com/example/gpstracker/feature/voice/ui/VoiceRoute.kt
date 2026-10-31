package com.example.gpstracker.feature.voice.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.SavedStateHandle
import com.example.gpstracker.core.common.ObserveAsEvents
import com.example.gpstracker.core.designsystem.component.LocationPermissionRationale
import com.example.gpstracker.core.designsystem.component.NoInternetDialog
import com.example.gpstracker.core.location.gate.LocationGate
import com.example.gpstracker.core.navigation.Route
import com.example.gpstracker.core.navigation.routePreviewOf
import com.example.gpstracker.core.permissions.currentMicrophonePermissionState
import com.example.gpstracker.core.permissions.openApplicationDetailsSettings
import com.example.gpstracker.core.permissions.rememberMicrophonePermissionLauncher
import com.example.gpstracker.R
import com.example.gpstracker.feature.routes.ui.launchMapsDirections

@Composable
fun VoiceRoute(
    onNavigateUp: () -> Unit,
    onOpenRoutePreview: (Route.RoutePreview) -> Unit,
    onPickStop: () -> Unit,
    savedStateHandle: SavedStateHandle,
    modifier: Modifier = Modifier,
    viewModel: VoiceViewModel = hiltViewModel(),
) {
    LocationGate(requiresNetwork = true, onDismiss = onNavigateUp, modifier = modifier) {
        VoiceSession(
            onNavigateUp = onNavigateUp,
            onOpenRoutePreview = onOpenRoutePreview,
            onPickStop = onPickStop,
            savedStateHandle = savedStateHandle,
            floating = false,
            viewModel = viewModel,
        )
    }
}

@Composable
internal fun VoiceSession(
    onNavigateUp: () -> Unit,
    onOpenRoutePreview: (Route.RoutePreview) -> Unit,
    onPickStop: () -> Unit,
    savedStateHandle: SavedStateHandle,
    floating: Boolean,
    onFillDestination: (VoiceEffect.FillDestination) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: VoiceViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = rememberSnackbarHost()
    val snackbarText = uiState.snackbarMessageRes?.let { stringResource(it) }

    val requestMic = rememberMicrophonePermissionLauncher(
        onMarkAsked = {},
        onResult = { viewModel.onEvent(VoiceEvent.PermissionResult(it)) },
    )

    LaunchedEffect(Unit) {
        viewModel.onEvent(
            VoiceEvent.ScreenOpened(
                permission = context.currentMicrophonePermissionState(hasAsked = false),
                returnTextOnly = floating,
            ),
        )
    }

    LaunchedEffect(savedStateHandle) {
        savedStateHandle.getStateFlow<String?>(VIA_NAME, null).collect { name ->
            if (name.isNullOrBlank()) return@collect
            val lat = savedStateHandle.get<Double>(VIA_LAT) ?: return@collect
            val lng = savedStateHandle.get<Double>(VIA_LNG) ?: return@collect
            viewModel.onEvent(
                VoiceEvent.ViaPicked(
                    VoicePlace(
                        name = name,
                        address = savedStateHandle.get<String>(VIA_ADDRESS).orEmpty(),
                        placeId = savedStateHandle.get<String>(VIA_PLACE_ID).orEmpty(),
                        latitude = lat,
                        longitude = lng,
                    ),
                ),
            )
            savedStateHandle[VIA_NAME] = null
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.onEvent(VoiceEvent.AppBackgrounded)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.onEvent(
                if (floating) VoiceEvent.SessionDismissed else VoiceEvent.ScreenLeft,
            )
        }
    }

    LaunchedEffect(snackbarText) {
        if (snackbarText != null) {
            snackbarHostState.showSnackbar(snackbarText)
            viewModel.onEvent(VoiceEvent.DismissSnackbar)
        }
    }

    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            VoiceEffect.NavigateUp -> onNavigateUp()
            VoiceEffect.OpenAppSettings -> context.openApplicationDetailsSettings()
            VoiceEffect.OpenWirelessSettings -> {
                context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
            }
            VoiceEffect.RequestMicPermission -> requestMic()
            VoiceEffect.OpenPickStop -> onPickStop()
            is VoiceEffect.OpenMaps -> {
                val launched = launchMapsDirections(context, effect.url)
                viewModel.onEvent(
                    if (launched) VoiceEvent.StartLaunched else VoiceEvent.StartFailed,
                )
            }
            is VoiceEffect.FillDestination -> onFillDestination(effect)
            is VoiceEffect.OpenRoutePreview -> onOpenRoutePreview(
                routePreviewOf(
                    destName = effect.destName,
                    destLatitude = effect.destLatitude,
                    destLongitude = effect.destLongitude,
                    destAddress = effect.destAddress.ifBlank { null },
                    destPlaceId = effect.destPlaceId.ifBlank { null },
                    viaName = effect.viaName.ifBlank { null },
                    viaPlaceId = effect.viaPlaceId.ifBlank { null },
                    viaLatitude = effect.viaLatitude,
                    viaLongitude = effect.viaLongitude,
                ),
            )
        }
    }

    Box(modifier.fillMaxSize()) {
        VoiceScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            floating = floating,
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
        if (uiState.showMicRationale) {
            LocationPermissionRationale(
                permanentlyDenied = uiState.micPermanentlyDenied,
                onAllowOrOpenSettings = { viewModel.onEvent(VoiceEvent.AllowMicClicked) },
                onDismiss = { viewModel.onEvent(VoiceEvent.DismissMicRationale) },
                titleRes = R.string.voice_mic_permission_title,
                bodyRes = R.string.voice_mic_permission_body,
                allowRes = R.string.voice_mic_permission_allow,
                illustrationContentDescriptionRes = R.string.cd_voice_mic_permission,
            )
        }
        if (uiState.showNoInternetDialog) {
            NoInternetDialog(
                onConnect = { viewModel.onEvent(VoiceEvent.ConnectInternet) },
                onDismiss = { viewModel.onEvent(VoiceEvent.DismissNoInternet) },
            )
        }
    }
}

@Composable
private fun rememberSnackbarHost(): SnackbarHostState =
    androidx.compose.runtime.remember { SnackbarHostState() }

internal const val VIA_NAME = "voice_via_name"
internal const val VIA_PLACE_ID = "voice_via_place_id"
internal const val VIA_ADDRESS = "voice_via_address"
internal const val VIA_LAT = "voice_via_lat"
internal const val VIA_LNG = "voice_via_lng"
