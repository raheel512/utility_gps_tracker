package com.example.gpstracker.feature.home.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.component.AppCard
import com.example.gpstracker.core.designsystem.component.AppTopBar
import com.example.gpstracker.core.designsystem.component.ConnectivityChip
import com.example.gpstracker.core.designsystem.component.CountryBadge
import com.example.gpstracker.core.designsystem.component.FeatureTile
import com.example.gpstracker.core.designsystem.component.GpsToolCard
import com.example.gpstracker.core.designsystem.component.HeroCard
import com.example.gpstracker.core.designsystem.component.NearbyCategoryItem
import com.example.gpstracker.core.designsystem.component.SectionHeader
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.IconContainerTint
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.LocalSpacing

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current
    val screenBrush = Brush.verticalGradient(
        listOf(appColors.screenTintStart, appColors.screenTintEnd),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(screenBrush),
    ) {
        AppTopBar(
            title = stringResource(R.string.home_app_bar_title),
            // design.md screenTint + screenshot: light blue header, not white.
            containerColor = appColors.screenTintStart,
            statusBarAware = true,
            actions = {
                val connected = uiState.connected
                ConnectivityChip(
                    connected = connected,
                    label = stringResource(
                        if (connected) R.string.home_chip_connected else R.string.home_chip_offline,
                    ),
                    contentDescription = stringResource(
                        if (connected) R.string.cd_connectivity_connected else R.string.cd_connectivity_offline,
                    ),
                    modifier = Modifier.padding(end = spacing.xs),
                )
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.contentSide)
                .padding(top = spacing.md, bottom = spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            HeroSection(onEvent = onEvent)
            GpsToolsSection(onEvent = onEvent)
            NearbySection(onEvent = onEvent)
            OtherFeatureSection(onEvent = onEvent)
        }
    }
}

@Composable
private fun HeroSection(onEvent: (HomeEvent) -> Unit) {
    HeroCard(
        title = stringResource(R.string.home_hero_title),
        subtitle = stringResource(R.string.home_hero_subtitle),
        ctaLabel = stringResource(R.string.home_hero_cta),
        ctaContentDescription = stringResource(R.string.cd_home_hero_cta),
        onCtaClick = { onEvent(HomeEvent.Select(HomeTarget.RouteFinder)) },
        illustrationRes = R.drawable.ic_home_route_illustration,
        illustrationContentDescription = stringResource(R.string.cd_home_hero_illustration),
    )
}

@Composable
private fun GpsToolsSection(onEvent: (HomeEvent) -> Unit) {
    val spacing = LocalSpacing.current
    Column(verticalArrangement = Arrangement.spacedBy(spacing.sectionLabelGap)) {
        SectionHeader(label = stringResource(R.string.home_section_gps_tools))
        Column(verticalArrangement = Arrangement.spacedBy(spacing.gridGap)) {
            GpsTools.chunked(2).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.gridGap)) {
                    rowItems.forEach { item ->
                        GpsToolCard(
                            label = stringResource(item.labelRes),
                            tint = tintFor(item.target),
                            drawableRes = item.drawableRes,
                            contentDescription = stringResource(item.labelRes),
                            onClick = { onEvent(HomeEvent.Select(item.target)) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NearbySection(onEvent: (HomeEvent) -> Unit) {
    val spacing = LocalSpacing.current
    Column(verticalArrangement = Arrangement.spacedBy(spacing.sectionLabelGap)) {
        SectionHeader(
            label = stringResource(R.string.home_section_nearby),
            actionLabel = stringResource(R.string.home_see_more),
            onActionClick = { onEvent(HomeEvent.Select(HomeTarget.NearbySeeMore)) },
        )
        AppCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.categoryGap),
            ) {
                NearbyCategories.forEach { item ->
                    NearbyCategoryItem(
                        label = stringResource(item.labelRes),
                        tint = tintFor(item.target),
                        drawableRes = item.drawableRes,
                        contentDescription = stringResource(item.labelRes),
                        onClick = { onEvent(HomeEvent.Select(item.target)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun OtherFeatureSection(onEvent: (HomeEvent) -> Unit) {
    val spacing = LocalSpacing.current
    Column(verticalArrangement = Arrangement.spacedBy(spacing.sectionLabelGap)) {
        SectionHeader(label = stringResource(R.string.home_section_other))
        Column(verticalArrangement = Arrangement.spacedBy(spacing.tileGap)) {
            OtherFeatures.chunked(3).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.tileGap)) {
                    rowItems.forEach { item ->
                        FeatureTile(
                            label = stringResource(item.labelRes),
                            tint = tintFor(item.target),
                            drawableRes = item.drawableRes,
                            contentDescription = stringResource(item.labelRes),
                            onClick = { onEvent(HomeEvent.Select(item.target)) },
                            modifier = Modifier.weight(1f),
                            badge = if (item.target == HomeTarget.CountryCode) {
                                { CountryBadge(text = stringResource(R.string.home_country_code_badge)) }
                            } else {
                                null
                            },
                            emoji = if (item.target == HomeTarget.CountryCode) {
                                stringResource(R.string.home_country_code_flag)
                            } else {
                                null
                            },
                        )
                    }
                }
            }
        }
    }
}

private data class HomeItem(
    val target: HomeTarget,
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val drawableRes: Int?,
)

private val GpsTools = listOf(
    HomeItem(HomeTarget.VoiceNavigation, R.string.home_tool_voice_navigation, R.drawable.ic_feature_voice_navigation),
    HomeItem(HomeTarget.MyLocation, R.string.home_tool_my_location, R.drawable.ic_feature_my_location),
    HomeItem(HomeTarget.SatelliteView, R.string.home_tool_satellite_view, R.drawable.ic_feature_satellite_view),
    HomeItem(HomeTarget.GpsCamera, R.string.home_tool_gps_camera, R.drawable.ic_feature_gps_camera),
)

private val NearbyCategories = listOf(
    HomeItem(HomeTarget.NearbyHotels, R.string.home_nearby_hotels, R.drawable.ic_feature_hotels),
    HomeItem(HomeTarget.NearbyCafes, R.string.home_nearby_cafes, R.drawable.ic_feature_cafes),
    HomeItem(HomeTarget.NearbyBanks, R.string.home_nearby_banks, R.drawable.ic_feature_banks),
    HomeItem(HomeTarget.NearbyHospitals, R.string.home_nearby_hospitals, R.drawable.ic_feature_hospitals),
)

private val OtherFeatures = listOf(
    HomeItem(HomeTarget.Compass, R.string.home_feature_compass, R.drawable.ic_feature_compass),
    HomeItem(HomeTarget.FamousPlaces, R.string.home_feature_famous_places, R.drawable.ic_feature_famous_places),
    HomeItem(HomeTarget.CountryCode, R.string.home_feature_country_code, null),
    HomeItem(HomeTarget.CountryInfo, R.string.home_feature_country_info, R.drawable.ic_feature_country_info),
    HomeItem(HomeTarget.Weather, R.string.home_feature_weather, R.drawable.ic_feature_weather),
    HomeItem(HomeTarget.Emergency, R.string.home_feature_emergency, R.drawable.ic_feature_emergency),
)

@Composable
private fun tintFor(target: HomeTarget): IconContainerTint {
    val c = LocalAppColors.current
    return when (target) {
        HomeTarget.VoiceNavigation -> c.voiceNavigation
        HomeTarget.MyLocation -> c.myLocation
        HomeTarget.SatelliteView -> c.satelliteView
        HomeTarget.GpsCamera -> c.gpsCamera
        HomeTarget.NearbyHotels -> c.hotels
        HomeTarget.NearbyCafes -> c.cafes
        HomeTarget.NearbyBanks -> c.banks
        HomeTarget.NearbyHospitals -> c.hospitals
        HomeTarget.Compass -> c.compass
        HomeTarget.FamousPlaces -> c.famousPlaces
        HomeTarget.CountryCode -> c.countryCode
        HomeTarget.CountryInfo -> c.countryInfo
        HomeTarget.Weather -> c.weather
        HomeTarget.Emergency -> c.emergency
        HomeTarget.RouteFinder, HomeTarget.NearbySeeMore -> c.myLocation
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun HomeScreenConnectedPreview() {
    AppTheme {
        HomeScreen(uiState = HomeUiState(connected = true), onEvent = {})
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun HomeScreenOfflinePreview() {
    AppTheme {
        HomeScreen(uiState = HomeUiState(connected = false), onEvent = {})
    }
}
