# TransitMate — Phase 06 Technical Report: FeliCa Balance Reading Investigation, MRT Buddy Reference Audit & Implementation

**Date:** 17 November 2026  
**Project:** TransitMate (`com.transitmate.bd`)  
**Phase Status:** In Progress (Code Implemented · Unit Tested · Pending Physical Rapid Pass Verification)

---

## 1. MRT Buddy Repository Audit Summary

The reference repository `https://github.com/aniruddha-adhikary/mrt-buddy` was cloned into the external reference workspace outside TransitMate's Git repository (`../_external_reference/mrt-buddy/`) for protocol and architecture research only.

Key findings from reading the full NFC/FeliCa stack in `mrt-buddy`:
- Both **Rapid Pass** (DTCA) and **MRT Pass** (DMTCL) use FeliCa (`android.nfc.tech.NfcF`) with unencrypted history service blocks accessible via standard FeliCa **Read Without Encryption** (`0x06` command / `0x07` response).
- Purse balance is NOT read from a standalone "balance-only" register; instead, the **post-transaction purse balance in BDT** is stored directly inside every 16-byte transaction log record under **Service Code `0x220F`**.

---

## 2. License Findings & Compliance

- **Reference License:** `../_external_reference/mrt-buddy/LICENCE` is **GNU General Public License v3.0 (GPL-3.0)**, Copyright (c) Aniruddha Adhikary 2024–2025.
- **Compliance Decision:** Direct copying, porting, or modifying GPL-3.0 code into TransitMate would require licensing the entire TransitMate application under GPL-3.0 upon distribution. Per project rules (`AI_AGENT_INSTRUCTIONS.md` & Section 2 directive), **zero GPL-3.0 source files or functions were copied**.
- **Clean-Room Execution:** All wire-level protocol framing (`FelicaProtocol`), block decoding (`TransitCardBlockDecoder`), transceiver abstractions (`FelicaBlockTransceiver`), UI, and Room persistence in TransitMate were independently authored from public JIS X 6319-4 FeliCa protocol rules and empirical byte-layout analysis.

---

## 3. Relevant Reference Files & Function-by-Function Explanation

### Reference Stack Audited (`../_external_reference/mrt-buddy/`)
1. `NfcManager.android.kt`:
   - `enableReaderMode(..., FLAG_READER_NFC_F)` invokes `readerCallback(tag)` -> `readFelicaCard(tag)`.
   - Connects `NfcF.get(tag)`, invokes `FelicaReader(NfcFTransceiver(nfcF)).readTransactionHistory()`, takes `transactions.firstOrNull()?.balance` as the latest card balance.
2. `FelicaReader.kt`:
   - `readTransactionHistory()`: Reads two 10-block windows (`startBlock = 0, count = 10` and `startBlock = 10, count = 10`) on Service Code `0x220F`. Catches `CardTransceiverException` after partial progress to preserve already-read windows.
3. `NfcCommandGenerator.kt`:
   - `generateReadCommand()`: Encodes FeliCa `0x06` Read Without Encryption packet: `[LEN, 0x06, IDm(8), 0x01, scLow(0x0F), scHigh(0x22), numBlocks, (0x80, blockIndex)...]`.
4. `FelicaFrameDecoder.kt`:
   - `decode()`: Validates minimum 13-byte header (`[LEN, 0x07, IDm(8), SF1, SF2, numBlocks]`), checks `SF1 == 0 && SF2 == 0`, splits remaining payload into 16-byte blocks.
5. `TransactionParser.kt` & `ByteParser.kt` & `TimestampService.kt` & `StationService.kt`:
   - `parseTransactionBlock()`: Extracts 4-byte fixed header (`0..3`), 24-bit Big-Endian packed timestamp (`4..6`), `fromStation` (`byte 8`), `toStation` (`byte 10`), and 24-bit Little-Endian BDT balance (`bytes 11..13`).
   - Filters entries predating `2020-01-01`.
6. `TransactionRepository.kt`:
   - Calculates signed trip/recharge amounts by comparing adjacent chronological blocks (`current.balance - older.balance`).
7. `HeuristicsUtils.kt` & `Transaction.kt`:
   - Identifies Rapid Pass via `IDm` prefix `01 27` (`01:27:...`) and top-up/Hatirjheel headers (`42 60 02 00`, `08 D2 20 00`, `42 D6 30 00`) vs MRT Pass top-up header (`1D 60 02 01`).

---

## 4. TransitMate vs MRT Buddy Comparison

| Dimension | TransitMate (Before Phase 06) | MRT Buddy (Reference) | TransitMate (After Phase 06) |
|---|---|---|---|
| **Reader Flags** | `NFC_F \| NFC_A \| SKIP_NDEF \| NO_SOUNDS` | `FLAG_READER_NFC_F` | Preserved `NFC_F \| NFC_A \| SKIP_NDEF \| NO_SOUNDS` |
| **FeliCa `transceive()`** | None (`connect()` + `close()` only) | `0x06` on Service `0x220F`, blocks `0..9` & `10..19` | Clean-room `FelicaProtocol` `0x06` on `0x220F`, windows `0..9` & `10..19` |
| **Service Auth Required** | Unverified / Assumed pending | Unencrypted `Read Without Encryption` (`0x06`) | Unencrypted `0x06` (`SF1/SF2` status validated honestly) |
| **Balance Source** | None (`Unavailable`) | Latest valid block `bytes 11..13` (LE uint24) | Latest valid block `bytes 11..13` (LE uint24) |
| **IDm Display** | Masked (`01:27:**:**:**:**:**:A0`) | Unmasked Hex (`01 27 ...`) | Full unmasked colon-hex (`01:27:XX:XX:XX:XX:XX:A0`) + Copy action |
| **License Model** | Clean-room proprietary/custom | GPL-3.0 Copyleft | Clean-room independent implementation |

---

## 5. Root Cause Analysis (Evidence-Based Evaluation of All 10 Hypotheses)

Baseline evidence from physical Rapid Pass test screenshot (`media_1794882706212_59bcd845.jpg` and prior Phase 03 diagnostic capture):
- `NfcF` discovered cleanly (`System Code: 0x90E3`, `PMm: 01:46:42:45:45:46:9B:FF`, `IDm: 01:27:**:**:**:**:**:A0`).
- Card Detection succeeded, while Card Recognition stayed `Unverified` and Balance stayed `Not Available`.

Audit of the 10 candidate causes:

1. **Incomplete FeliCa Polling — NOT A BLOCKER:** Android NFC discovery + `NfcF.get(tag)` already completed FeliCa anticollision and populated `IDm`, `PMm`, and `System Code 0x90E3`.
2. **Wrong System Code / Service Code — CONTRIBUTED BY OMISSION:** TransitMate never queried Service Code `0x220F` (`DTCA` transaction history service).
3. **Wrong Block Number / Block List — PRIMARY CAUSE (NOT SENT):** TransitMate did not transmit any block list read packet (`0x80, 0x00..0x09` / `0x80, 0x0A..0x13`).
4. **Wrong Command / Response Parsing — PRIMARY CAUSE (NOT IMPLEMENTED):** [NfcDiagnosticReader.kt](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/app/src/main/kotlin/com/transitmate/bd/core/nfc/NfcDiagnosticReader.kt) previously executed only `nfcF.connect()` followed immediately by `nfcF.close()` with zero `transceive()` calls, while [NfcTagScanMapper.kt](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/app/src/main/kotlin/com/transitmate/bd/core/nfc/NfcTagScanMapper.kt) hardcoded `CardRecognitionStatus.UNVERIFIED` and `BalanceAvailability.Unavailable(UNVERIFIED_CARD_FORMAT)`.
5. **Byte Order / Endianness — ADDRESSED FOR IMPLEMENTATION:** DTCA FeliCa blocks use **Big-Endian** for 24-bit packed timestamps (`bytes 4..6`), **Little-Endian** for 16-bit service code (`0x0F, 0x22`) in commands, and **Little-Endian** for 24-bit BDT balance (`bytes 11..13`).
6. **Status Flag / Length Handling — IMPLEMENTED:** Non-zero `SF1`/`SF2` flags indicate protected/missing service blocks and must never be treated as valid zero balances.
7. **Incomplete Card Type Recognition — CONFIRMED SECONDARY CAUSE:** `IDm` prefix `01:27` (matching owner's physical Rapid Pass) and FeliCa block headers (`42 60 02 00`, `08 D2 20 00`, `42 D6 30 00`, `1D 60 02 01`) were never evaluated.
8. **Balance Stored in Transaction Logs Rather Than Balance-Only Block — KEY ARCHITECTURAL INSIGHT:** Latest balance lives at bytes `11..13` of the newest valid history block in Service `0x220F`.
9. **Encryption / Authentication Blocking — NOT PRESENT FOR `0x220F`:** Service `0x220F` supports FeliCa **Read Without Encryption** (`0x06`) without secret keys.
10. **Reader Session Lifecycle Issue — PARTIAL IMPROVEMENT:** Adding a 1500 ms transceive timeout (`runCatching { nfcF.timeout = 1500 }`), `READING` feedback state, and partial window preservation (keeping blocks `0..9` if blocks `10..19` drop mid-read) prevents brittle read failures.

---

## 6. Implemented Changes (TransitMate Clean-Room Stack)

- **Protocol & Block Decoder (Pure Kotlin):**
  - [FelicaProtocol.kt](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/app/src/main/kotlin/com/transitmate/bd/core/nfc/FelicaProtocol.kt): Builds `0x06` Read Without Encryption packets (`0x220F`), validates `0x07` responses, matches `IDm`, enforces `SF1 == 0 && SF2 == 0`, splits 16-byte blocks.
  - [TransitCardBlockDecoder.kt](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/app/src/main/kotlin/com/transitmate/bd/core/nfc/TransitCardBlockDecoder.kt): Decodes timestamps (`Asia/Dhaka` `UTC+06:00`), rejects zero/pre-2020/invalid calendar blocks, maps MRT Line 6 & Hatirjheel station codes (EN/বাংলা), extracts Little-Endian 24-bit BDT balances, computes adjacent trip/top-up deltas (leaving oldest-entry delta `null`), and recognizes Rapid Pass / MRT Pass.
  - [FelicaBlockTransceiver.kt](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/app/src/main/kotlin/com/transitmate/bd/core/nfc/FelicaBlockTransceiver.kt): Clean hardware interface enabling JVM unit testing without an Android phone.
- **NFC Reader & Mapper Upgrades:**
  - [NfcDiagnosticReader.kt](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/app/src/main/kotlin/com/transitmate/bd/core/nfc/NfcDiagnosticReader.kt): Reads windows `0..9` and `10..19` with partial-window preservation, explicit `TagLostException` vs `IOException` separation, and `finally { transceiver.close() }`.
  - [NfcTagScanMapper.kt](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/app/src/main/kotlin/com/transitmate/bd/core/nfc/NfcTagScanMapper.kt): Maps verified balances, empty-history (`READ_EMPTY_NO_HISTORY`), protected (`PROTECTED_OR_UNREADABLE`), and unverified states honestly.
- **Room Database & ViewModel Persistence:**
  - Upgraded [TransitMateDatabase.kt](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/app/src/main/kotlin/com/transitmate/bd/core/database/TransitMateDatabase.kt) to `version = 2` with `MIGRATION_1_2`, adding full `cardIdm`, station codes, `postBalanceBdt`, `rawBlockHex` unique deduplication index per card, and `sequenceOrder`.
  - Updated [MainViewModel.kt](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/app/src/main/kotlin/com/transitmate/bd/feature/main/MainViewModel.kt) to surface `READING` phase, auto-refresh already-saved cards & transactions on rescan, and persist full IDm + balance + transactions on save.

---

## 7. Full IDm Display Changes

- Removed `redactIdm()` (`**`) and replaced `maskedIdentifier` / `idmRedacted` across domain, Room, and UI with full colon-separated uppercase hex `cardIdm` / `idmHex` (`01:27:XX:XX:XX:XX:XX:A0`).
- Preserved SHA-256 `cardDigestKey` solely as an internal Room uniqueness key and prefix in sanitized JSON export (never substituted for IDm in UI).
- Added [FullIdmRow](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/app/src/main/kotlin/com/transitmate/bd/core/designsystem/component/TransitComponents.kt) with a one-tap **Copy IDm** button + Snackbar in [ScanResultScreen.kt](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/app/src/main/kotlin/com/transitmate/bd/feature/scanresult/ScanResultScreen.kt), [CardDetailsScreen.kt](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/app/src/main/kotlin/com/transitmate/bd/feature/carddetails/CardDetailsScreen.kt), and [NfcDiagnosticsScreen.kt](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/app/src/main/kotlin/com/transitmate/bd/feature/diagnostics/NfcDiagnosticsScreen.kt).

---

## 8. Scan Result & Card UI Improvements

- Redesigned [ScanResultScreen.kt](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/app/src/main/kotlin/com/transitmate/bd/feature/scanresult/ScanResultScreen.kt) around TransitMate's existing Warm Off-White Light / Neutral Graphite Dark design system:
  - Eliminated duplicate detection/technology banners.
  - Placed **Latest Verified Balance** (`৳ ...` with tabular numerals `tnum`) as the dominant focal element inside the top Hero Pass Card (or calm, clear unavailable reason callout when unreadable/empty).
  - Rendered **Card Recognition** (`Rapid Pass` / `MRT Pass` vs `Unverified`) separately from **Balance Availability**.
  - Added live **Recent Card Activity** preview directly on Scan Result when transactions are extracted from the card.
  - Shared localized [VerifiedTransactionRow](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/app/src/main/kotlin/com/transitmate/bd/core/designsystem/component/TransitComponents.kt) across `ScanResult`, `Home`, `CardDetails`, `TransactionHistory`, and `Insights` using `LocalConfiguration` for instant EN/বাংলা updates.

---

## 9. Unit Test Results

Executed cleanly after the final code edit:
```sh
./gradlew clean testDebugUnitTest lintDebug assembleDebug --no-daemon
```
- **Total:** **40 tests passed, 0 failed, 0 skipped**
  - `FelicaProtocolTest`: 5/5 passed
  - `TransitCardBlockDecoderTest`: 4/4 passed
  - `NfcFCardReaderTest`: 4/4 passed
  - `NfcTagScanMapperTest`: 7/7 passed
  - `NfcDiagnosticModelTest`: 4/4 passed
  - `DefaultNfcAvailabilityRepositoryTest`: 2/2 passed
  - `StaticTransitInfoRepositoryTest`: 4/4 passed
  - `CardTypeTest`: 3/3 passed
  - `MainViewModelTest`: 3/3 passed
  - `StartupViewModelTest`: 4/4 passed

---

## 10. Build & Android Lint Results

- **Build Outcome:** `BUILD SUCCESSFUL in 58s` (exit code `0`)
- **Android Lint:** **0 errors**, 22 warnings
- **APK Artifact:** `app/build/outputs/apk/debug/app-debug.apk` (20 MB)

---

## 11. Physical Device & Emulator Test Results

- **Baseline Physical Evidence (Prior to Phase 06 Code):** Physical Rapid Pass tap proved `NfcF` discovery, `System Code 0x90E3`, `PMm 01:46:42:45:45:46:9B:FF`, and `IDm` beginning `01:27:...` while showing `Balance Not Available` due to missing `0x220F` reads.
- **Emulator Limitation:** Android Virtual Devices lack RF NFC simulation hardware; real FeliCa `0x06` transceive and Rapid Pass purse reading cannot be executed on an emulator.
- **Current Phase 06 Physical Verification:** **Pending Owner Physical Device Test** using the newly built `app-debug.apk` and physical Rapid Pass card.

---

## 12. Known Limitations

- Transaction timestamps on FeliCa DTCA cards encode year/month/day/hour without minute/second granularity (`sequenceOrder` preserves intra-hour card ring-buffer order).
- The oldest transaction remaining in a card's 20-block buffer has no prior block balance to compute fare difference (`amountBdt == null`, displaying post-transaction balance instead).
- Hatirjheel bus stop codes outside the 5 verified reference stops render safely as `"Station #<code>"` rather than failing.

---

## 13. Remaining Blockers

1. **Physical Rapid Pass End-to-End Tap Confirmation (Awaiting Test Execution):** Needs one physical scan on the owner's NFC Android phone running Phase 06 `app-debug.apk` to confirm live balance and transaction rows on hardware.
2. **Physical MRT Pass Card (`BLOCKED — CARD NOT AVAILABLE`):** Physical MRT Pass remains unavailable in the test lab, so MRT Pass parsing (`1D 60 02 01` / MRT IDm range) cannot yet be marked physically verified.

---

## 14. Next Recommended Task

1. Install `app/build/outputs/apk/debug/app-debug.apk` onto the NFC-enabled Android phone.
2. Open **Scan**, hold the physical **Rapid Pass** steady against the upper back of the phone for 1–2 seconds, and verify:
   - Full unmasked 8-byte `IDm` (with Copy button)
   - `Rapid Pass` recognition badge
   - Prominent verified Balance (`৳ ...`)
   - Parsed recent journeys/top-ups & saving to **My Cards** / Room DB.
3. Also verify **Settings → NFC Diagnostics** (`SF1=0x00 SF2=0x00`, `Blocks Read`, `Full Card IDm`) if any block is empty or unexpected.
