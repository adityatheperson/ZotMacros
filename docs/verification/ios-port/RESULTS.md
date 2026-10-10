# Native iOS port verification — 2026-10-10

## Status

Source implementation is present on `codex/ios-port`. **Not yet a verified iPhone build or release.** Xcode, its iOS SDK/runtime, and the SwiftData macro plugin are absent on this Mac. No simulator screenshots, physical iPhone install, TestFlight upload or App Store submission occurred.

## Checks performed

- `./ios/scripts/test-core.sh`: Final deterministic run: **26 tests executed successfully, 2 opt-in live tests skipped (28 discovered)**. The portable Foundation/Observation suite compiles and runs with Swift 6.3.3 in Swift 5 language mode. The runner explicitly excludes SwiftData under Command Line Tools; it does not represent a storage or iOS UI pass.
- The suite covers unknown versus reported zero nutrients, invalid/overflow quantities, partial totals, preserved diary snapshots, campus midnight and DST, source parsing, exact station identity, configurable parents, captured popcorn chicken, schedule overrides/blank hours, request batching, cache fallbacks, cancellation, late-response rejection, editor retry/idempotency and invalid input. Supplement failure/cache preservation and current metadata tests were also added.
- `ZOT_LIVE_MENU=1 ZOT_LIVE_DAY=2026-10-10 ./ios/scripts/test-core.sh`: passed the then-current 23-test suite, including the actual UCI/Anteater network integration. Matching foods retained original Anteater nutrition and each meal had unique station/dish placements.
- `swiftc -frontend -parse` succeeded for app, UI, native integration tests, test host and UI-test Swift sources. This verifies syntax only, not iOS type checking or API availability.
- `plutil -lint` passed for the generated Xcode project and privacy manifest. Shared scheme XML parsed, all referenced source/resources existed, and the production source list contained no test-host fixture files.
- `xcodebuild ... -showdestinations` failed: the selected developer directory is a Command Line Tools instance. SwiftData tests failed at compilation because `SwiftDataMacros` is not installed. These checks remain blocked, not passed.

## Live menu comparison

For Anteatery, campus date **2026-10-10**, the Swift integration returned:

| Period | Food placements |
|---|---:|
| Breakfast | 19 |
| Brunch | 45 |
| Dinner | 36 |
| Afternoon Snack | 29 |
| All Day | 26 |
| Lunch | 26 |

An independent Python extraction of the raw UCI lunch `dateSkuMap` (simple and configurable parents, excluding explicitly hidden products) matched the Swift lunch output exactly: **26 placements, no missing or extra identities**.

| Lunch station | Raw UCI | Swift |
|---|---:|---:|
| Sizzle Grill | 12 | 12 |
| Oven | 2 | 2 |
| Soups | 1 | 1 |
| Farmer's Market | 11 | 11 |

This was Saturday: UCI's active schedule includes Brunch. Lunch is retained because Anteater returned it and UCI returned placements for that explicit meal query, matching the existing Android policy. Popcorn chicken is not claimed to be served on this date; the captured **2026-10-08** lunch fixture verifies its representation under Fire and Ice among 46 placements, with unknown nutrition.

## Bugs reproduced and corrected

- A cancelled refresh whose loader returned normally left `loading` active. A failing regression demonstrated this; generation-guarded `defer` cleanup made it pass.
- UCI metadata represents inactive meals with empty opening-hours strings. Live verification failed on that input; a minimal failing test and the captured metadata now cover the corrected interpretation.
- Independent review found initial meal selection could ignore an already-loaded snapshot, and the manual-refresh preference could still fetch on Menu appearance. Initial observation and refresh gating were corrected; native UI tests cover both but cannot run until Xcode is installed.

## Remaining mandatory checks

1. Build the `ZotMacros` scheme on an iPhone simulator and run native integration/UI tests.
2. Run the complete Swift package suite with Xcode selected, including save/reopen/edit/delete, rollback/retry, preferences, corrupt-cache isolation and unreadable-store preservation.
3. Inspect native Today/Menu/History/Settings and food sheet with normal/accessibility text, keyboard visible, Reduce Motion and safe areas. Confirm tab transitions and sheet dismissal on-device before making performance claims.
4. Check actual physical iPhone installation/signing if requested. App Store/TestFlight publication is a separate step.

## Implementation decisions

- Work stayed in the approved `codex/ios-port` checkout and new `ios/` directory; existing Android edits were preserved. Tradeoff: no second filesystem worktree; selective staging protects scope.
- Core tests use Swift Testing, which is present in Command Line Tools; an explicit framework and runtime path makes that runner usable. Tradeoff: the runner needs toolchain-specific setup only when full Xcode is absent.
- Storage and observable state live in separate local package targets for isolation and testing. State is verified here; SwiftData still requires Xcode's macro plugin. Tradeoff: package layout differs from the initial file map, while the app consumes the same planned native components.
- The reviewer deferred judgments on macro compilation, Xcode build correctness, keyboard/Dynamic Type and visual fidelity. Those remain explicit blockers rather than inferred successes; native defects can still be found by those checks.
- UI fixtures live in a dedicated `ZotMacrosUITestHost` target. Production `ZotMacros` never compiles that test entry point or its menu fixtures; archive only the production scheme.

No deferred cosmetic findings were reported. No source push or publication was performed during this iOS task.

## Brandywine source limitation

The strict Brandywine completion check failed on the actual response received by URLSession: station `1881` referenced simple SKUs `1821_122168_M4649_1_40474` and `1821_122168_M20609_1_18887`, but neither product record was returned. A separate curl request did not consistently reproduce that payload, so the exact Swift response is captured as a test fixture. No names or nutrition were invented for these unresolved IDs.

The production repository live check passed: it returned all five Anteater periods plus “UCI menu supplement unavailable. Some foods may be missing; retry to refresh.” It did not claim the menu was verified. Brandywine's complete-menu parity is therefore **not verified**, even though the app's fallback path is verified at the repository level. This upstream response limitation is separate from the missing Xcode blocker.
