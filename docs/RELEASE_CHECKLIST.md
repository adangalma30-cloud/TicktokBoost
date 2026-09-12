# TickTokBoost — Release / Publishing Checklist

## Where we are (v0.0.13)

- ✅ Functional MVP with 26 passing tests across 5 suites
- ✅ Clean versioning: branch-per-release (`0.0.1`…`0.0.13`), `main` untouched
- ✅ CI (Workflow1 repo) builds + signs every release with the project keystore
- ✅ R8-minified APK (~2.2 MB)
- ✅ Privacy policy, store listing copy, data-safety answers (this folder)

## Before Play Store submission

1. **Play Console account** — $25 one-time fee; needs the developer's identity.
2. **Release keystore (IMPORTANT)** — Play requires an upload keystore. The current
   signing key is a shared *demo* keystore stored as a CI secret (fine for
   testing, wrong for production). Generate a private release keystore:
   ```
   keytool -genkeypair -v -keystore ticktokboost-release.keystore \
     -alias ticktokboost -keyalg RSA -keysize 4096 -validity 10000
   ```
   Keep it offline + backed up. Then either:
   - enable **Play App Signing** (recommended: Google holds the app signing key,
     you keep the upload key), or
   - rotate the `ANDROID_KEYSTORE_BASE64` CI secret to the new upload keystore.
3. **Host the privacy policy** at a public URL (GitHub Pages on this repo works:
   `docs/PRIVACY_POLICY.md` → `https://adangalma30-cloud.github.io/TicktokBoost/privacy`).
4. **Screenshots** — 4–8 phone screenshots (min 320px, max 3840px, 16:9 or 9:16):
   Home (dark + light), Discover with match chips, a creator profile with content,
   Earn/quests, Boost tiers, Premium comparison.
5. **Feature graphic** — done: `art/feature-graphic.png` (1024×500).
6. **Content rating** — complete the IARC questionnaire (answers in DATA_SAFETY.md).
7. **Remove demo affordances before public launch** (product decision):
   - Admin dashboard entry (Settings → Admin) — gate behind a hidden developer
     gesture or remove from release builds.
   - "Simulate friend joining (demo)" buttons in referrals.
   - Mock billing disclaimers are already honest — keep until real billing.
8. **Backend decision** — current app is local-only by design. Before promising
   multi-user features (real confirmations from other users, real referrals),
   the backend must exist; otherwise keep messaging honest ("demo community").

## Build & roll out

- Internal testing track first → Closed testing → Open testing → Production.
- Staged rollout (start 10%) and watch ANRs/crashes in Play Console
  (Crashlytics not integrated; Play's native crash reporting covers basics).
