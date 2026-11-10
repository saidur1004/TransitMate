# TransitMate --- Phase 01: Project Identity & Master Planning

**Document version:** 1.0\
**Date:** 10 November 2026\
**Status:** Planning baseline --- pending owner approval of package ID
and repository details\
**Primary platform:** Android\
**Primary language:** Kotlin\
**Documentation language:** Bangla (technical identifiers remain
English)

------------------------------------------------------------------------

## 1. Product identity

  -----------------------------------------------------------------------
  Item                                Decision
  ----------------------------------- -----------------------------------
  Product name                        TransitMate

  Product category                    Bangladesh transit-card companion

  Product positioning                 An independent Android companion
                                      for MRT Pass and Rapid Pass users

  Descriptive store title (draft)     TransitMate --- Metro & Rapid Pass

  Primary platform                    Android

  Initial operating mode              Offline-first, read-only card
                                      inspection

  Initial language plan               Bangla and English (implementation
                                      order to be confirmed)

  Brand status                        Name selected by project owner;
                                      legal/trademark clearance not
                                      performed
  -----------------------------------------------------------------------

### Naming note

The name `TransitMate` is already used by at least one public-transit
information website and an older student transit-app project. This does
not automatically prohibit use, but it means the name is not globally
unique. Before public release, perform a proper trademark, domain,
app-store and social-handle clearance. Do not represent this preliminary
search as legal clearance.

### Proposed Android application ID

`bd.transitmate.app`

This is a proposal, not yet a locked decision. Confirm before creating
the production Android project and especially before publishing to
Google Play. The application ID should be treated as permanent after the
first public release.

------------------------------------------------------------------------

## 2. Product definition

TransitMate is an independent Android application intended to
communicate with supported Bangladesh transit cards through the phone's
NFC hardware. Its primary targets are:

-   Dhaka Metro Rail MRT Pass
-   Rapid Pass

The app must not be described as capable of reading every field until
that field is verified on physical cards. Card detection, IDm/identifier
reading, balance reading and transaction-history reading are separate
capabilities and must have separate test evidence.

### Product vision

Build a reliable, privacy-conscious, polished transit-card companion
that reads technically and legally accessible card data, explains the
result clearly, and helps users understand their local travel-card
activity.

### Initial product principles

1.  Read-only first: no card-writing, balance modification, recharge or
    payment in the initial scope.
2.  Offline-first: core NFC reading and local history should not require
    an internet connection.
3.  Evidence-led compatibility: distinguish researched, experimentally
    observed and fully verified card capabilities.
4.  Privacy by default: card data remains local unless a future feature
    explicitly requires user-authorized synchronization.
5.  Honest UX: never display fabricated balance, transaction or station
    information as real card data.
6.  Accessibility: support readable typography, touch targets, contrast,
    screen readers and dynamic text where practical.
7.  Maintainability: modular architecture, tests, documentation and
    reproducible builds.

------------------------------------------------------------------------

## 3. Scope

### Initial release candidates

-   NFC hardware and availability check
-   NFC enabled/disabled guidance
-   FeliCa/NFC-F tag detection where supported
-   Card identification and supported-card classification
-   Balance display, only after parser and physical-card validation
-   Transaction history, only after parser and physical-card validation
-   Multiple locally saved cards
-   Local scan history
-   Offline card information
-   Clear error, unsupported-card and partial-read states
-   Light and dark themes
-   Bangla and English localization
-   Privacy/data-management controls

### Candidate later features

-   Travel statistics and spending summaries
-   Fare calculator
-   Station information and route references
-   CSV export
-   Local backup/import
-   Optional cloud sync, only if a concrete user need justifies it

### Explicitly out of initial scope

-   Card top-up or payment processing
-   Writing/modifying card balance or transaction records
-   Card emulation or cloning
-   Bypassing authentication or access controls
-   Backend account system unless later justified
-   Claims of official government/DTCA/DMTCL affiliation

------------------------------------------------------------------------

## 4. Technical baseline

  -----------------------------------------------------------------------
  Area                                Baseline
  ----------------------------------- -----------------------------------
  Language                            Kotlin

  UI framework                        Jetpack Compose

  Design components                   Material 3

  Architecture                        Clean Architecture + MVVM

  UI state                            ViewModel + StateFlow;
                                      lifecycle-aware collection

  Async work                          Kotlin Coroutines and Flow

  Dependency injection                Hilt (confirm versions during
                                      setup)

  Local database                      Room

  Preferences                         DataStore

  Navigation                          Navigation Compose

  NFC                                 Android NFC APIs, including `NfcF`
                                      where appropriate

  Build                               Gradle Kotlin DSL

  Dependency catalog                  Gradle Version Catalog

  Tests                               JUnit, coroutine tests, Compose UI
                                      tests and instrumentation tests

  Source control                      Git + GitHub

  CI                                  GitHub Actions build/test workflow
  -----------------------------------------------------------------------

Dependency versions must be selected as a mutually compatible stable set
at project initialization. Do not copy arbitrary latest versions
independently. Record the selected versions in
`gradle/libs.versions.toml` and in the technical-stack document.

### Architecture direction

-   `app`: application composition root, manifest, startup and
    dependency wiring.
-   `core`: reusable platform and infrastructure capabilities.
-   `domain`: entities, repository contracts and use cases.
-   `data`: repository implementations, local data sources and mappings.
-   `feature`: feature-specific presentation and navigation modules,
    introduced when needed.

Do not create empty modules merely to make the project look large. Add
modules when a real dependency boundary or independently testable
feature justifies them.

------------------------------------------------------------------------

## 5. NFC research and verification policy

The project has two distinct tracks:

### Research track

Study published implementations, protocol documentation, Android APIs,
card-system documentation and credible technical reports. Record source
URLs, dates, license terms and confidence.

### Experimental validation track

Use authorized physical MRT Pass and Rapid Pass cards with NFC-capable
Android devices. Record raw responses only where safe and lawful, redact
personal/sensitive values, and document:

-   device model and Android version
-   card category (avoid publishing personal card identifiers)
-   test date and app build
-   command/service/block hypothesis
-   expected response
-   actual response length and redacted bytes
-   parser interpretation
-   repeatability across scans and cards
-   error conditions
-   confidence status

### Evidence labels

-   **Unverified hypothesis:** found in a source or proposed by
    analysis; not tested.
-   **Observed:** seen in one experiment but not independently
    reproduced.
-   **Reproduced:** repeated successfully under documented conditions.
-   **Verified:** independently reviewed and reproduced across the
    defined test matrix.
-   **Unsupported/blocked:** evidence shows the capability is
    unavailable in the tested context, or requires authorization/access
    not available to the app.

Never hardcode disputed System Codes, Service Codes, block numbers,
offsets or timestamp formats as production facts without validation.

NFC I/O must not run on the Android main thread. Handle tag loss,
timeouts, unsupported technology, cancellation and partial reads
explicitly. Do not attempt to bypass card authentication or access
controls.

------------------------------------------------------------------------

## 6. Documentation structure

Create and maintain these root-level documents:

``` text
TransitMate/
├── README.md
├── PROJECT_MASTER_PLAN.md
├── PROJECT_CONTEXT.md
├── PROJECT_WORKFLOW.md
├── PROJECT_PROGRESS.md
├── AI_AGENT_INSTRUCTIONS.md
├── docs/
│   ├── ARCHITECTURE.md
│   ├── TECHNICAL_STACK.md
│   ├── NFC_RESEARCH.md
│   ├── CARD_COMPATIBILITY.md
│   ├── MRT_BUDDY_ANALYSIS.md
│   ├── UI_UX_SPECIFICATION.md
│   ├── SCREEN_CATALOG.md
│   ├── DESIGN_SYSTEM.md
│   ├── DATABASE_SCHEMA.md
│   ├── STATE_MANAGEMENT.md
│   ├── PRIVACY_SECURITY.md
│   ├── TESTING_STRATEGY.md
│   ├── RELEASE_GUIDE.md
│   ├── references/
│   └── decisions/
├── design/
│   ├── brand/
│   ├── references/
│   ├── light/
│   ├── dark/
│   ├── components/
│   └── dialogs/
├── app/
├── core/
├── domain/
├── data/
├── feature/
└── gradle/
```

The actual module tree may be introduced progressively. The
documentation and design folders can exist before feature UI
implementation.

### PROJECT_PROGRESS.md required fields

-   current phase and task
-   completed / in progress / pending / blocked
-   files created or modified
-   dependency/toolchain changes
-   exact build/test commands and outcomes
-   known bugs and limitations
-   decisions and rationale
-   next actionable task
-   timestamp and responsible agent

An agent must not mark work complete without evidence. A new agent must
read the root documents, inspect Git status and verify the last recorded
build/test state before editing.

------------------------------------------------------------------------

## 7. Updated development sequence

### Phase 01 --- Project identity and master planning

-   Confirm product name and positioning.
-   Define scope, constraints, quality gates and technical baseline.
-   Establish root documentation and workflow.
-   Record unresolved decisions.

### Phase 02 --- Initial Android project setup

-   Finalize application ID and repository name.
-   Create Kotlin/Compose Android project.
-   Configure Gradle, version catalog, manifest and SDK levels.
-   Add only essential baseline dependencies.
-   Add original launcher icon assets and adaptive icon resources.
-   Configure Git, GitHub and CI.
-   Add a temporary startup screen only.
-   Verify debug build, install, launch and tests.

**No final feature UI or dashboard is to be implemented in this phase.**

### Phase 03 --- NFC/FeliCa research and physical validation

-   Analyze independent implementations and license obligations.
-   Study Android NFC-F APIs and relevant FeliCa specifications.
-   Compare competing protocol hypotheses.
-   Test actual MRT Pass and Rapid Pass cards.
-   Publish evidence-based compatibility matrix.

### Phase 04 --- Complete UI/UX planning and visual design

-   Finalize user journeys and navigation.
-   Inventory every screen, dialog and bottom sheet.
-   Define design tokens and component rules.
-   Generate Light and Dark visual references.
-   Review and approve references before UI implementation.

### Phase 05 --- Application architecture foundation

-   Implement approved navigation and design system.
-   Add state-management conventions, DI, database and feature
    boundaries.
-   Add architecture and persistence tests.

### Phase 06 --- NFC reader and parser implementation

-   Implement NFC session lifecycle.
-   Implement only validated card communication paths.
-   Parse only verified fields.
-   Add fixtures and parser tests using sanitized sample data.

### Phase 07 --- Core product features

-   Card management, dashboard, history and local persistence.
-   Loading, empty, partial, stale, error and unsupported states.

### Phase 08 --- Advanced features

-   Statistics, fare tools, station information and export, subject to
    reliable source data.

### Phase 09 --- Quality assurance

-   Unit, integration, UI, device compatibility, regression,
    accessibility and performance tests.
-   Physical NFC tests are mandatory for NFC claims.

### Phase 10 --- Release and maintenance

-   Signing and release build.
-   Privacy policy and store listing.
-   Release checklist, changelog and maintenance process.

------------------------------------------------------------------------

## 8. Initial setup acceptance criteria

Phase 02 is complete only when all applicable checks pass:

1.  Project opens in the selected Android Studio version.
2.  Gradle sync succeeds from a clean checkout.
3.  Debug APK builds successfully.
4.  App installs and launches on an emulator.
5.  App installs and launches on at least one physical Android device.
6.  NFC capability is detected correctly on a supported device.
7.  An NFC-unsupported device/emulator produces a safe, understandable
    state.
8.  Unit test task runs successfully.
9.  Static analysis/format checks run successfully.
10. Git repository contains no secrets, local SDK paths or generated
    build outputs.
11. README explains environment requirements and exact build commands.
12. PROJECT_PROGRESS.md records actual evidence and unresolved issues.

The temporary startup screen is only a technical smoke-test surface. It
is not an approved product design.

------------------------------------------------------------------------

## 9. Git and AI-agent workflow

-   Use small, descriptive commits.
-   Never commit signing keys, API secrets, private card data or
    personal identifiers.
-   Use feature branches for substantial work.
-   Keep the default branch buildable.
-   Require a review summary before merging major changes.
-   Agents must inspect existing files before creating replacements.
-   Agents must not silently change architecture, package ID, SDK policy
    or dependency versions.
-   Every task ends with changed-file list, test/build evidence, known
    issues and next task.
-   Never claim "100% bug-free"; report measured coverage and known
    limitations.

Suggested commit prefixes:

-   `docs:`
-   `build:`
-   `feat:`
-   `fix:`
-   `test:`
-   `refactor:`
-   `chore:`

------------------------------------------------------------------------

## 10. Decisions still requiring confirmation before Phase 02

  -----------------------------------------------------------------------
  Decision                Current proposal        Status
  ----------------------- ----------------------- -----------------------
  Product name            TransitMate             Selected by owner

  Application ID          `bd.transitmate.app`    Pending confirmation

  GitHub repository       `TransitMate` or        Pending
                          `transitmate-android`   

  Minimum Android SDK     Choose after            Pending
                          device/support strategy 
                          review                  

  Target/compile SDK      Latest stable           Pending
                          compatible at setup     
                          time                    

  License                 Choose after ownership  Pending
                          and distribution goals  
                          are clear               

  Brand palette           Deep navy + teal/cyan   Draft
                          accent; refine after    
                          logo review             

  Localization            Bangla + English        Planned

  Backend                 None for initial        Proposed
                          offline-first scope     
  -----------------------------------------------------------------------

------------------------------------------------------------------------

## 11. Reference material

-   Primary project source: `NFC Project.md` (the project owner's master
    document).
-   Historical source: `NFC Project (1).md` (historical research only;
    older implementation details are not automatically current).
-   Android `NfcF` API:
    https://developer.android.com/reference/android/nfc/tech/NfcF
-   MRT Buddy source: https://github.com/aniruddha-adhikary/mrt-buddy
-   MRT Buddy memory-layout research:
    https://github.com/aniruddha-adhikary/mrt-buddy/wiki/MRT-Pass-Memory-Layout
-   Rapid Pass official app listing:
    https://play.google.com/store/apps/details?id=com.bd.rapidpass.app
-   Rapid Pass official usage guidelines:
    https://rapidpass.com.bd/en/usage-guidelines
-   Rapid Pass official terms:
    https://www.rapidpass.com.bd/en/terms-conditions

------------------------------------------------------------------------

## 12. Current project status

**Phase:** 01 --- Project identity and master planning\
**Product name:** TransitMate\
**Implementation:** Not started\
**Android project:** Not created\
**NFC balance/history capability:** Research and physical validation
pending\
**Final UI design:** Not started by design\
**Next task:** Confirm application ID and repository naming, then
execute Phase 02 initial Android setup.
