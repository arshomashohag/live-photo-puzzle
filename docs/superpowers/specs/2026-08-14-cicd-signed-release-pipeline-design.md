# CI + Signed-AAB Release Pipeline — Design

**Date:** 2026-08-14
**Status:** Approved (design); pending implementation
**Scope:** GitHub Actions automation for Tessera — continuous checks,
signed release-bundle builds for manual Play upload, and public hosting
of the privacy policy and release notes.

---

## 1. Goal

Reduce the manual steps in shipping Tessera to Google Play, **without**
publishing to Play automatically. Specifically:

- Run tests + lint automatically on every change.
- On a version tag, produce a **signed AAB** attached to a GitHub
  Release, ready for the maintainer to download and upload to the Play
  Console manually.
- Publish the **privacy policy** at a stable public URL (required by
  Play) and surface **release notes** the maintainer can paste into the
  Play "What's new" field.

Explicitly **not** in scope: uploading to the Play Developer API,
service-account setup, production/promotion automation, and running the
OWASP dependency-check in CI (it stays a local pre-release step per
`RELEASE_CHECKLIST.md`).

---

## 2. Current state (verified)

- No `.github/workflows/` directory exists yet.
- Repo remote: `arshomashohag/live-photo-puzzle` (GitHub).
- `app/build.gradle.kts` signing config reads `keystore.properties`
  from the repo root; that file is gitignored and absent in CI. When it
  is absent the release build is left unsigned (existing fallback).
- `versionCode = 1`, `versionName = "1.0.0"` are hardcoded.
- Build environment: JDK 21 toolchain, Java-17 bytecode, `minSdk 29`,
  `compileSdk`/`targetSdk 35`. 53 unit tests, lint clean.
- `PRIVACY.md` exists at repo root (plain-language, no-data-collection
  policy) and is intended to be hosted at a public URL.

---

## 3. Architecture — three workflows

### 3.1 `ci.yml` — continuous checks

- **Trigger:** `push` and `pull_request` targeting `main`.
- **Runner:** `ubuntu-latest`.
- **Steps:**
  1. `actions/checkout@v4`
  2. `actions/setup-java@v4` (Temurin, JDK 21)
  3. `gradle/actions/setup-gradle@v4` (dependency + build cache)
  4. `./gradlew testDebugUnitTest lintDebug`
- **Secrets:** none. Runs on forks/PRs safely.
- **Purpose:** fast feedback; the quality gate for `main`.

### 3.2 `release.yml` — signed release bundle

- **Trigger:** `push` of a tag matching `v*` (e.g. `v1.0.1`).
- **Runner:** `ubuntu-latest`.
- **Permissions:** `contents: write` (to create the GitHub Release).
- **Steps:**
  1. Checkout, setup-java (JDK 21), setup-gradle.
  2. **Reconstruct signing material** from secrets (see §4).
  3. Compute version values:
     - `VERSION_NAME` = tag with leading `v` stripped (`v1.0.1` →
       `1.0.1`).
     - `VERSION_CODE` = `github.run_number` (monotonic, unique).
  4. `./gradlew :app:bundleRelease` with `VERSION_CODE` / `VERSION_NAME`
     exported as environment variables.
  5. Confirm the AAB is signed (grep `META-INF` for `*.RSA`/`*.SF`).
  6. Extract the CHANGELOG section for this version (see §6).
  7. Create a GitHub Release via `softprops/action-gh-release@v2`:
     - `tag_name` = the pushed tag
     - `body` = extracted changelog section
     - `files` = `app/build/outputs/bundle/release/app-release.aab`
- **Output:** a GitHub Release with the signed AAB attached and the
  release notes as its body.

### 3.3 `pages.yml` — public privacy policy + notes

- **Trigger:** `push` to `main` affecting `PRIVACY.md`, `CHANGELOG.md`,
  or the pages workflow itself; plus `workflow_dispatch`.
- **Runner:** `ubuntu-latest`.
- **Permissions:** `pages: write`, `id-token: write`.
- **Behaviour:**
  1. Convert `PRIVACY.md` and `CHANGELOG.md` to HTML with a small,
     dependency-free inline script (or a minimal Markdown action), and
     write a plain `index.html` that links to `privacy.html` and
     `changelog.html`. No site generator (Jekyll/Hugo) — a static
     three-file output keeps the workflow trivial and the URL stable.
  2. Upload the output via `actions/upload-pages-artifact@v3` and
     deploy via `actions/deploy-pages@v4`.
- **Output:** a stable public URL, e.g.
  `https://arshomashohag.github.io/live-photo-puzzle/privacy.html`, to
  paste into the Play Console. The repo is already public, so exposing
  the policy publicly introduces nothing new.

---

## 4. Signing in CI (no committed secrets)

The existing signing config reads `keystore.properties`. Rather than
change that logic, `release.yml` **recreates those inputs from GitHub
Actions Secrets** at runtime:

| Secret | Contents |
|---|---|
| `KEYSTORE_BASE64` | The upload `.jks` file, base64-encoded |
| `KEYSTORE_PASSWORD` | Keystore (store) password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |

Release job (conceptually):

```bash
echo "$KEYSTORE_BASE64" | base64 --decode > "$RUNNER_TEMP/upload.jks"
cat > keystore.properties <<EOF
storeFile=$RUNNER_TEMP/upload.jks
storePassword=$KEYSTORE_PASSWORD
keyAlias=$KEY_ALIAS
keyPassword=$KEY_PASSWORD
EOF
```

- The keystore lives only in the ephemeral runner's temp dir; the
  runner is destroyed after the job.
- GitHub masks secret values in logs automatically.
- No keystore, password, or `keystore.properties` is ever committed;
  `.gitignore` already excludes `*.jks` and `keystore.properties`.
- The maintainer creates the keystore and holds all passwords; this
  design never generates or stores them. It only consumes the four
  secret values the maintainer sets in the repo.

---

## 5. Version injection

`app/build.gradle.kts` is changed so the version is overridable from the
environment, keeping today's values as the local fallback:

```kotlin
versionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1
versionName = System.getenv("VERSION_NAME") ?: "1.0.0"
```

- **Local builds:** no env vars set → `versionCode 1`, `versionName
  "1.0.0"` exactly as today. No behaviour change for developers.
- **CI release builds:** `VERSION_CODE` = run number (always increases,
  so Play never rejects a reused code), `VERSION_NAME` = the tag.

This is the only production-code change in the design.

---

## 6. Release notes source — `CHANGELOG.md`

- A `CHANGELOG.md` is added at the repo root in Keep-a-Changelog style,
  seeded with a `1.0.0` entry.
- Each release section is headed by its version, e.g. `## [1.0.1]`.
- `release.yml` extracts the section whose version matches the tag and
  uses it as the GitHub Release body. The same text is what the
  maintainer pastes into Play's "What's new".
- If no matching section is found, the workflow falls back to a generic
  body ("Release <tag>") and does not fail the build — the AAB is the
  critical artifact.

Maintainer flow per release:
1. Add a `## [x.y.z]` section to `CHANGELOG.md`, commit to `main`.
2. `git tag vx.y.z && git push origin vx.y.z`.
3. CI builds the signed AAB and creates the Release with those notes.

---

## 7. New and changed files

**New:**
- `.github/workflows/ci.yml`
- `.github/workflows/release.yml`
- `.github/workflows/pages.yml`
- `CHANGELOG.md` (seeded with `1.0.0`)
- `docs/CICD_SETUP.md` (secret + Pages setup instructions)

**Changed:**
- `app/build.gradle.kts` (env-overridable `versionCode`/`versionName`)

---

## 8. Maintainer one-time setup (documented in `docs/CICD_SETUP.md`)

1. Create the upload keystore locally (own the passwords):
   `keytool -genkeypair -v -keystore upload.jks -alias tessera \
   -keyalg RSA -keysize 2048 -validity 10000`.
2. Base64-encode it: `base64 -i upload.jks | pbcopy`.
3. Add four repo secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`,
   `KEY_ALIAS`, `KEY_PASSWORD`.
4. Enable GitHub Pages for the repo (Settings → Pages → Source: GitHub
   Actions).
5. Keep the keystore backed up somewhere safe — losing it means Play
   updates become impossible.

CI cannot perform these; they are inherently maintainer-side.

---

## 9. Third-party actions

- `actions/checkout@v4`
- `actions/setup-java@v4` (Temurin JDK 21)
- `gradle/actions/setup-gradle@v4`
- `softprops/action-gh-release@v2` (create Release + attach AAB)
- `actions/configure-pages@v5`, `actions/upload-pages-artifact@v3`,
  `actions/deploy-pages@v4`

All are widely used, actively maintained actions pinned by major
version.

---

## 10. Error handling

- **Missing signing secrets:** `bundleRelease` would produce an
  unsigned bundle (existing fallback). `release.yml` verifies signature
  presence (§3.2 step 5) and **fails** the job if the AAB is unsigned,
  so a broken/unsigned artifact is never published to a Release.
- **CHANGELOG section missing:** non-fatal; generic body used (§6).
- **CI job failures (tests/lint):** block the merge signal on `main`;
  do not affect release tags (which run `release.yml`, not `ci.yml`).
- **Pages deploy failure:** independent of releases; does not block the
  AAB build.

---

## 11. Testing / verification

- **`ci.yml`:** verified by the fact that the same commands
  (`testDebugUnitTest`, `lintDebug`) already pass locally (53/53, lint
  0). Confirm green on a real push.
- **`release.yml`:** verify end-to-end by pushing a throwaway tag (e.g.
  `v0.0.1-ci-test`) once secrets are configured, confirming: the AAB
  builds, the signature check passes, and the Release is created with
  the AAB attached. Delete the test tag/release afterward.
- **`pages.yml`:** confirm the published URL renders `PRIVACY.md`.
- **Version injection:** confirm a local `./gradlew` build still reports
  `versionCode 1` / `versionName 1.0.0` (fallback intact), and that a CI
  build reports the run number / tag.

---

## 12. Out of scope (YAGNI)

- Uploading to the Play Developer API / service-account credentials.
- Production track or track promotion automation.
- OWASP dependency-check in CI (remains a local pre-release step).
- Auto-tagging, auto-versioning of `CHANGELOG.md`, or commit-based
  changelog generation.
- R8/minification (unchanged; off this cycle).
