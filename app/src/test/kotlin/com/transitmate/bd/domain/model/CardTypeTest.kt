package com.transitmate.bd.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CardTypeTest {

    @Test
    fun cardType_mrtPass_hasCorrectMetadata() {
        val type = CardType.MRT_PASS
        assertEquals("MRT Pass", type.displayName)
        assertEquals("Dhaka Mass Transit Company Limited (DMTCL)", type.issuingAuthority)
    }

    @Test
    fun cardType_rapidPass_hasCorrectMetadata() {
        val type = CardType.RAPID_PASS
        assertEquals("Rapid Pass", type.displayName)
        assertEquals("Dhaka Transport Coordination Authority (DTCA)", type.issuingAuthority)
    }

    @Test
    fun cardType_unknown_hasFallbackMetadata() {
        val type = CardType.UNKNOWN
        assertNotNull(type.displayName)
        assertNotNull(type.issuingAuthority)
    }
}

