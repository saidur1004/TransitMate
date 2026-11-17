# Phase 09 — Complete Product UI/UX Audit & Improvement Matrix

**Project:** TransitMate (`com.transitmate.bd`)  
**Stage:** Phase 09 Design-First Audit (Steps 1–4: Repository Audit, Screen Matrix, Light/Dark Mockups & Consistency Review)  
**Baseline Commit:** `77e78ba` (`main`)

---

## 1. Global Design System & Cross-Cutting Audit Findings

1. **Tablet / Expanded Width Constraint (`DESIGN_SYSTEM.md` §9):** All screens currently use unbounded `fillMaxWidth()`, lacking the `600.dp` centered container width constraint on larger viewports.
2. **Card IDm Persistence Privacy Communication:** `ScanResultScreen`, `CardDetailsScreen`, `HomeScreen`, `MyCardsScreen`, and `NfcDiagnosticsScreen` display full read-only `Card IDm` (`FullIdmRow`), but nowhere in consumer UI or FAQs is it explained that `IDm` is a persistent hardware identifier that should not be shared in public screenshots or reports.
3. **Component & KPI Tile Inconsistency:**
   - `InsightsScreen` (`MetricBox`) uses `surfaceVariant` without border, whereas `CardDetailsScreen` (`CardStatBox`) uses `surface` with `1.dp` `outline` border.
   - Secondary tonal CTAs (`MyCardsScreen`, `HelpFaqScreen`, `NfcDiagnosticsScreen`) use inline `FilledTonalButton` with fixed `height(48.dp)` (risking Bangla multi-line clipping) instead of a unified `SecondaryTonalButton` with `heightIn(min = 48.dp)` and `maxLines = 2`.
4. **Hardcoded English Strings in Bangla Mode:**
   - `NfcDiagnosticsScreen` has hardcoded English labels across all telemetry rows and `"Scan #..."` badge.
   - `FareCalculatorScreen` (`StationPickerField` & `StationPickerSheetContent`) and `StationExplorerScreen` (`StationDetails` bottom sheet) show English station names first even when the locale is set to `bn`, and `StationDetails` hardcodes `" km"` plus crams bilingual landmarks into a right-aligned key-value column.
5. **Missing Dialogs, Scroll Containers & Empty States:**
   - `DataManagementScreen` lacks `verticalScroll()`, causing the bottom destructive button to risk clipping on compact screens / 1.3x–1.5x font scale; **Clear Scan History** has no confirmation dialog; **Delete All Local Data** reuses `R.string.dialog_delete_card_message` awkwardly; JSON preview sheet lacks scrolling.
   - `StationPickerSheetContent` (`FareCalculatorScreen`) and `HelpFaqScreen` lack empty search states when queries match 0 entries.
   - `FareCalculatorScreen` `SwapVert` button uses `size(38.dp)` (below the `48.dp` M3 touch target minimum).
6. **Legacy Code:** [`StartupScreen.kt`](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/app/src/main/kotlin/com/transitmate/bd/feature/startup/StartupScreen.kt), [`StartupUiState.kt`](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/app/src/main/kotlin/com/transitmate/bd/feature/startup/StartupUiState.kt), and [`StartupViewModel.kt`](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/app/src/main/kotlin/com/transitmate/bd/feature/startup/StartupViewModel.kt) are unreferenced by `MainActivity` and `TransitMateNavHost`.

---

## 2. Full Screen Inventory & Improvement Matrix

| # | Screen / Modal | Classification | Concrete Findings & Planned Phase 09 Improvements | Updated Mockup (`v3_phase09/`) |
|---|---|---|---|---|
| 01 | **Splash** (`SplashScreen.kt`) | **Production-ready** | Clean edge-to-edge hierarchy and adaptive brand mark. Optional minor spacing polish. | *(Baseline parity)* |
| 02 | **Onboarding** (`OnboardingScreen.kt`) | **Needs improvement** | Slides 2–3 use small boxed icons lacking visual parity with Slide 1 diagram; `"Offline-First · Read-Only"` pill repeats verbatim on all 3 slides. Align all 3 slides with cohesive orbital vector cards + slide-specific context pills. | `01_onboarding_{light,dark}.png` |
| 03 | **Home** (`HomeScreen.kt`) | **Production-ready** | Already redesigned (`v2_redesign`). Minor polish: localize authority labels cleanly & unify width cap. | `design/ui_designs/v2_redesign/01_home_*.png` |
| 04 | **NFC Scanner** (`NfcScannerScreen.kt`) | **Needs improvement** | Waiting chip and Cancel button sit disjointed; Disabled state repeats title in both Banner and Card. Unify into crisp scan guide card + inline supported-card / case tip strip + clean Pause button. Also audit Unsupported/Comm/TagLost bottom sheets. | `02_scanner_{light,dark}.png` |
| 05 | **Scan Result** (`ScanResultScreen.kt`) | **Needs improvement** | Upgraded in `v2_redesign`; add concise `IDm` persistence privacy hint beneath `FullIdmRow` (`Avoid posting raw Card IDm in public screenshots`) + localize authority subtitles. | `03_scan_result_{light,dark}.png` |
| 06 | **My Cards** (`MyCardsScreen.kt`) | **Production-ready** | Already redesigned (`v2_redesign`). Replace fixed `height(48.dp)` bottom tonal button with `SecondaryTonalButton` (`heightIn(min = 48.dp)`). | `design/ui_designs/v2_redesign/03_my_cards_*.png` |
| 07 | **Card Details** (`CardDetailsScreen.kt`) | **Needs improvement** | Unify `CardStatBox` styling with `InsightsScreen` KPI cards; add `IDm` persistence hint; ensure Primary `Switch` row enforces `minHeight = 48.dp`. | `design/ui_designs/v2_redesign/04_card_details_*.png` |
| 08 | **Transaction History** (`TransactionHistoryScreen.kt`) | **Requires redesign** | Detached card-per-row repetition wastes vertical space; Banner + Empty Card overlap redundantly on zero-match filters; lacks date/month grouping. Redesign with month/day section grouping inside shared `SurfaceSectionCard` containers + distinct empty states (`filter empty` vs `zero records`). | `04_history_{light,dark}.png` |
| 09 | **Insights** (`InsightsScreen.kt`) | **Needs improvement** | Single toggle chip (`All Time` / `Last 30 Days`) feels ambiguous; KPI tiles lack bordered surface parity with `CardDetails`; missing empty-window `"Switch to All Time"` CTA. Switch to explicit dual `FilterChip` pair + bordered KPI cards. | `05_insights_{light,dark}.png` |
| 10 | **Fare Calculator** (`FareCalculatorScreen.kt`) | **Needs improvement** | `SwapVert` is `38.dp` (< `48.dp`); `StationPickerField` & sheet ignore `bn` locale ordering; picker search lacks empty state; visual origin-to-destination route connector is absent. | `06_fare_calculator_{light,dark}.png` |
| 11 | **Station Explorer** (`StationExplorerScreen.kt`) | **Needs improvement** | Add continuous MRT Line 6 vertical route spine connecting station nodes `01`–`16` for instant spatial clarity. | `07_stations_and_sheet_{light,dark}.png` |
| 12 | **Station Details** (`ModalBottomSheet` in `StationExplorerScreen.kt`) | **Requires redesign** | Hardcodes English `station.nameEn` & `" km"` even in `bn` locale; squeezes multi-line `landmarksEn\nlandmarksBn` into right-aligned `InfoKeyValueRow`. Redesign into localized header badge + dedicated stacked Landmark block + localized distance row. | `07_stations_and_sheet_{light,dark}.png` |
| 13 | **NFC Diagnostics** (`NfcDiagnosticsScreen.kt`) | **Requires redesign** | 100% hardcoded English labels (zero বাংলা localization); lacks persistent-IDm share warning near `FullIdmRow` and balanced dual-button sizing (`SecondaryTonalButton`). | `09_nfc_diagnostics_{light,dark}.png` |
| 14 | **Settings** (`SettingsScreen.kt`) | **Production-ready** | Cohesive grouped surface cards and clean modal selectors; minor icon-box size alignment (`38.dp`). | `v1` baseline + emulator verified |
| 15 | **Data Management** (`SettingsScreen.kt`) | **Requires redesign** | Missing `verticalScroll` (clips bottom danger CTA on small screens/large fonts); Clear History has no confirmation dialog; Delete All uses wrong `"Saved Cards"` card-removal string; JSON sheet lacks scroll; missing persistent-IDm export caution banner. | `08_data_management_{light,dark}.png` |
| 16 | **Help & FAQ** (`HelpFaqScreen.kt`) | **Needs improvement** | Lacks empty state when search finds 0 FAQs; fixed `height(48.dp)` button; FAQ privacy entry needs explicit warning against exposing raw `IDm` publicly. | `10_help_faq_{light,dark}.png` |
| 17 | **About** (`AboutScreen.kt`) | **Needs improvement** | Align TopBar title (`About TransitMate`); polish typography spacing and card grouping alongside `Help & FAQ` and `Settings`. | *(Paired with `Settings`/`Help`)* |
| — | **Legacy Startup** (`StartupScreen.kt`) | **Deprecated / Removed in Phase 09** | Verified zero references in navigation, manifest, DI, or production features; safe for removal alongside `StartupUiState` & `StartupViewModel`. | — |

### 2.1 Inventory Breakdown (Total: 17 Screens)
- **Production-ready (4):** Splash, Home, My Cards, Settings
- **Needs improvement (9):** Onboarding, NFC Scanner, Scan Result, Card Details, Insights, Fare Calculator, Station Explorer, Help & FAQ, About
- **Requires redesign (4):** Transaction History, Station Details (Sheet), NFC Diagnostics, Data Management
- **Verification Sum:** `4 + 9 + 4 = 17` screens (`+ Legacy Startup cleanup`).

---

## 3. Consistency Review (`v3_phase09` Mockups vs. Design System)

- **Color Parity:** All 20 PNGs (`10 screens × Light & Dark`) match `Color.kt` tokens (`#F6F7F8`/`#FFFFFF` Warm Off-White Light; `#111315`/`#191C1F` Neutral Graphite Dark; `#12746A`/`#2FA497` Muted Transit Teal; `#31628D`/`#5B8FB9` Metro Blue) with zero neon glows, glassmorphism, or decorative cityscapes.
- **Privacy & Honesty:** All mockups use synthetic IDm `01:27:AA:BB:CC:DD:EE:A0`, display explicit persistent-IDm sharing cautions, and never show fake/unverified numbers.
- **Implementability:** Every component in `v3_phase09/` maps 1:1 to existing or slightly refactored Compose building blocks (`TransitTopBar`, `SurfaceSectionCard`, `TransitHeroCard`, `StatusBannerCard`, `StatusPill`, `FullIdmRow`, `VerifiedTransactionRow`, `MetricBox`/`CardStatBox`, `PrimaryActionButton`, `SecondaryOutlinedButton`, `SecondaryTonalButton`).
