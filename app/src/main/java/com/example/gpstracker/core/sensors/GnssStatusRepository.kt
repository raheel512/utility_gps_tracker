package com.example.gpstracker.core.sensors

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

interface GnssStatusRepository {
    /** Satellites used in the current fix while collected; null when unavailable. */
    fun satellitesUsedInFix(): Flow<Int?>
}

@Singleton
class LocationManagerGnssStatusRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : GnssStatusRepository {

    override fun satellitesUsedInFix(): Flow<Int?> = callbackFlow {
        val locationManager = context.getSystemService<LocationManager>()
        if (locationManager == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val callback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                var used = 0
                for (i in 0 until status.satelliteCount) {
                    if (status.usedInFix(i)) used++
                }
                trySend(used)
            }
        }
        try {
            locationManager.registerGnssStatusCallback(
                callback,
                Handler(Looper.getMainLooper()),
            )
        } catch (_: SecurityException) {
            trySend(null)
            close()
            return@callbackFlow
        }
        awaitClose {
            locationManager.unregisterGnssStatusCallback(callback)
        }
    }
}
