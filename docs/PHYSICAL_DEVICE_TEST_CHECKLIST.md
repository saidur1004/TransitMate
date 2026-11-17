# TransitMate — Physical Device NFC Validation Checklist

**Application:** TransitMate (`com.transitmate.bd`)  
**Target Build:** `app/build/outputs/apk/debug/app-debug.apk` (Debug APK · `v1.0.0`, `versionCode = 1`, `minSdk = 26`, `targetSdk = 36`)  
**Scope Rules:** Record **`Not Tested`** until executed on a real NFC-enabled Android smartphone with physical cards. **Never mark `Pass` or fill `Actual Result` without real-device evidence, and never expose full unredacted hardware `IDm` bytes (`01:27:XX:XX:XX:XX:XX:A0` masking mandatory in shared screenshots, logs, docs, and commits).**

---

## Pre-Test Device & Build Record

| Field | Value |
|---|---|
| **Tester** | *Not Tested* |
| **Phone Manufacturer & Model** | *Not Tested (Real NFC-enabled Android phone required)* |
| **Android Version** | *Not Tested* |
| **TransitMate Version / Build Type** | `v1.0.0` (`versionCode = 1`) · Debug (`com.transitmate.bd.debug`) |
| **APK Commit Reference** | `cb1b0a66fe6950c81ddb7bf1cc4862f24d28ae4c` |
| **NFC Antenna Location** | Upper-center back *(remove metallic or magnetic phone cases before scanning)* |

---

## 1. Rapid Pass Physical Validation Test Cases (`RP-01` – `RP-10`)

> **Baseline Context:** Earlier physical baseline testing confirmed `NfcF` discovery, `৳ 14` purse balance, and `14` records, and exposed a classification defect where Rapid Pass cards recharged at Metro counters (`1D 60 02 01`) were mislabeled as MRT Pass. That fix (`01:27` `IDm` prefix precedence) and the SQLite `updateCard` rescan-preservation fix are verified via unit & emulator Room tests and await physical Rapid Pass confirmation below.

---

### RP-01: NFC availability and permission/device readiness
- **Test ID:** `RP-01`
- **Purpose:** Verify that TransitMate accurately detects physical NFC hardware, system NFC enabled/disabled toggles, and `android.permission.NFC` readiness on launch and `onResume()`.
- **Preconditions:**
  - Physical Android phone has built-in NFC hardware.
  - `app-debug.apk` is freshly installed (or app storage cleared before test).
- **Step-by-Step Procedure:**
  1. Turn **OFF** NFC in Android Quick Settings / System Settings.
  2. Launch TransitMate and complete Onboarding (`Get Started` / `Skip`).
  3. Inspect `Home` status chip/banner and `Scan` tab UI.
  4. Tap **Open NFC Settings** (or enable NFC from Android Settings) and return to TransitMate without force-closing.
  5. Open **Settings → NFC Diagnostics** and inspect `ADAPTER & READER MODE`.
- **Expected Result:**
  - When NFC is off, `Home` and `Scan` show the `"NFC Disabled"` notice cleanly with a working **Open NFC Settings** action.
  - Returning with NFC turned on updates `Home` header badge to ready/available (`NFC Available`), `Scan` enters waiting polling state without restarting the app, and **NFC Diagnostics** shows NFC Hardware ready with Reader Flags `NFC_F | NFC_A | SKIP_NDEF`.
- **Actual Result:** *Not Tested*
- **Status:** `Not Tested`
- **Evidence:** *Pending screenshot/log*
- **Notes:** Emulator testing cannot validate physical NFC RF toggles.

---

### RP-02: Rapid Pass card detection
- **Test ID:** `RP-02`
- **Purpose:** Confirm physical `NfcF` (FeliCa / JIS X 6319-4) discovery, reliable RF link completion across read windows (`0..9` and `10..19`), and smooth transition from `Scan` to `Scan Result`.
- **Preconditions:**
  - `RP-01` satisfied (NFC enabled).
  - Real physical DTCA **Rapid Pass** card in hand (no thick metal/magnetic case on phone).
- **Step-by-Step Procedure:**
  1. Navigate to the **Scan** tab in TransitMate.
  2. Place the Rapid Pass card flat against the upper-center back NFC antenna and hold steady for `1.5–2.5` seconds.
  3. Wait until the app transitions automatically to **Scan Result**.
  4. Also optionally test holding the card in **Settings → NFC Diagnostics** and confirm `Scan #1`, `Blocks Read` count, and `RF Link OK`.
- **Expected Result:**
  - Card is discovered over `FeliCa (NFC-F)` without premature `TagLost` / `Communication Failure` error sheets when held steady for `1.5–2` seconds.
  - **Scan Result** opens cleanly with detected card metadata (`Scan #1` in Diagnostics shows `RF Link: Connect & Close OK` and up to `20` blocks read).
- **Actual Result:** *Not Tested*
- **Status:** `Not Tested`
- **Evidence:** *Pending screenshot/logcat*
- **Notes:** Record phone antenna sweet spot and hold duration in seconds if multiple attempts are needed.

---

### RP-03: Correct Rapid Pass classification
- **Test ID:** `RP-03`
- **Purpose:** Confirm post-fix `TransitCardBlockDecoder.classifyCard` classifies a physical **Rapid Pass** (`IDm` prefix `01:27`, including cards topped up at MRT Line 6 counters with `1D 60 02 01` headers) as **`Rapid Pass`** (`DTCA · FeliCa (NFC-F)`), NOT **`MRT Pass`** (`DMTCL`).
- **Preconditions:**
  - `RP-02` completed on updated `app-debug.apk`.
- **Step-by-Step Procedure:**
  1. Inspect the card title, authority subtitle, and status pill at the top of **Scan Result**.
  2. Scroll to the **Card Recognition** metadata card at the bottom of **Scan Result**.
- **Expected Result:**
  - Card header displays **`Rapid Pass`**, subtitle shows **`DTCA · FeliCa (NFC-F)`**, status pill reads **`Verified`** (localized cleanly in English or বাংলা), and `Card Recognition` reads **`Rapid Pass`** — even if recent activity includes Metro trips or MRT counter top-ups.
- **Actual Result:** *Not Tested*
- **Status:** `Not Tested`
- **Evidence:** *Pending masked screenshot of Scan Result*
- **Notes:** Core verification target for Phase 06 classification regression fix (`01:27` `IDm` precedence).

---

### RP-04: IDm handling and privacy-safe diagnostics
- **Test ID:** `RP-04`
- **Purpose:** Verify full 8-byte `Card IDm` local display, `Copy IDm` action, presence of `IdmPrivacyCaption` share caution, and isolated developer diagnostics in `Settings → NFC Diagnostics`.
- **Preconditions:**
  - Valid Rapid Pass scan completed (`RP-02`).
- **Step-by-Step Procedure:**
  1. On **Scan Result**, confirm full 8-byte `IDm` (`01:27:XX:XX:XX:XX:XX:XX`) is displayed cleanly inside the Hero card alongside the **Copy IDm** pill button and privacy reminder caption underneath.
  2. Tap **Copy IDm** and verify clipboard content + confirmation feedback.
  3. Navigate to **Settings → NFC Diagnostics**, tap the card once, inspect `Tech List`, `IDm`, `System Code` (`0x90E3` formatting without extra colons), `PMm`, and `IDmPrivacyCaption`.
  4. Verify raw `PMm`, `System Code`, and `SF1/SF2` flags do **NOT** leak onto `Home`, `Scan Result`, `My Cards`, or `Data Management` JSON preview.
- **Expected Result:**
  - Consumer screens show full read-only `Card IDm` with privacy warning and clean UI; developer RF telemetry (`System Code 0x90E3`, `PMm`, block/RF status) appears strictly in **NFC Diagnostics**.
  - `Preview JSON Summary` in **Data Management** shows hashed `digestPrefix` only (zero raw `Card IDm`).
- **Actual Result:** *Not Tested*
- **Status:** `Not Tested`
- **Evidence:** *Pending masked screenshot + copied diagnostic log*
- **Notes:** Mask middle 5 bytes (`01:27:**:**:**:**:**:A0`) before sharing any screenshot or diagnostic text externally.

---

### RP-05: Balance reading and display
- **Test ID:** `RP-05`
- **Purpose:** Verify real-time purse balance decoding from FeliCa service `0x220F` matches the actual Rapid Pass card balance without fabrication or stale mismatch.
- **Preconditions:**
  - Rapid Pass card scanned (`RP-02`).
  - Know actual recent card balance from a metro station AVM, gate display, or official top-up receipt if available.
- **Step-by-Step Procedure:**
  1. Read **`LATEST VERIFIED BALANCE`** (`৳ ...`) on **Scan Result**.
  2. Compare against the latest block post-balance and known physical card balance.
  3. Check formatting in both **English** (`৳ ...`) and **বাংলা** modes.
- **Expected Result:**
  - Balance displays exact verified BDT amount (`৳ <balance>`) matching physical card state (never guessed/placeholder digits).
- **Actual Result:** *Not Tested*
- **Status:** `Not Tested`
- **Evidence:** *Pending masked screenshot + balance cross-check note*
- **Notes:** If card purse is empty or unread, verify balance matches block `0` little-endian bytes `11..13`.

---

### RP-06: Transaction history reading and display
- **Test ID:** `RP-06`
- **Purpose:** Verify trip (`- ৳`) and top-up (`+ ৳`) decoding, station name resolution (Metro & Hatirjheel), timestamps, and chronological ordering.
- **Preconditions:**
  - Rapid Pass card contains real trip/recharge records in service `0x220F`.
- **Step-by-Step Procedure:**
  1. Inspect **Recent Card Activity** on **Scan Result** (`X records` badge).
  2. Check station origin/destination names, fare deltas (`- ৳` vs `+ ৳`), post-balance captions, and timestamps (`Asia/Dhaka +06:00`).
  3. Switch app language to **বাংলা** in **Settings** and confirm station names & top-up labels render cleanly in Bangla without clipping.
- **Expected Result:**
  - Up to `20` blocks decode chronologically (newest first), with accurate BDT deltas, verified station labels in EN and বাংলা, and `null` delta handled honestly for the oldest boundary block without fabricated fares.
- **Actual Result:** *Not Tested*
- **Status:** `Not Tested`
- **Evidence:** *Pending masked screenshot (EN & বাংলা)*
- **Notes:** Check Hatirjheel or unfamiliar station codes (if any show `Station #N`) and note exact station code if unmapped.

---

### RP-07: Repeated scanning of the same card
- **Test ID:** `RP-07`
- **Purpose:** Verify saving a card and scanning the exact same Rapid Pass a **second time** updates the existing card seamlessly without creating duplicate saved card rows.
- **Preconditions:**
  - First Rapid Pass scan (`RP-02`–`RP-06`) completed.
- **Step-by-Step Procedure:**
  1. On **Scan Result** from Scan #1, tap **+ Save to My Cards**, enter a nickname (e.g., `"Daily Rapid Pass"`), confirm **`Rapid Pass`**, and save.
  2. Check **My Cards** (`1` card saved, marked `Primary`) and **Home**.
  3. Tap **Tap Card to Refresh** (from **Card Details**) or open **Scan** and scan the **exact same Rapid Pass card a second time** after `> 2` seconds.
  4. Re-open **My Cards** and **Home**.
- **Expected Result:**
  - **My Cards** still contains **only 1 card** for that Rapid Pass (`cardDigestKey` match), preserving nickname and `Primary` flag while updating `lastScannedAt` and balance.
  - Note: Holding the card stationary for `< 1.2s` is safely debounced without double-triggering scans.
- **Actual Result:** *Not Tested*
- **Status:** `Not Tested`
- **Evidence:** *Pending My Cards / Card Details masked screenshots after Scan #1 and Scan #2*
- **Notes:** Tests `MainViewModel.onConsumerTagOutcome` + `RoomCardRepository.saveOrUpdateCard`.

---

### RP-08: Previously stored transactions remain intact after rescanning
- **Test ID:** `RP-08`
- **Purpose:** Verify that rescanning an already-saved Rapid Pass neither duplicates identical blocks nor triggers SQLite `ON DELETE CASCADE` deletion of older locally persisted transactions (`RoomCardRepository.updateCard` fix).
- **Preconditions:**
  - Card saved with initial transaction count `N` recorded in **History** / **Insights** (`RP-07` Scan #1 complete).
- **Step-by-Step Procedure:**
  1. Note total transaction count on **Insights** (`All Time`) and **Transaction History** after Scan #1 (`N` records).
  2. Perform Scan #2 of the exact same Rapid Pass card (ideally also test after taking a new ride/top-up if possible, or immediate identical rescan).
  3. Inspect **Transaction History** (filtering by this card) and **Insights** (`Total Trips` / records count).
- **Expected Result:**
  - Identical rescan keeps exact count **`N`** (zero duplicate rows thanks to unique `(cardId, rawBlockHex)` index).
  - If new trips occurred, count increases cleanly (`N + k`) and **all older transactions remain intact** (never wiped by `updateCard`).
- **Actual Result:** *Not Tested*
- **Status:** `Not Tested`
- **Evidence:** *Pending History & Insights record counts before/after Scan #2*
- **Notes:** Directly validates the Phase 09 Audit SQLite `OnConflictStrategy.REPLACE` -> `@Update` fix on hardware.

---

### RP-09: Saved card and history persistence after app restart
- **Test ID:** `RP-09`
- **Purpose:** Confirm Room database (`transitmate_local.db`) and DataStore preferences (`themeMode`, `language`, `hasCompletedOnboarding`) persist cleanly across force-stop / cold relaunch with zero network connectivity, and CSV export works safely.
- **Preconditions:**
  - Saved Rapid Pass (`RP-07`) and verified transactions (`RP-08`) exist locally.
- **Step-by-Step Procedure:**
  1. Toggle **Airplane Mode ON** (verify 100% offline operation).
  2. Swipe TransitMate away from Android Recents / Force Stop in Android App Info.
  3. Relaunch TransitMate cold.
  4. Verify **Home**, **My Cards**, **Card Details**, **Transaction History**, and **Insights** (`All Time`).
  5. Open **Settings → Data Management**, confirm saved card & history counts match, and tap **Export History as CSV (UTF-8)** to save a file locally.
- **Expected Result:**
  - App launches directly to **Home** (skipping Onboarding), showing saved Rapid Pass, balance, and all transaction history offline.
  - CSV export saves cleanly without `ActivityResultRegistryOwner` crash and opens with accurate EN/বাংলা columns and zero unmasked unwanted errors.
- **Actual Result:** *Not Tested*
- **Status:** `Not Tested`
- **Evidence:** *Pending cold-restart screenshot + CSV confirmation snackbar/file check*
- **Notes:** Confirms `MainActivity` `LocalActivityResultRegistryOwner` fix + offline Room persistence on device.

---

### RP-10: Error handling, unsupported data handling, and crash-free operation
- **Test ID:** `RP-10`
- **Purpose:** Verify graceful, crash-free UI handling when a card is pulled away too early (`TagLost`), when a non-transit NFC card (e.g., contactless bank card / MIFARE Classic / NDEF tag) is tapped, or when protected/unverified FeliCa blocks are encountered.
- **Preconditions:**
  - NFC enabled (`RP-01`).
  - Optional non-transit NFC card (contactless debit/credit card or office/building RFID keycard) if available.
- **Step-by-Step Procedure:**
  1. On **Scan** tab, tap and immediately pull the Rapid Pass away within `< 0.2s` to trigger an early removal (`TagLost` / IO interruption).
  2. Verify the error bottom sheet message and tap **Try Again**; confirm scanning resumes normally.
  3. Tap a non-transit contactless card (e.g., `IsoDep` EMV bank card) if available and verify **Unsupported Card** sheet appears cleanly without crashing or showing fake transit balance.
  4. Tap **Pause** and **Resume** on **Scan** tab, switch tabs rapidly, and rotate/background-foreground the app to confirm no crash or stuck RF lock.
- **Expected Result:**
  - Early removal shows plain-language **"Hold card steady"** error sheet with working **Try Again** recovery (no crash or fake balance).
  - Non-transit card shows **Unsupported Card** notice (`IsoDep` / non-FeliCa).
  - Unreadable/protected services honestly report **Balance Not Available** / **Unverified** — never fabricated balances or trips.
- **Actual Result:** *Not Tested*
- **Status:** `Not Tested`
- **Evidence:** *Pending screenshot/logcat of error recovery*
- **Notes:** Also record `adb logcat` if any OEM NFC quirk occurs.

---

## 2. Physical Test Plan — MRT Pass (`DMTCL · FeliCa / NFC-F`)

> **Blocker Note:** Physical **MRT Pass** card is **not currently available** in the test environment. All MRT Pass hardware verification cases (`MRT-01`–`MRT-05`) remain **`Blocked`** until a real physical MRT Pass sample is obtained.

| ID | Purpose | Preconditions | Expected Result | Actual Result | Status | Evidence | Notes |
|---|---|---|---|---|---|---|---|
| **MRT-01** | Card detection & RF identity via **Settings → NFC Diagnostics** | NFC enabled, physical MRT Pass available | Detects `NfcF` (`Scan #1`), captures 8-byte `IDm` (record prefix!), `System Code`, and `PMm`. | *Physical MRT Pass not available* | `Blocked` | *Pending redacted diagnostic screenshot/log* | Card unavailable in test lab |
| **MRT-02** | FeliCa command probe (`0x06 / 0x220F`) | `MRT-01` detected | Verifies whether `Read Without Encryption (0x06)` on service `0x220F` succeeds (`SF1=0x00, SF2=0x00`) or returns protected status flags (`SF1/SF2 != 0`). | *Physical MRT Pass not available* | `Blocked` | *Pending `SF1/SF2` & `Blocks Read` log* | Never bypass protected encryption |
| **MRT-03** | Card classification (`MRT Pass · DMTCL`) | Non-`01:27` `IDm` with `1D 60 02 01` or `08 52 10 00` blocks | Classifies as **`MRT Pass`** (`DMTCL · FeliCa (NFC-F)`). Confirm vendor `IDm` prefix for MRT Pass cards. | *Physical MRT Pass not available* | `Blocked` | *Pending Scan Result screenshot* | Confirm prefix vs decoder rules |
| **MRT-04** | Balance & transaction history feasibility | `MRT-02` returns readable blocks | Confirms whether MRT Pass blocks match the verified DTCA 16-byte purse/station layout (`10..90`) or require separate read layout evidence. | *Physical MRT Pass not available* | `Blocked` | *Pending AVM balance cross-check* | Do not guess block offsets |
| **MRT-05** | Protected/unsupported data honest fallback | Encrypted/unknown MRT Pass service response | Displays **`Balance Not Available`** / **`Unverified`** honestly with zero fabricated numbers and zero crashes. | *Physical MRT Pass not available* | `Blocked` | *Pending screenshot* | Privacy & honesty check |

---

## 3. ADB Logcat Capture Reference (Optional for Troubleshooting)

```bash
# Clear log buffer and filter TransitMate + NFC runtime output during physical tap tests:
adb logcat -c && adb logcat | grep -iE "TransitMate|Nfc|FeliCa|AndroidRuntime"
```
> **Privacy Rule:** Before sharing screenshots or terminal logs in chat, docs, or GitHub, verify the middle 5 bytes of any 8-byte `IDm` are masked (e.g., `01:27:**:**:**:**:**:A0`).
