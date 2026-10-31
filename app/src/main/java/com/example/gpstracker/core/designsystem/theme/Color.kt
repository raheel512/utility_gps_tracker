package com.example.gpstracker.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

/**
 * Raw color values from docs/design.md section 1. These are the single source
 * of hex values; prefer the semantic [AppColors] (via [LocalAppColors]) or the
 * Material 3 [androidx.compose.material3.ColorScheme] in UI code.
 */
object ColorTokens {
    // Brand / primary (resolved: primary0 / primary500 / primary700 only)
    val Primary0 = Color(0xFFDBEAFE)
    val Primary500 = Color(0xFF2563EB)
    val Primary700 = Color(0xFF1D4ED8)
    val Sky600 = Color(0xFF0284C7)

    // Text
    val TextPrimary = Color(0xFF131B2E)
    val TextSecondary = Color(0xFF434655)
    val TextMuted = Color(0xFF64748B)
    val TextOnPrimary = Color(0xFFFFFFFF)

    // Surfaces and outlines
    val Surface = Color(0xFFFFFFFF)
    val ScreenTintStart = Color(0xFFF4F5FF) // VERIFY against Figma
    val ScreenTintEnd = Color(0xFFFFFFFF) // VERIFY against Figma
    val NavBar = Color(0xE6FAF8FF) // #FAF8FF at ~90% alpha
    val NavIndicator = Color(0xFFE2E7FF)
    val OutlineDefault = Color(0xFFE6E6E6)
    val CardBorder = Color(0xFFF1F5F9)
    val TileBorder = Color(0xFFE5E7EB)

    // Feature icon container tints (background / border)
    val VoiceNavigationBg = Color(0xFFD8F5D9)
    val MyLocationBg = Color(0xFFDBEAFE)
    val SatelliteViewBg = Color(0xFFEEF2FF)
    val GpsCameraBg = Color(0xFFECFEFF)
    val HotelsBg = Color(0xFFFFF1F2)
    val HotelsBorder = Color(0xFFFFE4E6)
    val CafesBg = Color(0xFFFFFBEB)
    val CafesBorder = Color(0xFFFEF3C7)
    val BanksBg = Color(0xFFEFF6FF)
    val BanksBorder = Color(0xFFDBEAFE)
    val HospitalsBg = Color(0xFFFEF2F2)
    val HospitalsBorder = Color(0xFFFEE2E2)
    val CompassBg = Color(0xFFEEF2FF)
    val CompassBorder = Color(0xFFE0E7FF)
    val FamousPlacesBg = Color(0xFFF0F9FF)
    val FamousPlacesBorder = Color(0xFFE0F2FE)
    val CountryCodeBg = Color(0xFFEEF2FF)
    val WeatherBg = Color(0xFFFFFBEB)
    val WeatherBorder = Color(0xFFFEF3C7)
    val EmergencyBg = Color(0xFFFFF1F2)

    // Connectivity chip
    val ConnectedChipBg = Color(0xFFECFDF5)
    val ConnectedChipContent = Color(0xFF047857)
    val OfflineChipBg = Color(0xFFFEF2F2)
    val OfflineChipContent = Color(0xFFB91C1C)

    val CountryBadgeBg = Color(0xFF2563EB)

    // Hero gradient (160°): start -> mid -> end — unchanged in dark
    val HeroGradientStart = Sky600 // #0284C7
    val HeroGradientMid = Primary500 // #2563EB
    val HeroGradientEnd = Primary700 // #1D4ED8

    // Pin gradients — unchanged in dark; hero illustration drawable includes pins
    val PinStartA = Color(0xFFE11D48)
    val PinStartB = Color(0xFFEF4444)
    val PinDestinationA = Color(0xFFDC2626)
    val PinDestinationB = Color(0xFFF43F5E)

    // Other-screen chips — defaults from [≈]; VERIFY against Figma
    val SuccessChipText = Color(0xFF15803D) // VERIFY against Figma
    val SuccessChipBg = Color(0xFFDCFCE7) // VERIFY against Figma
    val ProBadge = Color(0xFF4F46E5) // VERIFY against Figma
    val ProBadgeBg = Color(0xFFEEF2FF) // VERIFY against Figma
    val Destructive = Color(0xFFB91C1C) // VERIFY against Figma
    val MapManeuverAccent = Color(0xFF047857) // VERIFY against Figma
    val SelectionRowBg = Color(0xFFDBEAFE)

    // Map overlays (Figma My Location 55505:2320, Satellite 55505:3207)
    val MapControlIcon = Color(0xFF334155)
    val MapPullHandle = Color(0xFFCBD5E1)
    val MapActionShare = Color(0xFFEF4444)
    val MapActionCopy = Color(0xFF10B981)
    val MapTooltipBg = Color(0xE60F172A) // #0F172A at 90%
    val MapStyleInactiveText = Color(0xFF475569)
    val MapStylePillBg = Color(0xE6FFFFFF) // white at 90%
    val CompassNorth = Color(0xFFEF4444)
    val CompassSouth = Color(0xFF94A3B8)
    val MapPulse = Color(0x4D3B82F6) // #3B82F6 at 30%

    // GPS Camera overlays (Figma GPS Camera 55505:1763)
    val CameraOnScrim = Color(0xFFFAF8FF)
    val CameraControlScrim = Color(0x99283044) // #283044 at 60%
    val CameraTemplateScrim = Color(0xB3283044) // #283044 at 70%
    val CameraStampScrim = Color(0xD9283044) // #283044 at 85%
    val CameraVignetteTop = Color(0xCC283044) // #283044 at 80%
    val CameraVignetteBottom = Color(0xF2283044) // #283044 at 95%
    val CameraChipScrim = Color(0x33DAE2FD) // #DAE2FD at 20%
    val CameraLabel = Color(0xFFDAE2FD)
    val CameraLabelAccent = Color(0xFFDBE1FF)
    val CameraAddressText = Color(0xFFE2E7FF)
    val CameraBadgeText = Color(0xFFEEEFFF)
    val CameraShutterCore = Color(0xFF004AC6)
    val CameraHeadingLocked = Color(0xFF4EDEA3)
    val CameraHeadingCoarse = Color(0xFFFBBF24)
    val CameraReticleDot = Color(0xFFB4C5FF)
    val CameraReticleTick = Color(0xCCB4C5FF) // #B4C5FF at 80%
    val CameraReticleCorner = Color(0xFFDBE1FF)
    val CameraLevelLine = Color(0xB36FFBBE) // #6FFBBE at 70%
    val CameraGridLine = Color(0x1FFAF8FF) // #FAF8FF at 20% × layer 60%
    val CameraProcessingScrim = Color(0x40000000)

    // Light Material scheme
    val PrimaryLight = Primary500
    val OnPrimaryLight = TextOnPrimary
    val PrimaryContainerLight = Primary0
    val OnPrimaryContainerLight = Primary700
    val SecondaryLight = TextSecondary
    val OnSecondaryLight = TextOnPrimary
    val SecondaryContainerLight = Primary0
    val OnSecondaryContainerLight = TextPrimary
    val TertiaryLight = Sky600
    val OnTertiaryLight = TextOnPrimary
    val TertiaryContainerLight = Primary0
    val OnTertiaryContainerLight = TextPrimary
    val ErrorLight = Destructive
    val OnErrorLight = TextOnPrimary
    val ErrorContainerLight = Color(0xFFFEE2E2)
    val OnErrorContainerLight = Color(0xFF7F1D1D)
    val BackgroundLight = Surface
    val OnBackgroundLight = TextPrimary
    val SurfaceLight = Surface
    val OnSurfaceLight = TextPrimary
    val SurfaceVariantLight = ScreenTintStart
    val OnSurfaceVariantLight = TextSecondary
    val OutlineLight = OutlineDefault
    val OutlineVariantLight = CardBorder

    // Dark — derived, review on device
    val BackgroundDark = Color(0xFF0B1220)
    val SurfaceDark = Color(0xFF131B2E)
    val OnSurfaceDark = Color(0xFFE6EAF5)
    val OnSurfaceVariantDark = Color(0xFFA9B1C7)
    val OutlineDark = Color(0xFF2A3550)
    val CardBorderDark = Color(0xFF1E2740)
    val PrimaryDark = Color(0xFF7AA2FF)
    val OnPrimaryDark = Color(0xFF0B1220)
    val PrimaryContainerDark = Color(0xFF1E3A8A)
    val OnPrimaryContainerDark = Color(0xFFDBEAFE)
    val NavBarDark = Color(0xE6131B2E) // #131B2E at 90% alpha
    val NavIndicatorDark = Color(0xFF1E2A4A)

    val SecondaryDark = OnSurfaceVariantDark
    val OnSecondaryDark = BackgroundDark
    val SecondaryContainerDark = OutlineDark
    val OnSecondaryContainerDark = OnSurfaceDark
    val TertiaryDark = Color(0xFF7DD3FC)
    val OnTertiaryDark = BackgroundDark
    val TertiaryContainerDark = Color(0xFF004D6B)
    val OnTertiaryContainerDark = Color(0xFFE0F2FE)
    val ErrorDark = Color(0xFFFECACA)
    val OnErrorDark = Color(0xFF7F1D1D)
    val ErrorContainerDark = Color(0xFF991B1B)
    val OnErrorContainerDark = Color(0xFFFEE2E2)
    val OnBackgroundDark = OnSurfaceDark
    val SurfaceVariantDark = OutlineDark
    val OutlineVariantDark = CardBorderDark
}

/**
 * A pale icon-container tint pair: [background] and an optional [border].
 */
data class IconContainerTint(
    val background: Color,
    val border: Color? = null,
)

/**
 * Semantic design-system colors that fall outside the Material 3 ColorScheme
 * (container tints, gradients, nav bar, badges). Supplied via [LocalAppColors].
 */
data class AppColors(
    // Brand references kept for convenience
    val primary: Color,
    val primaryContainer: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textOnPrimary: Color,
    // Surfaces / outlines
    val screenTintStart: Color,
    val screenTintEnd: Color,
    val navBar: Color,
    val navIndicator: Color,
    val outlineDefault: Color,
    val cardBorder: Color,
    val tileBorder: Color,
    // Hero gradient stops
    val heroGradientStart: Color,
    val heroGradientMid: Color,
    val heroGradientEnd: Color,
    // Connectivity chip
    val connectedChipBg: Color,
    val connectedChipBorder: Color,
    val connectedChipText: Color,
    val offlineChipBg: Color,
    val offlineChipBorder: Color,
    val offlineChipText: Color,
    // Badges / paywall chips
    val countryBadgeBg: Color,
    val countryBadgeText: Color,
    val proBadge: Color,
    val proBadgeBg: Color, // VERIFY against Figma
    val successChipBg: Color, // VERIFY against Figma
    val successChipText: Color, // VERIFY against Figma
    // Feature icon container tints
    val voiceNavigation: IconContainerTint,
    val myLocation: IconContainerTint,
    val satelliteView: IconContainerTint,
    val gpsCamera: IconContainerTint,
    val hotels: IconContainerTint,
    val cafes: IconContainerTint,
    val banks: IconContainerTint,
    val hospitals: IconContainerTint,
    val compass: IconContainerTint,
    val famousPlaces: IconContainerTint,
    val countryCode: IconContainerTint,
    val countryInfo: IconContainerTint,
    val weather: IconContainerTint,
    val emergency: IconContainerTint,
    // Map overlays
    val mapControlSurface: Color,
    val mapControlIcon: Color,
    val mapControlDivider: Color,
    val mapPullHandle: Color,
    val mapActionShare: Color,
    val mapActionCopy: Color,
    val mapActionIcon: Color,
    val mapTooltipBg: Color,
    val mapTooltipText: Color,
    val mapStylePillBg: Color,
    val mapStyleInactiveText: Color,
    val compassNorth: Color,
    val compassSouth: Color,
    val mapPulse: Color,
)

/** Light tint at [alpha] composited over [surface] (dark icon-container recipe). */
private fun Color.tintOverSurface(surface: Color, alpha: Float = 0.16f): Color =
    copy(alpha = alpha).compositeOver(surface)

private fun darkIconTint(bg: Color, border: Color? = null): IconContainerTint {
    val surface = ColorTokens.SurfaceDark
    return IconContainerTint(
        background = bg.tintOverSurface(surface),
        border = border?.tintOverSurface(surface),
    )
}

/** Light semantic colors built from [ColorTokens]. */
val LightAppColors = AppColors(
    primary = ColorTokens.Primary500,
    primaryContainer = ColorTokens.Primary0,
    textPrimary = ColorTokens.TextPrimary,
    textSecondary = ColorTokens.TextSecondary,
    textMuted = ColorTokens.TextMuted,
    textOnPrimary = ColorTokens.TextOnPrimary,
    screenTintStart = ColorTokens.ScreenTintStart,
    screenTintEnd = ColorTokens.ScreenTintEnd,
    navBar = ColorTokens.NavBar,
    navIndicator = ColorTokens.NavIndicator,
    outlineDefault = ColorTokens.OutlineDefault,
    cardBorder = ColorTokens.CardBorder,
    tileBorder = ColorTokens.TileBorder,
    heroGradientStart = ColorTokens.HeroGradientStart,
    heroGradientMid = ColorTokens.HeroGradientMid,
    heroGradientEnd = ColorTokens.HeroGradientEnd,
    connectedChipBg = ColorTokens.ConnectedChipBg,
    connectedChipBorder = ColorTokens.ConnectedChipContent,
    connectedChipText = ColorTokens.ConnectedChipContent,
    offlineChipBg = ColorTokens.OfflineChipBg,
    offlineChipBorder = ColorTokens.OfflineChipContent,
    offlineChipText = ColorTokens.OfflineChipContent,
    countryBadgeBg = ColorTokens.CountryBadgeBg,
    countryBadgeText = ColorTokens.TextOnPrimary,
    proBadge = ColorTokens.ProBadge,
    proBadgeBg = ColorTokens.ProBadgeBg, // VERIFY against Figma
    successChipBg = ColorTokens.SuccessChipBg, // VERIFY against Figma
    successChipText = ColorTokens.SuccessChipText, // VERIFY against Figma
    voiceNavigation = IconContainerTint(ColorTokens.VoiceNavigationBg),
    myLocation = IconContainerTint(ColorTokens.MyLocationBg),
    satelliteView = IconContainerTint(ColorTokens.SatelliteViewBg),
    gpsCamera = IconContainerTint(ColorTokens.GpsCameraBg),
    hotels = IconContainerTint(ColorTokens.HotelsBg, ColorTokens.HotelsBorder),
    cafes = IconContainerTint(ColorTokens.CafesBg, ColorTokens.CafesBorder),
    banks = IconContainerTint(ColorTokens.BanksBg, ColorTokens.BanksBorder),
    hospitals = IconContainerTint(ColorTokens.HospitalsBg, ColorTokens.HospitalsBorder),
    compass = IconContainerTint(ColorTokens.CompassBg, ColorTokens.CompassBorder),
    famousPlaces = IconContainerTint(ColorTokens.FamousPlacesBg, ColorTokens.FamousPlacesBorder),
    countryCode = IconContainerTint(ColorTokens.CountryCodeBg),
    countryInfo = IconContainerTint(ColorTokens.FamousPlacesBg, ColorTokens.FamousPlacesBorder),
    weather = IconContainerTint(ColorTokens.WeatherBg, ColorTokens.WeatherBorder),
    emergency = IconContainerTint(ColorTokens.EmergencyBg),
    mapControlSurface = ColorTokens.Surface,
    mapControlIcon = ColorTokens.MapControlIcon,
    mapControlDivider = ColorTokens.CardBorder,
    mapPullHandle = ColorTokens.MapPullHandle,
    mapActionShare = ColorTokens.MapActionShare,
    mapActionCopy = ColorTokens.MapActionCopy,
    mapActionIcon = ColorTokens.TextOnPrimary,
    mapTooltipBg = ColorTokens.MapTooltipBg,
    mapTooltipText = ColorTokens.TextOnPrimary,
    mapStylePillBg = ColorTokens.MapStylePillBg,
    mapStyleInactiveText = ColorTokens.MapStyleInactiveText,
    compassNorth = ColorTokens.CompassNorth,
    compassSouth = ColorTokens.CompassSouth,
    mapPulse = ColorTokens.MapPulse,
)

/**
 * Dark semantic colors — derived, review on device.
 * Hero gradient and pins are unchanged from light.
 */
val DarkAppColors = AppColors(
    primary = ColorTokens.PrimaryDark,
    primaryContainer = ColorTokens.PrimaryContainerDark,
    textPrimary = ColorTokens.OnSurfaceDark,
    textSecondary = ColorTokens.OnSurfaceVariantDark,
    textMuted = ColorTokens.OnSurfaceVariantDark,
    textOnPrimary = ColorTokens.OnPrimaryDark,
    screenTintStart = ColorTokens.BackgroundDark,
    screenTintEnd = ColorTokens.BackgroundDark,
    navBar = ColorTokens.NavBarDark,
    navIndicator = ColorTokens.NavIndicatorDark,
    outlineDefault = ColorTokens.OutlineDark,
    cardBorder = ColorTokens.CardBorderDark,
    tileBorder = ColorTokens.CardBorderDark,
    heroGradientStart = ColorTokens.HeroGradientStart,
    heroGradientMid = ColorTokens.HeroGradientMid,
    heroGradientEnd = ColorTokens.HeroGradientEnd,
    connectedChipBg = ColorTokens.ConnectedChipBg,
    connectedChipBorder = ColorTokens.ConnectedChipContent,
    connectedChipText = ColorTokens.ConnectedChipContent,
    offlineChipBg = ColorTokens.OfflineChipBg,
    offlineChipBorder = ColorTokens.OfflineChipContent,
    offlineChipText = ColorTokens.OfflineChipContent,
    countryBadgeBg = ColorTokens.CountryBadgeBg,
    countryBadgeText = ColorTokens.TextOnPrimary,
    proBadge = ColorTokens.ProBadge,
    proBadgeBg = ColorTokens.ProBadgeBg, // VERIFY against Figma
    successChipBg = ColorTokens.SuccessChipBg, // VERIFY against Figma
    successChipText = ColorTokens.SuccessChipText, // VERIFY against Figma
    voiceNavigation = darkIconTint(ColorTokens.VoiceNavigationBg),
    myLocation = darkIconTint(ColorTokens.MyLocationBg),
    satelliteView = darkIconTint(ColorTokens.SatelliteViewBg),
    gpsCamera = darkIconTint(ColorTokens.GpsCameraBg),
    hotels = darkIconTint(ColorTokens.HotelsBg, ColorTokens.HotelsBorder),
    cafes = darkIconTint(ColorTokens.CafesBg, ColorTokens.CafesBorder),
    banks = darkIconTint(ColorTokens.BanksBg, ColorTokens.BanksBorder),
    hospitals = darkIconTint(ColorTokens.HospitalsBg, ColorTokens.HospitalsBorder),
    compass = darkIconTint(ColorTokens.CompassBg, ColorTokens.CompassBorder),
    famousPlaces = darkIconTint(ColorTokens.FamousPlacesBg, ColorTokens.FamousPlacesBorder),
    countryCode = darkIconTint(ColorTokens.CountryCodeBg),
    countryInfo = darkIconTint(ColorTokens.FamousPlacesBg, ColorTokens.FamousPlacesBorder),
    weather = darkIconTint(ColorTokens.WeatherBg, ColorTokens.WeatherBorder),
    emergency = darkIconTint(ColorTokens.EmergencyBg),
    mapControlSurface = ColorTokens.SurfaceDark,
    mapControlIcon = ColorTokens.OnSurfaceDark,
    mapControlDivider = ColorTokens.CardBorderDark,
    mapPullHandle = ColorTokens.OutlineDark,
    mapActionShare = ColorTokens.MapActionShare,
    mapActionCopy = ColorTokens.MapActionCopy,
    mapActionIcon = ColorTokens.TextOnPrimary,
    mapTooltipBg = ColorTokens.MapTooltipBg,
    mapTooltipText = ColorTokens.TextOnPrimary,
    mapStylePillBg = ColorTokens.NavBarDark,
    mapStyleInactiveText = ColorTokens.OnSurfaceVariantDark,
    compassNorth = ColorTokens.CompassNorth,
    compassSouth = ColorTokens.CompassSouth,
    mapPulse = ColorTokens.MapPulse,
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
