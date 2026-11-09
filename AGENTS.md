# Working on ZotMacros (Android)

These instructions apply to the `android` branch. Follow the user's current instructions when they override this guidance.

## Product and scope

ZotMacros is a native Android nutrition tracker for UC Irvine students, built for all modern Android phones — varied screen sizes, densities, and supported API levels, never a single device model. Its core flow is real dining menu → food → serving quantity → persistent diary → updated daily totals.

Branch map (do not reorganize without being asked):
- `android` (this branch): source of truth for the Android app (`app/`), shared docs (`PRODUCT.md`, `DESIGN.md`, `docs/`), and `website/` (deployed to GitHub Pages from here).
- `ios`: the iPhone app. Read-only reference on this branch — never modify it here.
- `andriod-uci-suppliment`: Android-only experiment restoring the Adobe menu supplement. Hands off — do not merge it into `android` without explicit user approval.
- `ios-uci-supplement`: iOS-side experiment. Ignore on this branch.
- `adityatheperson/website`: website workspace branch.

Product decisions in force on this branch: menus come solely from the Anteater API (no secondary menu sources); the station filter always has exactly one station selected (no 'All stations' option, defaults to the first station); foods served all day appear only under All Day, never duplicated into Lunch, Dinner, or Brunch (see `dedupeAllDayOverlaps`).

Before changing behavior or design, read:
- `PRODUCT.md` for product scope.
- `docs/REQUIREMENTS.md` for the original MVP requirements.
- `DESIGN.md` and `docs/design/approved-mockup.png` for the approved visual direction.
- `docs/API.md` for the verified Anteater API contract.
- `docs/play/` for Play Store listing, data-safety, and launch-checklist answers.

The approved lighter UCI blue-and-gold design supersedes the original brief's lime palette. Mockup nutrition values are illustrative, never production data. Keep the app native; do not replace Compose with a website or WebView. Accounts, payments, ads, cloud sync, barcode scanning, and AI coaching are outside the MVP unless requested.

## Architecture

The application module is `app`, package `com.zotmacros`.
- `domain/`: pure nutrition calculations, menu models, meal selection, All-Day dedup, and campus date rules.
- `data/`: Retrofit/OkHttp, defensive Kotlinx JSON parsing, Room diary/menu cache, DataStore preferences, repositories, and Hilt bindings.
- `ui/`: Jetpack Compose Material 3 screens, theme, Navigation Compose, and lifecycle-aware ViewModel/StateFlow state.
- `app/src/test/`: local JVM tests and API fixtures.
- `app/src/androidTest/`: Room persistence and native navigation tests.

Keep network and database work out of composables. Preserve coroutine cancellation. Scope asynchronous menu requests to hall and date, and prevent obsolete results from replacing current state. Keep independent concerns in focused files.

## Nutrition and persistence invariants

- Use `America/Los_Angeles` for menu and diary dates, regardless of device timezone. Handle midnight rollover.
- Missing or malformed nutrition is unknown, never zero. A reported zero remains known.
- Preserve explicit nutrient units. Do not guess conversions or fabricate unavailable fields.
- Totals must distinguish complete, partial, and entirely unavailable nutrition.
- Validate serving quantities as finite, greater than zero, and at most 1,000. Do not round intermediate calculations; round only for display.
- Exceeding a goal is valid and must not clamp totals or appear as an error.
- Each diary entry keeps its original per-serving snapshot. Quantity edits use that snapshot, not refreshed API data.
- A repeated tap on one save must not duplicate the entry; a separate intentional add must remain possible.
- Cache menus separately from the diary, keyed by hall and date. Show original cache dates and freshness; never call a historical menu today's confirmed menu.
- A network or API failure must never delete saved entries.
- Room schemas are versioned in `app/schemas`. Database changes require a version increment, explicit migration registration, exported schema, and migration tests. Do not enable destructive migration fallback.

## API changes

Menus come solely from the Anteater API; `docs/API.md` is the contract. Do not add secondary menu sources on this branch.

Verify current official documentation at `https://anteaterapi.com/reference` / `https://anteaterapi.com/openapi.json` before changing integration assumptions. Production base URL: `https://anteaterapi.com/v2/rest/`.

Use returned meal periods and station mappings; do not assume only Breakfast/Lunch/Dinner exist. Deduplicate dish IDs and batch requests. Respect rate limits and preserve cached data on failures. Keep linked “Data from Anteater API” attribution near search/data surfaces and in About; see `docs/API.md`.

Test fixtures belong only in test source sets. Never seed production diaries or silently substitute sample menus for real responses.

## UI conventions

Use Material 3 components and role-based colors from `ui/Theme.kt`. Preserve the approved blue surfaces, gold selected controls and primary actions, and labeled macro colors. Use Android system typography, scalable `sp`, minimum 48dp touch targets, and system/keyboard insets.

Design responsive layouts that work on any modern phone: no hardcoded dimensions tuned to one device; use adaptive layouts and window size classes, scrollable content, and edge-to-edge handling for cutouts and system bars. Support portrait and landscape, small phones through large phones and foldables, and all screen densities. Never assume a Pixel 8 Pro screen size, density, or API level.

Keep Today, Menu, History, and Settings accessible through navigation. Honor system Back. Food sheets must keep their save action reachable while details scroll. Include loading, empty, unavailable, retry, cached/stale, and saving states. Goals must be clearly labeled as examples until accepted or personalized. Destructive history deletion requires confirmation.

Verify changed screens across a device matrix, not one phone: the lab AVDs (`Pixel8Pro`, `GalaxyS25`, `Pixel8Pro36`), min SDK 26 through the current target SDK, plus larger font sizes. Use native screenshots, not browser renders, as evidence. Do not claim physical-device verification from emulator tests.

## Build and verification

Use JDK 17, Android SDK 36, Build Tools 36.0.0, and the committed Gradle wrapper. Minimum SDK is 26, and the app must install and run cleanly on every API level from 26 through the current target SDK. Let Android Studio create `local.properties`, or set its `sdk.dir` to the local SDK. Never commit machine-specific SDK/JDK paths.

Local device lab (this Mac): SDK at `$HOME/Library/Android/sdk` (`local.properties` sets `sdk.dir`; untracked). Run Gradle under JDK 17 (e.g. Homebrew `openjdk@17` via `JAVA_HOME`), not whatever system Java is newest. Launch with `$ANDROID_HOME/emulator/emulator -avd <name>` (add `-no-window -no-audio` for headless); address one device with `adb -s <serial>`.

Standard checks:

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

With a running emulator or connected device:

```sh
./gradlew :app:connectedDebugAndroidTest
```

If the Gradle device-test host cannot run but Android test APKs can be built, use the native runner and record that route:

```sh
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w com.zotmacros.test/androidx.test.runner.AndroidJUnitRunner
```

Use `adb -s <serial>` when multiple devices are attached. Prefer an emulator for tests that clear data or alter connectivity/font settings; restore settings afterward. Test calculation/parser/repository changes with focused regression cases and run the relevant full suite. Run lint and assembly for native UI or configuration changes. Documentation-only edits need link/content checks, not new application tests.

Release builds: `bundleRelease` signs with the upload keystore via `ZOT_KEYSTORE_*` env vars (keystore lives outside git; password in keychain). Never commit keys or passwords.

Report commands actually run and their outcomes. `docs/verification/RESULTS.md` records the MVP's historical verification, not proof for later changes. Do not overwrite prior evidence as if it verified a different build.

## Repository hygiene and handoff

Work directly on `android`. Do not create new branches, merge the experiment branches, or reorganize branch ownership unless the user explicitly asks. Keep unrelated changes out of commits. Do not force-push or discard user work.

Do not commit SDKs, build caches, `local.properties`, generated APKs/AABs, signing keys, credentials, or device databases. Keep the Gradle wrapper JAR, exported Room schemas, tests, approved design, and useful documentation tracked. Generated APKs/AABs belong in local build output or an explicitly requested release artifact, not source history.

Before handoff, check the diff and repository status, summarize the user-visible changes and verification, and state material limits. Push, merge, or publish when the user authorizes that action; never claim a push succeeded without checking the remote result.
