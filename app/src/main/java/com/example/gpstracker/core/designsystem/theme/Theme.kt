package com.example.gpstracker.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColorScheme = lightColorScheme(
    primary = ColorTokens.PrimaryLight,
    onPrimary = ColorTokens.OnPrimaryLight,
    primaryContainer = ColorTokens.PrimaryContainerLight,
    onPrimaryContainer = ColorTokens.OnPrimaryContainerLight,
    secondary = ColorTokens.SecondaryLight,
    onSecondary = ColorTokens.OnSecondaryLight,
    secondaryContainer = ColorTokens.SecondaryContainerLight,
    onSecondaryContainer = ColorTokens.OnSecondaryContainerLight,
    tertiary = ColorTokens.TertiaryLight,
    onTertiary = ColorTokens.OnTertiaryLight,
    tertiaryContainer = ColorTokens.TertiaryContainerLight,
    onTertiaryContainer = ColorTokens.OnTertiaryContainerLight,
    error = ColorTokens.ErrorLight,
    onError = ColorTokens.OnErrorLight,
    errorContainer = ColorTokens.ErrorContainerLight,
    onErrorContainer = ColorTokens.OnErrorContainerLight,
    background = ColorTokens.BackgroundLight,
    onBackground = ColorTokens.OnBackgroundLight,
    surface = ColorTokens.SurfaceLight,
    onSurface = ColorTokens.OnSurfaceLight,
    surfaceVariant = ColorTokens.SurfaceVariantLight,
    onSurfaceVariant = ColorTokens.OnSurfaceVariantLight,
    outline = ColorTokens.OutlineLight,
    outlineVariant = ColorTokens.OutlineVariantLight,
)

/** Dark colors — derived, review on device. Follows system via [AppTheme] darkTheme. */
private val DarkColorScheme = darkColorScheme(
    primary = ColorTokens.PrimaryDark,
    onPrimary = ColorTokens.OnPrimaryDark,
    primaryContainer = ColorTokens.PrimaryContainerDark,
    onPrimaryContainer = ColorTokens.OnPrimaryContainerDark,
    secondary = ColorTokens.SecondaryDark,
    onSecondary = ColorTokens.OnSecondaryDark,
    secondaryContainer = ColorTokens.SecondaryContainerDark,
    onSecondaryContainer = ColorTokens.OnSecondaryContainerDark,
    tertiary = ColorTokens.TertiaryDark,
    onTertiary = ColorTokens.OnTertiaryDark,
    tertiaryContainer = ColorTokens.TertiaryContainerDark,
    onTertiaryContainer = ColorTokens.OnTertiaryContainerDark,
    error = ColorTokens.ErrorDark,
    onError = ColorTokens.OnErrorDark,
    errorContainer = ColorTokens.ErrorContainerDark,
    onErrorContainer = ColorTokens.OnErrorContainerDark,
    background = ColorTokens.BackgroundDark,
    onBackground = ColorTokens.OnBackgroundDark,
    surface = ColorTokens.SurfaceDark,
    onSurface = ColorTokens.OnSurfaceDark,
    surfaceVariant = ColorTokens.SurfaceVariantDark,
    onSurfaceVariant = ColorTokens.OnSurfaceVariantDark,
    outline = ColorTokens.OutlineDark,
    outlineVariant = ColorTokens.OutlineVariantDark,
)

/**
 * App theme. [darkTheme] defaults to the system setting (Auto). Callers may
 * pass an explicit value for Light / Dark when a theme preference is applied.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    CompositionLocalProvider(
        LocalSpacing provides AppSpacing(),
        LocalAppColors provides appColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
