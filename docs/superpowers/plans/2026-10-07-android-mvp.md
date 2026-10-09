# ZotMacros Android MVP Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Deliver an installable native Android nutrition diary using actual UCI dining menus.
**Architecture:** Compose screens observe a Hilt ViewModel, Room diary and menu cache, and DataStore preferences. Retrofit retrieves menus and batches dish IDs. Immutable per-serving snapshots make historical totals independent of later API changes.
**Tech Stack:** Kotlin, Compose Material 3, Navigation Compose, Hilt, Room, DataStore, Retrofit/OkHttp, Kotlinx Serialization, coroutines/StateFlow. Min SDK 26, compile/target SDK 35.
**Spec:** ../../REQUIREMENTS.md; approved visual reference ../../design/approved-mockup.png. User authorized building after approving lighter UCI blue/gold.

## Global Constraints
- Native Android; no WebView and no seeded demo menus or food logs.
- America/Los_Angeles determines menu and diary dates.
- Null nutrients remain unknown; totals report incomplete coverage.
- Approved blue/gold palette supersedes original lime palette.
- No accounts, cloud diary, ads, payments, AI, barcode or wearables.

## Review Focus
- Malformed or missing nutrients cannot silently become zero (parser/domain tests).
- Cached historical menus cannot appear as confirmed today (cache/date tests).
- Decimal quantities and repeated taps must not corrupt totals (domain/repository tests).
- API failures and restarts cannot delete existing diary snapshots (Room device tests).
- Midnight, long dish names, font scaling, and unknown nutrition must remain usable (date tests/device checks).

### Task 1: Foundation and nutrition model
- [x] Configure Gradle wrapper, Android project and dependencies.
- [x] Write tests for decimal serving multiplication, invalid quantities, null/partial aggregation, over-goal totals and Los Angeles date boundaries.
- [x] Implement domain/Nutrition.kt and domain/Models.kt; run unit tests.

### Task 2: Real dining data and durable storage
- [x] Record verified API contracts in docs/API.md.
- [x] Write parser tests against actual captured JSON and malformed numeric fields.
- [x] Implement data/DiningApi.kt, data/DiningParser.kt and data/MenuRepository.kt; batch requests, cache by hall/date, preserve stale fallback with explicit dates and errors.
- [x] Implement data/Database.kt, data/DiaryRepository.kt and data/Preferences.kt. Entries store UUID, date, timestamp, origin, quantity, per-serving nutrition and calculated totals. Use explicit Room migration policy.
- [x] Add Room persistence/edit/delete/idempotence tests and verify.

### Task 3: Native app and main flow
- [x] Implement blue/gold Material theme and Hilt application/activity.
- [x] Implement state in ui/ZotViewModel.kt; today rollover, refresh throttling, preference updates, add/edit/delete results.
- [x] Implement Today, Menu, FoodSheet, History and Settings Compose screens with bottom navigation, adaptive rail, accessible targets, loading/empty/error/stale states, search and filters.
- [x] Implement skippable four-step onboarding with editable example goals.
- [x] Wire haptics, snackbars, pull refresh, meal assignment and serving validation.

### Task 4: Verification and delivery
- [x] Run debug assembly, unit tests and Android lint; resolve actionable failures.
- [x] Install on emulator if available, exercise actual menu → serving → add → diary → restart → edit/delete, inspect phone screenshots and increased font scale.
- [x] Review completed changes independently, fix important defects, record evidence/limitations.
- [x] Provide README build/install instructions, APK and tested device details.
