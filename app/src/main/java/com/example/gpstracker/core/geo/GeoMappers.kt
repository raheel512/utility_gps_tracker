package com.example.gpstracker.core.geo

import com.example.gpstracker.core.places.LatLngPoint
import com.example.gpstracker.core.places.PlaceDetails
import com.example.gpstracker.core.places.PlaceSuggestion
import com.example.gpstracker.core.places.PlaceSummary
import kotlinx.serialization.Serializable

@Serializable
data class PhotonResponse(
    val features: List<PhotonFeature> = emptyList(),
)

@Serializable
data class PhotonFeature(
    val geometry: PhotonGeometry? = null,
    val properties: PhotonProperties = PhotonProperties(),
)

@Serializable
data class PhotonGeometry(
    val coordinates: List<Double> = emptyList(),
)

@Serializable
data class PhotonProperties(
    val osm_id: Long? = null,
    val osm_type: String? = null,
    val name: String? = null,
    val street: String? = null,
    val housenumber: String? = null,
    val city: String? = null,
    val district: String? = null,
    val county: String? = null,
    val state: String? = null,
    val country: String? = null,
    val locality: String? = null,
)

@Serializable
data class OverpassResponse(
    val elements: List<OverpassElement> = emptyList(),
)

@Serializable
data class OverpassElement(
    val type: String = "",
    val id: Long = 0,
    val lat: Double? = null,
    val lon: Double? = null,
    val center: OverpassCenter? = null,
    val tags: Map<String, String> = emptyMap(),
)

@Serializable
data class OverpassCenter(
    val lat: Double,
    val lon: Double,
)

object OsmIds {
    fun fromPhoton(type: String?, id: Long?): String? {
        val kind = when (type) {
            "N" -> "node"
            "W" -> "way"
            "R" -> "relation"
            else -> return null
        }
        if (id == null) return null
        return "$kind/$id"
    }

    fun fromOverpass(type: String, id: Long): String? = when (type) {
        "node" -> "node/$id"
        "way" -> "way/$id"
        "relation" -> "relation/$id"
        else -> null
    }
}

object PhotonMapper {
    fun suggestions(response: PhotonResponse): List<PlaceSuggestion> =
        response.features.mapNotNull { feature ->
            val id = OsmIds.fromPhoton(feature.properties.osm_type, feature.properties.osm_id) ?: return@mapNotNull null
            val primary = feature.properties.primaryText() ?: return@mapNotNull null
            PlaceSuggestion(
                placeId = id,
                primaryText = primary,
                secondaryText = feature.properties.secondaryText(),
            )
        }

    fun details(response: PhotonResponse): List<PlaceDetails> =
        response.features.mapNotNull { it.toDetails() }

    fun addressParts(properties: PhotonProperties): PhotonAddress {
        fun String?.clean(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
        val street = properties.street.clean()
        val district = properties.district.clean() ?: properties.locality.clean()
        val city = properties.city.clean() ?: properties.county.clean()
        // A city or suburb hit by reverse geocoding puts the place in `name`, not `city`.
        val namedPlace = properties.name.clean()?.takeIf { it != street }
        return PhotonAddress(
            thoroughfare = listOfNotNull(properties.housenumber.clean(), street)
                .takeIf { it.isNotEmpty() }
                ?.joinToString(" "),
            subLocality = district,
            locality = city ?: namedPlace?.takeIf { district == null } ?: properties.state.clean(),
            countryName = properties.country.clean(),
        )
    }
}

data class PhotonAddress(
    val thoroughfare: String?,
    val subLocality: String?,
    val locality: String?,
    val countryName: String?,
)

private fun PhotonFeature.toDetails(): PlaceDetails? {
    val coordinates = geometry?.coordinates.orEmpty()
    if (coordinates.size < 2) return null
    val id = OsmIds.fromPhoton(properties.osm_type, properties.osm_id) ?: return null
    val name = properties.primaryText() ?: return null
    return PlaceDetails(
        placeId = id,
        name = name,
        formattedAddress = properties.addressLine(),
        latitude = coordinates[1],
        longitude = coordinates[0],
    )
}

private fun PhotonProperties.primaryText(): String? =
    name?.trim()?.takeIf { it.isNotEmpty() }
        ?: street?.trim()?.takeIf { it.isNotEmpty() }
        ?: city?.trim()?.takeIf { it.isNotEmpty() }

private fun PhotonProperties.secondaryText(): String =
    listOfNotNull(
        street?.trim()?.takeIf { it.isNotEmpty() && it != name?.trim() },
        city?.trim()?.takeIf { it.isNotEmpty() },
        state?.trim()?.takeIf { it.isNotEmpty() },
        country?.trim()?.takeIf { it.isNotEmpty() },
    ).distinct().joinToString(", ")

private fun PhotonProperties.addressLine(): String? =
    listOfNotNull(
        listOfNotNull(
            housenumber?.trim()?.takeIf { it.isNotEmpty() },
            street?.trim()?.takeIf { it.isNotEmpty() },
        ).takeIf { it.isNotEmpty() }?.joinToString(" "),
        district?.trim()?.takeIf { it.isNotEmpty() },
        city?.trim()?.takeIf { it.isNotEmpty() },
        state?.trim()?.takeIf { it.isNotEmpty() },
        country?.trim()?.takeIf { it.isNotEmpty() },
    ).distinct().takeIf { it.isNotEmpty() }?.joinToString(", ")

object OverpassMapper {
    fun summaries(response: OverpassResponse, maxResults: Int): List<PlaceSummary> =
        response.elements.mapNotNull { element ->
            val id = OsmIds.fromOverpass(element.type, element.id) ?: return@mapNotNull null
            val name = element.tags["name"]?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            val lat = element.lat ?: element.center?.lat ?: return@mapNotNull null
            val lon = element.lon ?: element.center?.lon ?: return@mapNotNull null
            PlaceSummary(
                placeId = id,
                name = name,
                formattedAddress = element.addressLine(),
                latitude = lat,
                longitude = lon,
                primaryType = element.tags["amenity"]
                    ?: element.tags["shop"]
                    ?: element.tags["tourism"]
                    ?: element.tags["leisure"]
                    ?: element.tags["aeroway"],
            )
        }.take(maxResults)
}

private fun OverpassElement.addressLine(): String? {
    val street = listOfNotNull(
        tags["addr:housenumber"]?.trim()?.takeIf { it.isNotEmpty() },
        tags["addr:street"]?.trim()?.takeIf { it.isNotEmpty() },
    ).takeIf { it.isNotEmpty() }?.joinToString(" ")
    return listOfNotNull(
        street,
        tags["addr:suburb"]?.trim()?.takeIf { it.isNotEmpty() },
        tags["addr:city"]?.trim()?.takeIf { it.isNotEmpty() },
        tags["addr:country"]?.trim()?.takeIf { it.isNotEmpty() },
    ).takeIf { it.isNotEmpty() }?.joinToString(", ")
}

fun overpassQuery(
    key: String,
    value: String,
    extraKey: String?,
    extraValue: String?,
    latitude: Double,
    longitude: Double,
    radiusMeters: Double,
): String {
    val filter = buildString {
        append("[\"")
        append(key)
        append("\"=\"")
        append(value)
        append("\"]")
        if (!extraKey.isNullOrBlank() && !extraValue.isNullOrBlank()) {
            append("[\"")
            append(extraKey)
            append("\"=\"")
            append(extraValue)
            append("\"]")
        }
    }
    val around = "(around:${radiusMeters.toInt()},$latitude,$longitude)"
    return """
        [out:json][timeout:25];
        (
          node$filter$around;
          way$filter$around;
          relation$filter$around;
        );
        out center;
    """.trimIndent()
}

@Serializable
data class ValhallaRequest(
    val locations: List<ValhallaLocation>,
    val costing: String = "auto",
    val costing_options: ValhallaCostingOptions? = null,
    val units: String = "kilometers",
)

@Serializable
data class ValhallaLocation(
    val lat: Double,
    val lon: Double,
    val type: String? = null,
)

@Serializable
data class ValhallaCostingOptions(
    val auto: ValhallaAutoCosting,
)

@Serializable
data class ValhallaAutoCosting(
    val use_tolls: Double,
    val use_ferry: Double,
)

@Serializable
data class ValhallaResponse(
    val trip: ValhallaTrip? = null,
)

@Serializable
data class ValhallaTrip(
    val summary: ValhallaSummary? = null,
    val legs: List<ValhallaLeg> = emptyList(),
)

@Serializable
data class ValhallaSummary(
    val time: Double? = null,
    val length: Double? = null,
)

@Serializable
data class ValhallaLeg(
    val shape: String? = null,
)

object ValhallaMapper {
    fun request(
        origin: LatLngPoint,
        destination: LatLngPoint,
        via: LatLngPoint?,
        avoidTolls: Boolean,
        avoidFerries: Boolean,
    ): ValhallaRequest {
        val locations = buildList {
            add(ValhallaLocation(origin.latitude, origin.longitude))
            if (via != null) add(ValhallaLocation(via.latitude, via.longitude, type = "through"))
            add(ValhallaLocation(destination.latitude, destination.longitude))
        }
        val options = if (avoidTolls || avoidFerries) {
            ValhallaCostingOptions(
                auto = ValhallaAutoCosting(
                    use_tolls = if (avoidTolls) 0.0 else 1.0,
                    use_ferry = if (avoidFerries) 0.0 else 1.0,
                ),
            )
        } else {
            null
        }
        return ValhallaRequest(locations = locations, costing_options = options)
    }
}
