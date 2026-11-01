package com.example.gpstracker.feature.emergency.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import com.example.gpstracker.core.routes.openNearbyInGoogleMaps

@Composable
fun EmergencyRoute(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EmergencyViewModel = hiltViewModel(),
) {
    // Dial / share coordinates work offline; geocode is best-effort inside the VM.
    LocationGate(requiresNetwork = false, onDismiss = onNavigateUp, modifier = modifier) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val shareChooser = stringResource(R.string.emergency_share_chooser)
        val shareHeadline = stringResource(R.string.emergency_share_headline)
        val clipLabel = stringResource(R.string.emergency_clip_label)
        val dialFailed = stringResource(R.string.emergency_dial_failed)
        val mapsFailed = stringResource(R.string.routes_maps_not_installed)

        ObserveAsEvents(viewModel.effects) { effect ->
            when (effect) {
                is EmergencyEffect.Dial -> {
                    val opened = openDialer(context, effect.number)
                    if (!opened) {
                        Toast.makeText(context, dialFailed, Toast.LENGTH_SHORT).show()
                    }
                }
                is EmergencyEffect.ShareLocation -> {
                    val body = buildString {
                        appendLine(shareHeadline)
                        effect.placeLine?.let { appendLine(it) }
                        effect.coordinates?.let { appendLine(it) }
                        append(effect.mapsUrl)
                    }
                    runCatching {
                        context.sharePlainText(body.trim(), shareChooser)
                    }
                }
                is EmergencyEffect.CopyText -> {
                    context.copyPlainText(clipLabel, effect.text)
                }
                is EmergencyEffect.OpenMapsQuery -> {
                    val opened = context.openNearbyInGoogleMaps(effect.query)
                    if (!opened) {
                        Toast.makeText(context, mapsFailed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        EmergencyScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onNavigateUp = onNavigateUp,
        )
    }
}

private fun openDialer(context: android.content.Context, number: String): Boolean {
    val uri = Uri.parse("tel:$number")
    val intent = Intent(Intent.ACTION_DIAL, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }
}
