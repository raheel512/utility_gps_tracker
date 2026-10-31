package com.example.gpstracker.core.navigation

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.theme.LocalAppColors

/**
 * Material 3 [NavigationBar] for top-level destinations, using the same
 * Figma-exported nav icons (`ic_nav_*`) and app color tokens.
 */
@Composable
fun AppBottomBar(
    currentDestination: TopLevelDestination?,
    onNavigate: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val appColors = LocalAppColors.current
    NavigationBar(
        modifier = modifier,
        containerColor = appColors.navBar,
        tonalElevation = 0.dp,
    ) {
        TopLevelDestination.entries.forEach { destination ->
            val selected = destination == currentDestination
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        painter = painterResource(destination.iconRes()),
                        contentDescription = stringResource(destination.iconContentDescriptionRes),
                    )
                },
                label = {
                    Text(
                        text = stringResource(destination.labelRes),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        softWrap = false,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = appColors.primary,
                    selectedTextColor = appColors.primary,
                    indicatorColor = appColors.navIndicator,
                    unselectedIconColor = appColors.textSecondary,
                    unselectedTextColor = appColors.textSecondary,
                ),
            )
        }
    }
}

@DrawableRes
private fun TopLevelDestination.iconRes(): Int = when (this) {
    TopLevelDestination.Home -> R.drawable.ic_nav_home
    TopLevelDestination.Saved -> R.drawable.ic_nav_saved
    TopLevelDestination.History -> R.drawable.ic_nav_history
    TopLevelDestination.Settings -> R.drawable.ic_nav_settings
}
