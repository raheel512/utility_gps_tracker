package com.example.gpstracker.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape tokens from docs/design.md section 3.
 *
 * - extraSmall: badges / small chips
 * - small: pills use [PillShape] directly (full radius)
 * - medium (12dp): icon containers
 * - large (16dp): cards, tiles, hero
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(16.dp),
)

/** Full pill: chips, buttons, circular icons, nav indicator. */
val PillShape = RoundedCornerShape(percent = 50)

/** Bottom bar — top corners 16dp only. */
val BottomBarShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
