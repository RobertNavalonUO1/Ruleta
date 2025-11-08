# Decision Log (ADRs)

## 2025-11-07: Introduce ViewModel + Sealed UI State for Login
Context: Login logic lived inside composable causing recomposition side-effects and hindering testability.
Options: (1) Keep logic inline, (2) Extract to ViewModel + repository, (3) Full MVVM with DI (Hilt).
Decision: Implement lightweight ViewModel (`LoginViewModel`) and simple repository using existing `App.database` singleton. Sealed `LoginUiState` for explicit loading/error/success.
Consequences: Easier unit/UI testing. Slight added indirection without DI. Future migration to Hilt remains straightforward.

## 2025-11-07: Replace fallbackToDestructiveMigration with Explicit Migration Stub
Context: Destructive migrations wipe user data. DB version already at 2.
Decision: Added `MIGRATION_1_2` no-op and removed fallback.
Consequences: Users upgrading from v1 keep data. Must implement real migration steps when schema changes.

## 2025-11-07: Add Timber for Structured Logging
Context: Need structured, opt-in debug logging without leaking PII.
Decision: Initialize Timber only in DEBUG builds.
Consequences: Improved diagnostics; production remains clean.

## 2025-11-07: Add Detekt & KtLint
Context: Code style and static analysis were missing.
Decision: Integrate tools with minimal config and fail on style issues.
Consequences: Higher consistency; initial overhead adjusting code style.

## 2025-11-07: Introduce i18n (EN/ES)
Context: Hardcoded strings hinder localization and accessibility.
Decision: Extract visible strings to resources in both languages.
Consequences: Future translations easier; tests rely on resource IDs.

