package com.example.gpstracker.core.permissions

import org.junit.Assert.assertEquals
import org.junit.Test

class CameraPermissionStateTest {

    @Test
    fun `granted`() {
        assertEquals(
            CameraPermissionState.Granted,
            resolveCameraPermissionState(granted = true, shouldShowRationale = false, hasAsked = true),
        )
    }

    @Test
    fun `not determined when never asked`() {
        assertEquals(
            CameraPermissionState.NotDetermined,
            resolveCameraPermissionState(granted = false, shouldShowRationale = false, hasAsked = false),
        )
    }

    @Test
    fun `denied when rationale shown`() {
        assertEquals(
            CameraPermissionState.Denied,
            resolveCameraPermissionState(granted = false, shouldShowRationale = true, hasAsked = true),
        )
    }

    @Test
    fun `permanently denied`() {
        assertEquals(
            CameraPermissionState.PermanentlyDenied,
            resolveCameraPermissionState(granted = false, shouldShowRationale = false, hasAsked = true),
        )
    }
}
