# CI + Signed-AAB Release Pipeline Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add GitHub Actions automation that runs tests/lint on every change, builds a signed release AAB on version tags and attaches it to a GitHub Release, and publishes the privacy policy + changelog to a public GitHub Pages URL — with no automatic upload to Google Play.

**Architecture:** Three independent workflow files under `.github/workflows/`. `ci.yml` runs the existing Gradle test/lint gate. `release.yml` reconstructs signing material from repo secrets, injects version values from the run number and git tag, builds the signed AAB, verifies its signature, and creates a GitHub Release with the AAB attached and notes pulled from `CHANGELOG.md`. `pages.yml` renders `PRIVACY.md` and `CHANGELOG.md` to static HTML and deploys to GitHub Pages. One small production-code change makes `versionCode`/`versionName` overridable from the environment.

**Tech Stack:** GitHub Actions, Gradle (Kotlin DSL), Android AAB (`bundleRelease`), Temurin JDK 21, `softprops/action-gh-release`, `actions/deploy-pages`.

**Spec:** `docs/superpowers/specs/2026-08-14-cicd-signed-release-pipeline-design.md`

## Global Constraints

- **Build env:** Temurin **JDK 21** toolchain; app compiles to Java-17 bytecode. `minSdk 29`, `compileSdk`/`targetSdk 35`.
- **Version fallback (must remain exact):** with no env vars set, a build must still report `versionCode = 1` and `versionName = "1.0.0"` — identical to current behaviour.
- **Secrets are never committed.** `.gitignore` already excludes `*.jks` and `keystore.properties`. The keystore and all passwords are maintainer-owned; the pipeline only consumes secret values, never generates them.
- **No Play API / auto-publish.** The pipeline stops at a downloadable signed AAB. No service account, no track upload.
- **No dependency-check in CI.** OWASP `dependencyCheckAnalyze` stays a local pre-release step (see `RELEASE_CHECKLIST.md`).
- **Repo:** `arshomashohag/live-photo-puzzle` (GitHub Pages base URL will be `https://arshomashohag.github.io/live-photo-puzzle/`).
- **Commit style:** Commitizen format, no Claude co-author line, stage specific files only (never `git add .`), never `--no-verify`.
- **Action pinning:** third-party actions pinned by major version tag (e.g. `@v4`).

---

### Task 1: Make versionCode/versionName environment-overridable

The only production-code change. Must preserve the exact `1` / `1.0.0` fallback so local and developer builds are unaffected.

**Files:**
- Modify: `app/build.gradle.kts:29-30`

**Interfaces:**
- Consumes: nothing from earlier tasks.
- Produces: the release build now honours two environment variables —
  `VERSION_CODE` (integer string) → `versionCode`, and
  `VERSION_NAME` (string) → `versionName`. `release.yml` (Task 3) sets these.

- [ ] **Step 1: Read the current version lines**

Confirm `app/build.gradle.kts` currently contains, inside `defaultConfig`:

```kotlin
        versionCode = 1
        versionName = "1.0.0"
```

- [ ] **Step 2: Replace them with env-overridable versions**

Change those two lines to:

```kotlin
        versionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1
        versionName = System.getenv("VERSION_NAME") ?: "1.0.0"
```

- [ ] **Step 3: Verify the local fallback is unchanged**

Run:

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) \
ANDROID_HOME=/opt/homebrew/share/android-commandlinetools \
./gradlew -q :app:properties | grep -E 'versionCode|versionName'
```

Expected: `versionCode: 1` and `versionName: 1.0.0` (no env vars set → fallback intact).

- [ ] **Step 4: Verify the override works**

Run:

```bash
VERSION_CODE=42 VERSION_NAME=9.9.9 \
JAVA_HOME=$(/usr/libexec/java_home -v 21) \
ANDROID_HOME=/opt/homebrew/share/android-commandlinetools \
./gradlew -q :app:properties | grep -E 'versionCode|versionName'
```

Expected: `versionCode: 42` and `versionName: 9.9.9`.

- [ ] **Step 5: Commit**

```bash
git add app/build.gradle.kts
git commit -m "build(app): make versionCode/versionName env-overridable"
```

---

### Task 2: CI workflow (tests + lint)

Fast feedback gate on `main`. No secrets, so it's safe on PRs/forks. This is the lowest-risk workflow — land it first so the repo has green checks before any signing logic exists.

**Files:**
- Create: `.github/workflows/ci.yml`

**Interfaces:**
- Consumes: nothing.
- Produces: a status check named `build` on pushes/PRs to `main`.

- [ ] **Step 1: Create the CI workflow file**

Create `.github/workflows/ci.yml`:

```yaml
name: CI

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Run unit tests and lint
        run: ./gradlew testDebugUnitTest lintDebug --no-daemon
```

- [ ] **Step 2: Verify the workflow is valid YAML**

Run:

```bash
python3 -c "import yaml,sys; yaml.safe_load(open('.github/workflows/ci.yml')); print('valid yaml')"
```

Expected: `valid yaml`.

- [ ] **Step 3: Commit**

```bash
git add .github/workflows/ci.yml
git commit -m "ci: add tests and lint workflow on push and pr"
```

- [ ] **Step 4: Push and confirm the run is green**

```bash
git push
```

Expected: on GitHub → Actions tab, the `CI` workflow runs and the `build` job passes (mirrors the local 53/53 tests + lint 0). If it fails for an environment reason (e.g. missing `ANDROID_HOME`), note it — `setup-android` is not needed because the Android SDK is preinstalled on `ubuntu-latest` GitHub runners and `gradle/actions/setup-gradle` provisions it; only investigate if the run actually fails.

---

### Task 3: Seed CHANGELOG.md

Release notes source. Needed before `release.yml` (Task 4) can extract a section. Small and standalone.

**Files:**
- Create: `CHANGELOG.md`

**Interfaces:**
- Consumes: nothing.
- Produces: a `CHANGELOG.md` whose per-version sections are headed
  `## [x.y.z] - YYYY-MM-DD`. Task 4's extraction script keys on the
  `## [x.y.z]` prefix (date optional).

- [ ] **Step 1: Create CHANGELOG.md**

Create `CHANGELOG.md`:

```markdown
# Changelog

All notable changes to Tessera are documented here. This project adheres
to [Keep a Changelog](https://keepachangelog.com/) and
[Semantic Versioning](https://semver.org/).

## [1.0.0] - 2026-08-14

### Added
- Slide-tile photo puzzles with Easy / Medium / Hard difficulty levels.
- Built-in puzzle set plus custom puzzles from the camera or photo picker.
- Directional swipe controls with slide animation; adjacent-only swaps.
- Hint overlay and a full-image reveal on solve.
- Sound and haptic feedback, dark theme, and adaptive layouts.
- Fully offline: no data collected, no data shared, no network permission.
```

- [ ] **Step 2: Commit**

```bash
git add CHANGELOG.md
git commit -m "docs(changelog): seed changelog with 1.0.0 entry"
```

---

### Task 4: Release workflow (signed AAB → GitHub Release)

The core deliverable. Reconstructs signing from secrets, injects version from run number + tag, builds and signature-verifies the AAB, and publishes a Release with notes from `CHANGELOG.md`. Depends on Task 1 (env version) and Task 3 (changelog).

**Files:**
- Create: `.github/workflows/release.yml`

**Interfaces:**
- Consumes: the `VERSION_CODE`/`VERSION_NAME` env contract from Task 1;
  the `## [x.y.z]` section format from Task 3.
- Produces: on a `v*` tag, a GitHub Release tagged identically, with
  `app/build/outputs/bundle/release/app-release.aab` attached and the
  matching changelog section as its body.
- Requires (maintainer-provided secrets): `KEYSTORE_BASE64`,
  `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.

- [ ] **Step 1: Create the release workflow file**

Create `.github/workflows/release.yml`:

```yaml
name: Release

on:
  push:
    tags:
      - 'v*'

permissions:
  contents: write

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Derive version values
        id: version
        run: |
          TAG="${GITHUB_REF_NAME}"
          echo "version_name=${TAG#v}" >> "$GITHUB_OUTPUT"
          echo "version_code=${GITHUB_RUN_NUMBER}" >> "$GITHUB_OUTPUT"

      - name: Reconstruct signing material
        env:
          KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: |
          if [ -z "$KEYSTORE_BASE64" ]; then
            echo "::error::KEYSTORE_BASE64 secret is not set; cannot sign." >&2
            exit 1
          fi
          echo "$KEYSTORE_BASE64" | base64 --decode > "$RUNNER_TEMP/upload.jks"
          cat > keystore.properties <<EOF
          storeFile=$RUNNER_TEMP/upload.jks
          storePassword=$KEYSTORE_PASSWORD
          keyAlias=$KEY_ALIAS
          keyPassword=$KEY_PASSWORD
          EOF

      - name: Build signed release bundle
        env:
          VERSION_CODE: ${{ steps.version.outputs.version_code }}
          VERSION_NAME: ${{ steps.version.outputs.version_name }}
        run: ./gradlew :app:bundleRelease --no-daemon

      - name: Verify the bundle is signed
        run: |
          AAB=app/build/outputs/bundle/release/app-release.aab
          if ! unzip -l "$AAB" | grep -qE 'META-INF/.*\.(RSA|SF)'; then
            echo "::error::AAB is not signed — refusing to publish." >&2
            exit 1
          fi
          echo "Signature present."

      - name: Extract changelog section
        id: notes
        run: |
          VER="${{ steps.version.outputs.version_name }}"
          NOTES=$(awk -v ver="$VER" '
            $0 ~ "^## \\[" ver "\\]" {grab=1; next}
            grab && /^## \[/ {exit}
            grab {print}
          ' CHANGELOG.md)
          if [ -z "$NOTES" ]; then
            NOTES="Release ${GITHUB_REF_NAME}"
          fi
          {
            echo "body<<EOF"
            echo "$NOTES"
            echo "EOF"
          } >> "$GITHUB_OUTPUT"

      - name: Create GitHub Release with the AAB
        uses: softprops/action-gh-release@v2
        with:
          tag_name: ${{ github.ref_name }}
          name: ${{ github.ref_name }}
          body: ${{ steps.notes.outputs.body }}
          files: app/build/outputs/bundle/release/app-release.aab

      - name: Clean up signing material
        if: always()
        run: rm -f keystore.properties "$RUNNER_TEMP/upload.jks"
```

- [ ] **Step 2: Verify the workflow is valid YAML**

Run:

```bash
python3 -c "import yaml,sys; yaml.safe_load(open('.github/workflows/release.yml')); print('valid yaml')"
```

Expected: `valid yaml`.

- [ ] **Step 3: Verify the changelog extraction logic locally**

Run (simulating the awk step for version 1.0.0 against the real file):

```bash
awk -v ver="1.0.0" '
  $0 ~ "^## \\[" ver "\\]" {grab=1; next}
  grab && /^## \[/ {exit}
  grab {print}
' CHANGELOG.md
```

Expected: prints the `### Added` block from the 1.0.0 section (non-empty), confirming the extraction keys on the version header correctly.

- [ ] **Step 4: Commit**

```bash
git add .github/workflows/release.yml
git commit -m "ci: add signed aab release workflow on version tags"
```

---

### Task 5: Pages workflow (privacy policy + changelog)

Publishes `PRIVACY.md` and `CHANGELOG.md` as static HTML to GitHub Pages, giving a stable public privacy-policy URL for the Play listing. Independent of the release path.

**Files:**
- Create: `.github/workflows/pages.yml`

**Interfaces:**
- Consumes: `PRIVACY.md` and `CHANGELOG.md` at the repo root.
- Produces: a deployed Pages site with `index.html`, `privacy.html`,
  `changelog.html` at `https://arshomashohag.github.io/live-photo-puzzle/`.

- [ ] **Step 1: Create the pages workflow file**

Create `.github/workflows/pages.yml`. The conversion uses a tiny inline Python script (no site generator) so the output is a fixed three-file set:

```yaml
name: Pages

on:
  push:
    branches: [main]
    paths:
      - PRIVACY.md
      - CHANGELOG.md
      - .github/workflows/pages.yml
  workflow_dispatch:

permissions:
  contents: read
  pages: write
  id-token: write

concurrency:
  group: pages
  cancel-in-progress: true

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Render Markdown to static HTML
        run: |
          python3 - <<'PY'
          import html, pathlib, re

          def render(md_path, title):
              text = pathlib.Path(md_path).read_text(encoding="utf-8")
              lines, out = text.splitlines(), []
              for line in lines:
                  esc = html.escape(line)
                  if line.startswith("### "):
                      out.append(f"<h3>{esc[4:]}</h3>")
                  elif line.startswith("## "):
                      out.append(f"<h2>{esc[3:]}</h2>")
                  elif line.startswith("# "):
                      out.append(f"<h1>{esc[2:]}</h1>")
                  elif line.startswith("- "):
                      out.append(f"<li>{esc[2:]}</li>")
                  elif line.strip() == "":
                      out.append("")
                  else:
                      out.append(f"<p>{esc}</p>")
              body = "\n".join(out)
              body = re.sub(r"(<li>.*?</li>\n?)+",
                            lambda m: f"<ul>{m.group(0)}</ul>", body,
                            flags=re.S)
              return (
                  "<!doctype html><html lang=\"en\"><head>"
                  "<meta charset=\"utf-8\">"
                  "<meta name=\"viewport\" content=\"width=device-width,"
                  "initial-scale=1\">"
                  f"<title>{html.escape(title)}</title>"
                  "<style>body{max-width:44rem;margin:2rem auto;padding:0 1rem;"
                  "font-family:system-ui,sans-serif;line-height:1.6}"
                  "h1,h2,h3{line-height:1.25}</style></head><body>"
                  f"{body}</body></html>"
              )

          site = pathlib.Path("_site")
          site.mkdir(exist_ok=True)
          (site / "privacy.html").write_text(
              render("PRIVACY.md", "Tessera — Privacy Policy"),
              encoding="utf-8")
          (site / "changelog.html").write_text(
              render("CHANGELOG.md", "Tessera — Changelog"),
              encoding="utf-8")
          (site / "index.html").write_text(
              "<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">"
              "<title>Tessera</title></head><body><h1>Tessera</h1>"
              "<ul><li><a href=\"privacy.html\">Privacy Policy</a></li>"
              "<li><a href=\"changelog.html\">Changelog</a></li></ul>"
              "</body></html>", encoding="utf-8")
          print("rendered:", [p.name for p in site.iterdir()])
          PY

      - name: Upload Pages artifact
        uses: actions/upload-pages-artifact@v3
        with:
          path: _site

  deploy:
    needs: build
    runs-on: ubuntu-latest
    environment:
      name: github-pages
      url: ${{ steps.deployment.outputs.page_url }}
    steps:
      - name: Deploy to GitHub Pages
        id: deployment
        uses: actions/deploy-pages@v4
```

- [ ] **Step 2: Verify the workflow is valid YAML**

Run:

```bash
python3 -c "import yaml,sys; yaml.safe_load(open('.github/workflows/pages.yml')); print('valid yaml')"
```

Expected: `valid yaml`.

- [ ] **Step 3: Verify the render script produces the three files locally**

Run the same inline script against the real Markdown (copy the Python block from Step 1 into a local run, or):

```bash
python3 - <<'PY'
import html, pathlib, re
exec(pathlib.Path('.github/workflows/pages.yml').read_text()
     .split("python3 - <<'PY'",1)[1].rsplit('PY',1)[0])
PY
ls -1 _site && rm -rf _site
```

Expected: lists `index.html`, `privacy.html`, `changelog.html`. (If the extraction of the embedded script is awkward, instead paste the Python block directly and run it — the pass criterion is that `_site/` contains the three HTML files and `privacy.html` is non-empty.)

- [ ] **Step 4: Commit**

```bash
git add .github/workflows/pages.yml
git commit -m "ci: publish privacy policy and changelog to github pages"
```

---

### Task 6: Setup documentation

Records the maintainer-side one-time steps CI cannot perform (create keystore, add secrets, enable Pages). Folded into its own task because it is a standalone deliverable a reviewer gates independently.

**Files:**
- Create: `docs/CICD_SETUP.md`

**Interfaces:**
- Consumes: the secret names from Task 4; the Pages output from Task 5.
- Produces: human-readable setup instructions. No code contract.

- [ ] **Step 1: Create docs/CICD_SETUP.md**

Create `docs/CICD_SETUP.md`:

```markdown
# CI/CD Setup

This repo has three GitHub Actions workflows:

- **CI** (`ci.yml`) — runs unit tests + lint on every push/PR to `main`.
- **Release** (`release.yml`) — on a `v*` tag, builds a **signed** release
  AAB and attaches it to a GitHub Release. You download it and upload to
  the Play Console manually.
- **Pages** (`pages.yml`) — publishes `PRIVACY.md` and `CHANGELOG.md` to
  GitHub Pages, giving a public privacy-policy URL for the Play listing.

## One-time setup

### 1. Create your upload keystore (you own the passwords)

```bash
keytool -genkeypair -v -keystore upload.jks -alias tessera \
  -keyalg RSA -keysize 2048 -validity 10000
```

Choose a store password and key password when prompted. **Store them in a
password manager and back up `upload.jks`** — losing it means you can
never update the Play listing.

### 2. Add repo secrets

Base64-encode the keystore:

```bash
base64 -i upload.jks | pbcopy    # macOS; copies to clipboard
```

In GitHub → Settings → Secrets and variables → Actions → New repository
secret, add:

| Secret | Value |
|---|---|
| `KEYSTORE_BASE64` | the base64 string from above |
| `KEYSTORE_PASSWORD` | your keystore (store) password |
| `KEY_ALIAS` | `tessera` (or whatever alias you chose) |
| `KEY_PASSWORD` | your key password |

Nothing here is ever committed — `*.jks` and `keystore.properties` are
gitignored, and the workflow rebuilds them from these secrets at runtime.

### 3. Enable GitHub Pages

Settings → Pages → Source: **GitHub Actions**. The Pages workflow then
deploys to `https://arshomashohag.github.io/live-photo-puzzle/` with a
`privacy.html` you paste into the Play Console listing.

## Cutting a release

1. Add a `## [x.y.z] - YYYY-MM-DD` section to `CHANGELOG.md`, commit to
   `main`.
2. Tag and push:

   ```bash
   git tag vX.Y.Z
   git push origin vX.Y.Z
   ```

3. The Release workflow builds the signed AAB, verifies its signature,
   and creates a GitHub Release with the AAB attached and the changelog
   section as the notes.
4. Download `app-release.aab` from the Release and upload it in the Play
   Console. Paste the same changelog text into "What's new".

`versionCode` is set automatically from the Actions run number;
`versionName` comes from the tag. Local builds still use the `1` /
`1.0.0` fallback.

## Not automated (by design)

- Uploading to Google Play (manual — the first upload and listing must be
  done by hand anyway).
- OWASP dependency-check (run `./gradlew dependencyCheckAnalyze` locally
  with an NVD API key before a release — see `RELEASE_CHECKLIST.md`).
```

- [ ] **Step 2: Verify internal references are consistent**

Confirm the four secret names in `docs/CICD_SETUP.md` exactly match those referenced in `.github/workflows/release.yml`:

```bash
for s in KEYSTORE_BASE64 KEYSTORE_PASSWORD KEY_ALIAS KEY_PASSWORD; do
  grep -q "$s" docs/CICD_SETUP.md && grep -q "$s" .github/workflows/release.yml \
    && echo "$s OK" || echo "$s MISMATCH";
done
```

Expected: all four print `OK`.

- [ ] **Step 3: Commit**

```bash
git add docs/CICD_SETUP.md
git commit -m "docs(cicd): document keystore secrets and release steps"
```

---

### Task 7: Cross-link the new docs

Small wiring task so the CI/CD docs are discoverable from the existing docs the maintainer already reads. Gated separately because it touches existing files a reviewer may want to check independently.

**Files:**
- Modify: `RELEASE_CHECKLIST.md`
- Modify: `README.md`

**Interfaces:**
- Consumes: `docs/CICD_SETUP.md` (Task 6), `CHANGELOG.md` (Task 3).
- Produces: nothing consumed downstream.

- [ ] **Step 1: Add a CI/CD pointer to RELEASE_CHECKLIST.md**

At the end of `RELEASE_CHECKLIST.md`, before or within the `## Notes`
section, add:

```markdown
- **Automated builds:** pushing a `vX.Y.Z` tag builds the signed AAB via
  GitHub Actions and attaches it to a GitHub Release — see
  [docs/CICD_SETUP.md](docs/CICD_SETUP.md). You still upload to Play
  manually.
```

- [ ] **Step 2: Add doc-index entries to README.md**

The README's `## Documentation` section (around line 81) is a Markdown
**table** with rows like `| [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) |
pre-upload gate |`. Add two rows in that same table format:

```markdown
| [docs/CICD_SETUP.md](docs/CICD_SETUP.md) | CI, signed release builds, Pages hosting |
| [CHANGELOG.md](CHANGELOG.md) | release history |
```

- [ ] **Step 3: Verify the links resolve**

Run:

```bash
for f in docs/CICD_SETUP.md CHANGELOG.md; do
  test -f "$f" && echo "$f exists" || echo "$f MISSING";
done
grep -q 'CICD_SETUP.md' RELEASE_CHECKLIST.md && echo "checklist linked" || echo "checklist NOT linked"
grep -q 'CICD_SETUP.md' README.md && echo "readme linked" || echo "readme NOT linked"
```

Expected: both files exist; both `linked` lines print.

- [ ] **Step 4: Commit**

```bash
git add RELEASE_CHECKLIST.md README.md
git commit -m "docs: cross-link ci/cd setup and changelog"
```

---

## Self-Review

**1. Spec coverage:**
- §3.1 `ci.yml` → Task 2. ✅
- §3.2 `release.yml` (signing, version, build, verify, changelog, Release) → Task 4 (+ Task 1 version, Task 3 changelog). ✅
- §3.3 `pages.yml` → Task 5. ✅
- §4 signing from secrets → Task 4 step 1 + Task 6 docs. ✅
- §5 version injection → Task 1. ✅
- §6 CHANGELOG source + fallback → Task 3 + Task 4 (awk + `Release <tag>` fallback). ✅
- §7 file list → Tasks 1–7 cover every new/changed file. ✅
- §8 maintainer setup → Task 6. ✅
- §9 third-party actions → used in Tasks 2/4/5, pinned by major version. ✅
- §10 error handling → Task 4 (missing secret exit 1, unsigned exit 1, changelog fallback). ✅
- §11 verification → each task has explicit verify steps; version fallback checked in Task 1. ✅
- §12 out of scope → nothing in the plan adds Play API, dep-check, or promotion. ✅

**2. Placeholder scan:** No TBD/TODO/"handle edge cases"/"write tests for the above". Every code step shows actual content. ✅

**3. Type/name consistency:** Secret names (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) identical across Task 4 and Task 6. Env var names (`VERSION_CODE`, `VERSION_NAME`) identical across Task 1 and Task 4. Changelog header format `## [x.y.z]` consistent between Task 3 and Task 4's awk pattern. AAB path `app/build/outputs/bundle/release/app-release.aab` identical in Task 4 build/verify/upload. ✅
