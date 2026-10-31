package com.example.gpstracker.feature.history.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gpstracker.core.common.ObserveAsEvents
import com.example.gpstracker.core.navigation.Route
import com.example.gpstracker.core.navigation.routePreviewOf

@Composable
fun HistoryRoute(
    onOpenRoutePreview: (Route.RoutePreview) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val menuState by viewModel.menuState.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            is HistoryEffect.OpenRoutePreview -> onOpenRoutePreview(
                routePreviewOf(
                    destName = effect.destName,
                    destLatitude = effect.destLatitude,
                    destLongitude = effect.destLongitude,
                    destPlaceId = effect.destPlaceId.ifBlank { null },
                    destAddress = effect.destAddress.ifBlank { null },
                    viaName = effect.viaName.ifBlank { null },
                    viaPlaceId = effect.viaPlaceId.ifBlank { null },
                    viaLatitude = effect.viaLatitude,
                    viaLongitude = effect.viaLongitude,
                ),
            )
        }
    }

    HistoryScreen(
        uiState = uiState,
        menuState = menuState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}
