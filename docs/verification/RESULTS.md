# Verification — 2026-10-08

## Final artifact
- `artifacts/ZotMacros-debug.apk` — debug-signed, application ID `com.zotmacros`, version 1.0.0.
- SHA-256: `4475045b980f12ef741ca5d1cd8173968a6c32a8cd00403dd269501aa695fa03`
- Minimum Android API 26; target/compile API 35.

## Automated checks
- `:app:testDebugUnitTest :app:assembleDebug :app:lintDebug :app:assembleDebugAndroidTest`: BUILD SUCCESSFUL.
- 18 local unit tests: no failures/errors. Serving precision, invalid/nonfinite amounts, null/zero/partial totals, over-goal values, Los Angeles date rollover, malformed API fields, menu fixture structure, cache fallback dates and period selection.
- 2 instrumentation tests: `OK (2 tests)` on Pixel 8 Pro API35 ARM64 emulator. Room add/reopen/edit/delete/clear and duplicate request idempotence; onboarding and all four tabs.
- Instrumentation was run directly with `adb shell am instrument -w com.zotmacros.test/androidx.test.runner.AndroidJUnitRunner`. The Gradle connected-test route needed an uncached host plugin; the compiled Android tests were run successfully with the native runner instead.
- Android lint: zero errors; 12 dependency-version update advisories. Dependencies are pinned to a tested compatible set.

## Live end-to-end checks
- Retrieved actual 2026-10-08 Anteatery menu and real station/period names.
- Selected API-reported Scrambled Eggs (142 kcal and 12.4 g protein per serving); chose 1.5 servings; sheet displayed 213 kcal and 18.6 g protein. Added to diary and confirmed immediate dashboard totals.
- Enabled airplane mode and disabled Wi-Fi on emulator, force-stopped and reopened app. Entry and totals remained present.
- Forced menu refresh offline: cached menu remained, with saved-data warning, retrieval time, connection error and Retry.
- Restored connectivity and retrieved Brandywine menu (Honeycakes, Croissant and other real dishes).
- Checked History with saved entry, Settings, system Back from food sheet, and Today at 1.3× system font scale. Restored normal text scale and connectivity.
- API menu changed on subsequent refresh; historical Scrambled Eggs snapshot remained unchanged.

## Review
Independent code review found three edge cases (midnight request replacement, obsolete station filters and stale-age updates); corrected and re-reviewed as resolved. Native visual review requested gold selected controls, light system-bar icons in sheet, and visible selected history date; corrected in final APK. The independent reviewer confirmed all three listed visual fixes resolved in the final captures.

## Evidence
Actual native emulator captures in this directory: `today.png`, `menu.png`, `serving.png`, `history.png`, `settings.png`, `today-large-text.png`, `menu-offline.png`, `brandywine.png`, `onboarding.png`. Screens show real API data or intentionally empty diary states. Settings/offline/large-text captures predate final gold selected-control fix; menu/serving/history were recaptured after it.

## Limits
Tested on an emulator, not a physical Pixel 8 Pro. API26 execution, TalkBack, tablet hardware and release signing/Play Store distribution were not verified. The APK is for local testing. SDK/JDK used on this machine are under `/tmp/zot-build-tools`; Android Studio users should point `local.properties` to their installed SDK. Live menus and their availability depend on Anteater API.
