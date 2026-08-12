# Release Checklist — Tessera

Run through this in order before every Play Store upload. Commands assume the
environment from [README.md](README.md) is set (`JAVA_HOME` → JDK 21,
`ANDROID_HOME`).

## 1. Version

- [ ] Bump `versionCode` in `app/build.gradle.kts` (must be **higher** than the
      last uploaded build — Play rejects a reused `versionCode`).
- [ ] Set `versionName` to the user-facing version (e.g. `1.0.0`).

## 2. Quality gates

- [ ] `./gradlew :app:testDebugUnitTest` → all tests pass (currently 53).
- [ ] `./gradlew :app:lintDebug` → 0 errors.
- [ ] `./gradlew dependencyCheckAnalyze` → review
      `build/reports/dependency-check-report.html`; no unaddressed high/critical
      CVEs. (First run downloads the NVD database and is slow.)

## 3. Signing

- [ ] `keystore.properties` exists at repo root and points to your real
      `.jks` (see [docs/RELEASE_SIGNING.md](docs/RELEASE_SIGNING.md)).
- [ ] Confirm the keystore is **backed up** somewhere safe — losing it means you
      can never update this listing.

## 4. Build the signed bundle

- [ ] `./gradlew :app:bundleRelease`
- [ ] Output exists: `app/build/outputs/bundle/release/app-release.aab`
- [ ] Verify it is signed:
      `unzip -l app/build/outputs/bundle/release/app-release.aab | grep META-INF`
      → shows `TESSERA.RSA` / `TESSERA.SF`.

## 5. Store listing & compliance

- [ ] Privacy policy hosted at a public URL and entered in the Play Console
      (source: [PRIVACY.md](PRIVACY.md)).
- [ ] **Data Safety** form completed: no data collected, no data shared
      (see [PLAY_STORE_COMPLIANCE.md](PLAY_STORE_COMPLIANCE.md)).
- [ ] **Content rating** questionnaire completed.
- [ ] Ads declaration: **no ads**. In-app purchases: **none**.
- [ ] App name, description, screenshots, and feature graphic uploaded.

## 6. Upload

- [ ] Upload `app-release.aab` to the desired track (internal → closed → open →
      production).
- [ ] Confirm Play App Signing is enabled (Play adds its signing key on top of
      your upload key).

## 7. Post-upload

- [ ] Review the pre-launch report in the Play Console for crashes/warnings.
- [ ] Tag the release in git once published.

## Notes

- **R8/minification is off** this cycle (not a Play requirement). If you enable
  it later, re-test thoroughly and add ProGuard/R8 keep rules as needed.
