# Working on ZotMacros (iOS)

These instructions apply to the `ios` branch. Follow the user's current instructions when they override this guidance.

## Product and scope

ZotMacros is a native iPhone nutrition tracker for UC Irvine students. Its core flow is real dining menu → food → serving quantity → persistent diary → updated daily totals.

This branch (`ios`) is the iOS source of truth. Work on `ios/` and iOS docs here. `app/` (Android) exists for reference — do not modify it on this branch; change it on `android` instead. `website/` is edited on `android` to avoid divergence.

Before changing behavior or design, read:
- `ios/README.md` for setup, structure, and test commands.
- `PRODUCT.md` for product scope (written Android-first; applies by analogy).
- `docs/REQUIREMENTS.md` for the original MVP requirements.
- `DESIGN.md` and `docs/design/approved-mockup.png` for the approved visual direction.
- `docs/API.md` for the verified Anteater API contract.
- `docs/verification/ios-port/` for prior iOS verification evidence.

The approved lighter UCI blue-and-gold design supersedes the original brief's lime palette. Mockup nutrition values are illustrative, never production data. Keep the app native; do not replace SwiftUI with a website or WebView. Accounts, payments, ads, cloud sync, barcode scanning, and AI coaching are outside the MVP unless requested. Never commit signing credentials, personal team IDs, or production fixture menus.

## Architecture

The iOS project lives in `ios/`, minimum deployment iOS 17, Swift with Swift Package Manager.
- `ZotMacrosCore/`: local package with separate `ZotMacrosCore` (domain models, nutrition, campus dates, parsers, URLSession networking, menu repository), `ZotMacrosState` (observable app/menu/food-editor models), and `ZotMacrosStorage` (SwiftData diary/cache stores, preferences) targets.
- `ZotMacros/App/`: app entry point and dependency composition.
- `ZotMacros/UI/`: SwiftUI theme, navigation, screens, food sheet, and onboarding.
- `ZotMacrosTests/`, `ZotMacrosUITests/`: native integration and UI tests.
- `ZotMacrosUITestHost/`: isolated fixture-backed test app. Never archive or distribute it; production archives only the `ZotMacros` scheme.
- `scripts/generate-project.py`: deterministic project generator. Run after adding/removing app or test source files, then commit the generator and generated project together.
- `scripts/test-core.sh`: package test runner.

Keep network and database work out of views. Preserve task cancellation. Scope menu requests to hall and date with request-generation guards so late responses never replace the current selection. Keep independent concerns in focused files.

## Nutrition and persistence invariants

- Use `America/Los_Angeles` for menu and diary dates, regardless of device timezone. Handle midnight rollover and DST transitions.
- Missing or malformed nutrition is unknown, never zero. A reported zero remains known.
- Preserve explicit nutrient units. Do not guess conversions or fabricate unavailable fields.
- Totals must distinguish complete, partial, and entirely unavailable nutrition.
- Validate serving quantities as finite, greater than zero, and at most 1,000. Do not round intermediate calculations; round only for display.
- Exceeding a goal is valid and must not clamp totals or appear as an error.
- Each diary entry keeps its original per-serving snapshot. Quantity edits use that snapshot, not refreshed API data.
- A repeated tap on one save must not duplicate the entry; a separate intentional add must remain possible.
- Cache menus separately from the diary, keyed by hall and date. Show original cache dates and freshness; never call a historical menu today's confirmed menu.
- A network or API failure must never delete saved entries. Persistence failures keep the food sheet open with a retry path; unreadable stores are surfaced, never silently reset.
- The SwiftData schema is versioned. Schema changes require migration stages and migration tests.
- Preferences persist in UserDefaults with validated defaults (2400 kcal, 160 g protein, 250 g carbs, 75 g fat, all marked examples until accepted or personalized). Goals must be finite, positive, and at most 100,000.

## API changes

Verify current official documentation at `https://anteaterapi.com/reference` / `https://anteaterapi.com/openapi.json` before changing integration assumptions. Production base URL: `https://anteaterapi.com/v2/rest/`.

Use returned meal periods and station mappings; do not assume only Breakfast/Lunch/Dinner exist. Deduplicate dish IDs and batch requests. Respect rate limits and preserve cached data on failures. Keep linked “Data from Anteater API” attribution near search/data surfaces and in About; see `docs/API.md`.

Test fixtures belong only in test source sets and the UI test host. Never seed production diaries or silently substitute sample menus for real responses.

## UI conventions

Use SwiftUI with the shared blue-and-gold theme, gold selected controls and primary actions, and labeled macro colors. Support Dynamic Type, VoiceOver, safe areas, and Reduce Motion. Minimum 48pt primary touch targets.

Keep Today, Menu, History, and Settings accessible through navigation. Food sheets must keep their save action reachable while details scroll, including with large text and the decimal keyboard visible. Include loading, empty, unavailable, retry, cached/stale, and saving states. Goals must be clearly labeled as examples until accepted or personalized. Destructive history deletion requires confirmation.

Verify changed screens in the iPhone simulator at normal and accessibility text sizes, with keyboard visible and Reduce Motion on. Use native screenshots, not browser renders, as evidence. Do not claim physical-device verification from simulator runs.

## Build and verification

Requires Xcode 16+ with an iOS simulator runtime; select Xcode under Settings → Locations → Command Line Tools. Simulator builds need no paid developer account. For a physical iPhone, use your own team and bundle identifier; never commit them.

Package tests from the repository root:

```sh
./ios/scripts/test-core.sh
```

Native unit/UI tests (fill in an installed simulator UDID; list with `-showdestinations`):

```sh
xcodebuild -project ios/ZotMacros.xcodeproj -scheme ZotMacros -showdestinations
xcodebuild -project ios/ZotMacros.xcodeproj -scheme ZotMacros \
  -destination 'platform=iOS Simulator,id=YOUR_SIMULATOR_UDID' test
```

Opt-in live-network verification (writes JSON to the system temp directory; never ships fixtures):

```sh
ZOT_LIVE_MENU=1 ./ios/scripts/test-core.sh
```

Android Gradle/`adb` commands do not apply on this branch. Documentation-only edits need link/content checks, not new application tests.

Report commands actually run and their outcomes. Record new evidence under `docs/verification/ios-port/`; do not overwrite prior results as if they verified a different build.

## Repository hygiene and handoff

Work directly on `ios`. The repository has only two branches (`android`, `ios`); do not create `codex/` or other feature branches unless the user explicitly asks. Keep unrelated changes out of commits. Do not force-push or discard user work.

Do not commit build output, signing credentials, personal team IDs, device databases, or production fixture menus. Keep tests, test fixtures, approved design, and useful documentation tracked.

Before handoff, check the diff and repository status, summarize the user-visible changes and verification, and state material limits. Push, merge, or publish when the user authorizes that action; never claim a push succeeded without checking the remote result.
