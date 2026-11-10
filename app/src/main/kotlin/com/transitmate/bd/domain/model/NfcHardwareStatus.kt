package com.transitmate.bd.domain.model

/**
 * Domain-level representation of device NFC hardware capability.
 * This model contains no Android framework dependencies.
 */
enum class NfcHardwareStatus {
    /**
     * NFC hardware is present and currently enabled in system settings.
     */
    AVAILABLE,

    /**
     * NFC hardware is present on the device, but currently turned off in system settings.
     */
    DISABLED,

    /**
     * The device does not possess NFC hardware.
     */
    NOT_SUPPORTED
}

