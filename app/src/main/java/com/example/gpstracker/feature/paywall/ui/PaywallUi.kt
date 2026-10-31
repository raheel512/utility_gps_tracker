package com.example.gpstracker.feature.paywall.ui

import androidx.annotation.StringRes
import com.example.gpstracker.R

data class PaywallUiState(
    val features: List<PaywallFeature> = defaultPaywallFeatures(),
    val priceText: String? = null,
    val trialText: String? = null,
    val isPurchasing: Boolean = false,
)

data class PaywallFeature(
    @StringRes val titleRes: Int,
    val icon: PaywallFeatureIcon,
    val isPro: Boolean,
)

/**
 * Material icon keys for paywall rows. Mapped to ImageVector + tint containers
 * in the UI layer (tints approximate — VERIFY against Figma).
 */
enum class PaywallFeatureIcon {
    LocationSharing,
    GpsCamera,
    SatelliteMap,
    StreetMap,
    Traffic,
    Compass,
}

sealed interface PaywallEvent {
    data object Close : PaywallEvent
    data object PurchaseClicked : PaywallEvent
    data object RestorePurchases : PaywallEvent
    data object OpenTerms : PaywallEvent
    data object OpenPrivacy : PaywallEvent
}

sealed interface PaywallEffect {
    data object NavigateUp : PaywallEffect
    data object OpenTermsUrl : PaywallEffect
    data object OpenPrivacyUrl : PaywallEffect
}

/**
 * Seeded from the Go PRO screenshot. Several rows (live location sharing, live
 * traffic, 3D street map) will not exist in v1.
 *
 * TODO(content): replace with the final v1 feature list before this screen is
 * reachable by users.
 */
fun defaultPaywallFeatures(): List<PaywallFeature> = listOf(
    PaywallFeature(
        titleRes = R.string.paywall_feature_live_location,
        icon = PaywallFeatureIcon.LocationSharing,
        isPro = false,
    ),
    PaywallFeature(
        titleRes = R.string.paywall_feature_gps_camera,
        icon = PaywallFeatureIcon.GpsCamera,
        isPro = false,
    ),
    PaywallFeature(
        titleRes = R.string.paywall_feature_satellite_map,
        icon = PaywallFeatureIcon.SatelliteMap,
        isPro = true,
    ),
    PaywallFeature(
        titleRes = R.string.paywall_feature_street_map,
        icon = PaywallFeatureIcon.StreetMap,
        isPro = false,
    ),
    PaywallFeature(
        titleRes = R.string.paywall_feature_traffic,
        icon = PaywallFeatureIcon.Traffic,
        isPro = true,
    ),
    PaywallFeature(
        titleRes = R.string.paywall_feature_compass,
        icon = PaywallFeatureIcon.Compass,
        isPro = false,
    ),
)
