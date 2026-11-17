# TransitMate — Design System Specification

**Document Version:** 1.0  
**Date:** 16 November 2026  
**Stage:** Stage 2 — Design System Finalization  
**Status:** Finalized Specification — Pending Owner Sign-Off Before Stage 4 & Stage 6  
**Platform / Stack:** Android · Kotlin · Jetpack Compose · Material 3 (`androidx.compose.material3`)

---

## 1. Design Philosophy

TransitMate is built around the **Premium Minimalist Transit Dashboard** design language: a calm, legible, commuter-grade utility that feels dependable at a crowded metro station turnstile and effortless during daily balance or route checks.

- **Clarity over Decoration:** Every surface, border, and label earns its pixel budget. Zero visual noise, zero decorative city/train wallpapers, and zero glow effects.
- **Honest Visual Communication:** Real hardware states, verified readings, and unavailable capabilities are presented plainly with calm semantic feedback. Unverified numbers are never styled as active currency balances.
- **Bilingual Native Craft:** Both English and বাংলা (Bangla) are first-class citizens in layout metrics, line heights, truncation budgets, and touch targets.
- **Dual-Theme Parity:** Dark Mode (Neutral Graphite) and Light Mode (Warm Off-White) share identical layouts, spacing, corner radii, and hierarchy—only surface and ink tokens shift.

### Strictly Prohibited Visual Patterns
1. Neon Cyan (`#00D2FF` / `#00FFFF`), electric blue themes, or outer glow halos.
2. Cyberpunk UI styling, scanline overlays, or glowing concentric radar rings.
3. Heavy multi-stop gradients or glassmorphic blur cards.
4. Decorative train, skyline, or cityscape background illustrations behind functional content.
5. Emojis (`💳`, `🔍`, `👋`, `🚇`) used as application or section icons.
6. Placeholder profile photos, fake user greetings, notification bells, or unverified mock balances.

---

## 2. Brand Identity

- **Product Name:** TransitMate (বাংলা Tagline: *ঢাকা মেট্রো ও র‍্যাপিড পাস সঙ্গী*)
- **App Icon Alignment:** The app launcher mark ([design/brand/logo_original.png](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/design/brand/logo_original.png)) features deep navy, teal, and an amber map-pin accent. In-app UI harmonizes with the teal and subtle amber accents while shifting away from deep glowing navy toward calm **Neutral Graphite** (Dark) and **Warm Off-White** (Light) so daily usage remains soothing and legible.
- **Transit Card Identity Differentiators (Minimalist):**
  - **MRT Pass (DMTCL):** Distinguished cleanly via typography, issuer badge, and subtle muted transit-green or slate-teal stripe accent (never loud gradients).
  - **Rapid Pass (DTCA):** Distinguished cleanly via typography, issuer badge, and subtle desaturated metro-blue stripe accent.
  - **Unidentified / Generic FeliCa Card:** Neutral slate card styling with clear "Unverified Card Type" badge.

---

## 3. Light Theme Color Tokens (Warm Off-White)

| Role | Token Name | Hex | Usage |
|---|---|---|---|
| Background | `md_theme_light_background` | `#F6F7F8` | Primary scaffold canvas |
| Surface | `md_theme_light_surface` | `#FFFFFF` | Primary cards, bottom sheets, dialogs |
| Surface Variant / Container High | `md_theme_light_surfaceVariant` | `#ECEFF2` | Nested list rows, chip backgrounds, input fields |
| Surface Container Highest | `md_theme_light_surfaceContainerHighest` | `#E3E7EC` | Pressed states, segmented track |
| Outline | `md_theme_light_outline` | `#DCE1E7` | `1.dp` card borders, hairline dividers |
| Outline Variant | `md_theme_light_outlineVariant` | `#E6EAEF` | Subtle section separators |
| Primary Text | `md_theme_light_onBackground` / `onSurface` | `#16191D` | Headings, primary values, list titles |
| Secondary Text | `md_theme_light_onSurfaceVariant` | `#5C6570` | Subtitles, timestamps, field labels |
| Disabled / Hint Text | `md_theme_light_onSurfaceDisabled` | `#8C95A0` | Placeholders, disabled controls |
| Primary | `md_theme_light_primary` | `#12746A` | Primary CTA fills, active nav icon/label, key accents |
| On Primary | `md_theme_light_onPrimary` | `#FFFFFF` | Text & icons on primary buttons |
| Primary Container | `md_theme_light_primaryContainer` | `#DDF2EE` | Active nav pill, subtle highlighted badges |
| On Primary Container | `md_theme_light_onPrimaryContainer` | `#0B4740` | Text & icons inside primary containers |
| Secondary | `md_theme_light_secondary` | `#31628D` | Secondary accents, Rapid Pass indicator, info links |
| On Secondary | `md_theme_light_onSecondary` | `#FFFFFF` | Text on secondary fills |
| Secondary Container | `md_theme_light_secondaryContainer` | `#E3EEF7` | Info chips, secondary badges |
| On Secondary Container | `md_theme_light_onSecondaryContainer` | `#1A3D5C` | Text inside secondary container |

---

## 4. Dark Theme Color Tokens (Neutral Graphite)

| Role | Token Name | Hex | Usage |
|---|---|---|---|
| Background | `md_theme_dark_background` | `#111315` | Primary scaffold canvas |
| Surface | `md_theme_dark_surface` | `#191C1F` | Primary cards, bottom sheets, dialogs |
| Surface Variant / Container High | `md_theme_dark_surfaceVariant` | `#22262A` | Nested list rows, chip fills, inputs, nav bar |
| Surface Container Highest | `md_theme_dark_surfaceContainerHighest` | `#2A2F35` | Pressed / selected tracks |
| Outline | `md_theme_dark_outline` | `#2E343B` | `1.dp` subtle borders creating surface separation |
| Outline Variant | `md_theme_dark_outlineVariant` | `#252A30` | Hairline dividers inside cards |
| Primary Text | `md_theme_dark_onBackground` / `onSurface` | `#F1F3F5` | Headings, primary values, list titles |
| Secondary Text | `md_theme_dark_onSurfaceVariant` | `#9AA2AC` | Subtitles, timestamps, captions |
| Disabled / Hint Text | `md_theme_dark_onSurfaceDisabled` | `#68707A` | Placeholders, disabled controls |
| Primary | `md_theme_dark_primary` | `#2FA497` | Primary CTA fills, active states, focal indicators |
| On Primary | `md_theme_dark_onPrimary` | `#072623` | High-contrast ink on primary fills |
| Primary Container | `md_theme_dark_primaryContainer` | `#1B3B38` | Active nav indicator pill, subtle status chips |
| On Primary Container | `md_theme_dark_onPrimaryContainer` | `#BCE8E2` | Text & icons inside primary containers |
| Secondary | `md_theme_dark_secondary` | `#5B8FB9` | Secondary links, Rapid Pass tag, chart secondary |
| On Secondary | `md_theme_dark_onSecondary` | `#0B2235` | Ink on secondary fills |
| Secondary Container | `md_theme_dark_secondaryContainer` | `#1C3144` | Secondary badges |
| On Secondary Container | `md_theme_dark_onSecondaryContainer` | `#CCE1F2` | Text inside secondary container |

---

## 5. Semantic Colors

| Semantic State | Dark Token | Dark Container / On-Container | Light Token | Light Container / On-Container | Usage |
|---|---|---|---|---|---|
| **Success** (Card detected, NFC ready, Verified) | `#34A874` | `#153326` / `#A8E4C5` | `#1E8255` | `#DCF4E7` / `#0F4F32` | Ready indicators, successful scan confirmation |
| **Warning** (NFC disabled, Unverified, Stale) | `#D99B26` | `#362811` / `#F5D693` | `#B87A14` | `#FDF3DC` / `#6E4706` | NFC disabled banner, unverified balance notice |
| **Error** (NFC unsupported, Scan failed, Delete) | `#D95F59` | `#381B1A` / `#F6B8B5` | `#C2413B` | `#FCE8E7` / `#781E1A` | Communication errors, destructive confirmations |
| **Info / Neutral** (Offline notice, Read-only badge) | `#5B8FB9` | `#1C3144` / `#CCE1F2` | `#31628D` | `#E3EEF7` / `#1A3D5C` | Informational banners, privacy notices |

---

## 6. Typography Scale

Base font family uses Android's system sans-serif (`FontFamily.Default` -> Roboto for Latin, Noto Sans Bengali on Android devices) to ensure zero external font bloat and guaranteed native rendering across API 26–36. Tabular numerals (`fontFeatureSettings = "tnum"`) are mandatory for balances, fares, timestamps, and diagnostic hex tables.

| M3 Style Token | Size | Weight | Line Height | Letter Spacing | Tabular / Special | Primary Usage |
|---|---|---|---|---|---|---|
| `displayLarge` | `36.sp` | `Bold` (`700`) | `44.sp` | `-0.25.sp` | `tnum` | Verified Hero Card Balance (`৳ 150`) |
| `displayMedium` | `28.sp` | `Bold` (`700`) | `36.sp` | `0.sp` | `tnum` | Analytics / Fare Calculator primary result |
| `headlineMedium` | `24.sp` | `SemiBold` (`600`) | `34.sp` | `0.sp` | Standard | Onboarding headlines, Scan Result status title |
| `headlineSmall` | `20.sp` | `SemiBold` (`600`) | `28.sp` | `0.sp` | Standard | Top App Bar titles, Section hero headers |
| `titleLarge` | `18.sp` | `SemiBold` (`600`) | `26.sp` | `0.sp` | Standard | Dialog titles, Card headers |
| `titleMedium` | `16.sp` | `SemiBold` (`600`) | `24.sp` | `0.15.sp` | Standard | Sub-section headers (`Recent Activity`, `My Cards`) |
| `titleSmall` | `14.sp` | `SemiBold` (`600`) | `20.sp` | `0.1.sp` | Standard | Card subheaders, list group headers (`Today`) |
| `bodyLarge` | `16.sp` | `Normal` (`400`) | `24.sp` | `0.25.sp` | Standard | Primary instructions, onboarding copy, FAQ answers |
| `bodyMedium` | `14.sp` | `Normal` (`400`) | `21.sp` | `0.2.sp` | `tnum` where numeric | Card rows, station names, alert messages |
| `bodySmall` | `12.sp` | `Normal` (`400`) | `18.sp` | `0.25.sp` | `tnum` where numeric | Timestamps, secondary metadata, captions |
| `labelLarge` | `14.sp` | `Medium` (`500`) | `20.sp` | `0.1.sp` | Standard | Primary & Secondary button text |
| `labelMedium` | `12.sp` | `Medium` (`500`) | `16.sp` | `0.3.sp` | Standard | Bottom Nav labels, Status chips, Filter chips |
| `labelSmall` | `11.sp` | `Medium` (`500`) | `16.sp` | `0.4.sp` | `FontFamily.Monospace` for Diagnostics | Masked Card IDs, Developer Diagnostics hex data |

---

## 7. Bangla Typography Rules

1. **Generous Line Height:** All বাংলা strings experience tall ascenders (`ৈ`, `ৌ`, `ো`, রেফ) and deep descenders (`ু`, `ূ`, `ৃ`, হসন্ত/যুক্তবর্ণ). Minimum line height multiplier is **1.40x–1.50x** for body/title text (never below `1.35x`).
2. **Minimum Readable Size:** Never render বাংলা text smaller than `12.sp` (except `11.sp` short tags with `16.sp` line height). Avoid `letterSpacing > 0.2.sp` on বাংলা text, as excessive tracking breaks connected matra (`মাত্রা`) continuity.
3. **No Aggressive Truncation:** Avoid forced `maxLines = 1` on বাংলা buttons, chip labels, or card headers; allow `maxLines = 2` with `TextOverflow.Ellipsis` and flexible height containers (`defaultMinSize` instead of fixed `height`) so বাংলা labels never clip vertically.
4. **Currency & Numeral Policy:** Keep `৳` prefix spaced consistently (`৳ 150` or `৳ ১৫০` matching selected locale) using tabular figures so multi-row trip fares align cleanly on the right edge.

---

## 8. Spacing System

4dp base spatial grid for consistent rhythm across all screens:

| Token | Value | Standard Usage |
|---|---|---|
| `space_2xs` | `4.dp` | Tight icon-to-label badge padding, hairline gaps |
| `space_xs` | `8.dp` | Inline spacing between icon and text, compact row gaps |
| `space_sm` | `12.dp` | Vertical spacing between list items, chip gaps |
| `space_md` | `16.dp` | Standard card internal padding, section-to-section spacing |
| `space_lg` | `20.dp` | Horizontal screen margins (`20.dp`), large card padding |
| `space_xl` | `24.dp` | Major vertical section separation, modal/bottom-sheet padding |
| `space_2xl` | `32.dp` | Hero spacing on Scanner and Empty states |
| `space_3xl` | `48.dp` | Top visual breathing room on Splash / Onboarding |

---

## 9. Layout Grid

- **Portrait Phone (< 600.dp width):** Single-column scrollable layout (`LazyColumn` / vertical `Column` with `verticalScroll`), `20.dp` left/right screen margins, `12.dp` or `16.dp` vertical card gutters.
- **2-Column Compact Grids:** Allowed strictly for small symmetrical metric tiles (e.g., `Total Trips` | `Total Spent`, or `Fare Calculator` | `Station Explorer` quick-shortcut pair on Home) with `12.dp` horizontal gap.
- **Expanded / Tablet (`>= 600.dp` width):** Constrain primary content width to `600.dp` centered horizontally via `ResponsiveScreenContainer(maxWidth = 600.dp)` across all feature screens, ensuring comfortable reading measure on tablets and foldables.

---

## 10. Screen Margins and Padding

- **Horizontal Screen Margin:** `16.dp` on compact phones (`< 360.dp`), **`20.dp`** standard on `360.dp–599.dp` phones.
- **Top Edge:** Edge-to-Edge (`enableEdgeToEdge()`) with `TopAppBar` consuming `WindowInsets.statusBars`.
- **Bottom Edge:** Content inside tabs applies `Scaffold`'s `innerPadding` plus `16.dp`–`24.dp` extra bottom scroll buffer so the last card never sits flush against the Bottom Navigation Bar.
- **Card Internal Padding:** `16.dp` standard (`20.dp` for Hero Transit Card and Scan Result header card).

---

## 11. Corner Radius System

| Token | Value | Components |
|---|---|---|
| `radius_xs` | `6.dp` | Inline tags, tiny hex badge |
| `radius_sm` | `10.dp` | Status chips, filter chips, icon container boxes (`40.dp` box) |
| `radius_md` | `14.dp` | Text inputs, dropdown triggers, nested list tiles |
| `radius_lg` | `16.dp` | Standard content cards, alert banners, station items |
| `radius_xl` | `20.dp` | Hero Transit Card, modal dialogs, primary bottom sheet top corners (`24.dp` top) |
| `radius_pill` | `999.dp` (`CircleShape`) | Primary/Secondary CTA buttons, active bottom-nav indicator, status dots |

---

## 12. Elevation and Shadow Rules

TransitMate uses **tonal surface separation + subtle `1.dp` borders** instead of heavy drop shadows or glowing blurs:
- **Dark Theme:** Zero drop shadow (`0.dp` shadow elevation). Depth is communicated purely through `Background (#111315)` -> `Surface (#191C1F)` -> `SurfaceVariant (#22262A)` paired with `1.dp` `Outline (#2E343B)`.
- **Light Theme:** Crisp `0.dp–1.dp` tonal/ambient elevation (`CardDefaults.cardElevation(defaultElevation = 1.dp)`) combined with `1.dp` `Outline (#DCE1E7)` so white cards sit cleanly on the `#F6F7F8` warm off-white canvas.
- **Modal Bottom Sheets & Dialogs:** `3.dp`–`6.dp` standard Material 3 tonal elevation + dim scrim (`Black.copy(alpha = 0.45f)` in Light, `Black.copy(alpha = 0.65f)` in Dark).

---

## 13. Border and Divider Rules

- **Card Border:** `BorderStroke(1.dp, MaterialTheme.colorScheme.outline)` on all primary cards in Dark Mode and optionally on Light Mode cards for crisp edge definition.
- **Focused / Active Input Border:** `1.5.dp` `Primary` (`#2FA497` Dark / `#12746A` Light).
- **Error Border:** `1.5.dp` `Error` (`#D95F59` Dark / `#C2413B` Light).
- **Internal Card Dividers:** `HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)` inset by `16.dp` horizontally when separating list rows inside a grouped card (e.g., Settings items or Recent Trips).

---

## 14. Iconography Guidelines

- **Source Priority:** Material Symbols Rounded/Outlined (`androidx.compose.material.icons.rounded.*` or `outlined.*` / curated Material Symbols XML Vector Drawables in `res/drawable/`). Never embed PNG bitmaps for UI icons and never use system emojis (`💳`, `🔍`, `👋`).
- **Standard Sizes:**
  - Navigation Bar & Top Bar Icons: `24.dp`
  - List Leading Icons (inside `40.dp` rounded container): `20.dp`
  - Inline Metadata Icons (clock, arrow, info): `16.dp`–`18.dp`
  - Empty State / Scanner Focal Icon: `40.dp`–`48.dp` inside a clean `88.dp` tonal container (no neon glow)
- **Canonical Screen-to-Icon Mapping:**
  - Home: `Home`
  - My Cards: `CreditCard` / `Wallet`
  - Scan: `Contactless` / `Nfc`
  - Insights: `BarChart` / `Analytics`
  - Settings: `Settings`
  - History: `History` / `ReceiptLong`
  - Fare Calculator: `Calculate` / `Route`
  - Station Explorer: `Train` / `DirectionsSubway`
  - Privacy / Offline: `Shield` / `VerifiedUser`
  - NFC Diagnostics: `Memory` / `Build` / `DeveloperMode`
  - Help / FAQ: `HelpOutline`
  - About: `InfoOutline`

---

## 15. Button Styles

All buttons enforce minimum touch height `48.dp` (`minHeight = 48.dp`), `padding(horizontal = 20.dp, vertical = 12.dp)`, and `radius_pill` (`RoundedCornerShape(50)` or `14.dp` for full-width form actions):

1. **Primary Filled Button (`Button`):**
   - Fill: `Primary` (`#2FA497` Dark / `#12746A` Light), Content: `OnPrimary`.
   - Usage: Primary action per screen (`Scan Card`, `Get Started`, `Calculate Fare`, `Open NFC Settings`). Maximum one primary filled CTA visible per viewport.
2. **Secondary Tonal Button (`SecondaryTonalButton`):**
   - Fill: `PrimaryContainer` / `SecondaryContainer` tonal fill (`minHeight = 48.dp`, `CircleShape`), Content: `OnPrimaryContainer` / `OnSecondaryContainer`.
   - Usage: Supportive secondary actions (`Card Details`, `Copy Diagnostic Log`, `Replay Welcome Guide`, `Go to My Cards`).
3. **Outlined Button (`SecondaryOutlinedButton` / `OutlinedButton`):**
   - Fill: `Transparent`, Border: `1.dp` `Outline`, Content: `OnSurface`.
   - Usage: Neutral cancel or escape actions (`Cancel` on Scanner, `Close`).
4. **Text Button (`TextButton`):**
   - Content: `Primary`, no background fill.
   - Usage: Inline links (`View All`, `Skip`, `Learn More`).
5. **Destructive Button:**
   - Fill/Text: `Error` (`#D95F59` Dark / `#C2413B` Light) — used exclusively in `Delete Card Confirmation` and `Clear All Local Data`.

---

## 16. Card Component Styles

1. **Hero Transit Card (`TransitCardHero`):**
   - Corner Radius: `20.dp`, Padding: `20.dp`, `1.dp` subtle `Outline` border, `Surface` fill with a restrained `4.dp` left/top authority indicator bar (Transit Teal for MRT Pass, Metro Blue for Rapid Pass, Slate Grey for Unidentified).
   - Displays: Card Nickname + Authority subtitle, Status badge (`Verified` / `Detected` / `Saved`), Full read-only `Card IDm` identifier row (`01:27:...` format) with copy pill button, and **Verified Balance** (`৳ ...`) OR crisp `Balance Unavailable` pill.
2. **Standard Info / Summary Card (`StandardSurfaceCard`):**
   - Corner Radius: `16.dp`, Padding: `16.dp`, `Surface` background, `1.dp` `Outline` border. Used for hardware readiness, recent trips container, settings groups, and insights summaries.
3. **Alert / Status Banner Card (`StatusBannerCard`):**
   - Corner Radius: `16.dp`, Padding: `16.dp`, Semantic Container fill (e.g., Warning Container `#362811` Dark / `#FDF3DC` Light) with matching `1.dp` border and action button (`Open Settings`, `Retry`).

---

## 17. Input and Search Component Styles

- **Text Fields (`OutlinedTextField`):** Used for `Rename Card Dialog` and `Search`. Height `>= 52.dp`, Radius `14.dp`, Fill `SurfaceVariant`, Unfocused Border `Outline`, Focused Border `Primary` (`1.5.dp`), Error Border `Error`.
- **Station Search Bar & Dropdown Selectors (`Fare Calculator` / `Station Explorer`):**
  - Rounded pill/card search container (`48.dp–52.dp` height, `14.dp` radius, leading `Search` icon, trailing clear `Close` icon when non-empty).
  - Origin/Destination station pickers use searchable bottom sheets rather than cramped native spinners so Dhaka Metro stations in both English and বাংলা are easy to tap.

---

## 18. Chips and Status Indicators

- **Filter Chips (`FilterChip`):** Height `36.dp` (with `48.dp` tap target padding), Radius `10.dp`. Selected state uses `PrimaryContainer` + `OnPrimaryContainer` + `Check` icon; unselected uses `SurfaceVariant` + `1.dp` `Outline`.
- **Status Badges (Non-interactive):** Compact pill (`padding(horizontal = 10.dp, vertical = 4.dp)`), `CircleShape`, `8.dp` dot + `labelMedium` text. Always paired with icon/text (never color-only status).
  - Examples: `NFC Ready` (Success), `NFC Off` (Warning), `Read-Only` (Info), `Unverified` (Warning).

---

## 19. Navigation Bar Design

- **Component:** Material 3 `NavigationBar` (`containerColor = Surface`, subtle top `1.dp` hairline `OutlineVariant` divider, zero elevation shadow).
- **Five Fixed Destinations (All Main Screens):**
  1. **Home** (`Home` icon)
  2. **My Cards** (`CreditCard` icon)
  3. **Scan** (`Contactless` / `Nfc` icon — highlighted with crisp `PrimaryContainer` pill when active; uses clean icon+label symmetry—**no oversized floating orb**)
  4. **Insights** (`BarChart` icon)
  5. **Settings** (`Settings` icon)
- **Label Behavior:** `alwaysShowLabel = true`, `maxLines = 1`, concise English/বাংলা labels (`হোম`, `কার্ড`, `স্ক্যান`, `তথ্য`, `সেটিংস`) that fit cleanly on `360.dp` width screens at up to `1.3x` font scale.

---

## 20. Dialog and Bottom Sheet Design

- **Modal Bottom Sheets (`ModalBottomSheet`):**
  - Top corners `24.dp`, Drag handle (`32.dp x 4.dp`, `Outline` color), `20.dp` horizontal padding, `24.dp` bottom padding (respecting navigation bar insets).
  - Used for: `Unsupported Card`, `Unknown Card`, `Scan Failed / Communication Error`, `Language Selection`, `Theme Selection`, `Privacy Information`, `Data Export Preview`, and `Station Picker`.
- **Alert Dialogs (`AlertDialog`):**
  - Radius `20.dp`, `Surface` container, `1.dp` `Outline` border.
  - Used strictly for short focused tasks: `Delete Card Confirmation` and `Rename Card Dialog`.

---

## 21. Empty State Design

- Centered vertical column with `maxWidth = 320.dp`, `24.dp` padding:
  1. `64.dp` tonal circle/rounded-square (`SurfaceVariant`) holding a `32.dp` muted Material icon (`OnSurfaceVariant`).
  2. `titleMedium` clear headline (e.g., *"No saved cards yet"* / *"এখনো কোনো কার্ড সংরক্ষিত নেই"*).
  3. `bodyMedium` honest, helpful explanation (`OnSurfaceVariant`, centered).
  4. Optional single `FilledTonalButton` or `Button` (`Scan a Card`, `Clear Filters`).

---

## 22. Error State Design

- Never expose raw stack traces or `IOException` internals to end users.
- Show calm `ErrorContainer` / `WarningContainer` icon badge + plain-language cause + immediate recovery CTA:
  - Example (`TagLost`): *"Card moved too early — Hold your card steady against the back of the phone for 1–2 seconds."* + `[ Try Again ]`.
  - Example (`NFC Disabled`): *"NFC is turned off — Enable NFC in system settings to scan your card."* + `[ Open NFC Settings ]`.

---

## 23. Loading State Design

- **Initial / Short Operations:** Clean `CircularProgressIndicator` (`24.dp–32.dp`, `strokeWidth = 3.dp`, `Primary` color) — never an indefinite blank screen.
- **List / Card Skeletons:** Subtle static or gently alpha-pulsed `SurfaceVariant` placeholder blocks (`alpha` oscillating `0.4f -> 0.8f`, `1000ms`), no flashy bright shimmer sweeps.
- **NFC Scanning Active:** Clean pulsing border ring (`alpha` + subtle `1.0f -> 1.06f` scale) around the phone/card alignment graphic. **Never trigger success animation before real `Tag` confirmation.**

---

## 24. Animation and Motion Guidelines

All animations respect `Settings.Global.ANIMATOR_DURATION_SCALE` (reduced motion disables non-essential scale/pulse effects, preserving instant/crossfade transitions).

| Interaction | Duration | Easing | Visual Behavior |
|---|---|---|---|
| **Screen Navigation** | `240 ms` | `FastOutSlowIn` | Subtle horizontal slide (`24.dp`) + fade (`0f -> 1f`) for push routes; instant crossfade (`180 ms`) across Bottom Nav tabs |
| **Bottom Nav Selection** | `200 ms` | `CubicBezier(0.2, 0.0, 0.0, 1.0)` | M3 active indicator pill horizontal scale/alpha fade + icon color crossfade |
| **Card Appearance** | `220 ms` | `EaseOutCubic` | Fade in + `12.dp` subtle vertical slide up |
| **Scanner Pulse (Waiting)** | `1600 ms` loop | `EaseInOutSine` | Subtle concentric ring opacity (`0.35f -> 0.08f`) and scale (`0.95f -> 1.12f`) — zero neon glow |
| **NFC Detection Feedback** | `260 ms` | `EaseOutBack` (capped) | Smooth transition from scanning ring to solid `Success` checkmark badge (`0.85f -> 1.0f`) |
| **Loading Spinner** | System M3 | Linear/Standard | Standard Material 3 `CircularProgressIndicator` |
| **Card / FAQ Expand-Collapse** | `220 ms` | `FastOutSlowIn` | `animateContentSize()` + `180°` chevron rotation |
| **Bottom Sheet Presentation** | `260 ms` | `DecelerateInterpolator` / M3 Default | Smooth vertical sheet slide-up with `180 ms` scrim fade |
| **Error / Alert Banner** | `200 ms` | `EaseOut` | `AnimatedVisibility` vertical expand + subtle horizontal shake (`±4.dp`, `150 ms`) only on scan error |

---

## 25. Accessibility and Contrast

- **Minimum Contrast Ratios (WCAG AA+):**
  - Primary text on Background/Surface: `>= 10:1` (`#F1F3F5` on `#111315`/`#191C1F`; `#16191D` on `#FFFFFF`).
  - Secondary text: `>= 4.5:1` (`#9AA2AC` on `#191C1F`; `#5C6570` on `#FFFFFF`).
  - Primary Button label: `#072623` on `#2FA497` (`> 5.8:1`); `#FFFFFF` on `#12746A` (`> 5.4:1`).
- **Touch Targets:** Minimum `48.dp x 48.dp` clickable area on every icon button, card action, and bottom nav item.
- **Screen Readers (TalkBack):** All icons carry explicit localized `contentDescription` (or `null` when purely decorative inside labeled rows). Dynamic state updates (`Card detected`, `NFC disabled`) use `semantics { liveRegion = LiveRegionMode.Polite }`.
- **Font Scaling:** Tested against `1.0x`, `1.3x`, and `1.5x` Android system font scales without overlapping or clipped controls.

---

## 26. Responsive Layout Rules

- **Small Phones (`320.dp–359.dp` width):** Reduce horizontal padding from `20.dp` to `16.dp`; stack 2-column metric pills vertically if text exceeds width at `1.3x` font scale (`FlowRow` or adaptive `Column`).
- **Standard Phones (`360.dp–411.dp` width):** Default `20.dp` margins, single-column scroll layout.
- **Large Phones / Foldables (`>= 412.dp` width):** Center hero scanner graphics (`maxWidth = 360.dp`), keep reading column comfortable (`maxWidth = 600.dp`).
- **Scroll Safety:** Every form, result, dialog content, and dashboard screen must support vertical scrolling so landscape or large font-scale modes never trap buttons off-screen.

---

## 27. Android Material 3 Implementation Guidelines

1. **Theme Entry (`TransitMateTheme`):** Set `dynamicColor = false` by default so the carefully calibrated Neutral Graphite / Warm Off-White transit tokens remain consistent across OEM devices (Samsung, Xiaomi, Pixel, Walton/Symphony).
2. **No Hardcoded Colors in Composables:** Every color inside `feature/` must read from `MaterialTheme.colorScheme` or a dedicated `TransitMateSemanticColors` `CompositionLocalProvider`.
3. **Reusable Component Contract:** Build shared primitives inside [`TransitComponents.kt`](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/app/src/main/kotlin/com/transitmate/bd/core/designsystem/component/TransitComponents.kt): `ResponsiveScreenContainer`, `TransitTopBar`, `TransitBottomNavigation`, `TransitHeroCard`, `SurfaceSectionCard`, `StatusBannerCard`, `MetricStatTile`, `IdmPrivacyCaption`, `SecondaryTonalButton`, `SecondaryOutlinedButton`, `EmptyStateView`, `ErrorStateView`, `LoadingStateView`, `InfoKeyValueRow`, `TransactionRowItem`, and `localizedRecognitionLabel()`.
4. **Clean Previews:** Every component and screen provides paired `@Preview(name = "Light")` and `@Preview(name = "Dark")`, plus bilingual preview samples for long বাংলা strings.
