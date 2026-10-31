package com.example.gpstracker.feature.country

/**
 * Reference directory for Country Code and Country Info.
 * Population figures are rounded, in the same style as the design ("38 million").
 */
data class Country(
    val iso: String,
    val name: String,
    val dial: String,
    val capital: String,
    val population: String,
    val currency: String,
    val region: String,
    val timeZone: String,
    val borders: List<String>,
)

object CountryCatalog {
    val countries: List<Country> = listOf(
        country("AF", "Afghanistan", "+93", "Kabul", "42 million", "Afghan Afghani", "Southern Asia", "UTC+4:30", "Pakistan", "Iran", "Turkmenistan", "Uzbekistan", "Tajikistan", "China"),
        country("AM", "Armenia", "+374", "Yerevan", "3 million", "Armenian Dram", "Western Asia", "UTC+4", "Georgia", "Azerbaijan", "Iran", "Turkey"),
        country("AU", "Australia", "+61", "Canberra", "27 million", "Australian Dollar", "Australia and New Zealand", "UTC+8 to UTC+11", "None"),
        country("AZ", "Azerbaijan", "+994", "Baku", "10 million", "Azerbaijani Manat", "Western Asia", "UTC+4", "Armenia", "Georgia", "Iran", "Russia", "Turkey"),
        country("BH", "Bahrain", "+973", "Manama", "1.5 million", "Bahraini Dinar", "Western Asia", "UTC+3", "Saudi Arabia"),
        country("BD", "Bangladesh", "+880", "Dhaka", "173 million", "Bangladeshi Taka", "Southern Asia", "UTC+6", "India", "Myanmar"),
        country("BE", "Belgium", "+32", "Brussels", "12 million", "Euro", "Western Europe", "UTC+1", "France", "Germany", "Luxembourg", "Netherlands"),
        country("BT", "Bhutan", "+975", "Thimphu", "0.8 million", "Bhutanese Ngultrum", "Southern Asia", "UTC+6", "China", "India"),
        country("BR", "Brazil", "+55", "Brasília", "216 million", "Brazilian Real", "South America", "UTC-5 to UTC-2", "Argentina", "Bolivia", "Colombia", "Guyana", "Paraguay", "Peru", "Suriname", "Uruguay", "Venezuela"),
        country("BN", "Brunei", "+673", "Bandar Seri Begawan", "0.5 million", "Brunei Dollar", "South-Eastern Asia", "UTC+8", "Malaysia"),
        country("CA", "Canada", "+1", "Ottawa", "41 million", "Canadian Dollar", "Northern America", "UTC-8 to UTC-4", "United States"),
        country("CN", "China", "+86", "Beijing", "1.4 billion", "Chinese Yuan", "Eastern Asia", "UTC+8", "Afghanistan", "Bhutan", "India", "Kazakhstan", "Kyrgyzstan", "Laos", "Mongolia", "Myanmar", "Nepal", "North Korea", "Pakistan", "Russia", "Tajikistan", "Vietnam"),
        country("DK", "Denmark", "+45", "Copenhagen", "6 million", "Danish Krone", "Northern Europe", "UTC+1", "Germany"),
        country("EG", "Egypt", "+20", "Cairo", "114 million", "Egyptian Pound", "Northern Africa", "UTC+2", "Israel", "Libya", "Sudan"),
        country("FR", "France", "+33", "Paris", "68 million", "Euro", "Western Europe", "UTC+1", "Belgium", "Germany", "Italy", "Luxembourg", "Spain", "Switzerland"),
        country("GE", "Georgia", "+995", "Tbilisi", "3.7 million", "Georgian Lari", "Western Asia", "UTC+4", "Armenia", "Azerbaijan", "Russia", "Turkey"),
        country("DE", "Germany", "+49", "Berlin", "84 million", "Euro", "Western Europe", "UTC+1", "Austria", "Belgium", "Czechia", "Denmark", "France", "Luxembourg", "Netherlands", "Poland", "Switzerland"),
        country("IN", "India", "+91", "New Delhi", "1.4 billion", "Indian Rupee", "Southern Asia", "UTC+5:30", "Bangladesh", "Bhutan", "China", "Myanmar", "Nepal", "Pakistan"),
        country("ID", "Indonesia", "+62", "Jakarta", "281 million", "Indonesian Rupiah", "South-Eastern Asia", "UTC+7 to UTC+9", "Malaysia", "Papua New Guinea", "Timor-Leste"),
        country("IR", "Iran", "+98", "Tehran", "91 million", "Iranian Rial", "Southern Asia", "UTC+3:30", "Afghanistan", "Armenia", "Azerbaijan", "Iraq", "Pakistan", "Turkey", "Turkmenistan"),
        country("IQ", "Iraq", "+964", "Baghdad", "46 million", "Iraqi Dinar", "Western Asia", "UTC+3", "Iran", "Jordan", "Kuwait", "Saudi Arabia", "Syria", "Turkey"),
        country("IE", "Ireland", "+353", "Dublin", "5 million", "Euro", "Northern Europe", "UTC+0", "United Kingdom"),
        country("IT", "Italy", "+39", "Rome", "59 million", "Euro", "Southern Europe", "UTC+1", "Austria", "France", "Slovenia", "Switzerland"),
        country("JP", "Japan", "+81", "Tokyo", "124 million", "Japanese Yen", "Eastern Asia", "UTC+9"),
        country("JO", "Jordan", "+962", "Amman", "11 million", "Jordanian Dinar", "Western Asia", "UTC+3", "Iraq", "Israel", "Saudi Arabia", "Syria"),
        country("KZ", "Kazakhstan", "+7", "Astana", "20 million", "Kazakhstani Tenge", "Central Asia", "UTC+5", "China", "Kyrgyzstan", "Russia", "Turkmenistan", "Uzbekistan"),
        country("KW", "Kuwait", "+965", "Kuwait City", "4 million", "Kuwaiti Dinar", "Western Asia", "UTC+3", "Iraq", "Saudi Arabia"),
        country("MY", "Malaysia", "+60", "Kuala Lumpur", "34 million", "Malaysian Ringgit", "South-Eastern Asia", "UTC+8", "Brunei", "Indonesia", "Thailand"),
        country("NP", "Nepal", "+977", "Kathmandu", "31 million", "Nepalese Rupee", "Southern Asia", "UTC+5:45", "China", "India"),
        country("NL", "Netherlands", "+31", "Amsterdam", "18 million", "Euro", "Western Europe", "UTC+1", "Belgium", "Germany"),
        country("NO", "Norway", "+47", "Oslo", "5.5 million", "Norwegian Krone", "Northern Europe", "UTC+1", "Finland", "Russia", "Sweden"),
        country("OM", "Oman", "+968", "Muscat", "5 million", "Omani Rial", "Western Asia", "UTC+4", "Saudi Arabia", "United Arab Emirates", "Yemen"),
        country("PK", "Pakistan", "+92", "Islamabad", "241 million", "Pakistani Rupee", "Southern Asia", "UTC+5", "Afghanistan", "China", "India", "Iran"),
        country("QA", "Qatar", "+974", "Doha", "3 million", "Qatari Riyal", "Western Asia", "UTC+3", "Saudi Arabia"),
        country("RU", "Russia", "+7", "Moscow", "144 million", "Russian Ruble", "Eastern Europe", "UTC+2 to UTC+12", "Azerbaijan", "China", "Finland", "Georgia", "Kazakhstan", "Norway", "Ukraine"),
        country("SA", "Saudi Arabia", "+966", "Riyadh", "37 million", "Saudi Riyal", "Western Asia", "UTC+3", "Bahrain", "Iraq", "Jordan", "Kuwait", "Oman", "Qatar", "United Arab Emirates", "Yemen"),
        country("SG", "Singapore", "+65", "Singapore", "6 million", "Singapore Dollar", "South-Eastern Asia", "UTC+8"),
        country("KR", "South Korea", "+82", "Seoul", "52 million", "South Korean Won", "Eastern Asia", "UTC+9", "North Korea"),
        country("ES", "Spain", "+34", "Madrid", "48 million", "Euro", "Southern Europe", "UTC+1", "Andorra", "France", "Portugal"),
        country("SE", "Sweden", "+46", "Stockholm", "10 million", "Swedish Krona", "Northern Europe", "UTC+1", "Finland", "Norway"),
        country("CH", "Switzerland", "+41", "Bern", "9 million", "Swiss Franc", "Western Europe", "UTC+1", "Austria", "France", "Germany", "Italy", "Liechtenstein"),
        country("TH", "Thailand", "+66", "Bangkok", "72 million", "Thai Baht", "South-Eastern Asia", "UTC+7", "Cambodia", "Laos", "Malaysia", "Myanmar"),
        country("TR", "Turkey", "+90", "Ankara", "86 million", "Turkish Lira", "Western Asia", "UTC+3", "Armenia", "Azerbaijan", "Bulgaria", "Georgia", "Greece", "Iran", "Iraq", "Syria"),
        country("AE", "United Arab Emirates", "+971", "Abu Dhabi", "10 million", "UAE Dirham", "Western Asia", "UTC+4", "Oman", "Saudi Arabia"),
        country("GB", "United Kingdom", "+44", "London", "68 million", "Pound Sterling", "Northern Europe", "UTC+0", "Ireland"),
        country("UA", "Ukraine", "+380", "Kyiv", "38 million", "Ukrainian Hryvnia", "Eastern Europe", "UTC+2", "Belarus", "Hungary", "Moldova", "Poland", "Romania", "Russia", "Slovakia"),
        country("US", "United States", "+1", "Washington, D.C.", "340 million", "US Dollar", "Northern America", "UTC-10 to UTC-5", "Canada", "Mexico"),
        country("UZ", "Uzbekistan", "+998", "Tashkent", "36 million", "Uzbekistani Som", "Central Asia", "UTC+5", "Afghanistan", "Kazakhstan", "Kyrgyzstan", "Tajikistan", "Turkmenistan"),
        country("VN", "Vietnam", "+84", "Hanoi", "100 million", "Vietnamese Dong", "South-Eastern Asia", "UTC+7", "Cambodia", "China", "Laos"),
    )

    fun find(iso: String): Country? = countries.find { it.iso == iso }

    fun filter(query: String): List<Country> {
        val needle = query.trim()
        if (needle.isEmpty()) return countries
        return countries.filter { country ->
            country.name.contains(needle, ignoreCase = true) ||
                country.capital.contains(needle, ignoreCase = true) ||
                country.dial.contains(needle)
        }
    }

    private fun country(
        iso: String,
        name: String,
        dial: String,
        capital: String,
        population: String,
        currency: String,
        region: String,
        timeZone: String,
        vararg borders: String,
    ): Country = Country(
        iso = iso,
        name = name,
        dial = dial,
        capital = capital,
        population = population,
        currency = currency,
        region = region,
        timeZone = timeZone,
        borders = borders.filter { it != "None" },
    )
}
