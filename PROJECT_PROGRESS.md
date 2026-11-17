# TransitMate — Project Progress & Verification Log

**Document Version:** 1.5  
**Last Updated:** 17 November 2026, 08:55 +06:00  
**Responsible Agent:** Antigravity Autonomous Coding Agent  
**Current Phase:** Phase 06 In Progress — Clean-Room FeliCa Balance Reading & Full IDm Implemented · Unit Tested (40/40) · Pending Physical Rapid Pass Device Verification

---

## 1. Phase Status Overview

| Phase | Description | Status | Evidence / Notes |
|---|---|---|---|
| **Phase 01** | Project Identity & Master Planning | **Completed** | Baseline specifications approved in `PROJECT_MASTER_PLAN.md` |
| **Phase 02** | Complete Initial Project Foundation & Configuration | **Completed** | Verified clean build, 8 unit tests passed, launcher icons, CI, APK generated |
| **Phase 03** | NFC / FeliCa Research & Physical Validation | **Completed (Rapid Pass Discovery)** | Rapid Pass physical tap confirmed `NfcF`, 8-byte `IDm`, `System Code 0x90E3`, `PMm 01:46:42:45:45:46:9B:FF`; MRT Pass remains **Blocked** |
| **Phase 04** | Complete UI/UX Planning & Visual Design | **Completed** | Audit (`PROJECT_UI_AUDIT.md`), specs (`design/DESIGN_SYSTEM.md`, `design/SCREEN_REQUIREMENTS.md`), and **87 verified visual PNG assets** |
| **Phase 05 & Stage 6** | Production Android App, NFC Isolation & Offline Core | **Completed** | M3 Light/Dark theme, 5-tab navigation, Room + DataStore, 16-station Fare Calculator & Station Explorer, bilingual EN/বাংলা |
| **Phase 06** | FeliCa Balance Reading, MRT Buddy Audit & Full IDm | **In Progress / Testing** | **Code Implemented** (clean-room `FelicaProtocol` `0x06`/`0x220F`, `TransitCardBlockDecoder`, full IDm display, `ScanResultScreen` redesign, Room `v2` deduplicated transactions) · **Unit Tested** (40/40 passed, 0 lint errors) · **Physical Device Verified: Pending** |
| **Phase 07** | Core Product Features (UI & Persistence) | **Completed** | Local Room card manager, deduplicated verified transaction storage, rename/favorite/delete workflows |
| **Phase 08** | Advanced Offline Transit Features | **Completed** | 16-station MRT Line 6 Explorer (`UTN`–`MTJ`), official DMTCL fare calculator (10% Smart Card discount), local JSON export |
| **Phase 09** | Quality Assurance & Physical Testing | **In Progress** | Prior Stage 6 Light/Dark/EN/বাংলা verified on `Pixel_9` AVD; Phase 06 physical Rapid Pass RF test pending on owner's NFC phone |
| **Phase 10** | Release & Maintenance | **Not Started** | Play Store listing, signed release |

---

## 2. Task Breakdown (Phase 03)

### Completed Tasks
- [x] **Workstream A: Android NFC/FeliCa Research:** Completed exhaustive investigation of ISO/IEC 18092, JIS X 6319-4, `android.nfc.tech.NfcF`, Reader Mode vs. Foreground Dispatch, threading, timeouts, and error handling.
- [x] **Workstream B: Rapid Pass Technical Analysis:** Completed investigation of DTCA Automated Fare Collection (AFC), JICA FeliCa standard, official app (`com.bd.rapidpass.app`) analysis, and offline purse storage vs. central server data segregation.
- [x] **Workstream C: Reference Project Audit (MRT Buddy):** Audited `aniruddha-adhikary/mrt-buddy`. Verified GPL-3.0 license obligations; enforced strict clean-room engineering with zero code copying. Cataloged protocol constants under `REFERENCE-OBSERVED — NOT YET VERIFIED ON OUR RAPID PASS CARD`.
- [x] **Workstream D: Physical Test Preparation & Protocol:** Created reproducible physical testing procedures (Test Cases 1–5), ADB diagnostic commands, and sensitive data redaction policies. Formally documented MRT Pass as `BLOCKED — CARD NOT AVAILABLE`.
- [x] **Minimal Diagnostic NFC Harness:** Implemented `NfcDiagnosticReader`, `NfcDiagnosticData`, and live diagnostic output card on `StartupScreen` using modern Android Reader Mode.
- [x] **Research Deliverables Authored:**
  - `docs/research/PHASE_03_NFC_TECHNICAL_RESEARCH.md`
  - `docs/research/PHASE_03_PHYSICAL_VALIDATION.md`
  - `docs/research/PHASE_03_FEASIBILITY_DECISION.md`
- [x] **Unit Testing & Verification:** Added `NfcDiagnosticModelTest` (4 tests) and updated `StartupViewModelTest` (4 tests). Total project test suite: 13/13 tests passed.

### In-Progress Tasks
- [ ] **Physical Card Test Execution:** Owner to tap physical Rapid Pass card against NFC-enabled Android phone running `app-debug.apk` to record actual live `IDm`, `SystemCode`, and `PMm`.

### Blocked Tasks
- [ ] **MRT Pass Physical Testing:** `BLOCKED — CARD NOT AVAILABLE`. Physical card sample not present in test lab.

---

## 3. Files Created & Modified (Phase 03)

### New Research Documentation
- `docs/research/PHASE_03_NFC_TECHNICAL_RESEARCH.md` (Low-level RF and Android API investigation)
- `docs/research/PHASE_03_PHYSICAL_VALIDATION.md` (Physical test protocol and ADB diagnostic commands)
- `docs/research/PHASE_03_FEASIBILITY_DECISION.md` (Independent capability matrix and architectural decisions)

### New & Modified Code Files
- `app/src/main/kotlin/com/transitmate/bd/core/nfc/NfcDiagnosticModel.kt` (Diagnostic data model with hex formatting & IDm redaction)
- `app/src/main/kotlin/com/transitmate/bd/core/nfc/NfcDiagnosticReader.kt` (Isolated Reader Mode NFC harness)
- `app/src/main/kotlin/com/transitmate/bd/feature/startup/StartupUiState.kt` (Added diagnostic state fields)
- `app/src/main/kotlin/com/transitmate/bd/feature/startup/StartupViewModel.kt` (Added diagnostic tag scan handlers)
- `app/src/main/kotlin/com/transitmate/bd/feature/startup/StartupScreen.kt` (Added diagnostic output card UI)
- `app/src/main/kotlin/com/transitmate/bd/MainActivity.kt` (Bound diagnostic reader lifecycle in onResume/onPause)
- `app/src/main/res/values/strings.xml` (Added diagnostic string resources)

### Unit Tests
- `app/src/test/kotlin/com/transitmate/bd/core/nfc/NfcDiagnosticModelTest.kt` (4 tests: hex formatting, null safety, privacy redaction)
- `app/src/test/kotlin/com/transitmate/bd/feature/startup/StartupViewModelTest.kt` (4 tests: updated with diagnostic event assertions)

---

## 4. Empirical Build & Verification Evidence

All commands were executed locally from the workspace root:

### 1. Unit Tests
```bash
./gradlew clean testDebugUnitTest --no-daemon
```
**Outcome:** `BUILD SUCCESSFUL in 9s` (25 actionable tasks executed)  
**Report Summary:**
- `NfcDiagnosticModelTest`: 4 tests passed (0 failures, 0 errors)
- `DefaultNfcAvailabilityRepositoryTest`: 2 tests passed (0 failures, 0 errors)
- `CardTypeTest`: 3 tests passed (0 failures, 0 errors)
- `StartupViewModelTest`: 4 tests passed (0 failures, 0 errors)
- **Total:** 13 tests passed, 0 failed, 0 skipped.

### 2. Debug APK Assembly
```bash
./gradlew assembleDebug --no-daemon
```
**Outcome:** `BUILD SUCCESSFUL in 5s` (36 actionable tasks executed)  
**Artifact:** `app/build/outputs/apk/debug/app-debug.apk` (Size: 12 MB)

### 3. Security Remediation (12 Nov 2026)
- **Incident:** Foreign upstream commit `32765c9` contained malicious `.vscode` scripts, a `service` Trojan, and command-injection hooks (`startApi`) in `build.gradle.kts` and `app/build.gradle.kts`.
- **Action Taken:** Complete excision of all malicious files, removal of Gradle hooks, verified clean builds and unit tests (13/13 passing).
- **Commit:** `abb538f` ("fix(security): purge unauthorized build hooks and malicious scripts") pushed to `origin/main`.

---

## 5. Technical Decisions & Rationale (Phase 03)

1. **Clean-Room Isolation from GPL-3.0 Reference:** An audit of `aniruddha-adhikary/mrt-buddy` confirmed it is licensed under GPL-3.0. To prevent license contagion and guarantee proprietary/custom license flexibility, TransitMate strictly prohibits code copying. All architecture and NFC readers are independently authored.
2. **Reader Mode Adoption:** Used `NfcAdapter.enableReaderMode` over legacy `enableForegroundDispatch`. Reader mode provides background binder thread delivery, suppresses intrusive system chimes, and filters exclusively for `NFC_F` and `NFC_A` carriers.
3. **No Premature Memory Reading:** The diagnostic harness strictly limits itself to anticollision polling metadata (`Tag.id` / `IDm`, `systemCode`, `manufacturer` / `PMm`). No service blocks are queried until physical presence of `android.nfc.tech.NfcF` is established.

---

## 6. Phase 04 UI/UX Planning & Visual Design Progress (16 Nov 2026)

### Completed Tasks (Stages 1–5)
- [x] **Physical Rapid Pass Discovery Logged:** Real Android scan screenshot confirmed `NfcF`, 8-byte redacted `IDm` (`01:27:**:**:**:**:**:A0`), `System Code` (`0x90E3`), and `PMm` (`01:46:42:45:45:46:9B:FF`).
- [x] **Stage 1 — Project & Reference Audit:** Audited existing Compose code, NFC Reader Mode flow, and 5 reference PNGs. Deliverable: `PROJECT_UI_AUDIT.md`.
- [x] **Stage 2 — Design System Finalization:** Defined Neutral Graphite Dark (`#111315`/`#191C1F`) and Warm Off-White Light (`#F6F7F8`/`#FFFFFF`) tokens, Muted Transit Teal (`#2FA497`/`#12746A`), Bangla-safe typography (`tnum`), spacing grid, iconography, and motion specs. Deliverable: `design/DESIGN_SYSTEM.md`.
- [x] **Stage 3 — Complete Screen & UX Specification:** Documented all 15 Core Screens (20 criteria each), 25 Supporting States, 5-destination navigation graph (`Home | My Cards | Scan | Insights | Settings`), and strict consumer vs. developer diagnostics isolation. Deliverables: `design/SCREEN_REQUIREMENTS.md`, `docs/UI_UX_DESIGN_SPECIFICATION.md`.
- [x] **Stage 4 — Complete Visual Design Generation (87 PNG Assets):**
  - Preserved all 5 original reference PNGs untouched and copied verbatim into `design/ui_designs/references/` (SHA-256 verified).
  - Generated 7 Design System visual boards (`design/ui_designs/design_system/`).
  - Generated all **15 Core Screens** in paired Light & Dark Android Material 3 (`780x1688 px` / `390x844 @2x`) assets (`01_splash/` through `15_about/`).
  - Generated all **25 Supporting States** in paired Light & Dark variants (`supporting_states/{nfc,card,balance,history,empty_states,dialogs,bottom_sheets}/` + `13_settings/data_management_{light,dark}.png`).
- [x] **Stage 5 — Cross-Screen Consistency Audit:** Verified 20-point checklist (color parity, zero neon/glow, zero emojis, zero fake balances, Bangla script safety, consistent 5-tab bar, and strict NFC diagnostics isolation). Deliverable: `design/ui_designs/DESIGN_GENERATION_REPORT.md`.

### Verification Note (End of Phase 04)
- Zero production Kotlin code, existing NFC readers (`NfcDiagnosticReader`, `NfcAvailabilityChecker`), Gradle config, or unit tests were modified.
- Unit tests and clean debug build verified cleanly after all Phase 04 work (see command check below).

---

## 7. Stage 6 — Production Android Implementation & NFC Issue Resolution (16 Nov 2026)

### Root Causes Resolved
1. **System Code Hex Formatting:** Added `NfcDiagnosticModel.formatSystemCodeHex` so 2-byte FeliCa system codes format as `0x90E3` instead of `0x90:E3`.
2. **Developer Diagnostics Isolation:** Moved raw `IDm`, `PMm`, `System Code`, `techList`, and RF traces out of the primary launch surface (`StartupScreen`) into `Settings -> NFC Diagnostics` (`NfcDiagnosticsScreen`), accessible in both debug and release builds.
3. **Reader Mode Robustness:** Upgraded `NfcDiagnosticReader` with `AtomicInteger` scan counters, `TagLostException` vs `IOException` separation, 1200 ms same-tag debounce, and lifecycle/route-aware Reader Mode activation in `MainActivity` (active only on `Scan` when not paused, or `NFC Diagnostics`).
4. **Honest Outcome Mapping (`NfcTagScanMapper`):** Enforced strict `Detection ≠ Recognition ≠ Balance ≠ Transactions`; discovered `NfcF` cards stay `UNVERIFIED` with `BalanceAvailability.Unavailable` until Phase 06 block parsing is validated.
5. **Compose Launcher Logo & Edge-to-Edge Insets:** Added `res/drawable-nodpi/ic_transitmate_logo.png` (replacing adaptive-icon Compose crash risk) and explicit `statusBarsPadding()` on non-top-bar routes (`OnboardingScreen`, `HomeScreen`).

### Implemented Architecture & Modules
- **Build & Dependencies (`gradle/libs.versions.toml`, `app/build.gradle.kts`):** `navigation-compose:2.9.8`, `datastore-preferences:1.2.1`, `room-runtime/ktx/compiler:2.8.5`, `ksp:2.3.14`, `material-icons-extended`, `localeFilters += listOf("en", "bn")`, `buildConfig = true`.
- **Domain & Data (`domain/`, `core/database/`, `data/`):** Pure Kotlin domain models (`CardScanModels.kt`, `TransitLineModels.kt`), `TransitMateDatabase` (Room v1 with `SavedCardEntity` and `VerifiedTransactionEntity`), `DataStorePreferencesRepository`, and offline `StaticTransitInfoRepository` (all 16 DMTCL MRT Line 6 stations `UTN`–`MTJ` and official fare chart with 10% Smart Card discount).
- **Design System & Localization (`core/designsystem/`, `res/values*/strings.xml`):** Neutral Graphite Dark & Warm Off-White Light M3 theme, Muted Teal accents, tabular numerals, Bangla-safe typography, and complete EN & বাংলা translations.
- **Navigation & Screens (`feature/`, `TransitMateNavHost`):** 5-tab Navigation Compose (`Home | My Cards | Scan | Insights | Settings`) covering all 15 Core Screens plus `DataManagementScreen` and all 25 Supporting States.

### Automated Build, Lint & Unit Test Verification (16 Nov 2026)
Executed after the final code edit:
- **Command:** `./gradlew clean testDebugUnitTest lintDebug assembleDebug --no-daemon` -> `BUILD SUCCESSFUL in 44s` (exit code `0`)
- **Unit Tests (`27/27 passed, 0 failed, 0 skipped`):**
  - `NfcDiagnosticModelTest`: 6/6 passed
  - `NfcTagScanMapperTest`: 5/5 passed
  - `DefaultNfcAvailabilityRepositoryTest`: 2/2 passed
  - `StaticTransitInfoRepositoryTest`: 4/4 passed
  - `CardTypeTest`: 3/3 passed
  - `MainViewModelTest`: 3/3 passed
  - `StartupViewModelTest`: 4/4 passed
- **Android Lint:** `0 errors, 18 warnings`
- **APK Built:** `app/build/outputs/apk/debug/app-debug.apk`

### Emulator & Hardware Verification Boundaries
- **Verified on `Pixel_9` AVD (`emulator-5554`):** Onboarding, Home, My Cards, Scan (`No NFC` honest banner & disabled scan action), Insights, Transaction History, Fare Calculator (`Uttara North` -> `Motijheel`: `৳ 90` Pass / `৳ 100` Single Journey), Station Explorer & Station Detail Sheet, Settings (live Light/Dark theme + EN/বাংলা locale switching), Data Management, Help & FAQ, About, and NFC Diagnostics.
- **Physical NFC Hardware Boundary:** Android emulator lacks RF NFC simulation; physical Rapid Pass anticollision discovery (`NfcF`, `0x90E3`) remains verified from prior Phase 03 hardware testing, while physical **MRT Pass** remains `BLOCKED — CARD NOT AVAILABLE` and live physical balance/history parsing belongs to Phase 06.

---

## 8. Phase 06 — FeliCa Balance Reading Investigation, MRT Buddy Audit & Clean-Room Implementation (17 Nov 2026)

### Status Matrix
- **Reference Audit (`mrt-buddy`):** **Completed** (`../_external_reference/mrt-buddy/`, kept strictly outside project Git repo; GPL-3.0 compliance enforced with zero copied code)
- **Root Cause Analysis:** **Completed** (`docs/PHASE_06_FELICA_BALANCE_READING_REPORT.md`)
- **Code Implemented:** **Completed** (`FelicaProtocol`, `TransitCardBlockDecoder`, `FelicaBlockTransceiver`, `NfcDiagnosticReader`, `NfcTagScanMapper`, Room `v2` `MIGRATION_1_2`, `MainViewModel`, `ScanResultScreen` redesign, full unmasked `IDm` + Copy IDm action)
- **Unit Tested:** **Completed** (`40/40` passed, `0` failed, `0` skipped)
- **Emulator Verified:** **Not Applicable for RF NFC** (AVDs do not emulate RF FeliCa taps; no emulator booted during Phase 06 turn)
- **Physical Device Verified:** **In Progress / Pending Owner Rapid Pass Tap** (`MRT Pass` remains **Blocked — Card Not Available**)

### Build, Test & Lint Verification (17 Nov 2026)
- **Command:** `./gradlew clean testDebugUnitTest lintDebug assembleDebug --no-daemon` -> `BUILD SUCCESSFUL in 58s` (exit code `0`)
- **Unit Tests:** **40/40 passed** (`FelicaProtocolTest`: 5, `TransitCardBlockDecoderTest`: 4, `NfcFCardReaderTest`: 4, `NfcTagScanMapperTest`: 7, `NfcDiagnosticModelTest`: 4, `DefaultNfcAvailabilityRepositoryTest`: 2, `StaticTransitInfoRepositoryTest`: 4, `CardTypeTest`: 3, `MainViewModelTest`: 3, `StartupViewModelTest`: 4)
- **Android Lint:** `0 errors, 22 warnings`
- **APK Artifact:** `app/build/outputs/apk/debug/app-debug.apk` (20 MB)

### Next Recommended Task
1. Install `app/build/outputs/apk/debug/app-debug.apk` on an NFC-enabled Android device and tap a physical **Rapid Pass** on the **Scan** screen to validate live BDT purse balance, full `IDm`, `Rapid Pass` classification, and Room transaction persistence.
2. When a physical **MRT Pass** becomes available, test it on **Settings → NFC Diagnostics** (`SF1/SF2` status + `0x220F` block read) to extend hardware-verified MRT Pass support.
