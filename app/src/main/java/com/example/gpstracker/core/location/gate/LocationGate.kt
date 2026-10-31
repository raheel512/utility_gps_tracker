package com.example.gpstracker.core.location.gate

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gpstracker.core.common.ObserveAsEvents
import com.example.gpstracker.core.designsystem.component.GpsDisabledDialog
import com.example.gpstracker.core.designsystem.component.LocationPermissionRationale
import com.example.gpstracker.core.designsystem.component.NoInternetDialog
import com.example.gpstracker.core.location.toIntentSenderRequestOrNull
import com.example.gpstracker.core.permissions.currentLocationPermissionState
import com.example.gpstracker.core.permissions.openApplicationDetailsSettings
import com.example.gpstracker.core.permissions.rememberLocationPermissionLauncher

/**
 * Wraps a location-dependent screen. Resolves network → permission → GPS → Ready.
 *
 * @param requiresNetwork when true, shows [NoInternetDialog] before permission/GPS.
 * @param softMode when true, [content] always shows; dialogs overlay; "Not Now" only
 *                 dismisses prompts for this session (GPS Camera). Flagged for design review.
 * @param onDismiss blocking mode: leave the previous screen. Soft mode: unused for Not Now.
 */
@Composable
fun LocationGate(
    requiresNetwork: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    softMode: Boolean = false,
    viewModel: LocationGateViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(requiresNetwork, softMode) {
        viewModel.onEvent(LocationGateEvent.Configure(requiresNetwork, softMode))
    }

    fun refreshPermission() {
        viewModel.onEvent(
            LocationGateEvent.PermissionStateChanged(
                context.currentLocationPermissionState(uiState.hasAskedPermission),
            ),
        )
    }

    DisposableEffect(lifecycleOwner, uiState.hasAskedPermission) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        refreshPermission()
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val requestPermission = rememberLocationPermissionLauncher(
        onMarkAsked = { viewModel.markPermissionAsked() },
        onResult = { state ->
            viewModel.onEvent(LocationGateEvent.PermissionStateChanged(state))
        },
    )

    val gpsResolutionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) {
        refreshPermission()
    }

    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            LocationGateEffect.RequestPermission -> requestPermission()
            LocationGateEffect.OpenAppSettings -> context.openApplicationDetailsSettings()
            is LocationGateEffect.ResolveGpsSettings -> {
                effect.resolution.toIntentSenderRequestOrNull()?.let { request ->
                    gpsResolutionLauncher.launch(request)
                }
            }
            LocationGateEffect.OpenWirelessSettings -> {
                context.startActivity(
                    Intent(Settings.ACTION_WIRELESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
            LocationGateEffect.Dismiss -> onDismiss()
        }
    }

    val showBlockingContent = !softMode
    val showOverlayDialog = softMode && !uiState.softSkipped &&
        uiState.phase != LocationGatePhase.Ready &&
        uiState.phase != LocationGatePhase.Checking

    Box(modifier = modifier.fillMaxSize()) {
        if (softMode) {
            content()
            if (showOverlayDialog) {
                GateDialogs(phase = uiState.phase, viewModel = viewModel)
            }
        } else if (showBlockingContent) {
            when (uiState.phase) {
                LocationGatePhase.Checking -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                LocationGatePhase.NeedsNetwork,
                LocationGatePhase.NeedsPermission,
                LocationGatePhase.PermanentlyDenied,
                LocationGatePhase.GpsOff,
                -> GateDialogs(phase = uiState.phase, viewModel = viewModel)
                LocationGatePhase.Ready -> content()
            }
        }
    }
}

@Composable
private fun GateDialogs(
    phase: LocationGatePhase,
    viewModel: LocationGateViewModel,
) {
    when (phase) {
        LocationGatePhase.NeedsNetwork -> {
            NoInternetDialog(
                onConnect = { viewModel.onEvent(LocationGateEvent.ConnectInternetClicked) },
                onDismiss = { viewModel.onEvent(LocationGateEvent.DismissClicked) },
            )
        }
        LocationGatePhase.NeedsPermission -> {
            LocationPermissionRationale(
                permanentlyDenied = false,
                onAllowOrOpenSettings = {
                    viewModel.onEvent(LocationGateEvent.AllowPermissionClicked)
                },
                onDismiss = { viewModel.onEvent(LocationGateEvent.DismissClicked) },
            )
        }
        LocationGatePhase.PermanentlyDenied -> {
            LocationPermissionRationale(
                permanentlyDenied = true,
                onAllowOrOpenSettings = {
                    viewModel.onEvent(LocationGateEvent.OpenSettingsClicked)
                },
                onDismiss = { viewModel.onEvent(LocationGateEvent.DismissClicked) },
            )
        }
        LocationGatePhase.GpsOff -> {
            GpsDisabledDialog(
                onEnableGps = { viewModel.onEvent(LocationGateEvent.EnableGpsClicked) },
                onCancel = { viewModel.onEvent(LocationGateEvent.DismissClicked) },
            )
        }
        LocationGatePhase.Checking,
        LocationGatePhase.Ready,
        -> Unit
    }
}
