package com.example.gpstracker.feature.places.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.gpstracker.feature.places.nearby.NearbyCategoriesRoute

/**
 * Entry for [com.example.gpstracker.core.navigation.Route.NearbyPlaces] (See More grid).
 */
@Composable
fun NearbyPlacesScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NearbyCategoriesRoute(
        onNavigateUp = onNavigateUp,
        modifier = modifier,
    )
}
