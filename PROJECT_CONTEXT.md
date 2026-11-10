# TransitMate — Project Context & Domain Reference

**Document Version:** 1.0  
**Last Updated:** 10 November 2026  
**Status:** Approved Reference  

---

## 1. Executive Summary & Purpose

TransitMate is an independent, offline-first Android companion application designed specifically for public transit commuters in Dhaka, Bangladesh who utilize contactless smart cards.

The primary transit cards in scope are:
1. **MRT Pass (ঢাকা ম্যাস র‍্যাপিড ট্রানজিট কোম্পানি লিমিটেড - DMTCL):** Issued primarily for the Dhaka Metro Rail network (currently MRT Line 6).
2. **Rapid Pass (ঢাকা পরিবহন সমন্বয় কর্তৃপক্ষ - DTCA):** A multi-modal smart card intended for Metro Rail, designated city bus routes, and eventual inter-modal transport services across the Dhaka metropolitan area.

---

## 2. Technical Card Ecosystem & Hardware Principles

### Card Technology Overview
Both the MRT Pass and Rapid Pass are built on high-security contactless smart card IC technologies typically compliant with **ISO/IEC 18092 (NFC-F / FeliCa)** or related contactless standards:
- **RF Standard:** NFC Type F (13.56 MHz, 212 kbps / 424 kbps).
- **Identifier Structure:** 8-byte Manufacturer ID (`IDm`) and 8-byte Parameter code (`PMm`).
- **Data Organization:** Organized into Areas, Services (Read-Only without encryption, Read-Write with encryption), and 16-byte Blocks.

### Evidence-Led Capability Staging
Per the Master Plan, capabilities are strictly partitioned into evidence levels:
1. **Card Presence & Identification:** Detecting the contactless card and determining its hardware `IDm` / card type.
2. **Card Type Classification:** Differentiating an MRT Pass from a Rapid Pass based on verified service indicators.
3. **Balance Reading:** Extracting stored monetary balance and currency format only after experimental verification on physical cards.
4. **Transaction History:** Reading historic tap-in / tap-out station records and fare deductions only after verified block parsing.

> [!IMPORTANT]  
> The application will **never** display simulated, guessed, or hardcoded mock balances as real card data. If a specific field or block cannot be securely read on a user's phone, the app will cleanly report the exact partial-read state.

---

## 3. Strict Legal, Ethical & Safety Boundaries

1. **Non-Affiliation Notice:** TransitMate is an independent utility. It has no formal affiliation with DMTCL, DTCA, Bangladesh Railway, or the Ministry of Road Transport and Bridges.
2. **No Card Modification:** The application is strictly **read-only**. It will never execute write commands (`Write Without Encryption`), attempt balance recharge, bypass access keys, or alter card state.
3. **No Emulation or Cloning:** TransitMate will not support Host Card Emulation (HCE) to spoof transit cards or bypass fare gates.
4. **Privacy by Default:** 
   - No personal card data or travel histories are transmitted off the device.
   - All persistence is local-only.
   - The app does not require a user account, phone number, or login.

---

## 4. Hardware Compatibility Strategy

- **NFC-Enabled Android Devices:** The core NFC scanner activates dynamically when the user initiates a scan or taps their card against the NFC antenna.
- **Devices Lacking NFC:** Since millions of budget Android smartphones in Bangladesh do not have NFC hardware, TransitMate declares `android.hardware.nfc` as optional (`required="false"`). This ensures the app remains installable on all devices for route exploration, offline fare calculators, and station guides in future phases.

---

## 5. Architectural Alignment

- **Presentation:** Jetpack Compose with Material 3.
- **State Management:** Unidirectional Data Flow (UDF) via ViewModel and `StateFlow`.
- **Domain:** Pure Kotlin models and repository contracts without Android dependencies.
- **Data & Platform:** Clear abstractions (`NfcAvailabilityChecker`, `NfcAvailabilityRepository`) isolating platform specifics.
