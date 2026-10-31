package com.example.gpstracker.feature.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.component.AppTopBar
import com.example.gpstracker.core.designsystem.theme.LocalSpacing

@Composable
fun PlaceholderScreen(
    screenName: String,
    modifier: Modifier = Modifier,
    onNavigateUp: (() -> Unit)? = null,
) {
    val spacing = LocalSpacing.current
    Column(modifier = modifier.fillMaxSize()) {
        AppTopBar(
            title = screenName,
            onNavigateUp = onNavigateUp,
            // Top tabs have no up caret — they own status-bar insets.
            statusBarAware = onNavigateUp == null,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.md),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = screenName,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.placeholder_todo),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = spacing.xs),
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
