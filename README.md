# ZotMacros

Native Android nutrition tracking for UC Irvine students. Built with Kotlin, Jetpack Compose and Material 3, using the approved lighter UCI blue-and-gold design.

## Verified build

The locally generated debug APK is at `artifacts/ZotMacros-debug.apk` (excluded from Git). To produce an APK from a fresh clone, follow the build steps below. Built and tested on a Pixel 8 Pro API35 emulator: 18 unit tests and 2 device tests passed, with zero Android lint errors. [Verification details and screenshots](docs/verification/RESULTS.md).

## Build and install

Open this folder in Android Studio with **JDK 17**, Android SDK **35**, and Build Tools **35.0.0** installed. Set `sdk.dir` in your untracked `local.properties` to your Android SDK location (Android Studio normally does this automatically).

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The app supports Android 8.0/API 26 and newer. No API key, account, backend server, or secrets are needed. Internet is required to retrieve new menus; saved food history works offline.

For device tests, start an emulator or attach an Android phone with USB debugging enabled:

```sh
./gradlew :app:connectedDebugAndroidTest
```

## Features

- Today: calorie ring, macro progress, grouped editable food diary, daily micronutrients.
- Menu: actual Anteatery/Brandywine meals and stations, search, station filters, serving detail sheet, pull-to-refresh, cache timestamps and retry.
- Food logging: decimal serving quantities, meal assignment, original nutrition snapshots, local persistence, duplicate-tap protection, edit/delete.
- History: day picker, previous logs, seven-day calorie and protein charts.
- Settings: goals, hall and meal preferences, refresh and haptic controls, confirmed history deletion.
- Skippable onboarding with explicitly labeled example goals.

No demonstration food data is seeded. The approved mockup contains illustrative data; production screens start with an empty diary and retrieve actual API menus. Test fixtures are confined to test source sets.

## Data and accuracy

API contract and attribution: [docs/API.md](docs/API.md). Menu and diary dates use `America/Los_Angeles`. Missing, malformed, negative, or non-finite nutrient values remain unknown. Known zero stays zero. Partial totals are marked `+`; an entirely unknown total is shown as unavailable. Goals do not cap logged amounts.

Food entries persist the original per-serving nutrient snapshot and calculated totals. Editing a quantity recomputes from that snapshot, never the current API. UI display rounds values; calculations do not round intermediates. Repeated taps on a single save share one request UUID, while a new intentional add creates a new entry.

Menus are keyed by hall and date and stored separately from diary entries. A failed fetch never clears diary data. Older cached menus keep their original date and are labeled unconfirmed for today. Automatic refresh is limited to 15-minute intervals; manual refresh remains available. Unavailable dish records are counted and surfaced rather than invented.

## Architecture

- `domain/`: nutrient model, aggregation, calendar rules and menu models.
- `data/`: Retrofit API, defensive parsing, Room diary/cache, DataStore preferences, repositories, Hilt bindings.
- `ui/`: lifecycle-aware ViewModel, Material theme, navigation and Compose screens.

Room starts at schema version 1 with exported schemas in `app/schemas`. No destructive migration fallback is enabled. Every future schema change must increment the database version, provide an explicit migration in `AppModule`, and add migration tests before release. App backup is disabled so the diary stays on this device.

## Design and requirements

- [Approved mockup](docs/design/approved-mockup.png)
- [Contributor and agent guidance](AGENTS.md)
- [Requirements](docs/REQUIREMENTS.md)
- [Design system](DESIGN.md)
- [Implementation plan](docs/superpowers/plans/2026-10-07-android-mvp.md)

Data from [Anteater API](https://icssc.link/about-anteaterapi), maintained by ICSSC. ZotMacros is an independent student application, not an official UCI app.
