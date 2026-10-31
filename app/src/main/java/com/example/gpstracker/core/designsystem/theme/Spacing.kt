package com.example.gpstracker.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing tokens aligned with docs/design.md section 3.
 */
data class AppSpacing(
    // Generic scale
    val xxxs: Dp = 2.dp,
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 22.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp,
    // Named from design.md section 3
    val appBarHorizontal: Dp = 20.dp,
    val appBarTop: Dp = 14.dp,
    val appBarBottom: Dp = 15.dp,
    val contentSide: Dp = 22.dp,
    val cardInner: Dp = 13.dp,
    val heroPadding: Dp = 16.dp,
    val gridGap: Dp = 12.dp,
    val sectionLabelGap: Dp = 8.dp,
    val categoryGap: Dp = 8.dp,
    val tileGap: Dp = 8.dp,
    // Touch / shape helpers
    val touchTarget: Dp = 48.dp,
    val buttonRadius: Dp = 28.dp,
    // Bottom bar (section 3)
    val navBarContentHeight: Dp = 64.dp,
    val navGestureHeight: Dp = 16.dp,
)

val LocalSpacing = staticCompositionLocalOf { AppSpacing() }
