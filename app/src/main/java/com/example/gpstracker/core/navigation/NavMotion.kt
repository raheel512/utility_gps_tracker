package com.example.gpstracker.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.example.gpstracker.core.common.ScreenMotion

/**
 * Bottom-tab changes slide in the tab-bar direction (with a light fade).
 * Pushed screens keep the existing start/end slide so stack navigation is unchanged.
 */
internal fun AnimatedContentTransitionScope<NavBackStackEntry>.appEnterTransition(): EnterTransition {
    if (isTabSwitch()) {
        val towards = tabSlideDirection()
        return slideIntoContainer(
            towards = towards,
            animationSpec = tween(ScreenMotion.TAB_MS, easing = FastOutSlowInEasing),
            initialOffset = { it / 5 },
        ) + fadeIn(tween(ScreenMotion.TAB_MS, easing = FastOutSlowInEasing))
    }
    return slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = tween(ScreenMotion.PUSH_MS, easing = FastOutSlowInEasing),
    )
}

internal fun AnimatedContentTransitionScope<NavBackStackEntry>.appExitTransition(): ExitTransition {
    if (isTabSwitch()) {
        val towards = tabSlideDirection()
        return slideOutOfContainer(
            towards = towards,
            animationSpec = tween(ScreenMotion.TAB_MS, easing = FastOutSlowInEasing),
            targetOffset = { it / 5 },
        ) + fadeOut(tween(ScreenMotion.TAB_MS, easing = FastOutSlowInEasing))
    }
    return slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = tween(ScreenMotion.PUSH_MS, easing = FastOutSlowInEasing),
    )
}

internal fun AnimatedContentTransitionScope<NavBackStackEntry>.appPopEnterTransition(): EnterTransition {
    if (isTabSwitch()) {
        val towards = tabSlideDirection()
        return slideIntoContainer(
            towards = towards,
            animationSpec = tween(ScreenMotion.TAB_MS, easing = FastOutSlowInEasing),
            initialOffset = { it / 5 },
        ) + fadeIn(tween(ScreenMotion.TAB_MS, easing = FastOutSlowInEasing))
    }
    return slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = tween(ScreenMotion.POP_MS, easing = FastOutSlowInEasing),
    )
}

internal fun AnimatedContentTransitionScope<NavBackStackEntry>.appPopExitTransition(): ExitTransition {
    if (isTabSwitch()) {
        val towards = tabSlideDirection()
        return slideOutOfContainer(
            towards = towards,
            animationSpec = tween(ScreenMotion.TAB_MS, easing = FastOutSlowInEasing),
            targetOffset = { it / 5 },
        ) + fadeOut(tween(ScreenMotion.TAB_MS, easing = FastOutSlowInEasing))
    }
    return slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = tween(ScreenMotion.POP_MS, easing = FastOutSlowInEasing),
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch(): Boolean {
    val fromTab = tabIndexOf(initialState.destination) >= 0
    val toTab = tabIndexOf(targetState.destination) >= 0
    return fromTab && toTab
}

/**
 * Higher tab index slides toward [SlideDirection.Start] (forward along the bar);
 * lower index slides toward [SlideDirection.End]. Start/End follow RTL.
 */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabSlideDirection():
    AnimatedContentTransitionScope.SlideDirection {
    val from = tabIndexOf(initialState.destination)
    val to = tabIndexOf(targetState.destination)
    return if (to >= from) {
        AnimatedContentTransitionScope.SlideDirection.Start
    } else {
        AnimatedContentTransitionScope.SlideDirection.End
    }
}

private fun tabIndexOf(destination: NavDestination): Int =
    TopLevelDestination.entries.indexOfFirst { dest ->
        destination.hasRoute(dest.route::class)
    }
