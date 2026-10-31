package com.example.gpstracker.feature.voice

/**
 * English command grammar for v1. Other languages are returned as the whole destination query.
 * "from X to Y" drops the origin and sets [VoiceCommand.ignoredOrigin].
 */
data class VoiceCommand(
    val destinationQuery: String,
    val viaQuery: String?,
    val originalText: String,
    val ignoredOrigin: Boolean,
)

object VoiceCommandParser {
    private val prefixes = listOf(
        "how do i get to",
        "directions to",
        "navigate to",
        "take me to",
        "drive to",
        "search for",
        "go to",
        "find",
    )

    private val viaMarkers = listOf(
        "by way of",
        "through",
        "via",
    )

    private val fromTo = Regex("""^from .+ to (.+)$""")

    fun parse(raw: String): VoiceCommand {
        var ignoredOrigin = false
        var text = normalize(raw)
        if (text.isEmpty()) {
            return VoiceCommand("", null, raw, ignoredOrigin = false)
        }
        fromTo.matchEntire(text)?.let { match ->
            ignoredOrigin = true
            text = match.groupValues[1].trim()
        }
        text = stripPrefix(text)
        val (destination, via) = splitVia(text)
        return VoiceCommand(
            destinationQuery = destination,
            viaQuery = via,
            originalText = raw,
            ignoredOrigin = ignoredOrigin,
        )
    }

    private fun normalize(raw: String): String {
        val cleaned = raw.lowercase()
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        return when {
            cleaned == "please" -> ""
            cleaned.endsWith(" please") -> cleaned.removeSuffix(" please").trim()
            else -> cleaned
        }
    }

    private fun stripPrefix(text: String): String {
        val prefix = prefixes.firstOrNull { text == it || text.startsWith("$it ") } ?: return text
        return text.removePrefix(prefix).trim()
    }

    private fun splitVia(text: String): Pair<String, String?> {
        var markerStart = -1
        var marker = ""
        for (candidate in viaMarkers) {
            val index = markerIndex(text, candidate)
            if (index >= 0 && (markerStart < 0 || index < markerStart)) {
                markerStart = index
                marker = candidate
            }
        }
        if (markerStart < 0) return text to null
        val destination = text.substring(0, markerStart).trim()
        val via = text.substring(markerStart + marker.length).trim().ifBlank { null }
        return destination to via
    }

    /** Index of [marker] as a whole phrase, or -1. */
    private fun markerIndex(text: String, marker: String): Int {
        val padded = " $text "
        val found = padded.indexOf(" $marker ")
        return if (found < 0) -1 else found
    }
}
