package com.example.gpstracker.feature.compass.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gpstracker.R
import com.example.gpstracker.core.common.ObserveAsEvents
import com.example.gpstracker.core.location.gate.LocationGate

@Composable
fun CompassRoute(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LocationGate(requiresNetwork = false, onDismiss = onNavigateUp, modifier = modifier) {
        val viewModel: CompassViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val context = LocalContext.current
        ObserveAsEvents(viewModel.effects) { effect ->
            when (effect) {
                CompassEffect.LocationUpdated -> Toast.makeText(
                    context,
                    R.string.compass_location_updated,
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
        CompassScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onNavigateUp = onNavigateUp,
        )
    }
}
