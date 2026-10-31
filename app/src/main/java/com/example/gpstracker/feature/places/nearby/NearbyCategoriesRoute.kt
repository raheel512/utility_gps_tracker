package com.example.gpstracker.feature.places.nearby

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
import com.example.gpstracker.core.places.NearbyCategory
import com.example.gpstracker.core.routes.openNearbyInGoogleMaps

@Composable
fun NearbyCategoriesRoute(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NearbyCategoriesViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    LocationGate(requiresNetwork = true, onDismiss = onNavigateUp, modifier = modifier) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ObserveAsEvents(viewModel.effects) { effect ->
            when (effect) {
                is NearbyCategoriesEffect.OpenCategory -> {
                    val category = NearbyCategory.fromId(effect.categoryId) ?: return@ObserveAsEvents
                    val opened = context.openNearbyInGoogleMaps(
                        category.mapsSearchQuery(context.getString(category.labelRes)),
                    )
                    if (!opened) {
                        Toast.makeText(
                            context,
                            R.string.routes_maps_not_installed,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            }
        }
        NearbyCategoriesScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onNavigateUp = onNavigateUp,
        )
    }
}
