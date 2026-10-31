package com.example.gpstracker.feature.saved.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gpstracker.R
import com.example.gpstracker.core.common.ObserveAsEvents
import com.example.gpstracker.core.navigation.Route
import com.example.gpstracker.core.navigation.routePreviewOf

@Composable
fun SavedRoute(
    onOpenPlace: (Long) -> Unit,
    onOpenRoutePreview: (Route.RoutePreview) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SavedViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val removedMessage = stringResource(R.string.saved_removed_snackbar)
    val undoLabel = stringResource(R.string.saved_undo)

    LaunchedEffect(uiState.snackbarMessageRes, uiState.pendingUndo) {
        if (uiState.snackbarMessageRes != null && uiState.pendingUndo != null) {
            val result = snackbarHostState.showSnackbar(
                message = removedMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short,
            )
            viewModel.onEvent(SavedEvent.SnackbarShown)
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.onEvent(SavedEvent.UndoUnsave)
            }
        }
    }

    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            is SavedEffect.OpenPlace -> onOpenPlace(effect.placeId)
            is SavedEffect.OpenRoutePreview -> onOpenRoutePreview(
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

    Box(modifier = modifier.fillMaxSize()) {
        SavedScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
