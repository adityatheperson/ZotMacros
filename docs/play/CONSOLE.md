# Play launch checklist — ZotMacros Android

## Code & build (done in repo)
- [x] `targetSdk`/`compileSdk` 36 (Play requires 36+ for new apps since Aug 31, 2026)
- [x] `minSdk` 26, HTTPS-only, `INTERNET`-only permission, no runtime permissions
- [x] 16 KB page-size alignment verified on arm64 `.so` files in the release AAB
- [x] Signed release AAB builds (`bundleRelease`; upload keystore outside git, password in keychain)
- [x] In-app privacy policy link (Settings → Privacy policy; constant `PrivacyPolicyUrl`)
- [x] Device suite (6 tests, 0 failures) + visual launch verified on the API 36 emulator (`Pixel8Pro36`)
- [x] `website/privacy.html` drafted — must be hosted before submission
- [x] No accounts/ads/analytics/crash SDKs; diary on-device only with in-app deletion
- [x] Phone screenshots for this release (`docs/play-listing/`: Today, live Menu, FoodSheet, logged Today, History, Settings)
- [x] 512px icon + 1024×500 feature graphic (`docs/play-listing/`)

## Play Console account (user)
- [ ] Register ($25 one-time, personal) + complete ID verification. Organization accounts need a D-U-N-S number instead and skip the closed-test gate below.
- [ ] Create app: name ZotMacros, package `com.zotmacros`, category Food & Drink, free, no ads.
- [ ] Enable Play App Signing; upload the signed AAB to a **closed testing** track first.

## App content forms (user, answers prepared)
- [ ] Privacy policy URL (hosted `privacy.html`)
- [ ] Data safety → see `docs/play/DATA_SAFETY.md`
- [ ] Health apps declaration → no Health Connect/sensors (all apps must complete it)
- [ ] Content rating questionnaire → expected Everyone
- [ ] Target audience 18+, no Families; App access: no login; Ads: no
- [ ] Store listing copy + graphics → see `docs/play/LISTING.md`

## Critical path (user)
1. Recruit **12+ testers** (real Google accounts) on the closed track, keep them opted in **14 consecutive days**.
2. Apply for production access; upload release to production; monitor pre-launch report + staged rollout.
