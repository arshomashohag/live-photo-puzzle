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
