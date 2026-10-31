package com.example.gpstracker.core.location.format

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Vectors from google/open-location-code: the Googleplex example and
 * test_data/encoding.csv + test_data/shortCodeTests.csv.
 */
class PlusCodeFormatterTest {

    @Test
    fun `googleplex encodes to 849VCWC8+R9`() {
        assertEquals("849VCWC8+R9", PlusCodeFormatter.fullCode(37.4220699, -122.0841244, codeLength = 10))
    }

    @Test
    fun `encoding csv vectors`() {
        assertEquals("7FG49QCJ+2V", PlusCodeFormatter.fullCode(20.3700625, 2.7821875, codeLength = 10))
        assertEquals("7FG49QCJ+2VX", PlusCodeFormatter.fullCode(20.3701125, 2.782234375, codeLength = 11))
        assertEquals("8FVC2222+22", PlusCodeFormatter.fullCode(47.0000625, 8.0000625, codeLength = 10))
        assertEquals("4VCPPQGP+Q9", PlusCodeFormatter.fullCode(-41.2730625, 174.7859375, codeLength = 10))
    }

    @Test
    fun `display length is 11 digits`() {
        assertEquals("7FG49QCJ+2VX", PlusCodeFormatter.fullCode(20.3701125, 2.782234375))
    }

    @Test
    fun `library shorten matches shortCodeTests csv`() {
        assertEquals("+2VX", PlusCodeFormatter.shorten("9C3W9QCJ+2VX", 51.3701125, -1.217765625))
        assertEquals("CJ+2VX", PlusCodeFormatter.shorten("9C3W9QCJ+2VX", 51.3708675, -1.217765625))
        assertEquals("9QCJ+2VX", PlusCodeFormatter.shorten("9C3W9QCJ+2VX", 51.3852125, -1.217765625))
        assertEquals("2GGG+GG", PlusCodeFormatter.shorten("8FVC2GGG+GG", 46.976, 8.526))
    }

    @Test
    fun `library recover matches shortCodeTests csv`() {
        assertEquals("9C3W9QCJ+2VX", PlusCodeFormatter.recover("+2VX", 51.3701125, -1.217765625))
        assertEquals("9C3W9QCJ+2VX", PlusCodeFormatter.recover("9QCJ+2VX", 51.3852125, -1.217765625))
        assertEquals("8FRCXGGG+GG", PlusCodeFormatter.recover("XGGG+GG", 47.026, 8.526))
    }

    @Test
    fun `locality short code drops the first four digits near the reference`() {
        // Own location as reference: shortCodeTests.csv rows prove 9QCJ+2VX recovers to 9C3W9QCJ+2VX here.
        assertEquals("9QCJ+2VX", PlusCodeFormatter.localityShortCode("9C3W9QCJ+2VX", 51.3701125, -1.217765625))
    }

    @Test
    fun `locality short code is null when the reference is too far away`() {
        assertNull(PlusCodeFormatter.localityShortCode("9C3W9QCJ+2VX", -33.8688, 151.2093))
    }

    @Test
    fun `locality short code is null for a code that is not full`() {
        assertNull(PlusCodeFormatter.localityShortCode("9QCJ+2VX", 51.3701125, -1.217765625))
    }
}
