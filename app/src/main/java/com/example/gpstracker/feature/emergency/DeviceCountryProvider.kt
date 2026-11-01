package com.example.gpstracker.feature.emergency

import android.content.Context
import android.telephony.TelephonyManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Best-effort country ISO without blocking. Prefers the cellular network,
 * then SIM, then the device locale. Never throws.
 */
@Singleton
class DeviceCountryProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun isoHint(): String? {
        return try {
            val telephony = context.getSystemService(TelephonyManager::class.java)
            normalize(telephony?.networkCountryIso)
                ?: normalize(telephony?.simCountryIso)
                ?: normalize(Locale.getDefault().country)
        } catch (_: Throwable) {
            normalize(Locale.getDefault().country)
        }
    }

    private fun normalize(raw: String?): String? {
        val code = raw?.trim()?.uppercase().orEmpty()
        return code.takeIf { it.length == 2 && it.all { ch -> ch in 'A'..'Z' } }
    }
}
