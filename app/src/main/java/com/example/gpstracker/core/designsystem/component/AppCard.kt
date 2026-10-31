package com.example.gpstracker.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gpstracker.core.designsystem.theme.AppShadow
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.LocalSpacing
import com.example.gpstracker.core.designsystem.theme.appShadow

/**
 * Elevated white card from docs/design.md: 16dp radius, 1dp cardBorder, card
 * shadow, 13dp inner padding.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current
    val shape = MaterialTheme.shapes.large
    val colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
    )
    val border = BorderStroke(1.dp, appColors.cardBorder)
    val cardModifier = modifier
        .fillMaxWidth()
        .appShadow(AppShadow.Card, shape)
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            colors = colors,
            shape = shape,
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(spacing.cardInner), content = content)
        }
    } else {
        Card(
            modifier = cardModifier,
            colors = colors,
            shape = shape,
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(spacing.cardInner), content = content)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppCardPreview() {
    AppTheme {
        AppCard {
            Text(text = "Status card")
        }
    }
}
