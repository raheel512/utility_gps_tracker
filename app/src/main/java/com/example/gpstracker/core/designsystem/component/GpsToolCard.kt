package com.example.gpstracker.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * GPS Tools grid card (docs/design.md section 5): 44dp icon container, 12dp gap,
 * two-line label. White surface, cardBorder, card shadow, 16dp radius.
 */
@Composable
fun GpsToolCard(
    label: String,
    tint: IconContainerTint,
    @DrawableRes drawableRes: Int?,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current
    val shape = MaterialTheme.shapes.large
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 70.dp)
            .appShadow(AppShadow.Card, shape),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = shape,
        border = BorderStroke(1.dp, appColors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.cardInner),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FeatureIconBox(
                tint = tint,
                drawableRes = drawableRes,
                contentDescription = null,
                containerSize = 44.dp,
                iconSize = 34.dp,
            )
            Spacer(modifier = Modifier.width(spacing.sm))
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = AppTextStyles.toolCardLabel,
                color = appColors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GpsToolCardPreview() {
    AppTheme {
        GpsToolCard(
            label = "Voice Navigation",
            tint = LocalAppColors.current.voiceNavigation,
            drawableRes = null,
            contentDescription = "Voice Navigation",
            onClick = {},
        )
    }
}
