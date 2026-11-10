package com.transitmate.bd.domain.model

/**
 * Supported transit card classifications in the TransitMate ecosystem.
 *
 * Capabilities for each card type are subject to physical verification
 * as defined in the Master Planning Document.
 */
enum class CardType(val displayName: String, val issuingAuthority: String) {
    MRT_PASS(
        displayName = "MRT Pass",
        issuingAuthority = "Dhaka Mass Transit Company Limited (DMTCL)"
    ),
    RAPID_PASS(
        displayName = "Rapid Pass",
        issuingAuthority = "Dhaka Transport Coordination Authority (DTCA)"
    ),
    UNKNOWN(
        displayName = "Unknown Card",
        issuingAuthority = "Unrecognized"
    )
}

