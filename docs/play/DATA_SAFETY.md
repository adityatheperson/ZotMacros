# Data safety answers — ZotMacros Android (play: `com.zotmacros`, v1.0.2+)

Fill these into Play Console → Policy → App content → Data safety. Rationale in brackets; re-check if networking or SDKs change.

## Overview
- Does your app collect or share any of the required user data types? **Yes**
- Is all user data collected by your app encrypted in transit? **Yes** (HTTPS only; `usesCleartextTraffic="false"`)
- Do you provide a way for users to request that their data is deleted? **Yes** (Settings → Clear local food history deletes the diary; goals/preferences are editable; uninstalling removes everything. Nothing is retained server-side by the developer.)
- Independent security review: **No**

## Collected: Device or other IDs → IP addresses
- Collected: **Yes** — any HTTPS menu request exposes the device IP to the menu server, as with any network use. No identifier, account, or diary data is sent.
- Shared: **Yes** — menu requests go to the Anteater API (`anteaterapi.com`, independent student-run menu service). Purpose: **App functionality**. No other recipients.
- Ephemeral: **No**. Required or optional: **Required** (menus cannot load without it).

## Everything else: No
- Location (precise or approximate): **No** — never requested or read.
- Personal info (name, email, phone, address): **No** — no accounts, no sign-in.
- Photos, videos, audio, files: **No**.
- Health and fitness: **No declaration** — the food diary, goals, and preferences are stored **only on this device** (Room/DataStore, backup disabled) and never transmitted. On-device-only data is not declared.
- App activity, contacts, messages, calendar: **No**.
- Financial info, credentials: **No**.

## Notes for review accuracy
- Permissions in the manifest: `INTERNET` only. No runtime permissions, no background location, no sensors, no Health Connect, no ads/analytics/crash SDKs. Verify with `aapt dump permissions` on the release AAB before each submission.
- Privacy policy URL must be live before submitting (see `website/privacy.html`; host it, e.g. GitHub Pages, then use that URL in the listing AND the in-app Settings link `PrivacyPolicyUrl`).
