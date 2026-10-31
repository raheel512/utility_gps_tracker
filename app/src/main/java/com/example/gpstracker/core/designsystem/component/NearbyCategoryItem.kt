package com.example.gpstracker.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gpstracker.core.designsystem.theme.AppTextStyles
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.IconContainerTint
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.LocalSpacing

/**
 * Nearby Places category item (docs/design.md section 5): a 54dp circle with a
 * 28dp icon and 1dp border, an 8dp gap, then a label.
 */
@Composable
fun NearbyCategoryItem(
    label: String,
    tint: IconContainerTint,
    @DrawableRes drawableRes: Int?,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        FeatureIconBox(
            tint = tint,
            drawableRes = drawableRes,
            contentDescription = null,
            containerSize = 54.dp,
            iconSize = 28.dp,
            shape = CircleShape,
        )
        Spacer(modifier = Modifier.height(spacing.categoryGap))
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

@Preview(showBackground = true)
@Composable
private fun NearbyCategoryItemPreview() {
    AppTheme {
        NearbyCategoryItem(
            label = "Hotels",
            tint = LocalAppColors.current.hotels,
            drawableRes = null,
            contentDescription = "Hotels",
            onClick = {},
        )
    }
}
