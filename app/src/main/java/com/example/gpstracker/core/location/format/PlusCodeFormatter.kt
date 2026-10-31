package com.example.gpstracker.core.location.format

import com.google.openlocationcode.OpenLocationCode

/**
 * Thin wrapper around the official Open Location Code library.
 *
 * The My Location design shows an 11-digit code with its first 4 digits dropped
 * ("M2XP+RFW") followed by the address, the way Google Maps pairs a short code with a locality.
 */
object PlusCodeFormatter {

    /** "M2XP+RFW" has 3 characters after "+", i.e. 11 significant digits. */
    const val DISPLAY_CODE_LENGTH = 11

    private const val LOCALITY_PREFIX_LENGTH = 4

    fun fullCode(
        latitude: Double,
        longitude: Double,
        codeLength: Int = DISPLAY_CODE_LENGTH,
    ): String = OpenLocationCode(latitude, longitude, codeLength).code

    /**
     * Drops the first 4 digits, but only when the short code recovers to the same full code
     * against [referenceLatitude]/[referenceLongitude]. Returns null when it cannot be shortened safely.
     */
    fun localityShortCode(
        fullCode: String,
        referenceLatitude: Double,
        referenceLongitude: Double,
    ): String? {
        if (!OpenLocationCode.isFullCode(fullCode)) return null
        val candidate = fullCode.substring(LOCALITY_PREFIX_LENGTH)
        val recovered = OpenLocationCode(candidate).recover(referenceLatitude, referenceLongitude).code
        return candidate.takeIf { recovered.equals(fullCode, ignoreCase = true) }
    }

    /** Library shortening (may drop up to 8 digits). */
    fun shorten(fullCode: String, referenceLatitude: Double, referenceLongitude: Double): String =
        OpenLocationCode(fullCode).shorten(referenceLatitude, referenceLongitude).code

    fun recover(shortCode: String, referenceLatitude: Double, referenceLongitude: Double): String =
        OpenLocationCode(shortCode).recover(referenceLatitude, referenceLongitude).code
}
