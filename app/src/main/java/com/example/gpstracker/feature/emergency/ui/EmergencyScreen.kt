package com.example.gpstracker.feature.emergency.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.gpstracker.R
import com.example.gpstracker.feature.common.PlaceholderScreen

@Composable
fun EmergencyScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        screenName = stringResource(R.string.screen_emergency),
        onNavigateUp = onNavigateUp,
        modifier = modifier,
    )
}
