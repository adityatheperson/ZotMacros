# Store listing — ZotMacros Android

## Basics
- App name: **ZotMacros**
- Package: `com.zotmacros` (cannot change after first upload — confirm before creating the Play app)
- Category (recommended): **Food & Drink** (dining-hall menus + food diary; avoids Health & Fitness category scrutiny. No Health Connect, sensors, or medical claims anywhere.)
- Tags: nutrition, dining hall, UCI, macro tracker, student
- Contact: app support email (required) + privacy policy URL (required, see `website/privacy.html` hosted)

## Short description (≤80 chars)
Track UCI dining-hall macros. Log meals, hit your goals. No account needed.

## Full description (≤4000 chars)
ZotMacros is a nutrition tracker built for UC Irvine students. Browse live Anteatery and Brandywine menus, log what you eat in a few taps, and watch calories, protein, carbs, and fats update instantly.

- Real dining-hall menus with stations, search, and serving details
- Fast food logging with adjustable servings and per-meal diary
- Calorie ring, macro progress, and daily micronutrients
- History with day picker plus 7-day calorie and protein charts
- Personal goals with clearly labeled example defaults
- Works on all modern Android phones (Android 8.0+), portrait and landscape
- No account. Your diary stays on your device and works offline.

Unknown nutrition is shown as unavailable — never guessed. Missing values are marked, and partial totals are labeled. ZotMacros is an independent student app, not an official UCI app and not medical advice; confirm ingredients and allergens with dining staff. Menu data from the Anteater API (ICSSC).

## Graphics (in `docs/play-listing/`)
- `icon-512.png` — 512×512 app icon (downscaled from `website/assets/logo.png`, matches launcher icon)
- `feature-graphic-1024x500.png` — 1024×500 feature graphic (brand colors, tagline, live Today capture)
- Phone screenshots: `01-today.png`, `02-menu.png` (live Sunday brunch), `03-foodsheet.png`, `04-today-logged.png`, `05-history.png`, `06-settings.png` — all native Pixel 8 Pro captures
- Notch cutout/edge-to-edge verified on small (Galaxy S25) and large (Pixel 8 Pro) screens

## Content rating (IARC questionnaire — expected outcome: Everyone)
No violence, sexual content, profanity, controlled substances, gambling, or user communication. No user-generated content sharing (diary is private/on-device). No ads. Answer each question literally; do not over-declare.

## Target audience & content
- Target age group: **18 and over** (UCI students). Do NOT opt into Designed for Families.
- Contains ads: **No**
- App access (login): **Not required** — reviewers can use every screen with internet only; no demo account needed.
- Health apps declaration: complete for **all** apps — declare **no Health Connect, no body sensors, no Google Fit**; manual nutrition diary only.

## Release
- Format: signed release **AAB** (Play App Signing enabled; keep `~/.keystores/zotmacros-upload.keystore` + keychain password backed up off-machine)
- versionCode must increase with every upload (currently 3)
- New personal accounts: closed test with **12+ testers for 14 consecutive days** before production access can be requested. Start recruiting testers early — this is the critical path.
