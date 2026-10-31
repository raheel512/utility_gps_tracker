package com.example.gpstracker.core.permissions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationPermissionStateTest {

    @Test
    fun `fine grant is precise`() {
        val state = resolveLocationPermissionState(
            fineGranted = true,
            coarseGranted = true,
            shouldShowRationale = false,
            hasAsked = true,
        )
        assertEquals(LocationPermissionState.Granted(isPrecise = true), state)
        assertTrue((state as LocationPermissionState.Granted).isPrecise)
    }

    @Test
    fun `coarse only is approximate grant`() {
        val state = resolveLocationPermissionState(
            fineGranted = false,
            coarseGranted = true,
            shouldShowRationale = false,
            hasAsked = true,
        )
        assertEquals(LocationPermissionState.Granted(isPrecise = false), state)
        assertFalse((state as LocationPermissionState.Granted).isPrecise)
    }

    @Test
    fun `never asked is not determined`() {
        val state = resolveLocationPermissionState(
            fineGranted = false,
            coarseGranted = false,
            shouldShowRationale = false,
            hasAsked = false,
        )
        assertEquals(LocationPermissionState.NotDetermined, state)
    }

    @Test
    fun `asked with rationale is denied can ask again`() {
        val state = resolveLocationPermissionState(
            fineGranted = false,
            coarseGranted = false,
            shouldShowRationale = true,
            hasAsked = true,
        )
        assertEquals(LocationPermissionState.Denied, state)
    }

    @Test
    fun `asked without rationale is permanently denied`() {
        val state = resolveLocationPermissionState(
            fineGranted = false,
            coarseGranted = false,
            shouldShowRationale = false,
            hasAsked = true,
        )
        assertEquals(LocationPermissionState.PermanentlyDenied, state)
    }
}
