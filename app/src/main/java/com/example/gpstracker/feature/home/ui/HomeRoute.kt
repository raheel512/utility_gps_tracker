package com.example.gpstracker.feature.home.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gpstracker.R
import com.example.gpstracker.core.common.ObserveAsEvents
import com.example.gpstracker.core.navigation.Route
import com.example.gpstracker.core.routes.openNearbyInGoogleMaps

/**
 * Stateful Home entry point: collects state and forwards one-off navigation
 * effects to [onNavigate]. The ViewModel never holds a NavController.
 */
@Composable
fun HomeRoute(
    onNavigate: (Route) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            is HomeNavEffect.Navigate -> onNavigate(effect.route)
            is HomeNavEffect.OpenNearbyMaps -> {
                val query = effect.category.mapsSearchQuery(
                    context.getString(effect.category.labelRes),
                )
                openMaps(context, query)
            }
            HomeNavEffect.OpenFamousPlacesMaps -> openMaps(context, "famous places")
        }
    }

    HomeScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

private fun openMaps(context: android.content.Context, query: String) {
    val opened = context.openNearbyInGoogleMaps(query)
    if (!opened) {
        Toast.makeText(
            context,
            R.string.routes_maps_not_installed,
            Toast.LENGTH_SHORT,
        ).show()
    }
}
