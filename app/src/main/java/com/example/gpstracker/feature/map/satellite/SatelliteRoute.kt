package com.example.gpstracker.feature.map.satellite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gpstracker.core.location.gate.LocationGate

@Composable
fun SatelliteRoute(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SatelliteViewModel = hiltViewModel(),
) {
    // Satellite imagery needs network; the gate shows NoInternetDialog while offline.
    LocationGate(requiresNetwork = true, onDismiss = onNavigateUp, modifier = modifier) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        SatelliteScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onNavigateUp = onNavigateUp,
        )
    }
}
