package com.example.gpstracker.feature.map.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.SavedStateHandle
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.location.gate.LocationGate
import com.example.gpstracker.core.navigation.Route
import com.example.gpstracker.feature.search.ui.FindLocationRoute

/**
 * Home "Route Finder" / Find Location entry ([com.example.gpstracker.core.navigation.Route.RouteFinder]).
 */
@Composable
fun RouteFinderScreen(
    onNavigateUp: () -> Unit,
    onOpenRoutePreview: (Route.RoutePreview) -> Unit = {},
    onPickStop: () -> Unit = {},
    savedStateHandle: SavedStateHandle? = null,
    modifier: Modifier = Modifier,
) {
    LocationGate(requiresNetwork = true, onDismiss = onNavigateUp, modifier = modifier) {
        FindLocationRoute(
            onNavigateUp = onNavigateUp,
            onOpenRoutePreview = onOpenRoutePreview,
            onPickStop = onPickStop,
            savedStateHandle = savedStateHandle,
        )
    }
}
