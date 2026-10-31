package com.example.gpstracker.feature.weather.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gpstracker.core.location.gate.LocationGate

@Composable
fun WeatherRoute(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LocationGate(requiresNetwork = true, onDismiss = onNavigateUp, modifier = modifier) {
        val viewModel: WeatherViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        WeatherScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onNavigateUp = onNavigateUp,
        )
    }
}
