# TransitMate — Stage 1: Project, NFC Diagnostic & UI/UX Reference Audit

**Document Version:** 1.0  
**Date:** 16 November 2026  
**Branch / Commit:** `main` @ `6411bb2` (`origin/main` synchronized; `design/ui_designs/` currently untracked)  
**Verified Baseline:** `./gradlew testDebugUnitTest --no-daemon` passed (13/13 tests, exit code 0)  
**Scope:** Stage 1 Audit only — no production UI code modified prior to Design System approval.

---

## 1. Project Structure & Toolchain Audit

### 1.1 Build & SDK Configuration (`gradle/libs.versions.toml` & `app/build.gradle.kts`)
- **Application ID:** `com.transitmate.bd` (Debug variant uses `.debug` suffix -> `com.transitmate.bd.debug`)
- **SDK Levels:** `minSdk = 26`, `compileSdk = 36`, `targetSdk = 36`, `jvmToolchain = 17`
- **Core Stack:**
  - Android Gradle Plugin: `9.0.1`
  - Kotlin: `2.3.20` (with Compose Compiler plugin)
  - Jetpack Compose BOM: `2026.03.01` (`material3`, `ui`, `ui-tooling-preview`)
  - Lifecycle / Activity Compose: `2.10.0` / `1.13.0`
  - Coroutines: `1.10.2`
  - Unit Testing: `junit:4.13.2`, `kotlinx-coroutines-test:1.10.2`
- **Build Features:** `compose = true`, `buildConfig = false` *(Note: if Debug-only diagnostic entry points are gated by `BuildConfig.DEBUG` later, `buildConfig = true` or explicit user-accessible Settings -> NFC Diagnostics route must be chosen intentionally).*
- **Missing Architectural Dependencies (Planned for Phase 05+):**
  - `androidx.navigation:navigation-compose` (Not yet added)
  - `androidx.compose.material:material-icons-extended` or curated Material Symbols vector drawables (Not yet added; must evaluate APK size impact vs inline vector drawables)
  - Room / DataStore / Hilt (Deferred to Phase 05 per `PROJECT_MASTER_PLAN.md`)

### 1.2 Manifest Audit (`app/src/main/AndroidManifest.xml`)
- `android.permission.NFC` declared properly.
- `<uses-feature android:name="android.hardware.nfc" android:required="false" />` properly declared so non-NFC handsets can install TransitMate for offline utilities.
- Single `MainActivity` (`exported="true"`, `windowSoftInputMode="adjustResize"`).

### 1.3 Kotlin Package Tree (`app/src/main/kotlin/com/transitmate/bd/`)
| Package / File | Role | Status |
|---|---|---|
| `MainActivity.kt` | Single activity entry, Compose host, `NfcDiagnosticReader` lifecycle in `onResume`/`onPause` | Working |
| `core/common/DispatcherProvider.kt` | Coroutine dispatcher abstraction for deterministic testing | Working |
| `core/common/Result.kt` | `AppResult<T>` sealed interface (`Success`, `Error`, `Loading`) | Foundation ready |
| `core/database/DatabaseFoundationMarker.kt` | Placeholder marker for Phase 05 Room DB | Marker only |
| `core/designsystem/theme/Color.kt` | Neon cyan + dark midnight navy color palette | **Requires Full Redesign** |
| `core/designsystem/theme/Theme.kt` | `TransitMateTheme` dark/light `ColorScheme` | **Requires Full Redesign** |
| `core/designsystem/theme/Type.kt` | Material 3 typography scale (missing `displayLarge/Medium`, `titleSmall`, `bodySmall`, `labelMedium`, Bangla spacing guidance) | **Requires Expansion** |
| `core/nfc/NfcAvailabilityChecker.kt` | Safe `NfcAdapter.getDefaultAdapter` availability check | Working — Keep intact |
| `core/nfc/NfcDiagnosticModel.kt` | `NfcDiagnosticData`, `bytesToHex`, `redactIdm` | Working — Isolate to Diagnostics |
| `core/nfc/NfcDiagnosticReader.kt` | `NfcAdapter.enableReaderMode` (`NFC_F \| NFC_A`), reads `IDm`, `systemCode`, `PMm`, safe `close()` in `finally` | Working — Keep RF engine intact |
| `domain/model/CardType.kt` | `MRT_PASS`, `RAPID_PASS`, `UNKNOWN` enum | Working (pure Kotlin) |
| `domain/model/NfcHardwareStatus.kt` | `AVAILABLE`, `DISABLED`, `NOT_SUPPORTED` enum | Working (pure Kotlin) |
| `domain/repository/NfcAvailabilityRepository.kt` | Repository contract | Working (pure Kotlin) |
| `data/DefaultNfcAvailabilityRepository.kt` | Implementation delegating to `NfcAvailabilityChecker` | Working |
| `feature/startup/StartupUiState.kt` | State containing hardware status + Phase 03 badge + diagnostic data | **Needs Separation** |
| `feature/startup/StartupViewModel.kt` | Checks NFC hardware & handles diagnostic scan state | Working |
| `feature/startup/StartupScreen.kt` | Temporary smoke-test & Phase 03 diagnostic UI | **Needs Replacement / Refactor** |

### 1.4 Documentation Discrepancies Observed
1. **`PROJECT_MASTER_PLAN.md` vs Actual Project:** Master plan initially proposed `bd.transitmate.app` (`Section 1`), whereas Phase 02 confirmed `com.transitmate.bd` (`app/build.gradle.kts`, `README.md`). Must keep `com.transitmate.bd`.
2. **Documentation Paths:** Master plan (`Section 6`) mentions `docs/` while the new UI prompt references `documentation/UI_UX_DESIGN_SPECIFICATION.md` and `documentation/PROJECT_PROGRESS.md`, whereas the repository already uses `docs/research/` and root `PROJECT_PROGRESS.md`. We should keep root `PROJECT_PROGRESS.md` and `docs/` as the active canonical paths (or mirror/clarify paths so nothing is duplicated or orphaned).
3. **Launcher Icon (`design/brand/logo_original.png` & `Neon Transit Card App Icon.png`):** Existing launcher assets were already generated from the rounded-square card/pin icon. Note that the original logo carries a dark navy + cyan/teal + amber pin aesthetic — the app UI should feel harmonious with the brand mark **without** forcing neon cyan or excessive glow across screens.

---

## 2. Existing Screen & NFC Diagnostic Flow Audit

### 2.1 Existing UI Problems (`StartupScreen.kt` & Real Device Screenshot)
Inspecting both `StartupScreen.kt` and the physical Rapid Pass scan screenshot on an Android phone reveals these critical UX and visual issues:

1. **Developer Diagnostics Exposed on Main User Surface:**
   - Displays `Phase 03 · NFC Research & Validation` pill, `Diagnostic NFC Scan Output`, `Scan #1`, `Technologies: NfcF`, `System Code: 0x90:E3`, `Manufacturer (PMm): 01:46:42:45:45:46:9B:FF`, and `Clean Architecture · Diagnostic Reader Mode Active`.
   - Regular commuters cannot tell what `PMm`, `System Code`, or `Anticollision` mean, nor whether their card was usable or what to do next.
2. **Partial Identifier Exposure Without Context:**
   - Displays `Tag ID / IDm (Redacted): 01:27:**:**:**:**:**:A0` directly on the landing screen without explanation or user privacy context.
3. **Emoji Used as Product UI Icons:**
   - `StartupScreen.kt:106` uses `"💳"` inside a `72.dp` icon box.
   - `strings.xml:14` uses `"🔍 Diagnostic NFC Scan Output"`.
   - Both violate the strict rule against emojis as professional UI icons.
4. **Neon / Midnight Blue Aesthetic Rejected by Owner:**
   - `Color.kt` defines `CyanPrimary = 0xFF00D2FF`, `DarkBackground = 0xFF040D1E`, `DarkSurface = 0xFF081730`, `DarkSurfaceVariant = 0xFF0E2347`, which creates a glowing cyan-on-navy aesthetic rather than a **Premium Neutral Dark Graphite** dashboard.
5. **Hardcoded / Non-Localized UI Strings:**
   - `StartupScreen.kt` hardcodes `"Unavailable"`, `"Not detected"`, `"Notice: ${diag.errorMessage}"`, and `"Clean Architecture · Diagnostic Reader Mode Active"`.
   - `StartupUiState` hardcodes `"TransitMate"`, `"Dhaka Metro & Rapid Pass Companion"`, `"Phase 03 · NFC Research & Validation"`.
   - Missing `res/values-bn/strings.xml` for proper runtime Bangla/English localization.
6. **Hardware Status Action Gap:**
   - When `NfcHardwareStatus.DISABLED`, the UI shows text (`NFC is supported but currently disabled`) with no `"Open NFC Settings"` action button.

### 2.2 Physical Card Validation Evidence Observed in Screenshot
From the owner's real Android scan screenshot of a physical **Rapid Pass** card:
- **NFC-F Detection:** `VERIFIED` (`Technologies: NfcF`)
- **8-byte IDm Extraction:** `VERIFIED` (`01:27:**:**:**:**:**:A0`)
- **System Code Observed:** `0x90E3` (rendered by `NfcDiagnosticModel.bytesToHex` as `0x90:E3` — colon insertion between the 2 bytes is a formatting quirk to clean up in the Diagnostics module).
- **Manufacturer (PMm) Observed:** `01:46:42:45:45:46:9B:FF`
- **Balance & Transaction Reading:** Still **NOT VERIFIED / NOT IMPLEMENTED**. The UI must continue to strictly enforce `Detection ≠ Recognition ≠ Balance Reading` and show honest `Not Available` / `Protected or Unverified` states.

### 2.3 NFC Code Separation Strategy (Preserving Working Logic)
- **Keep Intact:** `AndroidNfcAvailabilityChecker`, `NfcAvailabilityRepository`, and the `NfcAdapter.enableReaderMode` configuration in `NfcDiagnosticReader` (`FLAG_READER_NFC_F | FLAG_READER_NFC_A | FLAG_READER_SKIP_NDEF_CHECK | FLAG_READER_NO_PLATFORM_SOUNDS`, `EXTRA_READER_PRESENCE_CHECK_DELAY = 250`, background callback execution, `nfcF.connect()` + `finally { nfcF.close() }`).
- **Architectural Separation:**
  - Introduce a clean domain-level `NfcScanSession` / `CardScanOutcome` model for **User-Facing Scanner & Scan Result** screens:
    - `DetectionStatus` (Detected / Failed / TagLost / UnsupportedTech)
    - `DetectedTechnology` (e.g., `FeliCa (NFC-F)` vs `NFC-A`)
    - `CardRecognition` (`Rapid Pass`, `MRT Pass`, or `Unidentified / Unverified Card`)
    - `PrivacyMaskedId` (only shown when meaningful/user-consented, e.g., on Card Details/Scan Result)
    - `BalanceState` (`Verified(amount)` vs `Unavailable(reason)` — strictly `Unavailable` until Phase 06 parser verification)
    - `Timestamp`
  - Isolate `NfcDiagnosticData` (raw `techList`, `systemCodeHex` formatted cleanly as `0x90E3`, `manufacturerPmmHex`, `scanCount`, `rfErrorTrace`) exclusively to the **Developer / Technical NFC Diagnostics Screen** (reached via `Settings -> NFC Diagnostics` or a dedicated diagnostic toggle, never shown on Home/Main Scan Result).

---

## 3. Design Reference Folder Audit (`/design/ui_designs/`)

### 3.1 File Inventory
Currently contains **5 flat PNG files** (no subfolders created yet):
1. `Neon Transit App Onboarding Duo.png`
2. `MetroSync BD_ Dual-Theme Transit Wallet.png`
3. `Dual-Theme NFC Scanner UI.png`
4. `Dual-Mode Transit Card Scan UI.png`
5. `Transit History App_ Dark and Light Themes.png`

### 3.2 Screen-by-Screen Reference Analysis

| Reference File | Covered Screens | Strengths to Preserve | Weaknesses / Violations to Eliminate |
|---|---|---|---|
| `Neon Transit App Onboarding Duo.png` | **Splash Screen** + **Onboarding Screen** | Dual-screen clean flow; concise headline + 1-line body; 3-dot progress indicator; clear `Skip` button; intuitive "phone back near card" visual concept. | Uses old name **MetroSync BD**; heavy electric cyan glow; futuristic illustrated train wallpaper; overly loud cyan pill CTA button. |
| `MetroSync BD_ Dual-Theme Transit Wallet.png` | **Home Dashboard** (Dark & Light) | Clear visual hierarchy: Top header -> Primary Hero Card (`Active` pill, masked ID, prominent `৳` balance area) -> Centered primary Scan trigger -> `Recent Trips` card with `View All ->` and clean trip rows. Dual Light/Dark parity. | Uses old name **MetroSync BD**; displays personal avatar, `"Hello, Siam! 👋"` emoji & notification bell (conflicts with **no-account, offline-first** product); bright neon cyan rings/borders; shows unverified `৳ 150` balance & fake trips; bottom nav uses `Home | History | Analytics | Cards | Settings` (differs from Master Plan). |
| `Dual-Theme NFC Scanner UI.png` | **NFC Scanner Screen** (Dark & Light) | Clear Top Bar (`Back`, `NFC Scanner`, `?` Help); central phone-antenna-to-card alignment graphic; live status indicator pill; clear `Cancel` action button at bottom. | Excessive neon concentric radar glow; city skyline + train background photo creates visual clutter; copy says `"Hold your MRT card"` (must cover both **MRT Pass & Rapid Pass**). |
| `Dual-Mode Transit Card Scan UI.png` | **Scan Result Screen** (Dark & Light) | Clean single-column summary: Back + Title -> Visual Card Preview -> Compact ID row with Copy icon -> Dedicated Balance & Last Updated card -> Bottom status pill (`Card Read Successfully`). | Uses iPhone/iOS status bar & home indicator; heavy bright-blue gradients; short/ambiguous `UID: 01:2E:55:11`; presents unverified `৳ 150` balance; lacks honest `Card Recognition` & `Balance Not Available` differentiation. |
| `Transit History App_ Dark and Light Themes.png` | **Transaction / Travel History** (Dark & Light) | Top summary banner (`Total Trips` + `Total Spent`); date-grouped headers (`Today`, `Yesterday`) with date chips; subtle vertical timeline connector; clear Origin -> Destination + Timestamp + Fare layout; Filter icon in top bar. | Bottom nav only shows **4 tabs** (`Home | History | Analytics | Settings`) — breaks navigation consistency with Dashboard; Light theme top summary card uses saturated electric-blue gradient with background train illustration bleed. |

### 3.3 Cross-Reference Inconsistencies Identified
1. **Navigation Bar Drift:** Home mockup has 5 tabs (`Home`, `History`, `Analytics`, `Cards`, `Settings`), History mockup has 4 tabs (drops `Cards`), and neither aligns with the Master Plan / Section 10 spec (**`Home`, `My Cards`, `Scan`, `Insights`, `Settings`**).
2. **Inconsistent App Naming:** `MetroSync BD` in reference mocks vs `TransitMate` in codebase & Master Plan.
3. **Platform Framing Drift:** `Dual-Mode Transit Card Scan UI.png` uses iOS chrome while others use Android status bars. All TransitMate screens must standardize on **Android Material 3** proportions, Edge-to-Edge insets, and system bars.
4. **Brand/Theme Inconsistency:** Excessive cyan glow (`#00FFFF`), saturated gradient cards, and train-photo wallpapers clutter Light & Dark surfaces.

---

## 4. Complete Screen & Supporting State Inventory

### 4.1 Core Application Screens (15 Total)

| # | Screen Name | Reference Art | Current Code Status | Target Phase / Priority |
|:---:|---|:---:|:---:|---|
| 1 | **Splash Screen** | Partial (`Neon Transit...`) | Missing (only `StartupScreen`) | Stage 4 (Design) -> Core Implementation |
| 2 | **Onboarding Screen** | Partial (`Neon Transit...`) | Missing | Stage 4 (Design) -> Core Implementation |
| 3 | **Home Dashboard** | Partial (`MetroSync BD...`) | Missing | Stage 4 (Design) -> Core Implementation |
| 4 | **NFC Scanner Screen** | Partial (`Dual-Theme NFC...`) | Partial (`StartupScreen` prompt) | Stage 4 (Design) -> Core Implementation (High) |
| 5 | **Scan Result / Success Screen** | Partial (`Dual-Mode Scan...`) | Partial (raw Diagnostic card) | Stage 4 (Design) -> Core Implementation (High) |
| 6 | **Card Information / Details Screen** | Missing | Missing | Stage 4 (Design) -> Core Implementation |
| 7 | **My Cards Screen** | Missing | Missing | Stage 4 (Design) -> Core Implementation |
| 8 | **Transaction History Screen** | Partial (`Transit History...`) | Missing | Stage 4 (Design) -> Core / Data-Gated |
| 9 | **Travel Analytics Screen** (`Insights`) | Missing | Missing | Stage 4 (Design) -> Core / Data-Gated |
| 10 | **Fare Calculator Screen** | Missing | Missing | Stage 4 (Design) -> Offline Utility |
| 11 | **Station Explorer Screen** | Missing | Missing | Stage 4 (Design) -> Offline Utility |
| 12 | **NFC Diagnostics Screen** (Developer) | Missing | Mixed into `StartupScreen` | Stage 4 (Design) -> **Extract immediately from Startup** |
| 13 | **Settings Screen** | Missing | Missing | Stage 4 (Design) -> Core Implementation |
| 14 | **Help & FAQ Screen** | Missing | Missing | Stage 4 (Design) -> Core Implementation |
| 15 | **About Screen** | Missing | Missing | Stage 4 (Design) -> Core Implementation |

### 4.2 Supporting States, Dialogs & Bottom Sheets (25 Inventory Items)
All 25 states mapped to appropriate UX containers (no bloat of unnecessary full screens):
- **Inline / Full-Screen States:** `Waiting for Card`, `Scanning in Progress`, `Card Detected`, `NFC Not Supported` (on Scanner/Home banner), `Empty Saved Cards`, `Empty Transaction History`, `Empty Travel History`, `No Analytics Data`, `Fare Calculator Empty State`, `Station Search Empty State`.
- **Actionable Alert Banners / Inline Error States:** `NFC Disabled` (with button to open system NFC Settings), `Balance Not Available` (honest status explanation), `Transaction Not Available`.
- **Modal Bottom Sheets / Result States:** `Unsupported Card` (e.g., bank EMV / MIFARE), `Unknown Card` (FeliCa detected, authority unverified), `Identifier Unavailable`, `Scan Failed`, `NFC Communication Error` (e.g., `TagLostException` / pulled away too early), `Data Export Preview`, `Privacy Information`, `Language Selection` (English / বাংলা), `Theme Selection` (System / Light / Dark).
- **Confirmation / Input Dialogs:** `Delete Card Confirmation`, `Rename Card Dialog`.
- **Dedicated Sub-Route / Section:** `Data Management` (inside Settings: clear local history, remove saved cards, export preview).

---

## 5. Proposed Design System Direction (For Owner Approval)

Aligned directly with **Section 4 ("Premium Minimalist Transit Dashboard")** — zero neon glow, zero cyberpunk dark-blue clichés, zero decorative photo backgrounds.

### 5.1 Color Direction (Dark & Light Parity)
- **Dark Theme — Neutral Graphite & Muted Transit Teal:**
  - `background`: `#111315` (Deep Charcoal / Graphite)
  - `surface`: `#191C1F` (Slightly Lighter Graphite — primary card container)
  - `surfaceContainerHigh` / `surfaceVariant`: `#22262A` (Muted Slate — nested chips, secondary surfaces)
  - `outline` / `outlineVariant`: `#2E343B` (1dp crisp subtle border for card separation without glow)
  - `onBackground` / `onSurface`: `#F1F3F5` (Soft White — high legibility, low eye fatigue)
  - `onSurfaceVariant`: `#9AA2AC` (Muted Cool Grey)
  - `primary`: `#2FA497` (Muted Transit Teal — calm, authoritative, high contrast on `#111315`)
  - `primaryContainer`: `#1B3B38` (`onPrimaryContainer`: `#BCE8E2`)
  - `secondary`: `#5B8FB9` (Desaturated Metro Slate Blue)
  - `success`: `#34A874` (Natural Transit Green)
  - `warning`: `#D99B26` (Soft Amber — replaces harsh bright yellow)
  - `error`: `#D95F59` (Muted Red)
- **Light Theme — Warm Off-White & Clean Transit Slate:**
  - `background`: `#F6F7F8` (Warm Off-White)
  - `surface`: `#FFFFFF` (Clean White Card Surface)
  - `surfaceVariant`: `#ECEFF2` (Soft Mist Grey)
  - `outline`: `#DCE1E7` (Subtle crisp border)
  - `onBackground` / `onSurface`: `#16191D` (Deep Slate Ink)
  - `onSurfaceVariant`: `#5C6570` (Slate Secondary Text)
  - `primary`: `#12746A` (Deep Muted Transit Teal — passes WCAG AA/AAA on white)
  - `primaryContainer`: `#DDF2EE` (`onPrimaryContainer`: `#0B4740`)
  - `secondary`: `#31628D` (Desaturated Metro Blue)
  - `success`: `#1E8255`, `warning`: `#B87A14`, `error`: `#C2413B`

### 5.2 Navigation Architecture
Standardizing on the Master Plan 5-destination Bottom Navigation Bar (`NavigationBar`):
1. **Home** (`Home` — Primary Card overview, verified balance/status, quick scan button, recent activity preview, offline transit shortcuts like Fare Calculator & Stations)
2. **My Cards** (`Wallet / CreditCard` — Saved cards list, rename/delete, Card Details)
3. **Scan** (`Nfc / Contactless` — Highlighted center nav item or crisp pill-accented tab launching the live NFC Reader experience — clean and balanced, no oversized floating orb)
4. **Insights** (`Analytics / BarChart` — Hub with segmented switch or direct cards for Travel Analytics + Transaction History)
5. **Settings** (`Settings` — Theme, Language EN/বাংলা, Data Management, Privacy Info, Help & FAQ, About, and gated/explicit **NFC Diagnostics** entry)

### 5.3 Typography & Iconography Rules
- **Numeric Figures:** Use `fontFeatureSettings = "tnum"` (tabular numerals) for currency (`৳`), scan counters, and fares.
- **Bangla Script Safety:** Generous `lineHeight` (`1.35x–1.45x` font size) on all `titleMedium`, `bodyLarge`, `bodyMedium`, and `labelMedium` styles so Bangla conjuncts (`যুক্তবর্ণ`) and upper/lower vowel signs (`কার`/`ফলা`) never clip.
- **Iconography:** Strictly crisp Material Icons / Material Symbols vector assets (`24.dp` standard, consistent `1.5dp–2dp` stroke weight). Zero emojis (`💳`, `🔍`, `👋`) anywhere in UI or `strings.xml`.

---

## 6. Realistic Staged Execution Plan

| Stage | Activity | Key Deliverable(s) | Entry / Exit Gate |
|:---:|---|---|---|
| **Stage 1** *(Current)* | Project, NFC Diagnostic & Reference Audit | `PROJECT_UI_AUDIT.md` | **Awaiting Owner Review & Approval** |
| **Stage 2** | Design System Finalization | `design/DESIGN_SYSTEM.md` | Approve Neutral Graphite/Teal Light & Dark tokens, typography, icons, motion specs |
| **Stage 3** | Complete Screen & Flow Specification | `design/SCREEN_REQUIREMENTS.md` & `docs/UI_UX_DESIGN_SPECIFICATION.md` | Document all 15 core screens + 25 supporting states & nav graph |
| **Stage 4** | Reference-Based Design Generation | Visual screen specs in `design/ui_designs/` (organized subfolders `01_...` to `13_...`) | Generate consistent Light & Dark Android M3 visuals screen-by-screen |
| **Stage 5** | Cross-Screen Design Consistency Review | Consolidated design sign-off | Verify typography, spacing, contrast, icon parity, zero neon/fake data |
| **Stage 6** | Jetpack Compose Implementation | Theme tokens, reusable components, Navigation Compose, separated Production Scan vs Developer NFC Diagnostics | Implement incrementally while keeping `NfcDiagnosticReader` / `NfcAvailabilityChecker` intact |
| **Stage 7** | Testing, Localization & Final Audit | Updated `PROJECT_PROGRESS.md`, passing `./gradlew clean testDebugUnitTest assembleDebug` | Verify Light/Dark, EN/বাংলা, small/large screen previews, unit tests & physical NFC scan parity |
