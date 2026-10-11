# Title and date refinement

Shared safe-area headers use bold rounded system title typography, left aligned, with a compact campus date on the right for Today and Menu. History retains its interactive date picker on the right. A ViewThatFits vertical fallback preserves space for larger Dynamic Type. Redundant Today brand/date rows were removed.

Xcode 27.0, iPhone 18 Pro iOS 27 simulator: built with the ZotMacros scheme and derived data at /private/tmp/zot-ios-derived. Initial selected test run passed navigation and large-text keyboard/save retry (2 tests). After vertically centering the History picker, the navigation confirmation passed again. Both xcodebuild runs reported TEST SUCCEEDED. Used -collect-test-diagnostics never to avoid the prior unrelated simulator diagnostic collection timeout. Native screenshots reviewed; no physical-device check. Updated production app installed and launched in DeviceHub.

Final screenshots: [Today](today.png), [Menu](menu.png), [History](history.png), [Settings](settings.png). Screenshots contain test-host fixture foods.
