# TransitMate — Project Progress & Verification Log

**Document Version:** 1.1  
**Last Updated:** 12 November 2026, 12:00 +06:00  
**Responsible Agent:** Antigravity Autonomous Coding Agent  
**Current Phase:** Phase 03 — NFC / FeliCa Technical Research & Rapid Pass Physical Validation  

---

## 1. Phase Status Overview

| Phase | Description | Status | Evidence / Notes |
|---|---|---|---|
| **Phase 01** | Project Identity & Master Planning | **COMPLETED** | Baseline specifications approved in `PROJECT_MASTER_PLAN.md` |
| **Phase 02** | Complete Initial Project Foundation & Configuration | **COMPLETED** | Verified clean build, 8 unit tests passed, launcher icons, CI, APK generated |
| **Phase 03** | NFC / FeliCa Research & Physical Validation | **IN-PROGRESS (Research & Harness Ready)** | Comprehensive research documents delivered; diagnostic harness built & verified (13 tests passed); physical validation procedure prepared for owner |
| **Phase 04** | Complete UI/UX Planning & Visual Design | **PENDING** | Design-first visual specs to be prepared before feature screens |
| **Phase 05** | Application Architecture Foundation | **PENDING** | Room DB, DataStore, and Navigation graph to be added |
| **Phase 06** | NFC Reader & Parser Implementation | **PENDING** | Evidence-based block parsing |
| **Phase 07** | Core Product Features | **PENDING** | Local card manager, scan history, state handling |
| **Phase 08** | Advanced Features | **PENDING** | Route maps, fare calculator, export |
| **Phase 09** | Quality Assurance & Physical Testing | **PENDING** | Device test matrix on NFC phones |
| **Phase 10** | Release & Maintenance | **PENDING** | Play Store listing, signed release |

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

## 6. Next Recommended Task

1. **Owner Action:** Install `app/build/outputs/apk/debug/app-debug.apk` on the physical NFC phone.
2. **Execute Test Cases 1–5:** Tap the physical Rapid Pass card against the phone antenna and record observed `IDm`, `SystemCode`, and `PMm`.
3. **Proceed to Phase 04:** UI/UX planning and design-first approval.
