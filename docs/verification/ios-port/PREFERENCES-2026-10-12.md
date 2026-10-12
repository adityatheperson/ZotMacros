# Immediate settings persistence

Preferred hall, default meal, auto-refresh and haptic settings now save immediately. Numeric goals retain explicit validation/save, and save only goal fields so they cannot overwrite a preference change. The preferred-hall UI regression failed before the fix and passed afterward: selecting Brandywine, navigating to Menu, returning to Settings, and terminating/relaunching all preserve the selection. Xcode test run on iPhone 18 Pro iOS 27 reported TEST SUCCEEDED (1 selected test), log /private/tmp/zot-settings-save-green.log. No physical-device verification.

This commit also saves the previously verified user-requested logo header, blue settings surfaces, gold dining-hall selector and meal tabs. Native navigation, hall switching, meal switching, and large-text keyboard/save checks passed in their respective targeted runs on October 11.
