# Direct UCI supplement verification — October 9, 2026

No hosted supplement service was deployed. ZotMacros 1.0.1 requests public UCI data directly and retains Anteater API nutrition for exact source identities.

- `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest --offline`: passed using JDK 17, SDK 35, Build Tools 35.0.0 and the committed wrapper. Final additional failure/cancellation tests: `./gradlew :app:testDebugUnitTest --offline`, 30 JVM tests passed.
- Installed both APKs with `adb -s emulator-5554 install -r`, then ran `adb -s emulator-5554 shell am instrument -w -e verifyLiveMenu true com.zotmacros.test/androidx.test.runner.AndroidJUnitRunner`: 5 tests passed on final APK.
- Live October 8 Anteatery lunch: all 46 placements, nine station counts matched independently recorded raw response; no duplicate station/dish pairs; matching Anteater nutrition unchanged. Chicken appeared at Fire and Ice and saved with unknown calories. Separate Room test reopened and edited unknown-nutrition entries and verified incomplete totals.
- Food sheet at system font scale 1.5: native save-reachability test passed. Screenshot inspected; compact macro unknowns use dashes to avoid broken word wrapping. Restored font scale to 1.0. Screenshots under `../direct-uci-2026-10-09/` are native emulator test screenshots; the sheet uses test-only dish input. Production diaries are never seeded.
- October 9 current menu manually opened in native app: current date, Lunch, station filters and existing known nutrition displayed with no source warning. The exhaustive 46-item comparison is specifically October 8, not October 9.
- Upstream helper: `node --test apps/data-pipeline/dining-scraper/src/menu-entries.test.mjs` passed 2 tests. Full upstream type checking and database ingestion are not verified; upstream patch/PR text are prepared locally, not submitted or merged.
- Initial regressions caught and corrected: 50 merged placements from four stale Anteater items, and configurable-parent matching to an arbitrary variant. Wrong-date, GraphQL failure, cache preservation, cancellation, schedules and missing-period discovery are covered.
- No physical-device verification performed. Connected physical phone was not modified. No Room schema change.

APK SHA-256: `9070d4dcaef9d218d81ee9dac8888293f86d3dd9ef08832f7013f6ba01039ca2`.

The artifact is a debug-signed APK for sideloading, not a Play Store release. Direct UCI schema changes or availability can affect freshness; cached menus and saved diaries remain available with warnings.
