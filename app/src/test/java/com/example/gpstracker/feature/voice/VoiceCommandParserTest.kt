package com.example.gpstracker.feature.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceCommandParserTest {
    @Test
    fun `parses english phrases`() {
        val cases = listOf(
            Case("navigate to faisal mosque", "faisal mosque", null, false),
            Case("Navigate to Faisal Mosque", "faisal mosque", null, false),
            Case("navigate to faisal mosque!", "faisal mosque", null, false),
            Case("navigate to faisal mosque please", "faisal mosque", null, false),
            Case("take me to the park", "the park", null, false),
            Case("directions to blue area", "blue area", null, false),
            Case("go to centaurus", "centaurus", null, false),
            Case("drive to the airport", "the airport", null, false),
            Case("find coffee", "coffee", null, false),
            Case("search for fuel", "fuel", null, false),
            Case("how do i get to the mosque", "the mosque", null, false),
            Case("navigate to faisal mosque via blue area", "faisal mosque", "blue area", false),
            Case("go to park through the mall", "park", "the mall", false),
            Case("take me to mosque by way of blue area", "mosque", "blue area", false),
            Case("navigate to a via b through c", "a", "b through c", false),
            Case("GO TO PARK", "park", null, false),
            Case("Take me to Faisal Mosque, please.", "faisal mosque", null, false),
            Case("navigate to  the   park  please", "the park", null, false),
            Case("", "", null, false),
            Case("   ", "", null, false),
            Case("please", "", null, false),
            Case("navigate to", "", null, false),
            Case("find", "", null, false),
            Case("مسجد فیصل", "مسجد فیصل", null, false),
            Case("faisal mosque", "faisal mosque", null, false),
            Case("from home to faisal mosque", "faisal mosque", null, true),
            Case("from rawalpindi to faisal mosque via blue area", "faisal mosque", "blue area", true),
        )
        assertTrue(cases.size >= 25)
        cases.forEach { case ->
            val parsed = VoiceCommandParser.parse(case.raw)
            assertEquals(case.raw, case.destination, parsed.destinationQuery)
            assertEquals(case.raw, case.via, parsed.viaQuery)
            assertEquals(case.raw, case.raw, parsed.originalText)
            if (case.ignoredOrigin) {
                assertTrue(case.raw, parsed.ignoredOrigin)
            } else {
                assertFalse(case.raw, parsed.ignoredOrigin)
            }
            if (case.via == null) assertNull(parsed.viaQuery)
        }
    }

    private data class Case(
        val raw: String,
        val destination: String,
        val via: String?,
        val ignoredOrigin: Boolean,
    )
}
