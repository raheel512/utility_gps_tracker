package com.example.gpstracker.feature.routes.ui

import android.content.ActivityNotFoundException
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gpstracker.core.common.ObserveAsEvents
import com.example.gpstracker.core.designsystem.component.NoInternetDialog
import com.example.gpstracker.core.location.gate.LocationGate

@Composable
fun RoutePreviewRoute(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoutePreviewViewModel = hiltViewModel(),
) {
    LocationGate(requiresNetwork = true, onDismiss = onNavigateUp, modifier = modifier) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val snackbarHostState = remember { SnackbarHostState() }
        val errorText = uiState.errorMessageRes?.let { stringResource(it) }
        val snackbarText = uiState.snackbarMessageRes?.let { stringResource(it) }

        DisposableEffect(Unit) {
            onDispose { viewModel.onEvent(RoutePreviewEvent.ScreenStopped) }
        }

        LaunchedEffect(errorText) {
            if (errorText != null) {
                snackbarHostState.showSnackbar(errorText)
                viewModel.onEvent(RoutePreviewEvent.DismissError)
            }
        }
        LaunchedEffect(snackbarText) {
            if (snackbarText != null) {
                snackbarHostState.showSnackbar(snackbarText)
                viewModel.onEvent(RoutePreviewEvent.DismissSnackbar)
            }
        }

        ObserveAsEvents(viewModel.effects) { effect ->
            when (effect) {
                is RoutePreviewEffect.OpenMaps -> {
                    val launched = launchMapsDirections(context, effect.url)
                    viewModel.onEvent(
                        if (launched) RoutePreviewEvent.StartLaunched else RoutePreviewEvent.StartFailed,
                    )
                }
                RoutePreviewEffect.OpenWirelessSettings -> {
                    context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                }
            }
        }

        Box(Modifier.fillMaxSize()) {
            RoutePreviewScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onNavigateUp = onNavigateUp,
            )
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
            if (uiState.showNoInternetDialog) {
                NoInternetDialog(
                    onConnect = { viewModel.onEvent(RoutePreviewEvent.ConnectInternet) },
                    onDismiss = { viewModel.onEvent(RoutePreviewEvent.DismissNoInternet) },
                )
            }
        }
    }
}

internal fun launchMapsDirections(context: android.content.Context, url: String): Boolean {
    val uri = url.toUri()
    return try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
