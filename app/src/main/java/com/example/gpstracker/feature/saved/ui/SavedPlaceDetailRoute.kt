package com.example.gpstracker.feature.saved.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gpstracker.core.common.ObserveAsEvents
import com.example.gpstracker.core.navigation.Route
import com.example.gpstracker.core.navigation.routePreviewOf

@Composable
fun SavedPlaceDetailRoute(
    onNavigateUp: () -> Unit,
    onOpenRoutePreview: (Route.RoutePreview) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SavedPlaceDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            is SavedPlaceDetailEffect.OpenRoutePreview -> onOpenRoutePreview(
                routePreviewOf(
                    destName = effect.destName,
                    destLatitude = effect.destLatitude,
                    destLongitude = effect.destLongitude,
                    destAddress = effect.destAddress.ifBlank { null },
                    destPlaceId = effect.destPlaceId.ifBlank { null },
                ),
            )
            SavedPlaceDetailEffect.Close -> onNavigateUp()
        }
    }

    SavedPlaceDetailScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateUp = onNavigateUp,
        modifier = modifier,
    )
}
