package com.example.gpstracker.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * App-specific type roles from docs/design.md section 2. Colors are applied at
 * the call site (via theme tokens), not baked into the style.
 *
 * Families: [RobotoFamily] for UI text and nav labels, [PlusJakartaSansFamily]
 * for the hero card and section headers.
 */
object AppTextStyles {
    /** App bar title ("GPS Tracker") — Roboto Bold 20/28, -0.5. */
    val appBarTitle = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.5).sp,
    )

    /** Hero title ("Route Finder") — Plus Jakarta Sans Bold 20/25, -0.5. */
    val heroTitle = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.5).sp,
    )

    /** Hero subtitle — Plus Jakarta Sans Medium 11.5/14.4. */
    val heroSubtitle = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.5.sp,
        lineHeight = 14.4.sp,
    )

    /** Hero CTA ("Find Route") — Plus Jakarta Sans Bold 12/16. */
    val heroCta = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )

    /** Section label (uppercase) — Plus Jakarta Sans Bold 12/16, +0.6. */
    val sectionLabel = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.6.sp,
    )

    /** Section link ("See More") — Plus Jakarta Sans SemiBold 12/16. */
    val sectionLink = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )

    /** Tool card label (two lines) — Roboto Medium 14/16. */
    val toolCardLabel = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 16.sp,
    )

    /** Tile / category label — Roboto SemiBold 12/16. */
    val tileLabel = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )

    /** Chip text — Roboto Regular 12/16. */
    val chipText = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )

    /** Badge ("+1") — Roboto Black 9/13.5. */
    val badge = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Black,
        fontSize = 9.sp,
        lineHeight = 13.5.sp,
    )

    /** Nav label (active) — Roboto SemiBold 11/14, +0.33. */
    val navLabelActive = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.33.sp,
    )

    /** Nav label (inactive) — Roboto Medium 11/14, +0.33. */
    val navLabelInactive = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.33.sp,
    )
}

/**
 * Material 3 Typography mapped to the design families so stock M3 components
 * inherit the right fonts. App-specific roles live in [AppTextStyles].
 */
val AppTypography = Typography(
    titleLarge = AppTextStyles.appBarTitle,
    titleMedium = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    labelLarge = AppTextStyles.toolCardLabel,
    labelMedium = AppTextStyles.tileLabel,
    labelSmall = AppTextStyles.navLabelInactive,
)
