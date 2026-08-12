# Release Signing & AAB Build

This app is released to Google Play as a **signed Android App Bundle (.aab)**.
Signing is driven by a **gitignored** `keystore.properties` at the repo root, so
no keystore or password ever enters version control.

> **You own the keystore and its passwords.** They are never generated, stored,
> or transmitted by any tooling in this repo. If you lose the keystore, you can
> never publish an update to the same Play listing — back it up securely.

## 1. Generate the release keystore (one time)

Run this from the repo root. `keytool` will **prompt** for the store and key
passwords — choose strong ones and store them in your password manager. They are
never passed on the command line.

```bash
keytool -genkeypair -v \
  -keystore release.jks \
  -alias tessera \
  -keyalg RSA -keysize 4096 -validity 10000
```

- `-validity 10000` (~27 years) comfortably exceeds Play's requirement that the
  key be valid until at least 2033.
- The alias `tessera` must match `keyAlias` in `keystore.properties`.
- `release.jks` is gitignored (`*.jks`).

## 2. Create keystore.properties (one time)

Copy the committed template and fill in your real values:

```bash
cp keystore.properties.template keystore.properties
```

Then edit `keystore.properties` (gitignored) so it reads:

```properties
storeFile=release.jks
storePassword=<the store password you chose>
keyAlias=tessera
keyPassword=<the key password you chose>
```

`storeFile` is resolved relative to the repo root. Put `release.jks` there, or
use an absolute path to a keystore kept outside the repo.

## 3. Build the signed AAB

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) \
ANDROID_HOME=/opt/homebrew/share/android-commandlinetools \
./gradlew :app:bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab` — upload this to the
Play Console.

If `keystore.properties` is **absent**, the release build still succeeds but is
**unsigned** (so a fresh clone/CI without the keystore builds without leaking or
requiring secrets). An unsigned bundle cannot be uploaded to Play — create the
keystore first.

## 4. Dependency vulnerability scan

```bash
./gradlew dependencyCheckAnalyze
```

Report: `build/reports/dependency-check-report.html`. Review before each
release. (Report-only; it does not fail the build in this cycle.)

## Notes

- **R8/minification is intentionally OFF** this cycle. Google Play does not
  require it; it is deferred to a later hardening pass to avoid a first-release
  runtime break.
- Play adds its own **app-signing key** on top of your upload key (Play App
  Signing). The keystore here is your **upload key**.
