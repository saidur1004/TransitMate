# TransitMate — Phase 03: Technical Feasibility Decision Matrix

**Document Version:** 1.0  
**Date:** 12 November 2026  
**Status:** Approved Technical Decisions  
**Lead Engineer:** Senior Android NFC/FeliCa Engineer  

---

## 1. Capability Feasibility Matrix

Every capability is evaluated independently based strictly on verified empirical evidence or public technical facts. No capability is marked `VERIFIED` without reproducible proof.

| # | Capability | Status | Evidence Basis | Immediate Next Action |
|:---:|---|:---:|---|---|
| **1** | **NFC Hardware Detection** | `VERIFIED` | Unit-tested and platform-verified via `NfcAdapter.getDefaultAdapter()`. Safely handles null adapters without crashing. | Incorporated in `core/nfc`. |
| **2** | **NFC Enabled / Disabled Detection** | `VERIFIED` | Unit-tested and platform-verified via `NfcAdapter.isEnabled`. Live re-checking verified on `onResume()`. | Incorporated in `StartupViewModel`. |
| **3** | **Rapid Pass Card Discovery** | `REFERENCE-OBSERVED` | Confirmed by DTCA documentation, Google Play official app listing, and open-source transit literature. Physical test harness prepared. | Owner executes Test Case 2 using diagnostic harness. |
| **4** | **NFC-F Protocol Support** | `REFERENCE-OBSERVED` | JICA AFC fare collection architecture specifies Sony FeliCa (ISO/IEC 18092 / JIS X 6319-4). Diagnostic reader filters for `android.nfc.tech.NfcF`. | Confirm presence of `android.nfc.tech.NfcF` in physical scan log. |
| **5** | **Identifier / IDm Reading** | `REFERENCE-OBSERVED` | FeliCa standard defines 8-byte IDm transmitted during anticollision polling, accessible via `Tag.id`. | Verify 8-byte length and log redacted IDm during Test Case 3. |
| **6** | **Balance Reading (Unencrypted)** | `UNVERIFIED` | Hypothesized in community reference (Service Code `0x100B` or `0x090F`), but **not yet verified** on our physical Rapid Pass card. | Deferred until System Code and Service Code existence are physically confirmed. |
| **7** | **Transaction History Reading** | `UNVERIFIED` | Hypothesized in community reference (Service Code `0x108F` or `0x094F`), but block structure and station mapping are unverified on Rapid Pass. | Deferred to Phase 06 dedicated parser validation. |
| **8** | **MRT Pass Compatibility** | `BLOCKED` | **Physical card not available in test laboratory.** Public sources indicate similar JICA FeliCa base, but physical parity is unproven. | Obtain physical MRT Pass sample before claiming compatibility. |

---

## 2. Key Architecture & Engineering Decisions

### Decision 1: Strict Clean-Room Implementation & GPL-3.0 Isolation
- **Context:** The public reference project `aniruddha-adhikary/mrt-buddy` is licensed under GPL-3.0.
- **Decision:** TransitMate will **not** copy, port, or adapt code from MRT Buddy. All TransitMate architectures (`core/nfc`, `domain`, `data`, `feature`) are independently authored under clean-room engineering rules. Protocol constants from the reference are cataloged solely as hypotheses (`REFERENCE-OBSERVED`) and must be independently verified.
- **Impact:** Complete legal safety, freedom from unwanted copyleft licensing contagion, and architectural independence.

### Decision 2: Isolated Diagnostic Harness vs. Production NFC Engine
- **Context:** Testing the physical card requires capturing low-level NFC tag metadata without building unverified balance/transaction features or compromising Clean Architecture.
- **Decision:** Introduce a lightweight, strictly isolated `NfcDiagnosticReader` and diagnostic UI card on `StartupScreen`. It performs only:
  1. `enableReaderMode` filtering for `FLAG_READER_NFC_F | FLAG_READER_NFC_A`.
  2. Inspection of `tag.techList`.
  3. Reading `Tag.id` (IDm), `nfcF.systemCode`, and `nfcF.manufacturer` (PMm).
  4. Immediate safe disconnection (`close()` in `finally`).
  5. Zero block reading, zero write operations.
- **Impact:** Allows the owner to execute physical validation tests immediately on their phone while keeping production architecture clean.

### Decision 3: Prohibition of Mock or Fabricated Balance
- **Context:** Commuters depend on transit cards for daily travel. Displaying guessed, fabricated, or mock balances creates severe user confusion and reputational risk.
- **Decision:** TransitMate will **never** display a simulated balance as real card data. If unencrypted service read fails or is protected, the app will explicitly report: `Card detected, but balance service is protected or unsupported`.

---

## 3. Transition Criteria to Subsequent Phases

- **Phase 03 Exit Gate:**
  - Complete technical research documentation (`PHASE_03_NFC_TECHNICAL_RESEARCH.md`, `PHASE_03_PHYSICAL_VALIDATION.md`, `PHASE_03_FEASIBILITY_DECISION.md`).
  - Diagnostic harness implemented, compiled, and unit-tested.
  - Test procedures provided for owner execution.
- **Phase 04 Entry Gate (UI/UX Design):**
  - Owner reviews Phase 03 research.
  - Phase 04 proceeds with wireframing and design token specification for all screens (Dashboard, Scanner, History, Settings) in Figma / visual specs before any feature UI code is implemented.
