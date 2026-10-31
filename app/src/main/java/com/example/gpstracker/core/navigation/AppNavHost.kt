package com.example.gpstracker.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.example.gpstracker.BuildConfig
import com.example.gpstracker.core.common.AppConfig
import com.example.gpstracker.core.common.ObserveAsEvents
import com.example.gpstracker.feature.camera.ui.GpsCameraRoute
import com.example.gpstracker.feature.compass.ui.CompassRoute
import androidx.navigation.toRoute
import com.example.gpstracker.feature.country.ui.CountryCodeScreen
import com.example.gpstracker.feature.country.ui.CountryDetailScreen
import com.example.gpstracker.feature.country.ui.CountryInfoScreen
import com.example.gpstracker.feature.emergency.ui.EmergencyScreen
import com.example.gpstracker.feature.history.ui.HistoryRoute
import com.example.gpstracker.feature.search.ui.FindLocationRoute
import com.example.gpstracker.feature.voice.ui.VIA_ADDRESS
import com.example.gpstracker.feature.voice.ui.VIA_LAT
import com.example.gpstracker.feature.voice.ui.VIA_LNG
import com.example.gpstracker.feature.voice.ui.VIA_NAME
import com.example.gpstracker.feature.voice.ui.VIA_PLACE_ID
import com.example.gpstracker.feature.voice.ui.VoiceRoute
import com.example.gpstracker.feature.routes.ui.RoutePreviewRoute
import com.example.gpstracker.feature.home.ui.HomeRoute
import com.example.gpstracker.feature.map.mylocation.MyLocationRoute
import com.example.gpstracker.feature.map.satellite.SatelliteRoute
import com.example.gpstracker.feature.map.ui.RouteFinderScreen
import com.example.gpstracker.feature.onboarding.ui.ConsentRoute
import com.example.gpstracker.feature.onboarding.ui.LanguageRoute
import com.example.gpstracker.feature.onboarding.ui.OnboardingRoute
import com.example.gpstracker.feature.debug.LocationTestRoute
import com.example.gpstracker.feature.paywall.ui.PaywallRoute
import com.example.gpstracker.feature.places.nearby.NearbyResultsRoute
import com.example.gpstracker.feature.places.ui.FamousPlacesScreen
import com.example.gpstracker.feature.places.ui.NearbyPlacesScreen
import com.example.gpstracker.feature.saved.ui.SavedPlaceDetailRoute
import com.example.gpstracker.feature.saved.ui.SavedRoute
import com.example.gpstracker.feature.settings.ui.SettingsRoute
import com.example.gpstracker.feature.weather.ui.WeatherRoute

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: AppNavViewModel = hiltViewModel(),
) {
    val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = TopLevelDestination.entries.any { dest ->
        currentDestination?.hasRoute(dest.route::class) == true
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            AppNavEvent.GoLanguage -> navController.navigate(Route.Language)
        }
    }

    when (val start = startDestination) {
        null -> {
            // System splash stays until DataStore resolves; avoid flashing Consent/Home.
            Box(modifier = modifier.fillMaxSize())
        }
        else -> {
            key(start) {
                Scaffold(
                    modifier = modifier,
                    // Top-level tabs: exclude top insets so AppTopBar(statusBarAware)
                    // can paint its container (e.g. Home light blue) behind the
                    // status bar. Detail screens keep full safeDrawing; their
                    // AppTopBar uses zero insets to avoid a double gap.
                    // Bottom bar owns navigationBarsPadding — exclude bottom here.
                    // Full-bleed screens (GPS Camera) draw behind system bars and pad
                    // their own controls with safeDrawing.
                    contentWindowInsets = when {
                        currentDestination?.hasRoute(Route.GpsCamera::class) == true ->
                            WindowInsets(0, 0, 0, 0)
                        currentDestination?.hasRoute(Route.Weather::class) == true ||
                            currentDestination?.hasRoute(Route.RouteFinder::class) == true ||
                            currentDestination?.hasRoute(Route.PickStop::class) == true ->
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
                            )
                        showBottomBar -> WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
                        else -> WindowInsets.safeDrawing
                    },
                    bottomBar = {
                        if (showBottomBar) {
                            val selected = TopLevelDestination.entries.firstOrNull { dest ->
                                currentDestination?.hasRoute(dest.route::class) == true
                            }
                            AppBottomBar(
                                currentDestination = selected,
                                onNavigate = { destination ->
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            )
                        }
                    },
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = when (start) {
                            StartDestination.Home -> Route.Home
                            StartDestination.Consent -> Route.Consent
                        },
                        modifier = Modifier.padding(innerPadding),
                        enterTransition = { appEnterTransition() },
                        exitTransition = { appExitTransition() },
                        popEnterTransition = { appPopEnterTransition() },
                        popExitTransition = { appPopExitTransition() },
                        sizeTransform = { null },
                    ) {
                        composable<Route.Consent> {
                            ConsentRoute(onContinue = viewModel::onConsentContinue)
                        }
                        composable<Route.Language> {
                            LanguageRoute(
                                onContinue = {
                                    // Navigate synchronously before locale recreate.
                                    navController.navigate(Route.Onboarding)
                                },
                            )
                        }
                        composable<Route.LanguageSettings> {
                            LanguageRoute(
                                fromSettings = true,
                                onContinue = { navController.popBackStack() },
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }
                        composable<Route.Onboarding> {
                            OnboardingRoute(
                                onFinished = {
                                    if (AppConfig.SHOW_PAYWALL_AFTER_ONBOARDING) {
                                        navController.navigate(Route.Paywall) {
                                            popUpTo(Route.Consent) { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate(Route.Home) {
                                            popUpTo(Route.Consent) { inclusive = true }
                                        }
                                    }
                                },
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }
                        composable<Route.Home> {
                            HomeRoute(
                                onNavigate = { route -> navController.navigate(route) },
                            )
                        }
                        composable<Route.Saved> {
                            SavedRoute(
                                onOpenPlace = { placeId ->
                                    navController.navigate(Route.SavedPlaceDetail(placeId))
                                },
                                onOpenRoutePreview = { preview -> navController.navigate(preview) },
                            )
                        }
                        composable<Route.SavedPlaceDetail> {
                            SavedPlaceDetailRoute(
                                onNavigateUp = { navController.popBackStack() },
                                onOpenRoutePreview = { preview -> navController.navigate(preview) },
                            )
                        }
                        composable<Route.History> {
                            HistoryRoute(
                                onOpenRoutePreview = { preview -> navController.navigate(preview) },
                            )
                        }
                        composable<Route.Settings> {
                            SettingsRoute(
                                onNavigateToLanguage = {
                                    navController.navigate(Route.LanguageSettings)
                                },
                            )
                        }
                        composable<Route.Paywall>(
                            deepLinks = if (BuildConfig.DEBUG) {
                                listOf(
                                    navDeepLink { uriPattern = "gpstracker://paywall" },
                                )
                            } else {
                                emptyList()
                            },
                        ) {
                            PaywallRoute(onNavigateUp = { navController.popBackStack() })
                        }
                        composable<Route.MyLocation> {
                            MyLocationRoute(onNavigateUp = { navController.popBackStack() })
                        }
                        composable<Route.SatelliteMap> {
                            SatelliteRoute(onNavigateUp = { navController.popBackStack() })
                        }
                        composable<Route.GpsCamera> {
                            GpsCameraRoute(onNavigateUp = { navController.popBackStack() })
                        }
                        composable<Route.NearbyPlaces> {
                            NearbyPlacesScreen(
                                onNavigateUp = { navController.popBackStack() },
                            )
                        }
                        composable<Route.NearbyResults> {
                            NearbyResultsRoute(
                                onNavigateUp = { navController.popBackStack() },
                                onOpenRoutePreview = { preview -> navController.navigate(preview) },
                            )
                        }
                        composable<Route.Compass> {
                            CompassRoute(
                                onNavigateUp = { navController.popBackStack() },
                            )
                        }
                        composable<Route.Weather> {
                            WeatherRoute(onNavigateUp = { navController.popBackStack() })
                        }
                        composable<Route.CountryCode> {
                            CountryCodeScreen(onNavigateUp = { navController.popBackStack() })
                        }
                        composable<Route.CountryInfo> {
                            CountryInfoScreen(
                                onNavigateUp = { navController.popBackStack() },
                                onOpenCountry = { iso ->
                                    navController.navigate(Route.CountryDetail(iso))
                                },
                            )
                        }
                        composable<Route.CountryDetail> { entry ->
                            val iso = entry.toRoute<Route.CountryDetail>().iso
                            CountryDetailScreen(
                                iso = iso,
                                onNavigateUp = { navController.popBackStack() },
                            )
                        }
                        composable<Route.FamousPlaces> {
                            FamousPlacesScreen(onNavigateUp = { navController.popBackStack() })
                        }
                        composable<Route.Emergency> {
                            EmergencyScreen(onNavigateUp = { navController.popBackStack() })
                        }
                        composable<Route.RouteFinder> { entry ->
                            RouteFinderScreen(
                                onNavigateUp = { navController.popBackStack() },
                                onOpenRoutePreview = { preview -> navController.navigate(preview) },
                                onPickStop = { navController.navigate(Route.PickStop) },
                                savedStateHandle = entry.savedStateHandle,
                            )
                        }
                        composable<Route.Voice> { entry ->
                            VoiceRoute(
                                onNavigateUp = { navController.popBackStack() },
                                onOpenRoutePreview = { preview -> navController.navigate(preview) },
                                onPickStop = { navController.navigate(Route.PickStop) },
                                savedStateHandle = entry.savedStateHandle,
                            )
                        }
                        composable<Route.PickStop> { entry ->
                            FindLocationRoute(
                                onNavigateUp = { navController.popBackStack() },
                                savedStateHandle = entry.savedStateHandle,
                                onOpenRoutePreview = { preview ->
                                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                                        set(VIA_NAME, preview.destName)
                                        set(VIA_PLACE_ID, preview.destPlaceId)
                                        set(VIA_ADDRESS, preview.destAddress)
                                        set(VIA_LAT, preview.destLatitude)
                                        set(VIA_LNG, preview.destLongitude)
                                    }
                                    navController.popBackStack()
                                },
                            )
                        }
                        composable<Route.RoutePreview> {
                            RoutePreviewRoute(onNavigateUp = { navController.popBackStack() })
                        }
                        composable<Route.LocationTest>(
                            deepLinks = if (BuildConfig.DEBUG) {
                                listOf(
                                    navDeepLink { uriPattern = "gpstracker://location-test" },
                                )
                            } else {
                                emptyList()
                            },
                        ) {
                            LocationTestRoute(onNavigateUp = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
