package com.example.gpstracker.core.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes.
 */
sealed interface Route {
    @Serializable
    data object Consent : Route

    @Serializable
    data object Language : Route

    /** Language picker opened from Settings (back saves and returns). */
    @Serializable
    data object LanguageSettings : Route

    @Serializable
    data object Onboarding : Route

    @Serializable
    data object Home : Route

    @Serializable
    data object Saved : Route

    @Serializable
    data class SavedPlaceDetail(val placeId: Long) : Route

    @Serializable
    data object History : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object Paywall : Route

    @Serializable
    data object MyLocation : Route

    @Serializable
    data object SatelliteMap : Route

    @Serializable
    data object GpsCamera : Route

    /** Nearby Places category grid (Figma near places / See More). */
    @Serializable
    data object NearbyPlaces : Route

    /** Nearby Search results for a [NearbyCategory.id]. */
    @Serializable
    data class NearbyResults(val categoryId: String) : Route

    @Serializable
    data object Compass : Route

    @Serializable
    data object Weather : Route

    @Serializable
    data object CountryCode : Route

    @Serializable
    data object CountryInfo : Route

    @Serializable
    data class CountryDetail(val iso: String) : Route

    @Serializable
    data object FamousPlaces : Route

    @Serializable
    data object Emergency : Route

    @Serializable
    data object RouteFinder : Route

    /** Speech-to-destination. Listening, then Voice Search Results. */
    @Serializable
    data object Voice : Route

    /** Existing Find Location screen, used to pick one via for voice results. */
    @Serializable
    data object PickStop : Route

    /** Route preview + Maps hand-off. Optional via is empty when unused. */
    @Serializable
    data class RoutePreview(
        val destName: String,
        val destLatitude: Double,
        val destLongitude: Double,
        val destAddress: String = "",
        val destPlaceId: String = "",
        val viaName: String = "",
        val viaPlaceId: String = "",
        val viaLatitude: Double? = null,
        val viaLongitude: Double? = null,
    ) : Route

    /** DEBUG-ONLY deep link target (`gpstracker://location-test`). */
    @Serializable
    data object LocationTest : Route
}

enum class TopLevelDestination(
    val route: Route,
    val labelRes: Int,
    val iconContentDescriptionRes: Int,
) {
    Home(
        route = Route.Home,
        labelRes = com.example.gpstracker.R.string.tab_home,
        iconContentDescriptionRes = com.example.gpstracker.R.string.cd_tab_home,
    ),
    Saved(
        route = Route.Saved,
        labelRes = com.example.gpstracker.R.string.tab_saved,
        iconContentDescriptionRes = com.example.gpstracker.R.string.cd_tab_saved,
    ),
    History(
        route = Route.History,
        labelRes = com.example.gpstracker.R.string.tab_history,
        iconContentDescriptionRes = com.example.gpstracker.R.string.cd_tab_history,
    ),
    Settings(
        route = Route.Settings,
        labelRes = com.example.gpstracker.R.string.tab_settings,
        iconContentDescriptionRes = com.example.gpstracker.R.string.cd_tab_settings,
    ),
}
