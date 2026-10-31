package com.example.gpstracker.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.IconContainerTint
import com.example.gpstracker.core.designsystem.theme.LocalAppColors

/**
 * Pale rounded icon container (docs/design.md section 1 + 5) holding an
 * untinted multi-color drawable. Renders an empty slot until the asset exists.
 *
 * @param tint background + optional border token pair.
 * @param drawableRes the multi-color artwork; never tinted.
 * @param emoji optional emoji glyph (e.g. Country Code flag from Figma) when
 * there is no separate drawable.
 */
@Composable
fun FeatureIconBox(
    tint: IconContainerTint,
    @DrawableRes drawableRes: Int?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    containerSize: Dp = 44.dp,
    iconSize: Dp = 24.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    emoji: String? = null,
) {
    val borderColor = tint.border
    Box(
        modifier = modifier
            .size(containerSize)
            .clip(shape)
            .background(tint.background)
            .then(
                if (borderColor != null) Modifier.border(1.dp, borderColor, shape) else Modifier,
            ),
        contentAlignment = Alignment.Center,
    ) {
        when {
            drawableRes != null -> AssetSlot(
                drawableRes = drawableRes,
                contentDescription = contentDescription,
                modifier = Modifier.size(iconSize),
            )
            emoji != null -> Text(text = emoji, fontSize = 20.sp)
            else -> AssetSlot(
                drawableRes = null,
                contentDescription = contentDescription,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FeatureIconBoxPreview() {
    AppTheme {
        FeatureIconBox(
            tint = LocalAppColors.current.myLocation,
            drawableRes = null,
            contentDescription = null,
        )
    }
}
