# Motion refinement — October 9, 2026

Scope: preserve the approved blue-and-gold graphics. Shorten tab transitions to 180ms in / 120ms out, use a shared 280ms non-bouncy progress tween, and keep food-sheet content mounted until Material's hide animation completes. Closing disables actions without replacing the button label with a spinner. Compose animations inherit the Android duration scale.

Verification:
- `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest --offline` passed with JDK 17 and SDK 35. An initial native-test compilation failure from the exposed experimental SheetState type was fixed by opting the existing direct-sheet test into that API.
- Native regression initially failed because the sheet vanished on the first frame after Close. With FoodSheetHost, the same test passes, including removal after the transition, reopening, and saving.
- Installed APKs on Pixel emulator `emulator-5554`; `adb ... shell am instrument -w com.zotmacros.test/androidx.test.runner.AndroidJUnitRunner` passed. Six test cases reported; the opt-in historical live-menu test was skipped. Five tests executed, including diary persistence, motion, navigation, and unknown nutrition.
- Navigation and unknown-nutrition tests passed again with animator_duration_scale=0 and font_scale=1.5. Restored font_scale=1.0 and removed the animator-duration override (originally unset).
- Inspected live native food-sheet screenshot (`sheet.png`) and exercised repeated tab changes. No physical-device smoothness or frame-rate claim.

Release artifact: `artifacts/ZotMacros-1.0.2-debug.apk` (debug-signed). Version code increased to 3; unit tests, lint and assembly passed again for 1.0.2 before publication. Existing nutrition/menu behavior and diary schema are unchanged.
