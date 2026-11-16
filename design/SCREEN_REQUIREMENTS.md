# TransitMate — Complete Screen & State Requirements Specification

**Document Version:** 1.0  
**Date:** 16 November 2026  
**Stage:** Stage 3 — Complete UI/UX Screen Planning  
**Status:** Finalized Specification — Pending Owner Sign-Off Before Stage 4 & Stage 6  
**Design System Basis:** [design/DESIGN_SYSTEM.md](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/design/DESIGN_SYSTEM.md)

---

## 1. Screen Inventory & Count Verification

Following an audit of `PROJECT_UI_AUDIT.md`, the application inventory consists of **15 distinct Core Screens** (with **Splash** and **Onboarding** counted separately as Screens #1 and #2, and zero double-counting of tab hubs vs. sub-routes) plus **25 Supporting States** mapped cleanly to inline layouts, alert banners, bottom sheets, dialogs, and sub-routes.

### 1.1 Core Screens Master Index (15 Screens)
| # | Screen Name | Nav Tier | Bottom Bar Visible | Reference Art Status |
|:---:|---|---|:---:|---|
| 01 | **Splash Screen** | Pre-App Launch | No | Adapted from `Neon Transit App Onboarding Duo.png` |
| 02 | **Onboarding Screen** | First-Launch Flow | No | Adapted from `Neon Transit App Onboarding Duo.png` |
| 03 | **Home Dashboard** | Primary Tab 1 | Yes | Adapted from `MetroSync BD_ Dual-Theme Transit Wallet.png` |
| 04 | **NFC Scanner Screen** | Primary Tab 3 | Yes *(Optional focused mode)* | Adapted from `Dual-Theme NFC Scanner UI.png` |
| 05 | **Scan Result / Success Screen** | Scan Outcome Route | No (Top Back + CTA) | Adapted from `Dual-Mode Transit Card Scan UI.png` |
| 06 | **Card Information / Details Screen** | Sub-route (`My Cards` / `Home`) | No | New Design Needed |
| 07 | **My Cards Screen** | Primary Tab 2 | Yes | New Design Needed |
| 08 | **Transaction History Screen** | Sub-route (`Home` / `Insights`) | Optional / Back Bar | Adapted from `Transit History App_ Dark and Light Themes.png` |
| 09 | **Travel Analytics / Insights Screen** | Primary Tab 4 | Yes | New Design Needed |
| 10 | **Fare Calculator Screen** | Offline Utility (`Home` / `Insights`) | No | New Design Needed |
| 11 | **Station Explorer Screen** | Offline Utility (`Home`) | No | New Design Needed |
| 12 | **NFC Diagnostics Screen** *(Developer)* | Sub-route (`Settings` only) | No | Extracted from Phase 03 `StartupScreen.kt` |
| 13 | **Settings Screen** | Primary Tab 5 | Yes | New Design Needed |
| 14 | **Help & FAQ Screen** | Sub-route (`Settings` / `Scanner`) | No | New Design Needed |
| 15 | **About Screen** | Sub-route (`Settings`) | No | New Design Needed |

---

## 2. Core Screen Specifications (All 15 Screens)

---

### Screen 01: Splash Screen
1. **Screen Name:** Splash Screen
2. **Screen Purpose:** Provide an immediate, calm brand launch transition while checking initial DataStore preferences (onboarding completion, theme preference, locale) and device NFC availability.
3. **User Goal:** Launch the app reliably without jarring flashes or delays.
4. **Entry Point:** Cold app launch (`MainActivity`).
5. **Navigation Destination:** Routes automatically to **Onboarding Screen (02)** on first launch, or **Home Dashboard (03)** for returning users.
6. **Layout Structure:** Centered single-column viewport (`fillMaxSize`), generous breathing room, zero clutter.
7. **Header Structure:** None (no TopAppBar, Edge-to-Edge system bars matching background).
8. **Main Content Sections:** Center TransitMate brand mark (`88.dp` adaptive rounded-square vector mark) -> App Name (`TransitMate`, `headlineLarge`) -> Bilingual subtle subtitle (`Dhaka Metro & Rapid Pass Companion`) -> Bottom discreet `CircularProgressIndicator` (`24.dp`) or Android 12+ SplashScreen API seamless handoff.
9. **Required Components:** Brand logo vector/painter, `Text`, optional `CircularProgressIndicator`.
10. **Icon Requirements:** Official TransitMate logo mark (never emoji).
11. **Primary & Secondary Actions:** None (auto-transitions within `<= 800ms` once preferences load).
12. **Loading State:** Subtle `24.dp` progress indicator (`Primary` teal) while preferences initialize.
13. **Empty State:** Not applicable.
14. **Error State:** Fallback safely to default Light/Dark system theme and Home Dashboard if preference read fails.
15. **Data Requirements:** `hasCompletedOnboarding: Boolean`, `themeMode`, `appLocale` (read locally). No NFC card reading occurs here.
16. **Light Theme Behavior:** Warm Off-White (`#F6F7F8`) background, dark slate title (`#16191D`), muted teal spinner (`#12746A`).
17. **Dark Theme Behavior:** Neutral Graphite (`#111315`) background, soft white title (`#F1F3F5`), muted teal spinner (`#2FA497`).
18. **Bangla Localization Requirements:** Subtitle switches cleanly between English and বাংলা (*ঢাকা মেট্রো ও র‍্যাপিড পাস সঙ্গী*) without vertical clipping.
19. **Animation Requirements:** Smooth alpha fade-in (`220ms`) and seamless crossfade (`180ms`) into Onboarding or Home. Zero neon pulses.
20. **Accessibility Requirements:** `contentDescription` on logo ("TransitMate Logo"); respects reduced motion.

---

### Screen 02: Onboarding Screen
1. **Screen Name:** Onboarding Screen
2. **Screen Purpose:** Educate first-time users on how TransitMate works (NFC card tap guidance, offline-first privacy, and honest capability boundaries).
3. **User Goal:** Understand where to tap their card and start using the app in under 15 seconds.
4. **Entry Point:** Automatically from Splash Screen (01) on first launch; optionally replayable from Help & FAQ (14).
5. **Navigation Destination:** **Home Dashboard (03)** (marks `hasCompletedOnboarding = true`).
6. **Layout Structure:** `Scaffold` with Top utility row (`Skip`), centered `HorizontalPager` (3 concise slides), bottom dot indicator + full-width Primary CTA button.
7. **Header Structure:** Minimal top row with right-aligned `Skip` `TextButton` (hidden on final slide).
8. **Main Content Sections:**
   - *Slide 1 — Tap & Inspect:* Minimalist vector diagram showing phone upper-back NFC zone aligned with a transit card; Title: *"Tap & Inspect Easily"*; Copy: *"Hold your MRT Pass or Rapid Pass behind your phone to detect card info."*
   - *Slide 2 — Offline & Private:* Shield vector graphic; Title: *"Offline-First & Private"*; Copy: *"Your card history stays strictly on your phone. No login or internet required."*
   - *Slide 3 — Honest Readings:* Verified-check vector graphic; Title: *"Transparent Card Support"*; Copy: *"Reads verified card capabilities honestly. Supports non-NFC phones for fare & station tools."*
9. **Required Components:** `HorizontalPager`, custom crisp vector onboarding illustrations (`200.dp`), pager indicator dots, `TextButton` (`Skip`), `Button` (`Next` / `Get Started`).
10. **Icon Requirements:** `Contactless` / custom clean line-art phone-card illustration, `VerifiedUser` / `Shield`, `DirectionsTransit` — uniform stroke weight, no neon glow.
11. **Primary & Secondary Actions:** Primary: `Next` (Slides 1–2) / `Get Started` (Slide 3); Secondary: `Skip`.
12. **Loading State:** Not applicable (static local slides).
13. **Empty State:** Not applicable.
14. **Error State:** Not applicable.
15. **Data Requirements:** Persists `hasCompletedOnboarding = true` locally in DataStore.
16. **Light Theme Behavior:** `#F6F7F8` canvas, illustrations in `#12746A` + `#31628D` on `#FFFFFF` subtle card backing.
17. **Dark Theme Behavior:** `#111315` canvas, illustrations in `#2FA497` + `#5B8FB9` on `#191C1F` subtle backing.
18. **Bangla Localization Requirements:** Concise 2-line headlines and 2–3 line body copy with `1.45x` lineHeight in বাংলা.
19. **Animation Requirements:** Smooth `HorizontalPager` swipe physics; animated pill width expansion (`200ms`) on active pager dot.
20. **Accessibility Requirements:** Swipeable or button-navigable (`Next` button always visible for TalkBack users who don't swipe pagers); `48.dp` min height on `Skip` and CTA.

---

### Screen 03: Home Dashboard
1. **Screen Name:** Home Dashboard
2. **Screen Purpose:** Act as the daily commuter command center displaying primary transit card status, verified balance (when available), quick NFC scan action, NFC readiness banner, and fast access to offline transit tools.
3. **User Goal:** Check current card status/balance at a glance, initiate a quick NFC scan, or jump to Fare Calculator / Stations.
4. **Entry Point:** App launch (returning user) or `Home` tab on Bottom Navigation.
5. **Navigation Destination:** **NFC Scanner (04)**, **Card Details (06)**, **My Cards (07)**, **Transaction History (08)**, **Fare Calculator (10)**, **Station Explorer (11)**.
6. **Layout Structure:** `Scaffold` with Top App Bar + Bottom Navigation Bar (5 tabs) + `LazyColumn` (`20.dp` horizontal margins, `16.dp` item gaps).
7. **Header Structure:** Clean Left Brand Title (`TransitMate` + concise subtle wordmark/icon) + Right contextual status chip (`Offline` / NFC Ready dot). **Strictly NO user avatar, profile greeting, or notification bell.**
8. **Main Content Sections (Visual Hierarchy Order):**
   1. **NFC Readiness Alert Banner** *(conditional — shown only if NFC is Disabled or Unsupported, with `[ Enable NFC ]` action)*.
   2. **Primary Card Hero (`TransitCardHero`):** Shows primary saved/last card nickname, Card Type (`MRT Pass` / `Rapid Pass` / `Unverified`), Status Pill, Privacy-masked Identifier (compact), and **Balance Area**:
      - If verified balance exists (Phase 06+): bold tabular `৳ 150` + verified timestamp.
      - If balance not verified/unavailable: honest `"Balance Unavailable"` state with clear brief note (*"Card detected; balance reading pending verification"*), NEVER fake numbers.
      - If no card saved yet: calm `"Tap a transit card to get started"` prompt card.
   3. **Primary Action Row:** Clean full-width `Button` (`Scan Transit Card`, `Contactless` icon) — sleek and crisp, no glowing orb.
   4. **Quick Tools Grid (2 Columns):** Compact equal cards for **Fare Calculator** (`Calculate`) and **Station Explorer** (`Train`).
   5. **Recent Activity Preview Card:** Header (`Recent Activity` + `View All ->` link). Shows last 3 verified trips/scans OR honest `Empty / Transactions Unavailable` state.
9. **Required Components:** `TransitTopBar`, `TransitBottomNav`, `StatusBannerCard`, `TransitCardHero`, `Button`, 2-column Shortcut Cards, `SurfaceSectionCard` (`Recent Activity`).
10. **Icon Requirements:** `Home`, `Contactless`, `CreditCard`, `Calculate`, `DirectionsSubway`, `History`, `ChevronRight`, `WarningAmber` (for NFC disabled).
11. **Primary & Secondary Actions:** Primary: `Scan Transit Card`; Secondary: Tap Hero Card -> `Card Details (06)`, `View All` -> `Transaction History (08)`, Shortcut tiles -> `Fare Calculator (10)` / `Station Explorer (11)`, `Open NFC Settings` (when disabled).
12. **Loading State:** Subtle placeholder surface blocks (`SurfaceVariant`) during initial local database hydration.
13. **Empty State:** When no cards are saved: Hero area shows clean onboarding-style `"No card added yet"` card with `[ Scan Your First Card ]` CTA; Offline tools (`Fare Calculator`, `Stations`) remain fully usable.
14. **Error State:** Actionable `NFC Disabled` banner with button to open `android.settings.NFC_SETTINGS`; non-intrusive `NFC Not Supported` informational banner explaining offline tools still work.
15. **Data Requirements:** `nfcHardwareStatus`, `primarySavedCard?` (`nickname`, `cardType`, `maskedId`, `verifiedBalance?`, `lastScannedAt?`), `recentActivity: List` (strictly local verified entries). Zero raw `systemCode`/`PMm` shown here.
16. **Light Theme Behavior:** `#F6F7F8` background, `#FFFFFF` cards with `1.dp` `#DCE1E7` border, `#12746A` CTA button.
17. **Dark Theme Behavior:** `#111315` background, `#191C1F` hero & section cards, `#2E343B` `1.dp` borders, `#2FA497` CTA.
18. **Bangla Localization Requirements:** All headers, shortcut titles (`ভাড়া হিসাব`, `স্টেশন তালিকা`), and honest empty/unavailable notes fully localized with `maxLines = 2` safety.
19. **Animation Requirements:** Gentle `220ms` card fade+slide entrance on first load; smooth `animateContentSize()` when conditional NFC warning banner appears/dismisses.
20. **Accessibility Requirements:** Logical focus order (Banner -> Hero Card -> Scan CTA -> Quick Tools -> Recent Activity -> Bottom Nav); explicit labels for masked IDs and status pills.

---

### Screen 04: NFC Scanner Screen
1. **Screen Name:** NFC Scanner Screen
2. **Screen Purpose:** Provide a focused, distraction-free contactless reading state that instructs the user on physical card alignment and executes Android Reader Mode (`NFC_F` / `NFC_A`) cleanly.
3. **User Goal:** Tap their MRT Pass or Rapid Pass card accurately and receive instant, reliable scan feedback.
4. **Entry Point:** Center `Scan` tab in Bottom Navigation, or `Scan Transit Card` button on Home / My Cards.
5. **Navigation Destination:** Automatically transitions to **Scan Result Screen (05)** on valid read, opens **Supporting Bottom Sheet** on `Unsupported Card` / `Scan Failed` / `NFC Communication Error`, or opens **Help & FAQ (14)** via Help button.
6. **Layout Structure:** `Scaffold` with Top Bar (`Back` or tab context, `NFC Scanner`, `Help` icon) + Bottom Nav Bar (or focused modal flow when launched as push route) + centered instruction & alignment area + bottom action area.
7. **Header Structure:** `TopAppBar` with Screen Title (`NFC Scanner`), Help (`?` `HelpOutline`) button on top-right.
8. **Main Content Sections:**
   1. **Hardware Readiness Pill:** Compact status chip (`NFC Ready` in Success green, or `NFC Off` / `Not Supported`).
   2. **Card Alignment Visual:** Clean vector illustration showing the back of an Android smartphone and a transit card positioned over the top-center NFC antenna coil, surrounded by a subtle animated ring (no neon glow, no train photo).
   3. **Clear Guidance Text:** *"Hold your MRT Pass or Rapid Pass steady against the back of your phone"* (`titleMedium` + `bodyMedium` helper: *"Keep steady for 1–2 seconds without moving"*).
   4. **Live Status Indicator Pill:**
      - Waiting: Subtle pulse dot + *"Waiting for card..."*
      - Detecting: Indeterminate spinner + *"Reading card..."*
   5. **Cancel / Retry Action:** `OutlinedButton` (`Cancel` / `Stop Scanning`).
9. **Required Components:** `TransitTopBar`, `StatusChip`, custom clean NFC Alignment Vector graphic, `CircularProgressIndicator`, `OutlinedButton`, `Button` (for `Open NFC Settings` when disabled).
10. **Icon Requirements:** `HelpOutline`, `Contactless`, `CheckCircle` (on momentary detect), `WifiOff` / `Nfc` (when disabled/unsupported), `Close`.
11. **Primary & Secondary Actions:** Primary (if NFC off): `Open NFC Settings`; Active scan: `Cancel`; Top-right: `Tap Tips / Help` -> opens Help sheet or Help Screen (14).
12. **Loading State:** `"Reading card..."` state (`ProgressIndicator` + disable cancel double-tap) active while `NfcDiagnosticReader` / scanner executes `connect()` and safe discovery.
13. **Empty State:** Waiting state itself (`Waiting for Card`) — clean, calm instruction.
14. **Error State:** Handled via inline state (`NFC Disabled`, `NFC Not Supported`) or actionable Modal Bottom Sheet (`TagLost` -> *"Card moved too early. Hold steady and try again."* + `[ Try Again ]`; `Unsupported Card` -> explains NFC-A/EMV card is not a supported FeliCa transit card).
15. **Data Requirements:** Reads `NfcHardwareStatus` and listens to `NfcAdapter.ReaderCallback`. Never shows raw `PMm`/`System Code` hex here. Never triggers success state before real `Tag` callback.
16. **Light Theme Behavior:** `#F6F7F8` canvas, alignment vector in `#12746A` & `#5C6570` inside `#FFFFFF` rounded frame.
17. **Dark Theme Behavior:** `#111315` canvas, alignment vector in `#2FA497` & `#9AA2AC` inside `#191C1F` frame.
18. **Bangla Localization Requirements:** Clear everyday বাংলা instructions (*"আপনার এমআরটি পাস বা র‍্যাপিড পাস ফোনের পেছনে ধরে রাখুন"*).
19. **Animation Requirements:** `1600ms` calm breathing pulse (`alpha 0.35 -> 0.08`) while waiting; crisp `260ms` transition upon physical `Tag` detection; respects reduced motion.
20. **Accessibility Requirements:** `liveRegion = LiveRegionMode.Polite` announces *"Waiting for NFC card"*, *"Card detected"*, or *"Scan failed, please hold card steady"*.

---

### Screen 05: Scan Result / Success Screen
1. **Screen Name:** Scan Result / Success Screen
2. **Screen Purpose:** Present the truthful, human-friendly outcome of a physical NFC scan, clearly separating **Detection**, **Card Recognition**, and **Balance/Transaction Availability**.
3. **User Goal:** Confirm their card was scanned successfully, see recognized card info (and verified balance if supported), save/bookmark the card locally, or scan again.
4. **Entry Point:** Automatic navigation immediately after a successful tag read in **NFC Scanner Screen (04)**.
5. **Navigation Destination:** Back to **Home Dashboard (03)**, **NFC Scanner (04)** (`Scan Another`), **Card Details (06)**, or opens **Rename Card Dialog** when saving.
6. **Layout Structure:** `Scaffold` with `TopAppBar` (`Back`, `Scan Result`), scrollable single-column body (`20.dp` margin), pinned or bottom-aligned primary/secondary action pair.
7. **Header Structure:** `TopAppBar` with Back arrow + `"Scan Result"` title + optional `BookmarkAdd` / Save action.
8. **Main Content Sections (Strict Honest Hierarchy):**
   1. **Outcome Confirmation Pill:** Clean `SuccessContainer` banner (`CheckCircle` icon + `"Card Detected Successfully"`).
   2. **Card Preview Header (`TransitCardHero`):** Visual card card showing Card Recognition status (`Rapid Pass · DTCA` / `MRT Pass · DMTCL` / `Unidentified Transit Card`), Detected Technology chip (`FeliCa / NFC-F`), and **Privacy-Safe Masked Identifier** (`01:27:••:••:••:••:••:A0` — optional reveal toggle or copy only if user taps).
   3. **Balance & History Capability Card:**
      - *When verified balance is available (Phase 06+):* Prominent `৳ ...` (`displayLarge`, tabular numerals) + `Last Scanned: Today, 2:35 PM`.
      - *When balance is not yet supported/verified (Current Phase):* Calm neutral-warning card with `InfoOutline` icon: **"Balance Not Available"** + honest explanation (*"NFC-F card detected cleanly. Verified balance parsing for this card type is not yet enabled or is protected."*). Zero fabricated figures!
   4. **Structured Summary Rows (`SurfaceSectionCard`):**
      - `Detection Status`: `Successful`
      - `RF Technology`: `FeliCa (NFC-F)`
      - `Card Recognition`: `Unverified / Pending Classification` *(or Verified `Rapid Pass` / `MRT Pass` once system/service proof passes)*
      - `Last Scanned`: Formatted local date & time
      *(Strictly NO `System Code`, NO `PMm`, NO `Scan #`, NO `Anticollision` jargon!)*
   5. **Action Buttons:** Primary `Button` (`Save to My Cards` or `View in My Cards` if already saved) + Secondary `OutlinedButton` (`Scan Another Card`).
9. **Required Components:** `TransitTopBar`, `StatusBannerCard`, `TransitCardHero`, `SurfaceSectionCard`, `InfoKeyValueRow`, `Button`, `OutlinedButton`.
10. **Icon Requirements:** `ArrowBack`, `CheckCircle`, `Contactless`, `AccountBalanceWallet` / `InfoOutline`, `BookmarkBorder` / `Bookmark`, `ContentCopy` (optional for masked ID).
11. **Primary & Secondary Actions:** `Save to My Cards` (triggers optional nickname dialog), `Scan Another Card` -> returns to Scanner (04), `Done` / `Back` -> Home (03).
12. **Loading State:** Brief (`< 300ms`) card-classification state if matching against local saved cards.
13. **Empty State:** Not applicable (only entered on detection; if identifier or balance missing, uses honest `"Unavailable"` sub-states).
14. **Error State:** If partial read occurs (card detected, identifier unavailable), displays `"Identifier Unavailable"` inline notice safely without crashing.
15. **Data Requirements:** `detectionSuccess: Boolean`, `rfTechnology: String`, `recognitionStatus: CardType/Unverified`, `maskedIdentifier: String?`, `verifiedBalance: Double?`, `scannedAt: Long`, `isAlreadySaved: Boolean`.
16. **Light Theme Behavior:** `#F6F7F8` canvas, `#DCF4E7` soft green confirmation pill, `#FFFFFF` structured detail cards.
17. **Dark Theme Behavior:** `#111315` canvas, `#153326` muted green confirmation pill, `#191C1F` structured detail cards with `1.dp` `#2E343B` outline.
18. **Bangla Localization Requirements:** Honest capability labels in natural বাংলা (*"ব্যালেন্স তথ্য পাওয়া যায়নি"*, *"কার্ড সফলভাবে শনাক্ত হয়েছে"*, *"প্রযুক্তি: ফেলিকা (NFC-F)"*).
19. **Animation Requirements:** Smooth `260ms` confirmation icon pop + staggered subtle card fade (`180ms`–`240ms`).
20. **Accessibility Requirements:** Screen reader announces card detection & recognition status first; masked ID reads cleanly character-by-character or announces `"Identifier masked for privacy"`.

---

### Screen 06: Card Information / Details Screen
1. **Screen Name:** Card Information / Details Screen
2. **Screen Purpose:** Display full details, capabilities, nickname management, and local history scoped to a single saved transit card.
3. **User Goal:** Manage a saved card's label, check its last verified status/timestamp, inspect its travel history, or remove it from the device.
4. **Entry Point:** Tapping a card on **Home Dashboard (03)** or **My Cards Screen (07)**, or tapping `View Card` after saving from Scan Result (05).
5. **Navigation Destination:** **NFC Scanner (04)** (`Re-scan Card`), **Transaction History (08)** (filtered to this card), **Rename Card Dialog**, **Delete Card Confirmation Dialog**.
6. **Layout Structure:** `Scaffold` with `TopAppBar` (`Back`, Card Title, Overflow `MoreVert` menu for Rename/Delete) + scrollable `Column`.
7. **Header Structure:** `TopAppBar` with Back button, Card Nickname (e.g., *"Daily Commute Card"*), and action icons (`Edit`, `Delete`).
8. **Main Content Sections:**
   1. **Hero Card Header:** Card nickname, Authority (`DTCA` / `DMTCL` / `Unverified`), Card Status chip (`Active Saved` / `Primary`), Masked Identifier (`•••• •••• ••A0` with optional tap-to-toggle partial mask), Verified Balance (or `"Not Available"`).
   2. **Primary Action:** `Button` (`Tap to Refresh / Scan Card`) — clearly clarifies balance updates only upon fresh physical scan.
   3. **Card Metadata Card (`SurfaceSectionCard`):** `Card Technology` (`FeliCa / NFC-F`), `Added On`, `Last Scanned`, `Primary Card` switch (`Switch` to pin card to Home Dashboard).
   4. **Card-Scoped Activity Preview:** Recent verified trips/scans for this card + `View Card History ->` or honest `No recorded trips` empty card.
   5. **Danger Action Row:** Subtle outlined/text destructive button (`Remove Card from Device`).
9. **Required Components:** `TransitTopBar`, `TransitCardHero`, `Button`, `SurfaceSectionCard`, `InfoKeyValueRow`, `Switch`, `RenameCardDialog`, `DeleteCardDialog`.
10. **Icon Requirements:** `ArrowBack`, `Edit`, `DeleteOutline`, `Contactless`, `Star` / `PushPin`, `History`, `VerifiedUser`.
11. **Primary & Secondary Actions:** `Scan Card to Update`, `Set as Primary Card` toggle, `Rename Card`, `Delete Card`, `View Full History`.
12. **Loading State:** Skeleton card placeholder while querying card entity & associated transactions from Room.
13. **Empty State:** If card has 0 verified trips -> `"No travel records stored for this card yet."`
14. **Error State:** If card ID not found (deleted externally) -> safe fallback `"Card no longer exists"` with `Back to My Cards`.
15. **Data Requirements:** `SavedCard` entity (`id`, `nickname`, `cardType`, `maskedIdentifier`, `isPrimary`, `addedAt`, `lastScannedAt`, `lastVerifiedBalance?`, `lastVerifiedBalanceAt?`). Note: If `lastVerifiedBalanceAt` is older than current scan without balance update, clearly label as *"Last verified on [Date]"*, never pass off stale balance as live.
16. **Light & Dark Theme Behavior:** Consistent surface tokens (`#FFFFFF` vs `#191C1F`); destructive button in `#C2413B` (Light) / `#D95F59` (Dark).
17. **Dark Theme Behavior:** `#111315` canvas, `#191C1F` hero & info containers, `1.dp` `#2E343B` border.
18. **Bangla Localization Requirements:** Labels for `"Primary Card"` (*"প্রাথমিক কার্ড"*), `"Last Verified"` (*"সর্বশেষ যাচাই"*), `"Remove Card"` (*"কার্ড মুছুন"*).
19. **Animation Requirements:** `220ms` slide transition; smooth `Switch` toggle; confirmation dialog scale-fade (`180ms`).
20. **Accessibility Requirements:** `Edit` and `Delete` icons have explicit labels (`"Rename card"`, `"Delete card"`); `Switch` wraps entire row with `Role.Switch` semantics.

---

### Screen 07: My Cards Screen
1. **Screen Name:** My Cards Screen
2. **Screen Purpose:** Manage multiple locally stored transit cards (MRT Pass, Rapid Pass, or labeled cards) in one clean list.
3. **User Goal:** View all saved cards, switch primary dashboard card, rename/delete cards, or scan a new card.
4. **Entry Point:** `My Cards` tab (Tab 2) in Bottom Navigation.
5. **Navigation Destination:** **Card Details Screen (06)**, **NFC Scanner Screen (04)** (`Add / Scan Card`).
6. **Layout Structure:** `Scaffold` with `TopAppBar` + Bottom Navigation Bar + `LazyColumn` of saved card items + bottom/top `+ Scan New Card` action.
7. **Header Structure:** `TopAppBar` (`My Cards` + `+` / `Add Card` icon button when cards exist).
8. **Main Content Sections:**
   1. **Primary Card Section** *(if >= 1 card)*: Highlighted card badge (`Primary`) + quick access.
   2. **All Saved Cards List:** Clean vertical stack of compact `SavedCardItem` cards showing: Authority color strip (`4.dp`), Nickname, Card Type, Masked ID (`•••• A0`), Verified Balance (or `Balance Unavailable`), Last Scanned relative time, and quick overflow menu (`Set Primary`, `Rename`, `Delete`).
   3. **Bottom Privacy Footnote:** Subtle lock/shield note (`"Cards are stored strictly offline on this device"`).
9. **Required Components:** `TransitTopBar`, `TransitBottomNav`, `SavedCardItem`, `DropdownMenu` (contextual row actions), `EmptyStateView`, `Button` (`Scan New Card`).
10. **Icon Requirements:** `CreditCard`, `Add`, `MoreVert`, `Star`, `Edit`, `DeleteOutline`, `Shield`.
11. **Primary & Secondary Actions:** Tap card -> `Card Details (06)`; `+ Scan Card` -> `NFC Scanner (04)`; Overflow menu -> `Set as Primary`, `Rename`, `Delete`.
12. **Loading State:** 2 card skeleton placeholders during Room query.
13. **Empty State:** `Empty Saved Cards` centered view (`CreditCard` in `64.dp` tonal circle, `"No saved cards yet"`, `"Scan your MRT Pass or Rapid Pass to save it locally for quick access."`, `[ Scan a Card ]` Primary CTA).
14. **Error State:** Local database read error banner with `Retry`.
15. **Data Requirements:** `List<SavedCard>` sorted by `isPrimary DESC, lastScannedAt DESC`.
16. **Light Theme Behavior:** `#F6F7F8` canvas, `#FFFFFF` cards with `#DCE1E7` border.
17. **Dark Theme Behavior:** `#111315` canvas, `#191C1F` cards with `#2E343B` border.
18. **Bangla Localization Requirements:** Proper plural/empty handling and concise card labels in বাংলা.
19. **Animation Requirements:** `animateItem()` smooth list reordering when toggling Primary card or deleting a card.
20. **Accessibility Requirements:** Each card row announces `"Nickname, Card Type, Status, double tap for details"`; overflow button `48.dp` target.

---

### Screen 08: Transaction History Screen
1. **Screen Name:** Transaction History Screen
2. **Screen Purpose:** Show a chronological, date-grouped ledger of verified card trips and top-ups recorded locally from scanned cards.
3. **User Goal:** Review past trips, filter by card or date range, inspect station-to-station fare deductions, and verify spending.
4. **Entry Point:** `View All` on Home Dashboard (03), from **Insights Screen (09)**, or from **Card Details (06)**.
5. **Navigation Destination:** **Card Details (06)** (if tapped via filter) or optional Trip Detail Bottom Sheet.
6. **Layout Structure:** `Scaffold` with `TopAppBar` (`Back`, `Transaction History`, `Filter` icon) + Top KPI Summary card + Filter chips row + date-grouped `LazyColumn` with subtle vertical timeline markers.
7. **Header Structure:** `TopAppBar` with Back navigation, title `"Transaction History"`, and `FilterList` action button (opens card/type filter sheet or toggles chip bar).
8. **Main Content Sections:**
   1. **Summary KPI Banner (`SurfaceSectionCard`):** Clean 2-column metric bar (`Total Trips` | `Total Spent` in tabular `৳` — strictly computed from real stored records; shows `—` or hidden if unavailable).
   2. **Filter Chips Row:** Horizontal scrollable `FilterChip`s (`All Cards`, `MRT Pass`, `Rapid Pass`, `Trips`, `Top-Ups`).
   3. **Date-Grouped Timeline List:** Sticky/grouped date headers (`Today · Nov 16`, `Yesterday · Nov 15`) -> Individual `TransactionRowItem` cards featuring: concise origin/destination (or station entry/exit), timestamp, card label badge, and fare (`- ৳ 30` or `+ ৳ 500` in Success green) with subtle vertical timeline rail (`2.dp` `Outline` line + `8.dp` muted teal dot — zero neon glow).
9. **Required Components:** `TransitTopBar`, `SummaryMetricCard`, `FilterChip` row, `TransactionRowItem`, `EmptyStateView`, `StatusBannerCard` (`Transaction Not Available`).
10. **Icon Requirements:** `ArrowBack`, `FilterList`, `DirectionsSubway`, `SwapHoriz` / `ArrowForward`, `AddCard` (for top-up), `Schedule`, `History`.
11. **Primary & Secondary Actions:** Filter by Card / Type, Clear Filters, Tap trip row for quick station/time breakdown sheet.
12. **Loading State:** 4 timeline row skeletons while reading local history.
13. **Empty State:**
    - *No verified trips recorded:* `Empty Transaction History` (*"No trips recorded yet"* + *"Verified card history appears here after scanning a supported card."*).
    - *Card type unsupported for history:* `Transaction Not Available` banner clearly stating block parsing is unverified/unavailable.
    - *Filter yields 0 results:* `"No matching trips"` + `[ Clear Filters ]`.
14. **Error State:** Local query failure inline retry banner.
15. **Data Requirements:** `List<TransactionRecord>` (`id`, `cardId`, `originStation?`, `destinationStation?`, `amount`, `type: TRIP | TOP_UP`, `timestamp`, `isVerified`). **Strictly zero fabricated transactions.**
16. **Light Theme Behavior:** Clean `#FFFFFF` summary card (`1.dp` `#DCE1E7` border, NO saturated blue gradient or train watermark behind card).
17. **Dark Theme Behavior:** `#191C1F` summary & trip cards on `#111315` canvas, subtle `#2FA497` muted timeline dots.
18. **Bangla Localization Requirements:** Date formatting (`আজ`, `গতকাল`), Dhaka station names in বাংলা/English, `৳` tabular numerals.
19. **Animation Requirements:** Staggered subtle entrance on filter update (`animateContentSize`).
20. **Accessibility Requirements:** Trip rows combine origin, destination, time, and amount into one clear TalkBack announcement.

---

### Screen 09: Travel Analytics / Insights Screen
1. **Screen Name:** Travel Analytics / Insights Screen
2. **Screen Purpose:** Provide commuter spending analytics, trip frequency trends, and quick entry to History, Fare Calculator, and Station Explorer.
3. **User Goal:** Understand monthly transit spending, frequent routes/stations, and access planning tools.
4. **Entry Point:** `Insights` tab (Tab 4) in Bottom Navigation.
5. **Navigation Destination:** **Transaction History (08)**, **Fare Calculator (10)**, **Station Explorer (11)**, **NFC Scanner (04)**.
6. **Layout Structure:** `Scaffold` with `TopAppBar` + Bottom Navigation Bar + `LazyColumn` (`20.dp` margins).
7. **Header Structure:** `TopAppBar` (`Insights`) + optional timeframe selector pill (`This Month` / `30 Days` / `All Time`).
8. **Main Content Sections:**
   1. **Monthly Travel Summary Card:** Key stats (`Trips This Month`, `Monthly Spend`, `Average Fare` — derived strictly from verified local data, or clean `"No data yet"` dashes).
   2. **Weekly / Monthly Spending Chart Card:** Minimalist, clean Compose Canvas bar chart (muted Teal bars on `SurfaceVariant` baseline grid — zero glowing neon bars). Hidden/replaced by `No Analytics Data` state when trip count == 0.
   3. **Top Routes / Frequent Stations Card:** Ranked horizontal progress bars showing top 3 visited stations or routes.
   4. **Transit Planning & Ledger Links (`SurfaceSectionCard`):** Clean list rows linking directly to:
      - **Full Transaction History** -> Screen (08)
      - **Fare Calculator** -> Screen (10)
      - **Station Explorer** -> Screen (11)
9. **Required Components:** `TransitTopBar`, `TransitBottomNav`, `FilterChip` (period), `MonthlySummaryCard`, `MinimalBarChart`, `NavigationActionRow`, `EmptyStateView`.
10. **Icon Requirements:** `BarChart`, `History`, `Calculate`, `DirectionsSubway`, `TrendingUp`, `ChevronRight`.
11. **Primary & Secondary Actions:** Switch timeframe (`Week` / `Month` / `All`), Open `Transaction History (08)`, Open `Fare Calculator (10)`, Open `Station Explorer (11)`.
12. **Loading State:** Chart & KPI surface placeholders.
13. **Empty State (`No Analytics Data`):** Calm visual placeholder inside Analytics area (*"Insights need recorded trips"* + *"Scan a card with verified travel history to unlock spending charts. Meanwhile, explore stations or calculate fares below."*) — keeping the `Fare Calculator` and `Station Explorer` cards accessible underneath!
14. **Error State:** Aggregation failure fallback card with retry.
15. **Data Requirements:** `AnalyticsSummary(period, tripCount, totalSpend, avgFare, dailyBuckets, topStations)` computed locally from verified transactions.
16. **Light Theme Behavior:** `#FFFFFF` chart card, `#12746A` bars, `#5C6570` axis labels.
17. **Dark Theme Behavior:** `#191C1F` chart card, `#2FA497` muted bars, `#9AA2AC` axis labels.
18. **Bangla Localization Requirements:** Localized period chips (`এই মাস`, `সব সময়`), Bangla numerals/station names, unobstructed chart labels.
19. **Animation Requirements:** `400ms` smooth vertical bar height interpolation (`EaseOutCubic`) on chart data load/period change.
20. **Accessibility Requirements:** Chart exposes spoken summary semantics (*"Total spent 360 Taka across 12 trips this month"*).

---

### Screen 10: Fare Calculator Screen
1. **Screen Name:** Fare Calculator Screen
2. **Screen Purpose:** Provide an offline-first fare lookup utility for Dhaka Metro Rail (MRT Line 6) routes between any two stations, working on both NFC and non-NFC devices.
3. **User Goal:** Quickly calculate single-journey vs. MRT/Rapid Pass discounted fare between Origin and Destination stations.
4. **Entry Point:** Quick Shortcut on **Home Dashboard (03)** or **Insights Screen (09)**.
5. **Navigation Destination:** **Station Explorer (11)** (`View Station Info` for selected station).
6. **Layout Structure:** `Scaffold` with `TopAppBar` (`Back`, `Fare Calculator`) + scrollable single-column form & result card.
7. **Header Structure:** `TopAppBar` with Back button and `"Fare Calculator"` title.
8. **Main Content Sections:**
   1. **Route Selector Card (`SurfaceSectionCard`):**
      - `From Station` selector field (`Origin` picker button)
      - Subtle `SwapVert` circular button on right edge to swap Origin <-> Destination cleanly
      - `To Station` selector field (`Destination` picker button)
   2. **Fare Result Card** *(displayed once Origin & Destination differ)*:
      - Route header (`Uttara North` -> `Motijheel` · `~16 stations`)
      - **Standard Single Journey Ticket:** e.g., `৳ 100`
      - **MRT Pass / Rapid Pass (10% Discounted Fare):** Highlighted in `PrimaryContainer` + `Verified` badge (e.g., `৳ 90`, clearly attributed to published official DMTCL fare matrix)
   3. **Recent / Popular Routes Chips:** Quick one-tap presets (only if previously used locally, or clear empty state).
   4. **Source Disclaimer Footer:** Subtle `InfoOutline` note (*"Based on published DMTCL MRT Line 6 fare chart. Works 100% offline."*).
9. **Required Components:** `TransitTopBar`, `StationSelectorField`, `IconButton` (Swap), `ModalBottomSheet` (`Searchable Station Picker`), `FareComparisonCard`, `EmptyStateView`.
10. **Icon Requirements:** `ArrowBack`, `TripOrigin`, `Place`, `SwapVert`, `Calculate`, `InfoOutline`.
11. **Primary & Secondary Actions:** Select `From` station, Select `To` station, `Swap` stations, `Clear Selection`, `View Station Details`.
12. **Loading State:** Minimal instant local lookup (`< 50ms`).
13. **Empty State (`Fare Calculator Empty State`):** Before both stations are picked -> calm instructional card (*"Select departure and destination stations to calculate official MRT fare"*). If same station selected -> clear inline helper (*"Select two different stations"*).
14. **Error State:** Fallback message if bundled static fare table fails to load.
15. **Data Requirements:** Bundled offline static JSON/Kotlin asset for verified MRT Line 6 stations & published fare matrix (`stations: List<MetroStation>`, `fareMatrix`). Must distinguish clearly between Single Journey Ticket and Smart Card discounted fare.
16. **Light Theme Behavior:** `#FFFFFF` route selector & fare result cards, `#DDF2EE` discounted pass pill.
17. **Dark Theme Behavior:** `#191C1F` route selector & fare cards, `#1B3B38` discounted pass pill.
18. **Bangla Localization Requirements:** Every station searchable and displayed in both বাংলা (`উত্তরা উত্তর`, `মতিঝিল`) and English; `৳` tabular formatting.
19. **Animation Requirements:** `180°` swap button rotation on tap; smooth `AnimatedVisibility` fade/expand of Fare Result Card.
20. **Accessibility Requirements:** Swap icon labeled `"Swap departure and destination stations"`; searchable picker announces filtered count.

---

### Screen 11: Station Explorer Screen
1. **Screen Name:** Station Explorer Screen
2. **Screen Purpose:** Provide an offline directory of Dhaka Metro Rail stations (and future transit hubs) with bilingual names, line order, landmarks, and accessibility/facility cues.
3. **User Goal:** Browse or search stations, check station order along the corridor, or launch Fare Calculator from a specific station.
4. **Entry Point:** Quick Shortcut on **Home Dashboard (03)** or **Insights Screen (09)** or from **Fare Calculator (10)**.
5. **Navigation Destination:** **Fare Calculator (10)** (pre-filled with selected station as Origin) or inline **Station Detail Bottom Sheet**.
6. **Layout Structure:** `Scaffold` with `TopAppBar` (`Back`, `Station Explorer`) + pinned `SearchBar` + Filter Chips + `LazyColumn` of station rows connected by a clean vertical line-track indicator.
7. **Header Structure:** `TopAppBar` with Back button + `"Station Explorer"`.
8. **Main Content Sections:**
   1. **Search Input:** Pill-rounded search bar (`"Search stations in English or বাংলা..."`) with instant local filtering.
   2. **Corridor / Filter Chips:** `All Stations`, `MRT Line 6` (with clean status note for operational stations).
   3. **Station List (`StationItem`):** Sequence indicator (`01`–`16`) connected by a subtle `2.dp` vertical transit line (`Primary` color), Station Name (primary locale) + secondary locale subtitle, operational badge, and right chevron -> tapping opens **Station Detail Sheet** (showing nearby landmarks, exits/gates summary if verified offline, and `[ Calculate Fare from Here ]`).
9. **Required Components:** `TransitTopBar`, `SearchInputField`, `FilterChip`s, `StationListItem`, `ModalBottomSheet` (Station Detail), `EmptyStateView`.
10. **Icon Requirements:** `ArrowBack`, `Search`, `Close`, `DirectionsSubway`, `Place`, `Calculate`.
11. **Primary & Secondary Actions:** Type to search, Tap station -> inspect Station Sheet, `[ Calculate Fare from Here ]` -> Fare Calculator (10).
12. **Loading State:** Instant static local dataset.
13. **Empty State (`Station Search Empty State`):** Magnifier/Train muted icon + `"No stations matched '[query]'"` + `[ Clear Search ]` tonal button.
14. **Error State:** Bundled station asset load failure fallback.
15. **Data Requirements:** Bundled offline `List<MetroStation>` (Station ID, Order, Name EN, Name BN, Line Name, Status, Verified Landmarks). Only verified public station facts included.
16. **Light & Dark Theme Behavior:** Line-track connector in `#12746A` (Light) / `#2FA497` (Dark); high-contrast bilingual text.
17. **Dark Theme Behavior:** `#191C1F` station list cards on `#111315` canvas.
18. **Bangla Localization Requirements:** Cross-script search support (typing বাংলা or English finds matching station reliably).
19. **Animation Requirements:** `animateItem()` filtering + smooth sheet slide-up on station tap.
20. **Accessibility Requirements:** Announces station index and name (*"Station 16 of 16, Motijheel"*).

---

### Screen 12: NFC Diagnostics Screen *(Developer / Technical Surface)*
1. **Screen Name:** NFC Diagnostics Screen
2. **Screen Purpose:** Provide an isolated, developer-grade technical inspection view for verifying physical NFC hardware polling, discovered technologies, anticollision metadata (`IDm`, `System Code`, `PMm`), and RF errors—completely separated from the consumer UX.
3. **User Goal:** Engineers or testers physically validate new cards (e.g., MRT Pass or updated Rapid Pass batches) and inspect raw read-only RF metadata safely.
4. **Entry Point:** Strictly inside **Settings Screen (13) -> Advanced -> NFC Diagnostics** (never reachable from Home Dashboard or consumer Scan Result).
5. **Navigation Destination:** Back to **Settings Screen (13)**.
6. **Layout Structure:** `Scaffold` with `TopAppBar` (`Back`, `NFC Diagnostics`, `Reset` icon) + clear Developer Notice banner + Hardware Status card + Live Technical Capture card + Clear/Copy Controls.
7. **Header Structure:** `TopAppBar` with Back arrow, `"NFC Diagnostics"`, and `DeleteSweep` / Reset counter icon.
8. **Main Content Sections:**
   1. **Isolation Notice Banner (`StatusBannerCard`):** Info badge explaining: *"Technical validation tool — strictly read-only anticollision discovery. No card blocks or balances are altered."*
   2. **Adapter & Reader State Card:** `NFC Hardware`, `Adapter State`, `Reader Mode Flags` (`NFC_F | NFC_A | SKIP_NDEF_CHECK`).
   3. **Live Diagnostic Scan Output Card:** Active only while this screen is foregrounded (or displays current diagnostic session):
      - `Scan Counter` (`Scan #1`)
      - `Tech List` (`android.nfc.tech.NfcF`)
      - `IDm (Redacted by default)` (`01:27:**:**:**:**:**:A0` — optional explicit `"Show Full IDm"` toggle for local debugging only, never persisted)
      - `System Code` (cleanly formatted `0x90E3` — fixes prior `0x90:E3` colon artifact)
      - `Manufacturer (PMm)` (`01:46:42:45:45:46:9B:FF`)
      - `RF Link Check` (`Connect & Close OK` or exact `IOException` / `TagLostException` trace)
   4. **Actions Row:** `[ Copy Redacted Log ]` (copies sanitized markdown/text report for sharing without unredacted card IDs) + `[ Clear Session ]`.
9. **Required Components:** `TransitTopBar`, `StatusBannerCard`, `SurfaceSectionCard`, `MonospaceKeyValueRow`, `FilledTonalButton`, `Switch` (reveal/mask toggle).
10. **Icon Requirements:** `ArrowBack`, `Memory` / `DeveloperMode`, `RestartAlt`, `ContentCopy`, `Shield`.
11. **Primary & Secondary Actions:** Tap card while on screen -> captures live diagnostic data; `Copy Redacted Summary`; `Clear Output`.
12. **Loading State:** `"Diagnostic Reader Mode Active — Tap card to capture metadata"`.
13. **Empty State:** Waiting state before first tap (`"No diagnostic scan recorded in this session"`).
14. **Error State:** Displays exact caught `IOException` / `TagLostException` message in `RF Link Trace` for hardware debugging.
15. **Data Requirements:** `NfcDiagnosticData` (in-memory session state only; **never saved to Room DB**). Preserves existing `NfcDiagnosticReader` RF lifecycle.
16. **Light & Dark Theme Behavior:** Monospace technical values inside `SurfaceVariant` subtle code-style boxes (`#ECEFF2` Light / `#22262A` Dark) — zero neon cyan!
17. **Dark Theme Behavior:** Neutral Graphite `#191C1F` cards, calm readable monospace ink (`#F1F3F5`).
18. **Bangla Localization Requirements:** Screen stays clean in English/Bangla UI chrome while standard protocol labels (`IDm`, `PMm`, `System Code`, `NfcF`) retain accurate technical identifiers.
19. **Animation Requirements:** Minimal instant update on scan counter increment (`AnimatedContent`).
20. **Accessibility Requirements:** Monospace fields remain legible (`>= 12.sp`); Copy action gives snackbar confirmation (`"Redacted diagnostic summary copied"`).

---

### Screen 13: Settings Screen
1. **Screen Name:** Settings Screen
2. **Screen Purpose:** Provide centralized preferences for Theme, Language (English / বাংলা), Local Data Management, Privacy controls, Support links, and Advanced NFC Diagnostics.
3. **User Goal:** Customize app appearance/language, verify offline privacy posture, manage or wipe stored card data, or access help/diagnostics.
4. **Entry Point:** `Settings` tab (Tab 5) in Bottom Navigation.
5. **Navigation Destination:** **Theme Selection Sheet**, **Language Selection Sheet**, **Data Management Sub-route** (containing **Data Export Preview**), **Privacy Info Sheet**, **NFC Diagnostics (12)**, **Help & FAQ (14)**, **About (15)**.
6. **Layout Structure:** `Scaffold` with `TopAppBar` + Bottom Navigation Bar + grouped `LazyColumn` of clean sectioned `SurfaceSectionCard`s (`16.dp` padding, inset `HorizontalDivider`).
7. **Header Structure:** `TopAppBar` (`Settings`).
8. **Main Content Sections (Grouped Cards):**
   - **Appearance & Language:**
     - `Theme` -> Current value (`System` / `Light` / `Dark`) -> opens **Theme Selection Sheet**
     - `Language` -> Current value (`English` / `বাংলা`) -> opens **Language Selection Sheet**
   - **Privacy & Data:**
     - `Data Management` -> opens **Data Management** view (`Export Preview`, `Clear Scan History`, `Delete All Saved Cards`)
     - `Privacy & Offline Mode` -> opens **Privacy Information Sheet**
   - **Support & Info:**
     - `Help & FAQ` -> Screen (14)
     - `About TransitMate` -> Screen (15)
   - **Advanced:**
     - `NFC Diagnostics` -> Screen (12) *(clearly labeled `"For technical card inspection"`)*
9. **Required Components:** `TransitTopBar`, `TransitBottomNav`, `SurfaceSectionCard`, `SettingsRowItem`, `OptionPickerBottomSheet` (`Theme`, `Language`), `PrivacyBottomSheet`, `DataManagementView`.
10. **Icon Requirements:** `Settings`, `Palette` / `DarkMode`, `Language`, `Storage`, `Shield`, `HelpOutline`, `InfoOutline`, `Memory` / `DeveloperMode`, `ChevronRight`.
11. **Primary & Secondary Actions:** Tap any row to open corresponding sheet, dialog, or sub-screen.
12. **Loading State:** Instant from DataStore state.
13. **Empty State:** Not applicable.
14. **Error State:** Snackbar alert if DataStore preference write or local clear fails.
15. **Data Requirements:** `AppPreferences(themeMode, languageTag)`, `storageCounts(savedCardCount, historyCount)`.
16. **Light Theme Behavior:** `#F6F7F8` canvas with crisp white grouped cards (`#FFFFFF`) and `#E6EAEF` inset hairline dividers.
17. **Dark Theme Behavior:** `#111315` canvas with `#191C1F` grouped cards and `#252A30` inset hairline dividers.
18. **Bangla Localization Requirements:** Instant reactive UI switch between English and বাংলা upon selecting inside `Language Selection Sheet`.
19. **Animation Requirements:** Bottom sheet slide-up (`260ms`); clean theme crossfade across app surfaces.
20. **Accessibility Requirements:** Each settings row acts as a single `>= 56.dp` touch target announcing title, current selected value, and action cue.

---

### Screen 14: Help & FAQ Screen
1. **Screen Name:** Help & FAQ Screen
2. **Screen Purpose:** Answer common commuter questions about NFC antenna placement, supported cards (MRT Pass vs. Rapid Pass), why certain data may show unavailable, and offline privacy.
3. **User Goal:** Troubleshoot a failed scan or understand app capabilities quickly without needing internet access.
4. **Entry Point:** **Settings Screen (13)** (`Help & FAQ`) or `?` Help button on **NFC Scanner Screen (04)**.
5. **Navigation Destination:** Optional `Replay Onboarding` -> Onboarding (02) or Back.
6. **Layout Structure:** `Scaffold` with `TopAppBar` (`Back`, `Help & FAQ`) + Search/Topic filter (optional) + expandable Accordion FAQ cards + quick NFC antenna guide diagram.
7. **Header Structure:** `TopAppBar` with Back button and `"Help & FAQ"`.
8. **Main Content Sections:**
   1. **Quick NFC Placement Card:** Visual diagram + 3 bullet steps explaining antenna position differences across phones and removing thick metal/magnet phone cases.
   2. **Expandable FAQ Accordion Cards (`ExpandableFaqItem`):**
      - *Which cards are supported?* (MRT Pass & Rapid Pass technical boundaries explained honestly)
      - *Why does Balance say "Not Available"?* (Explains read-only verification policy vs card encryption/service validation)
      - *Can I top up / recharge my card here?* (Clearly states **No** — official station AVMs or official DTCA portal required)
      - *Does TransitMate work without internet or NFC?* (Yes — local history, Fare Calculator, Station Explorer work 100% offline)
      - *How is my card data protected?* (Offline-only storage, no server uploads, no accounts)
   3. **Replay Guide Button:** Secondary tonal action (`View Welcome Guide` -> Screen 02).
9. **Required Components:** `TransitTopBar`, `SurfaceSectionCard`, `ExpandableAccordionCard`, `FilledTonalButton`.
10. **Icon Requirements:** `ArrowBack`, `HelpOutline`, `ExpandMore` / `ExpandLess`, `Contactless`, `Shield`.
11. **Primary & Secondary Actions:** Expand/collapse FAQ items, Replay Onboarding.
12. **Loading State:** Instant static local content.
13. **Empty State:** Not applicable.
14. **Error State:** Not applicable.
15. **Data Requirements:** Static localized string arrays / structured FAQ items in English & বাংলা.
16. **Light & Dark Theme Behavior:** High-contrast readable typography (`bodyLarge`/`bodyMedium`) on `Surface` cards (`#FFFFFF` / `#191C1F`).
17. **Dark Theme Behavior:** `1.dp` `#2E343B` border distinguishes expandable items cleanly without glowing highlights.
18. **Bangla Localization Requirements:** Full natural, jargon-free বাংলা FAQ answers with `1.45x` line height.
19. **Animation Requirements:** `animateContentSize()` (`220ms`) on accordion expand/collapse + smooth `180°` chevron rotation.
20. **Accessibility Requirements:** FAQ headers expose expanded/collapsed state (`StateDescription`) to TalkBack.

---

### Screen 15: About Screen
1. **Screen Name:** About Screen
2. **Screen Purpose:** Provide transparent product identity, version details, independent non-affiliation disclosure, privacy commitment, and open-source/clean-room notices.
3. **User Goal:** Verify app version, confirm authenticity, and review legal/non-affiliation disclosures.
4. **Entry Point:** **Settings Screen (13)** (`About TransitMate`).
5. **Navigation Destination:** Optional **Privacy Information Sheet** or Open-Source Licenses sheet/dialog -> Back.
6. **Layout Structure:** `Scaffold` with `TopAppBar` (`Back`, `About`) + centered compact Brand Header + structured info cards.
7. **Header Structure:** `TopAppBar` with Back button and `"About"`.
8. **Main Content Sections:**
   1. **App Identity Header:** Official TransitMate logo mark (`72.dp`), `TransitMate`, Version (`v1.0.0`), Bilingual tagline.
   2. **Non-Affiliation Disclosure Card (`StatusBannerCard`):** Clear neutral card stating: *"Independent Companion Utility — TransitMate is not officially affiliated with DMTCL, DTCA, or any government transport authority."*
   3. **Project Principles & Licenses Card:**
      - `Operating Mode`: `100% Offline & Read-Only`
      - `Open-Source Notices`: Clean-room notice + third-party Android/Jetpack library attributions
      - `Privacy Policy Summary`: Opens Privacy Information Sheet
9. **Required Components:** `TransitTopBar`, `SurfaceSectionCard`, `StatusBannerCard`, `InfoKeyValueRow`.
10. **Icon Requirements:** `ArrowBack`, `InfoOutline`, `VerifiedUser`, `Gavel` / `Description`.
11. **Primary & Secondary Actions:** View Open-Source Notices, View Privacy Information.
12. **Loading & Empty States:** Instant static local screen (not applicable).
13. **Error State:** Not applicable.
14. **Data Requirements:** `versionName`, `versionCode`, static legal/attribution strings.
15. **Light & Dark Theme Behavior:** Clean centered brand framing matching `Splash Screen (01)` surface discipline.
16. **Dark Theme Behavior:** Neutral Graphite `#111315` canvas, `#191C1F` disclosure cards.
17. **Bangla Localization Requirements:** Clear official-sounding বাংলা non-affiliation notice.
18. **Animation Requirements:** Subtle screen fade transition (`240ms`).
19. **Accessibility Requirements:** Readable disclosure text (`>= 14.sp`), proper headings hierarchy.
20. **Legal / Store Compliance Check:** Zero trademark misrepresentation; explicitly states read-only and non-affiliated status.

---

## 3. Supporting States, Sheets, Dialogs & Sub-Routes (All 25 States)

Every one of the 25 Supporting States identified in Stage 1 is mapped below with its **Trigger**, **User Message**, **Available Action**, and **Recovery Path**.

### 3.1 Inline / Full-Screen States (10)
| # | State Name | Screen / Container | Trigger | User Message | Available Action | Recovery Path |
|:---:|---|---|---|---|---|---|
| 1 | **Waiting for Card** | Scanner (04) inline | Reader mode enabled, no tag in field | *"Hold your MRT Pass or Rapid Pass steady behind your phone."* | `Cancel` / `Tap Help` | Place card near top-back NFC antenna |
| 2 | **Scanning in Progress** | Scanner (04) inline | `onTagDiscovered` invoked, RF link open | *"Reading card... Please do not move the card."* | Disabled buttons (brief) | Auto-transitions to Result (05) or Error Sheet |
| 3 | **Card Detected** | Scanner (04) -> Result (05) | `NfcF` / tag metadata read succeeds | *"Card detected successfully"* | Auto-advance (`260ms`) | Proceeds immediately to Scan Result (05) |
| 4 | **NFC Not Supported** | Home (03) / Scanner (04) inline | `NfcAdapter.getDefaultAdapter == null` | *"This phone does not have NFC hardware. You can still use Fare Calculator, Stations, and saved info."* | `Use Offline Tools` | Open Fare Calculator (10) / Station Explorer (11) |
| 5 | **Empty Saved Cards** | My Cards (07) full-screen inline | `savedCards.isEmpty()` | *"No saved cards yet. Scan a transit card to save it locally on this device."* | `Scan a Card` | Navigates to NFC Scanner (04) |
| 6 | **Empty Transaction History** | History (08) full-screen inline | `transactions.isEmpty()` | *"No transactions recorded yet. Verified card trips appear here after scanning."* | `Scan a Card` | Navigates to NFC Scanner (04) or clears filter |
| 7 | **Empty Travel History** | Card Details (06) / Home (03) card | Card has 0 saved trips | *"No recent travel activity for this card."* | `Scan Card` | Tap physical card on Scanner (04) |
| 8 | **No Analytics Data** | Insights (09) inline section | `verifiedTripsCount == 0` | *"Insights require verified trip history. Explore stations or calculate fares anytime."* | `Fare Calculator` / `Stations` | Appears automatically once verified trips are logged |
| 9 | **Fare Calculator Empty State** | Fare Calculator (10) inline card | Origin or Destination unselected | *"Choose departure and destination stations to view official fare rates."* | `Select Station` | Opens Searchable Station Picker sheet |
| 10 | **Station Search Empty State** | Station Explorer (11) inline | Search query matches 0 stations | *"No stations found matching '[query]'."* | `Clear Search` | Resets search bar to show full corridor list |

### 3.2 Inline Alerts / Error States (3)
| # | State Name | Screen / Container | Trigger | User Message | Available Action | Recovery Path |
|:---:|---|---|---|---|---|---|
| 11 | **NFC Disabled** | Home (03) / Scanner (04) Alert Banner | `adapter != null && !adapter.isEnabled` | *"NFC is turned off. Enable NFC in device settings to scan your card."* | `Open NFC Settings` | Opens `Settings.ACTION_NFC_SETTINGS`; `onResume` re-checks automatically |
| 12 | **Balance Not Available** | Home Hero (03) / Result (05) / Details (06) | `verifiedBalance == null` | *"Balance unavailable — Card detected, but balance data is unverified or protected."* | `Learn Why` (FAQ) / `Done` | Transparently informs user without fake digits |
| 13 | **Transaction Not Available** | History (08) / Details (06) Alert Banner | Card recognized/detected, history block unreadable | *"Transaction history is unavailable for this card at this stage."* | `View Card Info` | Honest partial-read communication |

### 3.3 Bottom Sheets / Result States (9)
| # | State Name | Trigger | User Message | Available Action | Recovery Path |
|:---:|---|---|---|---|---|
| 14 | **Unsupported Card** (`ModalBottomSheet`) | Non-transit tag scanned (e.g., `IsoDep` EMV bank card, `MifareClassic`, `Ndef`) | *"Unsupported card detected. TransitMate only supports compatible FeliCa transit cards (MRT Pass / Rapid Pass)."* | `Try Another Card` / `Close` | Dismisses sheet & resumes Reader Mode polling |
| 15 | **Unknown Card** (`ModalBottomSheet` / Result banner) | `NfcF` tag detected, but issuer/system unverified for Rapid Pass/MRT Pass | *"Unrecognized FeliCa card detected. Card presence is confirmed, but card authority could not be verified."* | `View Detection Info` / `Scan Again` | Shows safe Detection result without false branding |
| 16 | **Identifier Unavailable** (`BottomSheet` / inline badge) | Tag discovered, `Tag.id` null or zero-length | *"Card detected, but hardware identifier was not returned by the NFC controller."* | `Retry Scan` | Re-tap card cleanly |
| 17 | **Scan Failed** (`ModalBottomSheet`) | General discovery failure or `NfcF.get(tag) == null` | *"Couldn't complete the card read. Please adjust card position and try again."* | `Try Again` / `Antenna Guide` | Dismisses sheet to retry or opens Help (14) |
| 18 | **NFC Communication Error** (`ModalBottomSheet`) | `TagLostException` or `IOException` during `connect()`/`transceive()` | *"Card moved too early. Hold your card flat against the upper back of your phone for 1–2 seconds."* | `Try Again` | Resumes scanning seamlessly |
| 19 | **Data Export Preview** (`ModalBottomSheet`) | User taps `Preview / Export Local Data` inside Data Management | Displays sanitized JSON/CSV preview summary (`X saved cards, Y trips — masked IDs only`) | `Share / Export File` / `Cancel` | Generates local share intent or cancels safely |
| 20 | **Privacy Information** (`ModalBottomSheet`) | User taps `Privacy & Offline Mode` in Settings (13) or About (15) | Summarizes zero cloud sync, zero analytics, read-only NFC guarantee, and masked ID handling | `Manage Data` / `Close` | Navigates to Data Management or closes |
| 21 | **Language Selection** (`ModalBottomSheet`) | User taps `Language` in Settings (13) | Radio options: `English` / `বাংলা (Bangla)` with instant preview cue | Select Option / `Close` | Saves preference to DataStore & updates locale immediately |
| 22 | **Theme Selection** (`ModalBottomSheet`) | User taps `Theme` in Settings (13) | Radio options: `System Default`, `Light (Warm Off-White)`, `Dark (Neutral Graphite)` | Select Option / `Close` | Saves preference to DataStore & updates `TransitMateTheme` |

### 3.4 Dialogs (2)
| # | State Name | Trigger | User Message | Available Action | Recovery Path |
|:---:|---|---|---|---|---|
| 23 | **Delete Card Confirmation** (`AlertDialog`) | User taps `Delete Card` in My Cards (07) or Card Details (06) | *"Remove '[Card Nickname]' from this device? Any locally stored history for this card will also be deleted."* | `Remove` (Error Red) / `Cancel` | Deletes local DB row on confirm or cancels safely |
| 24 | **Rename Card Dialog** (`AlertDialog`) | User taps `Rename` in My Cards (07), Card Details (06), or `Save Card` flow | `OutlinedTextField` (`maxLength = 30`, e.g., `"Office Rapid Pass"`) with validation for blank input | `Save` / `Cancel` | Updates card nickname in Room DB |

### 3.5 Settings Sub-Route (1)
| # | State Name | Trigger | User Message / Content | Available Action | Recovery Path |
|:---:|---|---|---|---|---|
| 25 | **Data Management** (Sub-route from Settings 13) | User taps `Data Management` | Storage summary (`Saved Cards: X`, `History Records: Y`), `Export Preview`, `Clear Scan History`, `Delete All Local Data` (with confirmation dialog) | `Preview Export`, `Clear History`, `Clear All` | Back to Settings (13); destructive actions require explicit confirmation |

---

## 4. Self-Audit & Consistency Checklist

- [x] **Screen Count Verified:** Exactly 15 distinct Core Screens + 25 Supporting States, zero double-counting.
- [x] **NFC Capability Boundaries:** `Detection != Recognition != Balance != History` explicitly enforced across Home (03), Scanner (04), Scan Result (05), Card Details (06), and History (08).
- [x] **Diagnostics Isolation:** Raw `IDm`, `System Code`, `PMm`, `techList`, `RF Error Trace`, and `Scan Counter` restricted strictly to **NFC Diagnostics Screen (12)**.
- [x] **Navigation Consistency:** Every primary destination uses the exact 5-tab `Home | My Cards | Scan | Insights | Settings` navigation bar.
- [x] **Zero Forbidden Elements:** Zero neon glow, zero fake balances (`৳ 150` removed unless genuinely verified in Phase 06+), zero emojis, zero profile avatars.
