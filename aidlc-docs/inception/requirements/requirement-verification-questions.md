# Phase 5 (Release & Hardening) — Requirement Verification Questions

Status: ANSWERED (via session AskUserQuestion + clarification). Recorded here
for the audit trail.

## Q1. Release target / output
A) Play Store AAB (signed)  ← **[Answer]: A**
B) Signed release APK
C) Hardening only, no signing

## Q2. Keystore
A) Create a new keystore  ← **[Answer]: A**
B) I have one already
C) Skip signing for now

## Q3. R8 minification + resource shrinking
A) Standard R8 + resource shrink
B) Full R8 (obfuscation on)
C) No minification yet
**[Answer]: Conditional — "if it's a requirement from Google then Standard R8,
else don't minify yet." Resolved to C (No minification yet): Google Play does
NOT require R8/minify/shrink for AAB uploads. Play requires AAB + targetSdk 35 +
signing + 64-bit, all otherwise satisfied. Lower-risk first release.**

## Q4. Dependency vulnerability scan (SECURITY-10)
A) OWASP Dependency-Check (Gradle plugin + documented task)  ← **[Answer]: A**
B) Document manual scan step

## Q5. Version
A) Keep versionCode 1 / versionName "1.0"
B) Bump to versionName "1.0.0" (versionCode 1)  ← **[Answer]: B**

## Follow-up analysis (ambiguities)
- Q3 was conditional; resolved by verifying Play's actual requirements (AAB, not
  minification). No remaining ambiguity.
- No other ambiguities: keystore creation involves passwords the USER supplies
  and keeps (never generated or held by the assistant, never committed —
  SECURITY-12).
