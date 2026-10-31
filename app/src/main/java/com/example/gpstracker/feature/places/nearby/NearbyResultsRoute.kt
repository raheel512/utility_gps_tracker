package com.example.gpstracker.feature.places.nearby

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gpstracker.core.common.ObserveAsEvents
import com.example.gpstracker.core.designsystem.component.NoInternetDialog
import com.example.gpstracker.core.location.gate.LocationGate
import com.example.gpstracker.core.navigation.Route
import com.example.gpstracker.core.navigation.routePreviewOf

@Composable
fun NearbyResultsRoute(
    onNavigateUp: () -> Unit,
    onOpenRoutePreview: (Route.RoutePreview) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: NearbyResultsViewModel = hiltViewModel(),
) {
    LocationGate(requiresNetwork = true, onDismiss = onNavigateUp, modifier = modifier) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val snackbarHostState = remember { SnackbarHostState() }
        val errorText = uiState.errorMessageRes?.let { stringResource(it) }

        LaunchedEffect(errorText) {
            if (errorText != null) {
                snackbarHostState.showSnackbar(errorText)
                viewModel.onEvent(NearbyResultsEvent.DismissError)
            }
        }

        ObserveAsEvents(viewModel.effects) { effect ->
            when (effect) {
                NearbyResultsEffect.OpenWirelessSettings -> {
                    context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                }
                is NearbyResultsEffect.OpenRoutePreview -> onOpenRoutePreview(
                    routePreviewOf(
                        destName = effect.destName,
                        destLatitude = effect.destLatitude,
                        destLongitude = effect.destLongitude,
                        destAddress = effect.destAddress.ifBlank { null },
                        destPlaceId = effect.destPlaceId.ifBlank { null },
                    ),
                )
            }
        }

        Box {
            NearbyResultsScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onNavigateUp = onNavigateUp,
            )
            SnackbarHost(hostState = snackbarHostState)
            if (uiState.showNoInternetDialog) {
                NoInternetDialog(
                    onConnect = { viewModel.onEvent(NearbyResultsEvent.ConnectInternet) },
                    onDismiss = { viewModel.onEvent(NearbyResultsEvent.DismissNoInternet) },
                )
            }
        }
    }
}
