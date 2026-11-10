# TransitMate 🚇💳

[![Android CI](https://github.com/saidur1004/TransitMate/actions/workflows/android-ci.yml/badge.svg)](https://github.com/saidur1004/TransitMate/actions/workflows/android-ci.yml)
![Platform](https://img.shields.io/badge/Platform-Android-green.svg)
![Language](https://img.shields.io/badge/Language-Kotlin%202.3-blue.svg)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20(Material%203)-purple.svg)
![Architecture](https://img.shields.io/badge/Architecture-Clean%20Architecture%20%2B%20MVVM-orange.svg)
![License](https://img.shields.io/badge/License-Proprietary%20%2F%20Pending-lightgrey.svg)

> **Dhaka Metro & Rapid Pass Companion**  
> ঢাকা মেট্রো ও র‍্যাপিড পাস সঙ্গী

An independent, offline-first Android companion for Dhaka Metro Rail MRT Pass and Rapid Pass users. TransitMate communicates with contactless transit cards using standard device NFC hardware to deliver transparent, reliable, and privacy-conscious card inspection.

---

## 📌 Project Identity

- **Application ID:** `com.transitmate.bd`
- **Platform:** Native Android (Min SDK: 26 / Android 8.0 Oreo, Target & Compile SDK: 36 / Android 16)
- **UI Framework:** Jetpack Compose with Material 3 Design System
- **Build System:** Gradle Kotlin DSL (`build.gradle.kts`) with Gradle Version Catalog (`libs.versions.toml`)
- **Repository:** [https://github.com/saidur1004/TransitMate.git](https://github.com/saidur1004/TransitMate.git)

---

## 🛡️ Core Principles

1. **Read-Only First:** No card-writing, balance manipulation, top-up, or emulation.
2. **Offline-First:** Core NFC operations and local scan history function entirely offline without internet dependencies.
3. **Evidence-Led Compatibility:** Card capabilities (detection, IDm, balance, transactions) are reported strictly based on physical validation evidence.
4. **Privacy by Default:** Card details remain strictly on the local device. No telemetry, third-party analytics, or ad SDKs.
5. **Universal Installation:** Declares `android.hardware.nfc` as optional (`required="false"`) so the app can be installed safely on all Android devices.

---

## 🏗️ Architecture Foundation

TransitMate adheres to strict **Clean Architecture** and **MVVM** principles with uni-directional data flow (UDF):

```
com.transitmate.bd/
├── core/
│   ├── common/           # Pure Kotlin utilities, Result wrappers, Coroutine dispatchers
│   ├── database/         # Local persistence contracts (Room foundation in Phase 05)
│   ├── designsystem/     # Material 3 theme, Brand Color palette, Typography, Design tokens
│   └── nfc/              # Safe Android NFC hardware checkers and platform adapters
├── domain/               # Pure Kotlin business rules and models (Zero Android dependencies)
│   ├── model/            # CardType, NfcHardwareStatus
│   └── repository/       # NfcAvailabilityRepository interfaces
├── data/                 # Concrete repository implementations and data source adapters
├── feature/              # Feature modules (presentation layer with ViewModel + StateFlow)
│   └── startup/          # Technical smoke-test startup screen verifying hardware readiness
└── MainActivity.kt       # Application entry point (ComponentActivity)
```

---

## 🛠️ Development & Environment Requirements

- **JDK:** OpenJDK 17 or higher
- **Android SDK:** Platform API 36 (`platforms/android-36`), Build-Tools `36.0.0`
- **Gradle:** 9.1.0 (Managed via `./gradlew`)
- **Android Gradle Plugin (AGP):** 9.0.1
- **Kotlin:** 2.3.20 (with Compose Compiler plugin)

---

## 🚀 Build & Test Commands

Execute all tasks from the project root using the Gradle wrapper:

```bash
# Clean project build outputs
./gradlew clean

# Run local unit tests (Domain, Data, and ViewModel tests)
./gradlew testDebugUnitTest

# Assemble Debug APK
./gradlew assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📖 Project Documentation

Complete project specifications, workflow protocols, and progress logs are maintained in the root directory:

- [PROJECT_MASTER_PLAN.md](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/PROJECT_MASTER_PLAN.md) — Master product identity, scope, technical baselines, and phase roadmap.
- [PROJECT_CONTEXT.md](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/PROJECT_CONTEXT.md) — Domain context, card systems (MRT Pass vs Rapid Pass), and regulatory boundaries.
- [PROJECT_WORKFLOW.md](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/PROJECT_WORKFLOW.md) — Multi-agent workflow, commit conventions, and phase gates.
- [PROJECT_PROGRESS.md](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/PROJECT_PROGRESS.md) — Real-time execution log with verified build/test evidence.
- [AI_AGENT_INSTRUCTIONS.md](file:///Users/betopia/StudioProjects/Projects_Another/TransitMate/AI_AGENT_INSTRUCTIONS.md) — Strict rules and operational constraints for AI pair-programmers.

---

## ⚖️ Disclaimer

TransitMate is an independent community project. It is **not** affiliated with, authorized, maintained, or sponsored by Dhaka Mass Transit Company Limited (DMTCL), Dhaka Transport Coordination Authority (DTCA), or any government body.
