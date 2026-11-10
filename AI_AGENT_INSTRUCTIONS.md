# TransitMate — AI Agent Operating Instructions

**Target Audience:** Autonomous AI Coding Assistants & Agentic Pair-Programmers  
**Status:** Mandatory Compliance Guidelines  
**Current Phase:** Phase 02 — Initial Project Setup & Foundation  

---

## 1. Golden Rules for AI Agents

1. **Read First, Edit Later:** Always read `PROJECT_MASTER_PLAN.md`, `PROJECT_CONTEXT.md`, and `PROJECT_PROGRESS.md` before making architectural or code decisions.
2. **Never Invent Capabilities:** Do not guess FeliCa system codes, service codes, memory block offsets, or balance parsing logic. All card protocol features belong strictly to Phase 03 and Phase 06 and must be supported by empirical evidence.
3. **Strict Scope Discipline:** Never jump ahead to implement final dashboards, scanner screens, card management, or transaction databases during foundation phases. Respect phase boundaries.
4. **Clean Architecture Enforcement:**
   - `domain/` must remain pure Kotlin with zero Android framework or database imports.
   - `core/` contains platform abstractions, theme tokens, and common utilities.
   - `data/` implements domain repository contracts.
   - `feature/` contains UI presentation and ViewModel logic.
5. **No Blind Dependency Upgrades:** Keep versions centralized in `gradle/libs.versions.toml`. Verify mutual compatibility between AGP, Kotlin, and Compose BOM before introducing or bumping any version.
6. **Empirical Evidence Required:** Never write "Tests passed" or "Build successful" in progress documents without running real CLI commands and logging actual exit codes.

---

## 2. Coding Standards

- **Language:** Kotlin with modern idiomatic patterns.
- **UI:** Jetpack Compose with Material 3 components.
- **State Management:** `StateFlow` + ViewModel, lifecycle-aware collection via `collectAsStateWithLifecycle()`.
- **Coroutines:** Injected dispatchers via `DispatcherProvider` for deterministic unit testing.
- **Resource Management:** All user-facing strings must be defined in `res/values/strings.xml` (with English and Bengali support).
- **Naming Conventions:**
  - Files: PascalCase (e.g., `StartupViewModel.kt`)
  - Packages: lowercase dot-separated (e.g., `com.transitmate.bd.core.common`)
  - Resources: snake_case (e.g., `ic_launcher_foreground.png`, `startup_status_header`)

---

## 3. Git Commit & Safety Guidelines

- Commit in small, logical increments using standard prefixes (`build:`, `feat:`, `fix:`, `test:`, `docs:`, `chore:`).
- Always verify that `.gitignore` ignores `local.properties`, `.gradle`, `build/`, and IDE-specific files.
- Never force-push or overwrite remote git history.

---

## 4. End-of-Turn Checklist for Agents

Before completing a turn:
- [ ] Run `./gradlew clean testDebugUnitTest assembleDebug`
- [ ] Verify test results (0 failures, 0 errors)
- [ ] Update `PROJECT_PROGRESS.md` with:
  - Current phase and task status
  - Files created/modified
  - Exact commands executed and empirical results
  - Next recommended task
- [ ] Confirm git working tree is clean or properly committed.

