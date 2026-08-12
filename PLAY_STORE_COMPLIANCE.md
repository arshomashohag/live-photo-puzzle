# Google Play Compliance — Tessera

This document maps Tessera to Google Play's submission requirements. It reflects
the app as built (verified against the manifest and dependencies): fully
offline, no data collection.

## App bundle & technical requirements

| Requirement | Status |
|-------------|--------|
| Android App Bundle (.aab), not APK | ✅ `./gradlew :app:bundleRelease` |
| Signed with an upload key | ✅ via gitignored `keystore.properties` (see `docs/RELEASE_SIGNING.md`) |
| `targetSdk` meets current Play minimum | ✅ `targetSdk = 35` |
| `minSdk` | `29` (Android 10) |
| 64-bit support | ✅ Kotlin/JVM app; no native 32-bit-only libs |
| `versionName` / `versionCode` | `1.0.0` / `1` (bump `versionCode` each upload) |

## Data Safety form answers

Fill the Play Console Data Safety section as follows.

- **Does your app collect or share any of the required user data types?** → **No.**
- **Data collected**: **None.**
- **Data shared**: **None.**
- **Is all user data encrypted in transit?** → Not applicable — the app makes no
  network connections (no `INTERNET` permission).
- **Do you provide a way for users to request data deletion?** → Data is stored
  only on-device; users delete custom puzzles in-app or by uninstalling.

Rationale (for your reference): the app declares **no `INTERNET` permission** and
bundles **no analytics, advertising, or crash-reporting SDKs**. Camera photos and
settings are stored only in app-private storage and are never transmitted.

## Permissions declared & justification

| Permission | Why it's needed |
|------------|-----------------|
| `android.permission.CAMERA` | Capture a photo to create a custom puzzle. Used only when the user initiates capture. |
| `android.permission.VIBRATE` | Haptic feedback during play; user-toggleable in settings. |

No location, contacts, microphone, storage-scope, or network permissions are
requested.

## Privacy policy

A privacy policy is required for the listing. Use [PRIVACY.md](PRIVACY.md) —
**host it at a public URL** (e.g. GitHub Pages or a gist) and enter that URL in
the Play Console. Contact: shohagsiraj.ru@gmail.com.

## Content rating

- Casual puzzle game; no violence, no user-generated content shared with others,
  no in-app purchases, no ads.
- Complete the Play **content rating questionnaire**; expected outcome is a
  suitable-for-all / "Everyone" rating. Answer truthfully in the questionnaire —
  this document is guidance, not the rating itself.

## Ads & monetization

- **No ads.** **No in-app purchases.** Declare accordingly in the Play Console.

## Pre-launch checklist reference

See [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) for the ordered steps to build,
verify, and upload.
