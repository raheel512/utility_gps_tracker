package com.example.gpstracker.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gpstracker.core.designsystem.theme.AppShadow
import com.example.gpstracker.core.designsystem.theme.AppTextStyles
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.IconContainerTint
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.LocalSpacing
import com.example.gpstracker.core.designsystem.theme.appShadow

/**
 * "Other Feature" tile (docs/design.md section 5): ~89dp tall, icon container
 * (40–44dp) centered, 8dp gap, one-line label. Optional [badge] overlays the
 * bottom-end of the icon (used by Country Code's "+1").
 */
@Composable
fun FeatureTile(
    label: String,
    tint: IconContainerTint,
    @DrawableRes drawableRes: Int?,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: (@Composable () -> Unit)? = null,
    emoji: String? = null,
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current
    val shape = MaterialTheme.shapes.large
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 89.dp)
            .appShadow(AppShadow.Tile, shape),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = shape,
        border = BorderStroke(1.dp, appColors.tileBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(contentAlignment = Alignment.Center) {
                FeatureIconBox(
                    tint = tint,
                    drawableRes = drawableRes,
                    contentDescription = null,
                    containerSize = 42.dp,
                    iconSize = 28.dp,
                    emoji = emoji,
                )
                if (badge != null) {
                    Box(
                        modifier = Modifier.align(Alignment.BottomEnd),
                    ) {
                        badge()
                    }
                }
            }
            Spacer(modifier = Modifier.height(spacing.tileGap))
            Text(
                text = label,
                style = AppTextStyles.tileLabel,
                color = appColors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FeatureTilePreview() {
    AppTheme {
        FeatureTile(
            label = "Compass",
            tint = LocalAppColors.current.compass,
            drawableRes = null,
            contentDescription = "Compass",
            onClick = {},
        )
    }
}
