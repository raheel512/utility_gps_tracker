package com.example.gpstracker.feature.debug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.component.AppTopBar
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.LocalSpacing
import com.example.gpstracker.core.location.UserLocation
import com.example.gpstracker.core.location.gate.LocationGate
import com.example.gpstracker.core.location.gate.LocationGatePhase
import com.example.gpstracker.core.location.gate.LocationGateViewModel

/**
 * DEBUG-ONLY throwaway screen: gate phase + latest [UserLocation] as plain text.
 * Opened via `gpstracker://location-test` (debug manifest only).
 */
@Composable
fun LocationTestRoute(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LocationTestViewModel = hiltViewModel(),
    gateViewModel: LocationGateViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gateState by gateViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(gateState.phase) {
        viewModel.onEvent(LocationTestEvent.GatePhaseChanged(gateState.phase))
    }

    LocationGate(
        requiresNetwork = false,
        onDismiss = onNavigateUp,
        viewModel = gateViewModel,
        modifier = modifier,
    ) {
        LocationTestScreen(
            uiState = uiState,
            onNavigateUp = onNavigateUp,
        )
    }
}

@Composable
fun LocationTestScreen(
    uiState: LocationTestUiState,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current

    Column(modifier = modifier.fillMaxSize()) {
        AppTopBar(
            title = stringResource(R.string.screen_location_test),
            onNavigateUp = onNavigateUp,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(spacing.contentSide),
        ) {
            Text(
                text = stringResource(R.string.location_test_gate_label),
                style = MaterialTheme.typography.labelLarge,
                color = appColors.textMuted,
            )
            Text(
                text = uiState.gatePhase.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = appColors.textPrimary,
            )

            Spacer(modifier = Modifier.height(spacing.md))

            Text(
                text = stringResource(R.string.location_test_location_label),
                style = MaterialTheme.typography.labelLarge,
                color = appColors.textMuted,
            )
            Text(
                text = uiState.latestLocation?.formatPlain()
                    ?: stringResource(R.string.location_test_no_fix),
                style = MaterialTheme.typography.bodyMedium,
                color = appColors.textPrimary,
            )
        }
    }
}

private fun UserLocation.formatPlain(): String = buildString {
    appendLine("lat=$latitude")
    appendLine("lng=$longitude")
    appendLine("accuracyMeters=$accuracyMeters")
    appendLine("speedMps=$speedMps")
    appendLine("altitudeMeters=$altitudeMeters")
    appendLine("bearingDegrees=$bearingDegrees")
    append("timestampMillis=$timestampMillis")
}

@Preview(showBackground = true)
@Composable
private fun LocationTestScreenPreview() {
    AppTheme {
        LocationTestScreen(
            uiState = LocationTestUiState(
                gatePhase = LocationGatePhase.Ready,
                latestLocation = UserLocation(
                    latitude = 33.6,
                    longitude = 73.0,
                    accuracyMeters = 12f,
                    speedMps = null,
                    altitudeMeters = 500.0,
                    bearingDegrees = null,
                    timestampMillis = 0L,
                ),
            ),
            onNavigateUp = {},
        )
    }
}
