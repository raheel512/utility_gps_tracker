package com.example.gpstracker.core.permissions

import org.junit.Assert.assertEquals
import org.junit.Test

class MicrophonePermissionStateTest {
    @Test
    fun `granted wins`() {
        assertEquals(
            MicrophonePermissionState.Granted,
            resolveMicrophonePermissionState(
                granted = true,
                shouldShowRationale = false,
                hasAsked = false,
            ),
        )
    }

    @Test
    fun `first visit is not determined`() {
        assertEquals(
            MicrophonePermissionState.NotDetermined,
            resolveMicrophonePermissionState(
                granted = false,
                shouldShowRationale = false,
                hasAsked = false,
            ),
        )
    }

    @Test
    fun `asked and rationale is denied`() {
        assertEquals(
            MicrophonePermissionState.Denied,
            resolveMicrophonePermissionState(
                granted = false,
                shouldShowRationale = true,
                hasAsked = true,
            ),
        )
    }

    @Test
    fun `asked without rationale is permanent`() {
        assertEquals(
            MicrophonePermissionState.PermanentlyDenied,
            resolveMicrophonePermissionState(
                granted = false,
                shouldShowRationale = false,
                hasAsked = true,
            ),
        )
    }
}
