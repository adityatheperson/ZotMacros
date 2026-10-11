# ZotMacros for iPhone

Native SwiftUI port of the Android app, targeting iOS 17 and later. No additional backend is required. Menus use Anteater API plus the public UCI MyDiningHub supplement; diary and preferences stay local.

## Status

The native app builds and launches in the iPhone simulator. All 31 executed package tests and 7 native integration/UI tests passed with Xcode 27.0. See [native verification](../docs/verification/ios-port/XCODE-2026-10-10.md) for screenshots and scope. Physical-device testing, signing, and App Store submission remain outstanding.

## Open and run

1. Install a current Xcode (16 or later) and open it once to install an iOS simulator runtime. Select it in Xcode → Settings → Locations → Command Line Tools.
2. Open `ZotMacros.xcodeproj`, choose the `ZotMacros` scheme and an iPhone simulator, then Run. Simulator builds do not require a paid developer account.
3. For a physical iPhone, select your own team in Signing & Capabilities and a unique bundle identifier as needed. This repository contains no team ID or signing credentials.

The production application target is `ZotMacros`. `ZotMacrosUITestHost` is a separate fixture-backed test app; it must never be archived or distributed. The shared scheme archives only `ZotMacros`.

## Tests

From the repository root:

```sh
./ios/scripts/test-core.sh
```

With full Xcode selected, this runs core, state, and SwiftData storage tests. With Command Line Tools selected, it explicitly excludes the storage target (the SwiftData macro plugin is not installed) and links the bundled Swift Testing framework. Requires a Swift 6 toolchain for Swift Testing; the package uses Swift 5 language mode for compatibility.

Native unit/UI tests:

```sh
xcodebuild -project ios/ZotMacros.xcodeproj -scheme ZotMacros -showdestinations
xcodebuild -project ios/ZotMacros.xcodeproj -scheme ZotMacros \
  -destination 'platform=iOS Simulator,id=YOUR_SIMULATOR_UDID' test
```

Native UI tests use only the separate test host with isolated preferences and a temporary diary, and include unknown nutrition, persistence across relaunch, failed-save retry, manual refresh, large text with keyboard, navigation and deletion confirmation. They do not require live network responses or modify the production app diary.

Opt-in public-network verification:

```sh
ZOT_LIVE_MENU=1 ./ios/scripts/test-core.sh
```

An optional `ZOT_LIVE_DAY=YYYY-MM-DD` pins the campus date; `ZOT_LIVE_HALL=brandywine` checks the other hall (default: Anteatery). The live test writes its result to the system temporary directory as `zot-ios-live-menu.json`. It checks duplicate placements and preservation of existing nutrient values; a captured October 8 fixture separately covers popcorn chicken. Neither fixture is included in the production target.

## Structure

- `ZotMacrosCore/`: local package with Foundation domain/network code, observable state, and SwiftData storage as separate modules.
- `ZotMacros/App/`: app entry point and dependency composition.
- `ZotMacros/UI/`: native screens, theme, sheets and navigation.
- `ZotMacrosTests/`: app integration tests.
- `ZotMacrosUITests/` and `ZotMacrosUITestHost/`: UI automation and isolated test composition.
- `scripts/generate-project.py`: deterministic project generator; run after adding/removing app or test source files, then commit both generator changes and generated project.

## Data and privacy

Nutrients retain source units. Missing values remain unknown; partial totals are labeled. Entries retain their original food snapshot. SwiftData's initial schema is explicitly versioned; future changes require migration stages and migration tests. Cache records are separate from diary records. Database open/save failures do not trigger a destructive reset.

The privacy manifest declares local UserDefaults access for the app's preferences. No analytics, tracking, advertising, account, or cloud-sync SDK is included. Before App Store submission, review App Store privacy disclosures against the final build and third-party menu services; add your support/privacy-policy URLs and confirm data/brand permissions. This project is not an App Store submission and no upload has occurred.
