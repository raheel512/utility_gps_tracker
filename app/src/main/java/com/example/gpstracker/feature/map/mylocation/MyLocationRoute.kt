package com.example.gpstracker.feature.map.mylocation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gpstracker.R
import com.example.gpstracker.core.common.ObserveAsEvents
import com.example.gpstracker.core.common.copyPlainText
import com.example.gpstracker.core.common.sharePlainText
import com.example.gpstracker.core.location.gate.LocationGate

@Composable
fun MyLocationRoute(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyLocationViewModel = hiltViewModel(),
) {
    // Offline is fine here: coordinates and Plus Code work without network.
    LocationGate(requiresNetwork = false, onDismiss = onNavigateUp, modifier = modifier) {
        // Collected only inside the gate's Ready content, and only while STARTED.
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val clipLabel = stringResource(R.string.my_location_clip_label)
        val chooserTitle = stringResource(R.string.my_location_share_chooser)

        ObserveAsEvents(viewModel.effects) { effect ->
            when (effect) {
                is MyLocationEffect.CopyToClipboard -> context.copyPlainText(clipLabel, effect.text)
                is MyLocationEffect.ShareText -> context.sharePlainText(effect.text, chooserTitle)
            }
        }

        MyLocationScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onNavigateUp = onNavigateUp,
        )
    }
}
