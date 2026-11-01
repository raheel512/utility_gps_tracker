package com.example.gpstracker.feature.emergency

import com.example.gpstracker.feature.country.CountryCatalog

/**
 * Offline emergency dial numbers by ISO 3166-1 alpha-2.
 * Prefer official national numbers; many regions also support a shared [general] line (e.g. 112).
 */
data class CountryEmergencyNumbers(
    val iso: String,
    val police: String,
    val ambulance: String,
    val fire: String,
    /** Shared SOS line when the country publishes one (often 112 or 911). */
    val general: String? = null,
) {
    val primaryDial: String get() = general ?: police
}

object EmergencyNumberCatalog {

    fun resolve(iso: String?): CountryEmergencyNumbers {
        val code = iso?.trim()?.uppercase().orEmpty()
        if (code.length == 2) {
            BY_ISO[code]?.let { return it }
        }
        return DEFAULT
    }

    fun countryDisplayName(iso: String?): String? {
        val code = iso?.trim()?.uppercase() ?: return null
        return CountryCatalog.find(code)?.name
    }

    private val DEFAULT = CountryEmergencyNumbers(
        iso = "",
        police = "112",
        ambulance = "112",
        fire = "112",
        general = "112",
    )

    private val BY_ISO: Map<String, CountryEmergencyNumbers> = listOf(
        nums("AF", police = "119", ambulance = "112", fire = "119", general = "112"),
        nums("AM", police = "102", ambulance = "103", fire = "101", general = "112"),
        nums("AU", police = "000", ambulance = "000", fire = "000", general = "000"),
        nums("AZ", police = "102", ambulance = "103", fire = "101", general = "112"),
        nums("BH", police = "999", ambulance = "999", fire = "999", general = "999"),
        nums("BD", police = "999", ambulance = "199", fire = "199", general = "999"),
        nums("BE", police = "101", ambulance = "112", fire = "112", general = "112"),
        nums("BT", police = "113", ambulance = "112", fire = "110", general = "112"),
        nums("BR", police = "190", ambulance = "192", fire = "193", general = "190"),
        nums("BN", police = "993", ambulance = "991", fire = "995"),
        nums("CA", police = "911", ambulance = "911", fire = "911", general = "911"),
        nums("CN", police = "110", ambulance = "120", fire = "119"),
        nums("DK", police = "112", ambulance = "112", fire = "112", general = "112"),
        nums("EG", police = "122", ambulance = "123", fire = "180"),
        nums("FR", police = "17", ambulance = "15", fire = "18", general = "112"),
        nums("GE", police = "112", ambulance = "112", fire = "112", general = "112"),
        nums("DE", police = "110", ambulance = "112", fire = "112", general = "112"),
        nums("IN", police = "100", ambulance = "102", fire = "101", general = "112"),
        nums("ID", police = "110", ambulance = "118", fire = "113", general = "112"),
        nums("IR", police = "110", ambulance = "115", fire = "125"),
        nums("IQ", police = "104", ambulance = "122", fire = "115"),
        nums("IE", police = "999", ambulance = "999", fire = "999", general = "112"),
        nums("IT", police = "112", ambulance = "118", fire = "115", general = "112"),
        nums("JP", police = "110", ambulance = "119", fire = "119"),
        nums("JO", police = "911", ambulance = "911", fire = "911", general = "911"),
        nums("KZ", police = "102", ambulance = "103", fire = "101", general = "112"),
        nums("KW", police = "112", ambulance = "112", fire = "112", general = "112"),
        nums("MY", police = "999", ambulance = "999", fire = "994", general = "999"),
        nums("NP", police = "100", ambulance = "102", fire = "101"),
        nums("NL", police = "112", ambulance = "112", fire = "112", general = "112"),
        nums("NO", police = "112", ambulance = "113", fire = "110", general = "112"),
        nums("OM", police = "9999", ambulance = "9999", fire = "9999", general = "9999"),
        nums("PK", police = "15", ambulance = "1122", fire = "16", general = "1122"),
        nums("QA", police = "999", ambulance = "999", fire = "999", general = "999"),
        nums("RU", police = "102", ambulance = "103", fire = "101", general = "112"),
        nums("SA", police = "911", ambulance = "997", fire = "998", general = "911"),
        nums("SG", police = "999", ambulance = "995", fire = "995"),
        nums("KR", police = "112", ambulance = "119", fire = "119", general = "112"),
        nums("ES", police = "091", ambulance = "061", fire = "080", general = "112"),
        nums("SE", police = "112", ambulance = "112", fire = "112", general = "112"),
        nums("CH", police = "117", ambulance = "144", fire = "118", general = "112"),
        nums("TH", police = "191", ambulance = "1669", fire = "199", general = "191"),
        nums("TR", police = "155", ambulance = "112", fire = "110", general = "112"),
        nums("AE", police = "999", ambulance = "998", fire = "997", general = "999"),
        nums("GB", police = "999", ambulance = "999", fire = "999", general = "112"),
        nums("UA", police = "102", ambulance = "103", fire = "101", general = "112"),
        nums("US", police = "911", ambulance = "911", fire = "911", general = "911"),
        nums("UZ", police = "102", ambulance = "103", fire = "101", general = "112"),
        nums("VN", police = "113", ambulance = "115", fire = "114"),
        // Extra common markets beyond CountryCatalog
        nums("AT", police = "133", ambulance = "144", fire = "122", general = "112"),
        nums("PT", police = "112", ambulance = "112", fire = "112", general = "112"),
        nums("PL", police = "997", ambulance = "999", fire = "998", general = "112"),
        nums("MX", police = "911", ambulance = "911", fire = "911", general = "911"),
        nums("NZ", police = "111", ambulance = "111", fire = "111", general = "111"),
        nums("ZA", police = "10111", ambulance = "10177", fire = "10111"),
        nums("PH", police = "911", ambulance = "911", fire = "911", general = "911"),
        nums("NG", police = "112", ambulance = "112", fire = "112", general = "112"),
        nums("KE", police = "999", ambulance = "999", fire = "999", general = "112"),
        nums("GR", police = "100", ambulance = "166", fire = "199", general = "112"),
        nums("CZ", police = "158", ambulance = "155", fire = "150", general = "112"),
        nums("FI", police = "112", ambulance = "112", fire = "112", general = "112"),
        nums("HU", police = "107", ambulance = "104", fire = "105", general = "112"),
        nums("RO", police = "112", ambulance = "112", fire = "112", general = "112"),
        nums("BG", police = "112", ambulance = "112", fire = "112", general = "112"),
        nums("HR", police = "192", ambulance = "194", fire = "193", general = "112"),
        nums("RS", police = "192", ambulance = "194", fire = "193", general = "112"),
        nums("IL", police = "100", ambulance = "101", fire = "102"),
        nums("LK", police = "119", ambulance = "1990", fire = "110"),
        nums("MM", police = "199", ambulance = "192", fire = "191"),
        nums("KH", police = "117", ambulance = "119", fire = "118"),
        nums("LA", police = "191", ambulance = "195", fire = "190"),
        nums("TW", police = "110", ambulance = "119", fire = "119"),
        nums("HK", police = "999", ambulance = "999", fire = "999", general = "999"),
        nums("MO", police = "999", ambulance = "999", fire = "999", general = "999"),
        nums("AR", police = "911", ambulance = "107", fire = "100", general = "911"),
        nums("CL", police = "133", ambulance = "131", fire = "132"),
        nums("CO", police = "123", ambulance = "123", fire = "119", general = "123"),
        nums("PE", police = "105", ambulance = "106", fire = "116"),
        nums("EG", police = "122", ambulance = "123", fire = "180"),
    ).associateBy { it.iso }

    private fun nums(
        iso: String,
        police: String,
        ambulance: String,
        fire: String,
        general: String? = null,
    ) = CountryEmergencyNumbers(iso, police, ambulance, fire, general)
}
