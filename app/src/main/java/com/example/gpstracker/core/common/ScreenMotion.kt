package com.example.gpstracker.core.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.delay

/**
 * Durations for screen changes. Push matches [rememberContentReady] so a
 * destination can draw a light placeholder while it slides in, then start
 * heavier work after the animation.
 */
object ScreenMotion {
    const val PUSH_MS = 250
    const val POP_MS = 220
    /** Bottom-tab swipe; kept short so maps/lists stay responsive. */
    const val TAB_MS = 240
}

/**
 * Becomes true after the navigation push has had a frame to start and the
 * push duration has elapsed. Waiting here does not block the main thread.
 */
@Composable
fun rememberContentReady(): Boolean {
    var ready by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        withFrameNanos { }
        delay(ScreenMotion.PUSH_MS.toLong())
        ready = true
    }
    return ready
}
