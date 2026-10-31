package com.example.gpstracker.core.location.geocode

class FakeReverseGeocoder(
    var result: GeocodedAddress? = null,
) : ReverseGeocoder {
    var calls = 0
        private set

    override suspend fun reverseGeocode(latitude: Double, longitude: Double): GeocodedAddress? {
        calls++
        return result
    }
}
