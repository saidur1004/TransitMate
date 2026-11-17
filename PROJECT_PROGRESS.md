# TransitMate — Project Progress & Verification Log

**Document Version:** 2.0  
**Last Updated:** 17 November 2026, 12:45 +06:00  
**Responsible Agent:** Antigravity Autonomous Coding Agent  
**Current Phase:** Phase 09 Hardware Validation Prep & Phase 10 Release Readiness Audit In Progress — Automated Gates Green (`39/39` Unit, `9/9` Instrumented, `0` Lint Errors) · Rapid Pass Physical Verification Pending · MRT Pass Blocked · Release Signing Pending

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
| **Phase 09** | Complete UI/UX Redesign, QA & Physical Testing | **In Progress** | 17-screen V3 redesign + emulator Light/Dark & EN/বাংলা verified (`39/39` unit, `9/9` instrumented); `RoomCardRepository.updateCard` rescan fix applied; physical Rapid Pass **Pending**, MRT Pass **Blocked** |
| **Phase 10** | Release Readiness & Store Preparation | **In Progress (Audit Complete — Blocked on Signing & HW)** | Unsigned `app-release-unsigned.apk` (`13 MB`) & `app-release.aab` (`13 MB`) build cleanly (`allowBackup=false`, `bundle.language.enableSplit=false`); missing release signing keystore, `proguard-rules.pro` (`isMinifyEnabled=false`), external Privacy Policy URL, OSS licenses notice, and physical NFC sign-off |

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
- **Baseline Physical Rapid Pass Test:** **Verified** — Read **`৳ 14`** balance and **`14` records** over `NfcF` on real Android device; exposed `1D 60 02 01` recharge header misclassification bug.
- **Physical Rapid Pass Post-Fix Re-Verification & Repeated Scan:** **Pending Physical Verification** (`01:27` `IDm` prefix precedence fix verified via JUnit regression test `decodeCardData_rapidPassToppedUpAtMetroCounterRemainsRapidPass`)
- **Physical MRT Pass Hardware Validation:** **Blocked (Card Not Available)**

---

## 9. Phase 06 Closure, Phase 07–08 Audit & UI/UX Wallet Redesign (17 Nov 2026)

### Implemented Fixes & Enhancements
1. **Rapid Pass vs. MRT Pass Classification Fix (`TransitCardBlockDecoder.kt`):** Prioritized `01:27` Rapid Pass `IDm` signature ahead of `1D 60 02 01` Metro counter recharge header (`hasMrtTopUp`) and added dynamic year guard (`YearMonth.now(DHAKA_ZONE_OFFSET).year + 2`). Deliverable: [`docs/PHASE_06_HARDWARE_VALIDATION.md`](docs/PHASE_06_HARDWARE_VALIDATION.md).
2. **CSV Transaction Export (`CsvExportFormatter.kt`, `SettingsScreen.kt`):** Implemented RFC-4180 UTF-8 BOM CSV formatting with EN/বাংলা station/header translation and real Android Storage Access Framework (`ActivityResultContracts.CreateDocument("text/csv")`) export.
3. **Insights & History Audit Fixes (`InsightsScreen.kt`, `TransactionHistoryScreen.kt`):** Switched `Insights` default filter window to `All Time`, added 4 KPI summary tiles and M3 `Top Travel Routes` breakdown bars, and added per-card filter chips to `TransactionHistoryScreen`.
4. **Priority UI/UX Wallet Redesign (`v2_redesign` + Compose):** Generated 8 reference mockups (`design/ui_designs/v2_redesign/`), enhanced `TransitHeroCard` (tonal gradients + `Canvas` contactless arcs) and `FullIdmRow` (`Copy IDm` pill), and redesigned `HomeScreen`, `ScanResultScreen`, `MyCardsScreen`, and `CardDetailsScreen`. Deliverable: [`docs/UI_UX_REDESIGN_REPORT.md`](docs/UI_UX_REDESIGN_REPORT.md).
5. **Onboarding Back-Stack & Localization Polish (`TransitMateNavHost.kt`, `FareCalculatorScreen.kt`, `StationExplorerScreen.kt`):** Fixed `popUpTo(TransitRoutes.ONBOARDING) { inclusive = true }` caught during emulator QA, localized fare route summary + added DMTCL tariff attribution, and ordered Bangla station names first in `bn` locale.

### Automated Build, Unit, Lint & Emulator QA Verification (17 Nov 2026)
- **Clean Build, Unit Test & Lint Gate:** `./gradlew clean testDebugUnitTest lintDebug assembleDebug --no-daemon` -> `BUILD SUCCESSFUL in 28s` (exit code `0`)
  - **Unit Tests:** **`43/43 passed`** (`0 failed`, `0 skipped` across 11 suites including `TransitCardBlockDecoderTest`: 5/5 and `CsvExportAndRepositoryTest`: 2/2)
  - **Android Lint:** **`0 errors, 22 warnings`**
  - **Debug APK:** `app/build/outputs/apk/debug/app-debug.apk` (`20 MB`)
- **Android Instrumentation & UI Tests (`Pixel_9` AVD):** `./gradlew connectedDebugAndroidTest --no-daemon` -> `BUILD SUCCESSFUL in 53s`
  - **Instrumented Tests:** **`6/6 passed`** (`0 failed`, `0 skipped` — `TransitMateInstrumentedTest`: Room `MIGRATION_1_2`, ScanResult Hero + Copy IDm, Home & Insights All-Time Route Breakdown, Bangla Station Explorer, Bottom Navigation switching)
- **Emulator Visual Walkthrough (`Pixel_9` AVD):** Verified Light (English) and Dark Graphite (বাংলা) theme switching, onboarding transition, bottom navigation, and offline tools (`Fare Calculator`, `Station Explorer`, `Insights`, `Settings`).

### Next Recommended Task
1. Install `app/build/outputs/apk/debug/app-debug.apk` on an NFC-enabled Android device and tap the physical **Rapid Pass** twice to verify post-fix `Rapid Pass · DTCA` classification and repeated-scan persistence (`Pending Physical Verification`).
2. When a physical **MRT Pass** becomes available, test it on **Settings → NFC Diagnostics** and **Scan** to unblock MRT Pass hardware verification (`Blocked (Card Not Available)`).

---

## 10. Phase 09 — Complete Product UI/UX Audit & Redesign (17 Nov 2026)

### Design-First Audit & Approval
- Completed 17-screen inventory classification (`4` Production-ready, `9` Needs improvement, `4` Requires redesign) in `docs/PHASE_09_UI_UX_AUDIT.md` paired with 20 Light/Dark mockups in `design/ui_designs/v3_phase09/`.

### Batch 1 — Reusable Components & Core UX (`Home`, `NFC Scanner`, `Scan Result`, `My Cards`, `Card Details`)
- **Reusable Components (`TransitComponents.kt`):** `ResponsiveScreenContainer` (`600.dp` width cap), `SecondaryTonalButton`, `MetricStatTile`, `IdmPrivacyCaption`, `ErrorStateView`, `LoadingStateView`, and `localizedRecognitionLabel()`.
- **Core Screens:** Updated `HomeScreen`, `NfcScannerScreen`, `ScanResultScreen`, `MyCardsScreen`, and `CardDetailsScreen` with responsive constraints, persistent-IDm share caution notices, unified KPI tiles, bilingual issuer strings, and clean scanner guidance.
- **Verification (Batch 1):**
  - `./gradlew clean testDebugUnitTest lintDebug assembleDebug connectedDebugAndroidTest --no-daemon`: `BUILD SUCCESSFUL` (`43/43` unit tests passed, `0` lint errors / `23` warnings, `6/6` instrumented tests passed on `Pixel_9` AVD).
  - Emulator Light (EN) & Dark (বাংলা) walkthrough verified cleanly.

### Batch 2 — Transaction History & Insights (`TransactionHistoryScreen`, `InsightsScreen`)
- **Transaction History:** Added `ResponsiveScreenContainer`, chronological month-grouped `SurfaceSectionCard` ledgers (`SimpleDateFormat("LLLL yyyy", locale)`), `VerticalDivider` summary KPI card, and distinct empty states (`transactions.isEmpty()` vs active filter zero matches).
- **Insights:** Replaced top-bar toggle chip with dual explicit `FilterChip` selectors (`All Time` | `Last 30 Days`), unified KPI cards using `MetricStatTile`, and added a 30-day-empty recovery CTA (`View All Time`).
- **Verification (Batch 2):**
  - `./gradlew clean testDebugUnitTest lintDebug assembleDebug connectedDebugAndroidTest --no-daemon`: `BUILD SUCCESSFUL` (`43/43` unit tests passed, `0` lint errors / `23` warnings, `6/6` instrumented tests passed on `Pixel_9` AVD).
  - Emulator Light (EN) & Dark (বাংলা) walkthrough verified cleanly (`Insights` + `Transaction History`).

### Batch 3 — Transit Utilities (`FareCalculatorScreen`, `StationExplorerScreen` + `Station Details Sheet`)
- **Fare Calculator:** Added `ResponsiveScreenContainer`, visual 3-dot origin-to-destination route connector, `48.dp` touch-target swap button, locale-aware English/বাংলা station ordering in fields and picker sheet, plus `EmptyStateView` in picker search.
- **Station Explorer & Redesigned Details Sheet:** Added continuous vertical MRT Line 6 rail connecting station nodes `01`–`16` and redesigned the `Station Details` `ModalBottomSheet` with localized header, station number badge, full-width stacked nearby landmarks block, and localized distance unit (`km` / `কিমি`). Also removed `IntrinsicSize.Min` + `fillMaxHeight` on list rows so station sheet opens instantaneously.
- **Verification (Batch 3):**
  - `./gradlew clean testDebugUnitTest lintDebug assembleDebug connectedDebugAndroidTest --no-daemon`: `BUILD SUCCESSFUL in 57s` (`43/43` unit tests passed, `0` lint errors / `23` warnings, `7/7` instrumented tests passed on `Pixel_9` AVD including `stationExplorer_clickingStation_opensRedesignedDetailsSheet`).
  - Emulator visual walkthrough verified cleanly (`Fare Calculator`, `Station Explorer`, and `Station Details` bottom sheet).

### Batch 4 — Settings, Support, Onboarding & Legacy Cleanup
- **Legacy Cleanup (`feature/startup`):** Removed unused legacy files (`StartupScreen.kt`, `StartupUiState.kt`, `StartupViewModel.kt`, `StartupViewModelTest.kt`) after confirming zero navigation, manifest, or production code references (`4` legacy unit tests removed cleanly -> `39` active unit tests).
- **Critical `MainActivity` Bug Caught & Fixed During Emulator QA:** Navigating from `Settings` to `Data Management` threw `IllegalStateException: No ActivityResultRegistryOwner was provided via LocalActivityResultRegistryOwner` because `createLocalizedContext` detached `ContextWrapper` resolution from `MainActivity`. Fixed [`MainActivity.kt`](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/app/src/main/kotlin/com/transitmate/bd/MainActivity.kt) by explicitly providing `LocalActivityResultRegistryOwner` and `LocalOnBackPressedDispatcherOwner` in `CompositionLocalProvider`, and added [`MainActivitySmokeTest.kt`](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/app/src/androidTest/kotlin/com/transitmate/bd/MainActivitySmokeTest.kt).
- **Settings & Data Management (`SettingsScreen.kt`):** Wrapped both screens in `ResponsiveScreenContainer`, added `IdmPrivacyCaption` to Privacy Info Sheet, made `DataManagementScreen` vertically scrollable with an `IDm Privacy Reminder` banner, added Clear Scan History confirmation dialog, fixed Delete All confirmation copy (`%1$d` cards / `%2$d` records), and made JSON export preview sheet scrollable.
- **Support, Diagnostics, Onboarding & Splash:**
  - `NfcDiagnosticsScreen.kt`: Wrapped in `ResponsiveScreenContainer`, localized all labels/badges/fallbacks (`diagnostics_*` EN/বাংলা), added `IdmPrivacyCaption` under `FullIdmRow`, unified bottom controls to `SecondaryTonalButton` + `SecondaryOutlinedButton`.
  - `HelpFaqScreen.kt`: Wrapped in `ResponsiveScreenContainer`, updated IDm & balance FAQs (EN/বাংলা), added `EmptyStateView` for zero search matches, switched bottom CTA to `SecondaryTonalButton`.
  - `AboutScreen.kt`: Wrapped in `ResponsiveScreenContainer` and aligned header title with `R.string.settings_about`.
  - `OnboardingScreen.kt`: Wrapped in `ResponsiveScreenContainer` and unified Slides 2–3 inside concentric orbital ring frames paired with contextual `StatusPill` tags matching Slide 1.
  - `SplashScreen.kt`: Standardized bottom progress indicator + status caption with `LoadingStateView`.
- **Verification (Batch 4):**
  - `./gradlew testDebugUnitTest lintDebug assembleDebug connectedDebugAndroidTest --no-daemon`: `BUILD SUCCESSFUL in 1m 44s` (`39/39` unit tests passed across `10` test files, `0` lint errors / `26` warnings, **`8/8` instrumented tests passed** on `Pixel_9` AVD including `MainActivitySmokeTest`).
  - Verified Light (EN) & Dark (বাংলা) rendering on `emulator-5554` (`Splash`, `Onboarding`, `Settings`, `Data Management`, `NFC Diagnostics`, `Help & FAQ`, `About TransitMate`).
  - **Physical Hardware Verification Remaining:** Rapid Pass repeat-scan (`Pending Physical Verification`) and physical MRT Pass validation (`Blocked (Card Not Available)`).

---

## 11. Phase 09 Hardware Validation Readiness & Phase 10 Release Readiness Audit (17 Nov 2026)

- **Last Updated:** 17 November 2026, 12:45 +06:00
- **Phase 09 Hardware Validation Status:** **In Progress** (Code & automated SQLite/NFC suite ready; **Rapid Pass Post-Fix Physical Verification: Pending**; **MRT Pass Physical Validation: Blocked — Card Not Available**)
- **Phase 10 Release Readiness Status:** **In Progress (Not Yet Release-Ready)** (Unsigned release APK/AAB verified; blocked on release signing keystore, external Privacy Policy URL, OSS license attribution notice, and physical NFC sign-off)

### 11.1 Audit Findings & Safe Fixes Applied
1. **Critical SQLite Rescan Cascade Bug Fixed (`TransitCardDao.kt`, `RoomCardRepository.kt`):** Previously `RoomCardRepository.saveOrUpdateCard` called `dao.insertCard(entity)` annotated with `@Insert(onConflict = OnConflictStrategy.REPLACE)`. On SQLite, `REPLACE` deletes an existing `saved_cards` row before re-inserting, which fired `ForeignKey.CASCADE` and **deleted all stored transactions** for that card on every repeated scan. Changed `insertCard` to `OnConflictStrategy.ABORT`, added `@Update suspend fun updateCard(card: SavedCardEntity)`, routed existing cards (`existing != null`) through `updateCard`, and added real SQLite instrumented regression test `roomCardRepository_rescanPreservesExistingTransactionsAndDeduplicates`.
2. **Cloud Backup Policy Aligned (`AndroidManifest.xml`, `backup_rules.xml`, `data_extraction_rules.xml`):** Set `android:allowBackup="false"`, configured explicit root/database/sharedpref/file/external exclusion rules in `backup_rules.xml` and `data_extraction_rules.xml` so Android Auto Backup and Cloud Backup cannot upload local `IDm`/Room records — matching the in-app `"Cloud Backups: Disabled (Offline-Only)"` privacy notice and clearing `2` `UnusedResources` lint warnings.
3. **Play App Bundle Locale Configuration (`app/build.gradle.kts`):** Added `bundle { language { enableSplit = false } }` so in-app English <-> বাংলা language switching remains intact on Play Store `.aab` installs and resolved the `AppBundleLocaleChanges` lint warning.
4. **Compose API Hygiene & Flaky Bangla Test Fixed (`TransitComponents.kt`, `MainActivitySmokeTest.kt`, `README.md`):** Reordered `LoadingStateView(modifier, message)` parameter list (clearing `ModifierParameter` lint warning), fixed `MainActivitySmokeTest` Bangla string expectation (`"স্থানীয় সংরক্ষণ সারাংশ"`), and removed the stale `startup/` directory mention in `README.md`.
5. **Physical Device Checklist Authored:** Created [`docs/PHYSICAL_DEVICE_TEST_CHECKLIST.md`](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/docs/PHYSICAL_DEVICE_TEST_CHECKLIST.md) with 10 Rapid Pass cases (`RP-01`..`RP-10`, status: **Pending**) and 5 MRT Pass cases (`MRT-01`..`MRT-05`, status: **Blocked — Card Not Available**).

### 11.2 Automated Test, Lint & Build Evidence
- **Clean Build, Test, Lint & Release Check:** `./gradlew clean testDebugUnitTest lintDebug assembleDebug assembleRelease connectedDebugAndroidTest --no-daemon` -> **`BUILD SUCCESSFUL in 2m 40s`**
- **Bundle Check:** `./gradlew bundleRelease --no-daemon` -> **`BUILD SUCCESSFUL in 6s`**
- **Unit Tests:** **`39/39 passed`** (`10` test suites, `0 failed`, `0 skipped`). Removing `StartupViewModelTest` (`4` legacy tests) caused zero functional coverage loss because `MainViewModelTest` validates NFC initialization (`refreshNfcStatus()`), diagnostics isolation (`onDiagnosticTagScanned()`), preferences, and card save flows.
- **Instrumented Tests (`Pixel_9(AVD) - 17`):** **`9/9 passed`** (`0 failed`, `0 skipped` — `TransitMateInstrumentedTest` `8` tests + `MainActivitySmokeTest` `1` test).
- **Android Lint (`lintDebug`):** **`0 errors, 22 warnings`** (reduced from `26` -> `22`):
  - **Resolved (`4`):** `AppBundleLocaleChanges` (1), `ModifierParameter` (1), `UnusedResources` (2).
  - **Acceptable with reason (`15`):** `OldTargetApi` (1: `targetSdk = 36` matches CI SDK baseline), `AndroidGradlePluginVersion` (2), `GradleDependency` (7), `NewerVersionAvailable` (4: dependency versions intentionally locked in `libs.versions.toml`), `ObsoleteSdkInt` (1: `mipmap-anydpi-v26` works cleanly on API 26+).
  - **Post-1.0 Polish / Should-fix (`7`):** `PluralsCandidate` (7 `%1$d` string formatting entries in `values/strings.xml`).
- **Build Artifacts Verified:**
  - Debug APK: `app/build/outputs/apk/debug/app-debug.apk` (`20 MB`)
  - Release APK *(unsigned)*: `app/build/outputs/apk/release/app-release-unsigned.apk` (`13 MB`)
  - Release AAB *(unsigned)*: `app/build/outputs/bundle/release/app-release.aab` (`13 MB`)

### 11.3 Phase 10 Release Readiness Audit Matrix
| Release Criterion | Current Status | Notes / Action Needed |
|---|---|---|
| Application Name & ID | **Ready** | `TransitMate` / `com.transitmate.bd` |
| Versioning | **Ready** | `versionCode = 1`, `versionName = "1.0.0"` |
| App Icons & Adaptive Monochrome | **Ready** | `mipmap-*/ic_launcher*`, monochrome & `drawable-nodpi/ic_transitmate_logo.png` |
| Launcher & Compatibility | **Ready** | `MainActivity` exported=`true`; `minSdk = 26`, `targetSdk = 36`; `android.hardware.nfc required="false"` |
| Permissions & Privacy Defaults | **Ready** | Only `NFC` permission; `allowBackup="false"` + strict exclude XMLs; `IdmPrivacyCaption` & `IDm Privacy Reminder` present |
| User Non-Affiliation Disclaimer | **Ready** | Present in `AboutScreen` (EN & বাংলা) |
| Release Minification / ProGuard | **Pending Decision** | `isMinifyEnabled = false`; `app/proguard-rules.pro` file does not exist yet in `app/` |
| Release Signing Keystore | **Missing / Blocker** | No `signingConfigs.release` configured (never commit keys/secrets to Git) |
| External Privacy Policy URL (Play Console) | **Missing / Blocker** | Offline summary exists in-app; Play Console Data Safety requires a public Privacy Policy URL |
| Third-Party / OSS License Notice | **Missing / Should Add** | About notice confirms clean-room independence, but no dedicated third-party Jetpack/AndroidX license list view exists yet |
| Physical NFC Hardware Validation | **Pending / Blocked** | Rapid Pass (`RP-01`..`RP-10`): **Pending**; MRT Pass (`MRT-01`..`MRT-05`): **Blocked (Card Not Available)** |
