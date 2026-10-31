package com.example.gpstracker.core.location.gate

import com.example.gpstracker.core.permissions.LocationPermissionState
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationGatePhaseTest {

    @Test
    fun `network required and offline wins first`() {
        val phase = resolveLocationGatePhase(
            requiresNetwork = true,
            isConnected = false,
            permission = LocationPermissionState.Granted(isPrecise = true),
            isLocationEnabled = true,
        )
        assertEquals(LocationGatePhase.NeedsNetwork, phase)
    }

    @Test
    fun `not determined shows permission before gps`() {
        val phase = resolveLocationGatePhase(
            requiresNetwork = false,
            isConnected = true,
            permission = LocationPermissionState.NotDetermined,
            isLocationEnabled = false,
        )
        assertEquals(LocationGatePhase.NeedsPermission, phase)
    }

    @Test
    fun `denied can ask again shows permission`() {
        val phase = resolveLocationGatePhase(
            requiresNetwork = false,
            isConnected = null,
            permission = LocationPermissionState.Denied,
            isLocationEnabled = true,
        )
        assertEquals(LocationGatePhase.NeedsPermission, phase)
    }

    @Test
    fun `permanently denied before gps check`() {
        val phase = resolveLocationGatePhase(
            requiresNetwork = false,
            isConnected = true,
            permission = LocationPermissionState.PermanentlyDenied,
            isLocationEnabled = false,
        )
        assertEquals(LocationGatePhase.PermanentlyDenied, phase)
    }

    @Test
    fun `granted but gps off`() {
        val phase = resolveLocationGatePhase(
            requiresNetwork = false,
            isConnected = true,
            permission = LocationPermissionState.Granted(isPrecise = false),
            isLocationEnabled = false,
        )
        assertEquals(LocationGatePhase.GpsOff, phase)
    }

    @Test
    fun `all clear is ready`() {
        val phase = resolveLocationGatePhase(
            requiresNetwork = true,
            isConnected = true,
            permission = LocationPermissionState.Granted(isPrecise = true),
            isLocationEnabled = true,
        )
        assertEquals(LocationGatePhase.Ready, phase)
    }

    @Test
    fun `waiting on permission is checking`() {
        val phase = resolveLocationGatePhase(
            requiresNetwork = false,
            isConnected = true,
            permission = null,
            isLocationEnabled = true,
        )
        assertEquals(LocationGatePhase.Checking, phase)
    }

    @Test
    fun `waiting on network when required is checking`() {
        val phase = resolveLocationGatePhase(
            requiresNetwork = true,
            isConnected = null,
            permission = LocationPermissionState.Granted(isPrecise = true),
            isLocationEnabled = true,
        )
        assertEquals(LocationGatePhase.Checking, phase)
    }
}
