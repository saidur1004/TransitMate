# Phase 06 — FeliCa Hardware Validation & Classification Closure

**Project:** TransitMate (`com.transitmate.bd`)  
**Phase Scope:** Physical Rapid Pass Baseline Verification, Classification Bug Fix, & Hardware Validation Matrix  
**Clean-Room Status:** Strictly Clean-Room (Sony FeliCa JIS X 6319-4 `POLLING` / `READ WITHOUT ENCRYPTION`, zero GPL-3.0 code imported)

---

## 1. Physical Device Validation Summary

A baseline release build of TransitMate was installed and tested on a physical NFC-enabled Android smartphone using a real **Bangladesh Rapid Pass** smart card (`FeliCa / NFC-F`, `IDm` prefix `01:27:...`).

> **Privacy Note:** In compliance with TransitMate's privacy standards, the tester's actual 8-byte card `IDm` is redacted (`01:27:XX:XX:XX:XX:XX:A0`) in all documentation and replaced with synthetic test identifier `01:27:AA:BB:CC:DD:EE:A0` in unit tests and UI design mockups.

### Observed Physical Baseline Results

| Check | Outcome | Observed Evidence |
| :--- | :--- | :--- |
| **NFC-F / FeliCa Tag Discovery** | **Verified** | Detected cleanly over `NfcF` (`FeliCa`) without modification to card state. |
| **8-Byte IDm Readout** | **Verified** | Full 8-byte `IDm` (`01:27:XX:XX:XX:XX:XX:A0`) read locally with copy capability. |
| **Card Balance Readout** | **Verified** | Exact balance **`৳ 14`** decoded from service `0x220F` little-endian balance field. |
| **Transaction & Top-Up History** | **Verified** | **`14` records** recovered accurately (e.g., `Motijheel -> Shewrapara - ৳ 45`, `Shewrapara -> Motijheel - ৳ 45`, `Balance Top-Up + ৳ 100`, `Shahbagh -> Shewrapara - ৳ 36`; dates `Oct 3, 2026`, `Feb 22, 2026`, `Feb 19, 2026`). Matches official DMTCL 10%-discounted smart-card tariffs. |

---

## 2. Root Cause Fixed: Rapid Pass Topped Up at Metro Counter Misclassification

### Problem Found on Physical Hardware
During the baseline physical Rapid Pass scan, the card was mislabeled on the Scan Result screen as **`MRT Pass` (`DMTCL`)** instead of **`Rapid Pass` (`DTCA`)**.

### Root Cause
When a **Rapid Pass** (`IDm` starting with `01:27`) is recharged at a Dhaka Metro Rail Line 6 counter, the terminal writes an MRT counter top-up block header (`1D 60 02 01`). Previously, [`TransitCardBlockDecoder.classifyCard`](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/app/src/main/kotlin/com/transitmate/bd/core/nfc/TransitCardBlockDecoder.kt) evaluated `hasMrtTopUp` (`1D 60 02 01`) **before** `isRapidIdmPrefix` (`01:27`), overriding the Rapid Pass `IDm` hardware issuer signature.

### Fix Implemented
1. Updated `TransitCardBlockDecoder.classifyCard` so **`01:27` `IDm` prefix takes precedence** over `1D 60 02 01` recharge headers, and `MRT_PASS` requires `!isRapidIdmPrefix && (hasMrtTopUp || hasMetroTrip)`.
2. Replaced hardcoded year ceiling `2035` with dynamic `YearMonth.now(DHAKA_ZONE_OFFSET).year + 2` in transaction timestamp validation.
3. Added regression unit test `decodeCardData_rapidPassToppedUpAtMetroCounterRemainsRapidPass` in [`TransitCardBlockDecoderTest.kt`](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/app/src/test/kotlin/com/transitmate/bd/core/nfc/TransitCardBlockDecoderTest.kt).

---

## 3. Honest Hardware Verification Matrix

| Test Case / Scenario | Current Status | Verification Scope |
| :--- | :--- | :--- |
| **Baseline Rapid Pass Scan (`৳ 14`, `14 records`)** | **Verified (Hardware Baseline)** | Real physical Rapid Pass card on NFC-enabled Android device. |
| **Rapid Pass Classification (`01:27` + `1D 60 02 01` Top-Up)** | **Pending Physical Verification** | Fixed in code and verified via JUnit regression test; requires one confirmation scan with physical Rapid Pass on updated build. |
| **Repeated Scan Consistency & Local Rescan Deduplication** | **Pending Physical Verification** | Deduplication logic verified via JUnit (`CardRepository`); Room schema `MIGRATION_1_2` verified on `Pixel_9` emulator (`TransitMateInstrumentedTest`). Hardware repeated-tap cycle not yet executed. |
| **MRT Pass Recognition, Balance, & History** | **Blocked (Card Not Available)** | Physical MRT Pass card not available during this test phase. |
