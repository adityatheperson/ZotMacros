# ZotMacros native iOS port

## Outcome and scope

Build an iPhone application alongside the working Android app. The user approved a native SwiftUI port that preserves ZotMacros's existing blue-and-gold design, four main screens, real dining menus, and local food diary. No additional hosted service, accounts, cloud sync, payments, advertising, or coaching is included. App Store publication is a later action requiring a signed, tested build and the user's Apple developer account.

The core acceptance flow is: choose Anteatery or Brandywine, browse the published campus-date menu by meal and station, select a food, adjust servings, save it, see accurate totals, and retain the entry after restart. Foods without nutrition remain loggable and make affected totals explicitly incomplete.

## Approach

Use SwiftUI with an iOS 17 minimum deployment target. Place the iOS project under `ios/` in this repository. Keep Android sources and existing unfinished tab-transition changes intact.

A separate native Swift implementation gives the app native sheets, navigation, accessibility, and lifecycle behavior without introducing Kotlin Multiplatform tooling. A cross-platform rewrite would disrupt the working Android application; a shared Kotlin core can be reconsidered if maintenance warrants it later. Port behavior and regression fixtures rather than attempting to compile Compose for iOS.

## Components

- A Foundation-only Swift package contains nutrition models, aggregation, serving validation, campus-date handling, parsers, schedule selection, and menu reconciliation. It can be tested independently from the iOS UI.
- A URLSession-based repository implements Anteater requests and the direct UCI menu supplement. Async requests are cancellable and scoped to hall/date; obsolete responses cannot replace current selections. Refreshes deduplicate dish IDs, batch at the verified limit, and keep supplemental meal requests sequential.
- SwiftData stores versioned diary entries and original per-serving snapshots. Menu cache records remain independent, keyed by hall and campus date, with fetch timestamps and supplement-verification state. Preferences use UserDefaults. A main-actor observable application model coordinates UI and repository state; networking does not run in view bodies.
- SwiftUI screens and shared components implement the existing product flows. A committed Xcode project and shared scheme permit simulator builds without a paid developer account. Signing identifiers remain configurable; no signing credentials are committed.

## Data fidelity

Preserve explicit nutrient units and reported zero values. Missing, malformed, negative, or nonfinite nutrients are unknown. Daily totals distinguish complete, partial, and entirely unavailable data for each nutrient. No intermediate rounding; goals may be exceeded without an error state.

Serving quantities must be finite, greater than zero, and at most 1,000. Diary edits use the saved snapshot rather than refreshed menu data. One save operation is idempotent, while a later intentional addition remains possible. Persistence errors keep the sheet open with a retryable error; network failures never remove diary entries. Versioned persistence must not silently reset unreadable data.

Use America/Los_Angeles for menu and diary dates, including foreground refresh and midnight rollover. Retain source meal and station mappings. Supplement exact-date UCI simple and configurable products using the Android reconciliation rules: preserve matching Anteater nutrition, never borrow variant nutrition for configurable parents, and deduplicate by exact identity within station. Preserve additional active meal periods from UCI schedules.

Reverify official Anteater documentation before implementing requests. Keep linked Anteater attribution near menu and food details and in About, plus UCI menu attribution. If supplement collection fails, retain the exact-date verified cache or clearly label the menu incomplete. Cached historical menus must never appear as today's confirmed menu.

## Screens and interaction

- **Today:** truthful calorie ring and labeled macro progress, example-goal status until accepted, meal-grouped diary, add/edit/delete, and explicit incomplete-total explanations.
- **Menu:** hall, returned meal period, search and station filtering, freshness state, pull to refresh, loading/empty/retry states, real foods only.
- **Food sheet:** source details, optional nutrition and ingredients, decimal serving presets/input, meal selection, and a save action reachable with the keyboard and large text. Unknown nutrition displays “Nutrition unavailable.”
- **History:** campus-date selection, editable persistent logs, seven-day calorie/protein summaries with partial and unavailable states, and confirmed destructive deletion.
- **Settings/onboarding:** skippable first-run introduction, preferred hall, clearly labeled editable example goals, refresh/haptic preferences, About, and confirmed history clearing.

Adapt the approved palette and hierarchy to San Francisco typography, Dynamic Type, VoiceOver, safe areas, and native controls. Maintain at least 48-point primary interaction targets. Bottom destinations stay labeled and use a shared restrained slide/fade transition, with the bar fixed. Respect Reduce Motion; numeric totals update immediately while progress may animate. Sheets use native presentation and dismissal. Support larger text without clipped save controls or horizontally compressed nutrition labels.

## Verification and completion

1. Unit tests cover nullable nutrition, explicit zero, totals completeness, quantities, immutable snapshots, campus rollover, parser fixtures, station identity, configurable products, schedules, and cache/repository failure behavior.
2. Persistence tests cover save/relaunch/edit/delete and failure preservation; request tests cover cancellation and stale-result rejection.
3. Build and run the iOS app in an iPhone simulator. Exercise all four destinations, logging a real food, an unknown-nutrition food, repeated-save protection, restart persistence, offline diary access, and menu failure states.
4. Check native screenshots at normal and accessibility text sizes, plus Reduce Motion. Compare a current live lunch menu station by station with UCI, recording the actual date and unavailable periods rather than assuming a particular dish is served every day.
5. Document build instructions and actual verification results. Device installation, TestFlight upload, and App Store submission remain explicitly separate from a simulator or source-only result.

## Environment findings

On 2026-10-10 the selected developer directory is `/Library/Developer/CommandLineTools`; Swift is available, but `simctl` is not. No Xcode application is present in `/Applications`. Full Xcode and an iOS simulator runtime are required to build and verify the SwiftUI target. Foundation package work can proceed with command-line Swift; completion must not be claimed until the native application is built and exercised. Apple account enrollment and signing are not prerequisites for implementing the app or simulator testing.
