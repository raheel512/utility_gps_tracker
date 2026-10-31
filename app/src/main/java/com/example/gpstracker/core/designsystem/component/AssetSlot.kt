package com.example.gpstracker.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gpstracker.core.designsystem.theme.ColorTokens

/**
 * Renders a drawable when present. Until assets are added, shows an empty slot.
 *
 * Expected asset names are documented beside each call site with TODO(asset).
 */
@Composable
fun AssetSlot(
    @DrawableRes drawableRes: Int?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholderSize: Dp = 120.dp,
    contentScale: ContentScale = ContentScale.Fit,
) {
    if (drawableRes != null) {
        Image(
            painter = painterResource(drawableRes),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
        )
    } else {
        // TODO(asset): replace with real drawable — empty slot keeps the build green
        Box(
            modifier = modifier
                .then(
                    if (modifier == Modifier) {
                        Modifier.size(placeholderSize)
                    } else {
                        Modifier
                    },
                )
                .background(Color.Transparent),
        )
    }
}

@Composable
fun FlagSlot(
    @DrawableRes drawableRes: Int?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    if (drawableRes != null) {
        Image(
            painter = painterResource(drawableRes),
            contentDescription = contentDescription,
            modifier = modifier.size(size),
            contentScale = ContentScale.Crop,
        )
    } else {
        // TODO(asset): flag_xx drawable
        Box(
            modifier = modifier
                .size(size)
                .background(ColorTokens.Primary0, shape = androidx.compose.foundation.shape.CircleShape),
        )
    }
}
