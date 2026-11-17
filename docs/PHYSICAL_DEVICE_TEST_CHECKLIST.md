# TransitMate — Physical Device NFC Validation Checklist

**Application:** TransitMate (`com.transitmate.bd`)  
**Target Build:** `app/build/outputs/apk/debug/app-debug.apk` (Post-Phase 09 + `RoomCardRepository.updateCard` cascade fix)  
**Test Policy:** Record **Pending** until executed on a real NFC-enabled Android device with physical cards. **Never invent test results or commit unredacted hardware `IDm` bytes (`01:27:XX:XX:XX:XX:XX:A0` format only).**

---

## Pre-Test Device & Build Record

| Field | Value |
|---|---|
| **Tester** | *Pending* |
| **Device Model** | *Pending (e.g., Pixel / Samsung / Xiaomi NFC-enabled device)* |
| **Android Version** | *Pending* |
| **APK Build / Commit** | `app/build/outputs/apk/debug/app-debug.apk` (Commit: *Fill at test time*) |
| **NFC Controller / Antenna Location** | Upper-center back *(remove metallic/magnetic cases before testing)* |

---

## 1. Physical Test Plan — Rapid Pass (`DTCA · FeliCa / NFC-F`)

> **Prior Baseline Context:** Baseline physical testing confirmed `NfcF` detection, `৳ 14` balance, and `14` trip/top-up records, and exposed a classification bug where Rapid Pass cards topped up at Metro counters (`1D 60 02 01`) were mislabeled as MRT Pass. That fix is unit-tested in `TransitCardBlockDecoderTest` (`decodeCardData_rapidPassToppedUpAtMetroCounterRemainsRapidPass`) and awaiting post-fix confirmation on physical hardware.

| ID | Test Case | Test Date | Expected Result | Actual Result | Status | Relevant Logs / Evidence Ref |
|---|---|---|---|---|---|---|
| **RP-01** | **NFC Hardware Status** | *Pending* | `Home` header badge shows NFC Ready (no "No NFC" or "NFC Disabled" banner). `Settings -> NFC Diagnostics` shows `NFC Hardware: Ready`. | *Not yet run on updated build* | **Pending** | *Pending screenshot/log* |
| **RP-02** | **NFC Enabled / Disabled Toggle** | *Pending* | Disabling system NFC displays `"NFC Disabled"` banner with working `Open NFC Settings` action; re-enabling NFC and returning to app updates status on `onResume()`. | *Not yet run on updated build* | **Pending** | *Pending screenshot/log* |
| **RP-03** | **First Scan (Clean DB)** | *Pending* | Holding Rapid Pass behind phone for 1–2s on `Scan` tab transitions smoothly to `Scan Result` without `TagLost` or crash. Full 8-byte `IDm` (`01:27:...`) and `Copy IDm` render cleanly. | *Not yet run on updated build* | **Pending** | *Pending screenshot (mask middle IDm bytes before sharing)* |
| **RP-04** | **Post-Fix Card Classification** | *Pending* | Rapid Pass (`01:27:...` IDm prefix, even with Metro counter recharge `1D 60 02 01`) classifies as **`Rapid Pass`** (`DTCA · FeliCa (NFC-F)`), NOT `MRT Pass`. | *Not yet run on updated build* | **Pending** | *Pending screenshot/logcat* |
| **RP-05** | **Balance Validation** | *Pending* | `LATEST VERIFIED BALANCE` shows exact current card purse balance (`৳ X`) matching station AVM / official reader. | *Not yet run on updated build* | **Pending** | *Pending screenshot/comparison* |
| **RP-06** | **Transaction Validation** | *Pending* | Up to `20` blocks (windows `0..9` + `10..19`) decode valid `TRIP` (`- ৳`) & `TOP_UP` (`+ ৳`) entries with accurate station names, EN/বাংলা translation, and chronological sequence. | *Not yet run on updated build* | **Pending** | *Pending screenshot + CSV export* |
| **RP-07** | **Save Card Flow** | *Pending* | Tapping `+ Save to My Cards` saves nickname & sets card as Primary (first card); `My Cards` & `Home` immediately show updated balance and `IDm`. | *Not yet run on updated build* | **Pending** | *Pending screenshot* |
| **RP-08** | **Second Scan (Repeated Scan Update)** | *Pending* | Rescanning the already-saved Rapid Pass from `Scan` or `Tap Card to Refresh` updates `lastScannedAt` and balance on the existing saved card without creating duplicate `My Cards` entries. | *Not yet run on updated build* | **Pending** | *Pending screenshot / DB check* |
| **RP-09** | **Duplicate Transaction & Preservation Check** | *Pending* | Rescanning identical FeliCa blocks produces **zero** duplicate transaction rows (`(cardId, rawBlockHex)` unique constraint) AND preserves older local transactions even when `saveOrUpdateCard` updates the card row. | *Not yet run on updated build* | **Pending** | *Pending Insights / History count check* |
| **RP-10** | **App Restart & Offline Persistence Check** | *Pending* | Force-stopping and relaunching app offline retains saved Rapid Pass, primary status, transaction history, theme, and EN/বাংলা preference cleanly; CSV export works offline. | *Not yet run on updated build* | **Pending** | *Pending screenshot / exported `.csv`* |

---

## 2. Physical Test Plan — MRT Pass (`DMTCL · FeliCa / NFC-F`)

> **Blocker Note:** Physical MRT Pass card is **not currently available** in the test environment. All MRT Pass hardware verification cases remain **Blocked** until a real MRT Pass card is obtained.

| ID | Test Case | Test Date | Expected Result | Actual Result | Status | Relevant Logs / Evidence Ref |
|---|---|---|---|---|---|---|
| **MRT-01** | **Card Detection (`Settings -> NFC Diagnostics`)** | *Pending* | Holding physical MRT Pass on `NFC Diagnostics` detects tag cleanly (`Scan #1`), captures `Tech List`, `IDm` (confirm byte prefix!), `System Code`, and `PMm`. | *Physical card unavailable* | **Blocked (Card Not Available)** | *Capture redacted Diagnostics log once card available* |
| **MRT-02** | **FeliCa Technology & Command Probe (`0x06 / 0x220F`)** | *Pending* | Verify whether `android.nfc.tech.NfcF` is exposed, what `System Code` is advertised, and whether Read Without Encryption (`0x06`) on service `0x220F` succeeds (`SF1=0x00, SF2=0x00`) or returns service-protected flags. | *Physical card unavailable* | **Blocked (Card Not Available)** | *Capture `SF1`/`SF2` flags from Diagnostics* |
| **MRT-03** | **Card Classification (`MRT Pass · DMTCL`)** | *Pending* | Non-`01:27` MRT Pass blocks carrying `1D 60 02 01` or `08 52 10 00` classify as `MRT Pass` (`DMTCL · FeliCa (NFC-F)`). Record actual MRT `IDm` vendor prefix to confirm or refine decoder rules. | *Physical card unavailable* | **Blocked (Card Not Available)** | *Pending hardware trace* |
| **MRT-04** | **Balance & Transaction Feasibility** | *Pending* | Verify whether MRT Pass blocks match the verified DTCA 16-byte layout (`postBalanceBdt` little-endian bytes `11..13`, station codes `10..90`) or use a distinct service code / protected encryption. | *Physical card unavailable* | **Blocked (Card Not Available)** | *Pending block dump inspection* |
| **MRT-05** | **Unsupported / Protected Data Handling** | *Pending* | If MRT Pass uses encrypted/protected service flags or unknown layout, `Scan Result` must honestly display **`Balance Not Available`** / **`Unverified`** (never guessed/fake `৳` amounts) and no non-FeliCa crash occurs. | *Physical card unavailable* | **Blocked (Card Not Available)** | *Pending screenshot* |

---

## 3. ADB Logcat & Evidence Collection Commands (For Tester Use)

```bash
# 1. Monitor NFC / TransitMate logs (excluding unrelated system spam) while tapping physical card:
adb logcat -c && adb logcat | grep -iE "TransitMate|Nfc|FeliCa|AndroidRuntime"

# 2. Redaction Check BEFORE sharing screenshots or logs publicly:
# Verify middle 5 bytes of 8-byte Card IDm (e.g., 01:27:XX:XX:XX:XX:XX:A0) are masked in shared media.
```
