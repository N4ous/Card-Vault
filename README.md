# Card Vault

Offline Android app for privately storing your own credit/debit card records.
Kotlin + Jetpack Compose + Material 3 + Room + Android Keystore.

## Status: Phase 1–6 (clean rebuild)

Implemented in this drop:

- Project setup, Gradle Kotlin DSL, GitHub Actions CI (debug APK)
- Compose UI: Splash, First-Time Setup, Lock, Dashboard, Card List (masked + search),
  Add Card, Card Details (reveal-on-demand), Edit Card, NFC Scan, Settings
- Room database with CRUD + safe metadata search
- AES-256-GCM encryption (Android Keystore-backed key) for card number/CVV
- PIN auth: PBKDF2 salted hash, brute-force lockout after 5 failed attempts, never plaintext
- Optional biometric unlock (BiometricPrompt, BIOMETRIC_STRONG)
- NFC: reads only tag ID / plain NDEF text as a non-sensitive identifier for local matching.
  Never assumes it can read a real bank card's PAN/CVV/PIN over NFC — Android does not expose
  that to third-party apps, and this app is explicit about it when a tag can't be used.
- FLAG_SECURE applied globally (screenshot/recording blocked)
- No internet permission, no analytics/ads SDKs, least-privilege manifest
- Unit tests for validation logic and NFC identifier parsing

## Not yet implemented (later phases per spec)

- Configurable auto-lock timeout (Phase 12) — currently locks on every cold start only
- Signed release build (Phase 16–17) — currently debug-only
- Encrypted backup/restore (Phase 18)
- Instrumented (androidTest) UI tests (Phase 13 continues)

## Build (from GitHub, no local Android Studio needed)

1. Push this project to a GitHub repository.
2. GitHub Actions (`.github/workflows/build.yml`) runs automatically on push to `main`,
   running unit tests then building `app-debug.apk`.
3. Download the APK from the workflow run's **Artifacts** section.
4. Install on your Android device (enable "Install unknown apps" for your browser/file manager).

To build locally instead: `./gradlew assembleDebug` (requires JDK 17 and Android SDK).

## Project structure

```
app/src/main/java/com/aj/cardvault/
├── data/
│   ├── database/   Room database
│   ├── dao/        CardDao (CRUD + search)
│   ├── entity/     CardEntity, CardType
│   └── repository/ CardRepository, CardValidator
├── security/       KeyManager, EncryptionManager, AuthManager, BiometricAuthHelper
├── nfc/            NfcManager, NfcReader, NfcIdentifier
├── ui/
│   ├── screens/    All Compose screens
│   ├── navigation/ NavRoutes, CardVaultNavHost
│   └── theme/      Material3 theme
├── viewmodel/      AuthViewModel, CardViewModel, factories
├── AppContainer.kt Manual DI container
└── MainActivity.kt
```

## Security notes (realistic, not overstated)

This app protects stored card data using AES-256-GCM with an Android Keystore-backed key,
plus a hashed PIN and optional biometric gate. It reduces exposure if the device is lost or
the database file is extracted. It does **not** guarantee protection on a rooted or actively
compromised device where the app's own decrypted memory could be inspected. It is not
"military grade" or "unbreakable" — it is standard, well-reviewed Android platform security
applied appropriately for local sensitive data.

## Next stage

Waiting on your confirmation to continue into Phase 12 (auto-lock timeout + Settings wiring)
and Phase 16–17 (signed release build), per the development order in the master spec.
