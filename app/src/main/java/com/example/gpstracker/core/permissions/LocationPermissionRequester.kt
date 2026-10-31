package com.example.gpstracker.core.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

fun Context.isPermissionGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun Activity.shouldShowLocationRationale(): Boolean =
    ActivityCompat.shouldShowRequestPermissionRationale(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) || ActivityCompat.shouldShowRequestPermissionRationale(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

fun Context.currentLocationPermissionState(hasAsked: Boolean): LocationPermissionState {
    val fine = isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
    val coarse = isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
    val activity = this as? Activity
    val showRationale = activity?.shouldShowLocationRationale() ?: false
    return resolveLocationPermissionState(
        fineGranted = fine,
        coarseGranted = coarse,
        shouldShowRationale = showRationale,
        hasAsked = hasAsked,
    )
}

fun Context.openApplicationDetailsSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

/**
 * Compose helper around [ActivityResultContracts.RequestMultiplePermissions].
 * Marks the DataStore "has asked" flag when a request is launched.
 */
@Composable
fun rememberLocationPermissionLauncher(
    onMarkAsked: () -> Unit,
    onResult: (LocationPermissionState) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            context.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            context.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        val activity = context as? Activity
        val showRationale = activity?.shouldShowLocationRationale() ?: false
        onResult(
            resolveLocationPermissionState(
                fineGranted = fine,
                coarseGranted = coarse,
                shouldShowRationale = showRationale,
                hasAsked = true,
            ),
        )
    }

    return remember(launcher, onMarkAsked) {
        {
            onMarkAsked()
            launcher.launch(LOCATION_PERMISSIONS)
        }
    }
}
