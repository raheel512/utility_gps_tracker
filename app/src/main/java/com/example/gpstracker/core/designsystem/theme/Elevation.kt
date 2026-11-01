package com.example.gpstracker.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Approximations of the CSS shadows in docs/design.md section 4. Compose cannot
 * reproduce CSS blur/spread exactly, so each token maps to a [Modifier.shadow]
 * elevation and tint. Use via [Modifier.appShadow].
 */
enum class AppShadow(
    val elevation: Dp,
    val color: Color,
) {
    /** Tool cards, Nearby card: softer drop than the original CSS 0 4 16. */
    Card(elevation = 2.dp, color = Color(0xFF0F172A)),

    /** "Other feature" tiles: light hairline shadow. */
    Tile(elevation = 1.dp, color = Color(0xFF000000)),

    /** Hero card: 0 12 28 -4 rgba(37,99,235,.28). */
    Hero(elevation = 8.dp, color = Color(0xFF2563EB)),

    /** Hero CTA button: soft pill lift. */
    Cta(elevation = 2.dp, color = Color(0xFF000000)),

    /** Floating map controls / style pill: barely-there lift over imagery. */
    MapControl(elevation = 0.5.dp, color = Color(0x22000000)),

    /** Bottom bar: 0 -4 20 rgba(15,23,42,.05). */
    Nav(elevation = 8.dp, color = Color(0xFF0F172A)),
}

/**
 * Applies the given [shadow] token with [shape], clipping to that shape so the
 * tinted shadow reads cleanly behind rounded surfaces.
 */
fun Modifier.appShadow(
    shadow: AppShadow,
    shape: Shape = RoundedCornerShape(16.dp),
): Modifier = this.shadow(
    elevation = shadow.elevation,
    shape = shape,
    clip = false,
    ambientColor = shadow.color,
    spotColor = shadow.color,
)
