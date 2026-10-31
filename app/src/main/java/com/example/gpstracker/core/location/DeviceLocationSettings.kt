package com.example.gpstracker.core.location

import android.content.Context
import android.content.IntentSender
import android.location.LocationManager
import android.os.Build
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.getSystemService
import androidx.core.location.LocationManagerCompat
import com.example.gpstracker.core.common.DispatcherProvider
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed interface LocationSettingsResolution {
    data object Satisfied : LocationSettingsResolution
    data class Resolvable(val intentSender: IntentSender) : LocationSettingsResolution
    data object Unresolvable : LocationSettingsResolution
}

interface DeviceLocationSettings {
    /** Emits whether device location (GPS/network providers) is enabled. */
    val isLocationEnabled: Flow<Boolean>

    /** Checks high-accuracy settings; may return a resolvable system dialog. */
    suspend fun checkLocationSettings(): LocationSettingsResolution
}

fun LocationSettingsResolution.toIntentSenderRequestOrNull(): IntentSenderRequest? =
    when (this) {
        is LocationSettingsResolution.Resolvable ->
            IntentSenderRequest.Builder(intentSender).build()
        else -> null
    }

@Singleton
class DeviceLocationSettingsImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider,
) : DeviceLocationSettings {

    private val settingsClient = LocationServices.getSettingsClient(context)

    override val isLocationEnabled: Flow<Boolean> = callbackFlow {
        val locationManager = context.getSystemService<LocationManager>()
        if (locationManager == null) {
            trySend(false)
            close()
            return@callbackFlow
        }

        fun emitCurrent() {
            trySend(LocationManagerCompat.isLocationEnabled(locationManager))
        }

        emitCurrent()

        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: android.content.Intent?) {
                emitCurrent()
            }
        }
        val filter = android.content.IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION).apply {
            addAction(LocationManager.MODE_CHANGED_ACTION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter)
        }

        awaitClose { context.unregisterReceiver(receiver) }
    }
        .distinctUntilChanged()
        .flowOn(dispatchers.io)

    override suspend fun checkLocationSettings(): LocationSettingsResolution =
        withContext(dispatchers.io) {
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                LocationRepository.DEFAULT_UPDATE_INTERVAL_MS,
            ).build()
            val settingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(request)
                .setAlwaysShow(true)
                .build()
            try {
                settingsClient.checkLocationSettings(settingsRequest).awaitTask()
                LocationSettingsResolution.Satisfied
            } catch (e: ResolvableApiException) {
                LocationSettingsResolution.Resolvable(e.resolution.intentSender)
            } catch (_: Exception) {
                LocationSettingsResolution.Unresolvable
            }
        }
}
