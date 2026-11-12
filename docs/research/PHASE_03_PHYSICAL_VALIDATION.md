# TransitMate — Phase 03: Rapid Pass Physical Validation Procedure & Evidence Protocol

**Document Version:** 1.0  
**Date:** 12 November 2026  
**Status:** Ready for Execution  
**Lead Engineer:** Senior Android NFC/FeliCa Engineer  
**Card Under Test:** Bangladesh Rapid Pass (Available)  
**Card Blocked:** Bangladesh MRT Pass (`BLOCKED — CARD NOT AVAILABLE`)  

---

## 1. Test Environment & Safety Protocols

### 1.1 Physical Hardware Availability
- **NFC-Enabled Android Smartphone:** Available in owner's possession.
- **Physical Rapid Pass Card:** Available in owner's possession (1 authentic issued card).
- **Physical MRT Pass Card:** **NOT AVAILABLE** (`BLOCKED — CARD NOT AVAILABLE`).

### 1.2 Cardinal Safety Rules
1. **Zero Card Modification:** The test harness and validation procedure execute strictly non-destructive, read-only commands. No write operations, balance updates, or authentication bypasses will be attempted.
2. **Privacy Protection & IDm Redaction:** Real card identifiers (`IDm`) and serial numbers must be redacted in all public reports and commits (e.g., `01:2E:3D:**:**:**:**:9F`).
3. **No Credential Ingestion:** The test procedure will never request the owner's passwords, OTPs, or mobile financial service PINs.

---

## 2. Device Identification & Diagnostics Procedure

### 2.1 ADB Device Inspection Commands
When the physical Android phone is connected to the development machine via USB cable or Wireless Debugging:

```bash
# 1. Verify device connection
adb devices -l

# 2. Extract Device Manufacturer and Model
adb shell getprop ro.product.manufacturer
adb shell getprop ro.product.model

# 3. Extract Android OS Version and API Level
adb shell getprop ro.build.version.release
adb shell getprop ro.build.version.sdk

# 4. Verify Physical NFC Hardware Feature
adb shell pm list features | grep -i nfc
# Expected output: feature:android.hardware.nfc

# 5. Check NFC Service State
adb shell dumpsys nfc | grep -E "mState|mIsZeroClick"
```

### 2.2 Manual Device Inspection (If ADB is disconnected)
If the phone is tested without an active ADB tether:
- Navigate to **Settings > About Phone** &rarr; Record **Device Name / Model** and **Android Version**.
- Navigate to **Settings > Connected Devices / Connections > NFC** &rarr; Verify toggle is **ON**.

---

## 3. Cards Under Test Matrix

| Card Target | Card Category | Physical Sample Status | Test Eligibility | Expected Technology |
|---|---|---|---|---|
| **Rapid Pass** | DTCA Contactless Multi-Modal Pass | **Available** | **ELIGIBLE (Primary Target)** | `android.nfc.tech.NfcF` |
| **MRT Pass** | DMTCL Metro Rail Smart Card | **Not Available** | **BLOCKED — CARD NOT AVAILABLE** | `android.nfc.tech.NfcF` (Hypothesis) |

---

## 4. Step-by-Step Validation Test Cases

### Test Case 1: NFC Hardware Readiness & Lifecycle
- **Objective:** Verify that the TransitMate application correctly detects hardware presence and live adapter state changes.
- **Preconditions:** TransitMate installed (`app-debug.apk`). Phone has physical NFC hardware.
- **Procedure:**
  1. Open TransitMate with NFC enabled in Android settings &rarr; Observe status badge.
  2. Navigate to Android Settings and turn NFC **OFF**. Return to TransitMate &rarr; Observe status badge.
  3. Turn NFC back **ON** and resume TransitMate &rarr; Observe status badge.
- **Expected Outcome:**
  - When ON: Status badge displays green `NFC hardware is available and ready`.
  - When OFF: Status badge displays amber `NFC is supported but currently disabled`.
- **Classification:** `DEVICE-OBSERVED` (Pending execution by owner).

---

### Test Case 2: Rapid Pass Discovery & Technology Identification
- **Objective:** Determine whether Android detects the physical Rapid Pass card and exposes `android.nfc.tech.NfcF`.
- **Preconditions:** TransitMate open in foreground with diagnostic reader active.
- **Procedure:**
  1. Hold the physical Rapid Pass card firmly against the back of the phone (near the camera module/NFC sweet-spot).
  2. Observe diagnostic card output on screen.
  3. Record the detected technology list.
- **Expected Outcome:**
  - Card detection triggers immediately without system chime conflict.
  - Technology list contains `android.nfc.tech.NfcF`.
- **Classification:** `DEVICE-OBSERVED` (Pending execution by owner).

---

### Test Case 3: Identifier (IDm) & Polling Metadata Observation
- **Objective:** Observe low-level anticollision metadata (`IDm`, `SystemCode`, `PMm`) from the Rapid Pass card.
- **Preconditions:** Successful execution of Test Case 2.
- **Procedure:**
  1. Observe the captured metadata in the diagnostic card.
  2. Record:
     - `Tag.id` byte count (Expected: 8 bytes).
     - System Code hex (Expected: 2 bytes, e.g., `0x88B4` or `0xFE00`).
     - Manufacturer / PMm hex (Expected: 8 bytes).
  3. Redact middle bytes of `Tag.id` before publishing.
- **Expected Outcome:**
  - `Tag.id` is exactly 8 bytes (Standard FeliCa IDm length).
  - System code is successfully reported.
- **Classification:** `DEVICE-OBSERVED` (Pending execution by owner).

---

### Test Case 4: Repeatability & RF Coupling Stability
- **Objective:** Test scan consistency, tag-loss recovery, and RF coupling repeatability across multiple consecutive taps.
- **Procedure:**
  1. Tap the Rapid Pass card against the phone antenna.
  2. Wait 2 seconds, remove card.
  3. Repeat for **5 consecutive scans**.
  4. Record scan result and counter for each iteration.
- **Test Matrix Table:**

| Scan # | Expected Result | Actual Result | Technologies Reported | IDm Consistency | Success (Y/N) |
|:---:|:---:|:---:|:---:|:---:|:---:|
| 1 | Tag detected & read | *Pending* | *Pending* | *Pending* | *Pending* |
| 2 | Tag detected & read | *Pending* | *Pending* | *Pending* | *Pending* |
| 3 | Tag detected & read | *Pending* | *Pending* | *Pending* | *Pending* |
| 4 | Tag detected & read | *Pending* | *Pending* | *Pending* | *Pending* |
| 5 | Tag detected & read | *Pending* | *Pending* | *Pending* | *Pending* |

- **Acceptance Criterion:** 5 out of 5 consecutive successful reads without app crash or unhandled `TagLostException`.
- **Classification:** `REPRODUCED` (Upon 5/5 consecutive passes).

---

### Test Case 5: Official App Comparative Baseline (Non-Intrusive)
- **Objective:** Compare card readability against the official DTCA Rapid Pass application without intercepting protected services.
- **Preconditions:** Official Rapid Pass app (`com.bd.rapidpass.app`) legitimately installed and registered by the owner.
- **Procedure:**
  1. Open the official Rapid Pass app.
  2. Tap the card to view the official balance and last transaction timestamp.
  3. Record the date and current nominal balance for future ground-truth reference.
- **Data Protection Clause:** Do **not** log account credentials or personal user phone numbers.
- **Classification:** `REFERENCE-OBSERVED`.

---

## 5. Limitations & Blockers Log

| Item | Status | Reason & Impact | Resolution Path |
|---|---|---|---|
| **MRT Pass Card** | `BLOCKED — CARD NOT AVAILABLE` | Physical card not present in test lab | Must obtain physical sample before claiming MRT Pass support |
| **Balance Block Parsing** | `PENDING RESEARCH GATE` | Requires physical confirmation of Service Code unencrypted read status | Execute diagnostic reader first to lock System Code before attempting service polling |
| **ADB Direct Connection** | `DISCONNECTED` | Phone not plugged in via USB during automated agent turn | Owner runs manual validation or connects via ADB |

