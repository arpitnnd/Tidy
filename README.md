# Tidy 🧹

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-Multiplatform-purple.svg)](#)

A beautiful, privacy-first, offline-first Android app built with **Kotlin Multiplatform (KMP)** and **Jetpack Compose** designed to strip tracking garbage (`utm_*`, `fbclid`, `gclid`, etc.) from URLs before you share them.

Tidy values your digital hygiene, offering robust whitelisting mechanics, automatic short URL resolution, and seamless clipboard automation—all running entirely on-device with zero remote server logging.

---

## Key Features 🚀

- **Local & Offline-First**: All URL cleaning is performed 100% locally on your phone. No logins, no trackers, and no external API dependencies.
- **Inline Selection Cleaning (`PROCESS_TEXT`)**: Clean URLs in-place from any third-party app via the system text-selection toolbar (Tidy+ only).
- **GitHub-hosted Remote Blocklist Sync**: Periodic background synchronization of the parameter blocklist via ETag cache headers.
- **Unified Bottom Sheet Details**: Displays parameter descriptions, one-tap whitelisting, and prefilled issue reporting templates for stripped parameters.
- **Smart Short URL Expansion**: Prepend protocols and resolve shortened links (e.g. `bit.ly`, `tinyurl.com`, `t.co`) to their final destinations before cleaning, bypassing aggressive redirect blockades.
- **Mobile Subdomain Removal**: Automatically strip `m.` or `mobile.` subdomains from cleaned links (preserving short domains like `m.me` that lack a second dot in their host).
- **Domain-Specific Parameter Whitelisting**: Allow specific parameters on certain domains (e.g., keep the `v` parameter on `youtube.com` while stripping it globally).
- **One-Tap Whitelist Shortcuts**: Instantly whitelist a parameter for the current domain by tapping its chip in the "Removed Parameters" results details card.
- **Advanced Share Automation**:
  - **Auto-copy when shared**: Tidy automatically cleans and copies shared URLs to your clipboard.
  - **Auto-close after copying**: The app closes itself automatically after copying to return you back to your previous app immediately.
- **Quick Settings Tile ("Tidy Clipboard")**: Tap a tile in your system shade to instantly clean whatever URL is currently on your clipboard in the background, without opening the app.
- **Zero-Friction Launch Auto-Clean**: Instantly clean your clipboard URL, copy it back, and exit the app on launch. Includes a double-launch escape hatch (bypasses auto-clean if reopened within 3 seconds) to access settings.
- **Debounced Auto-Clean on Typing**: Automatically cleans URLs in the input field as you type or paste without requiring manual clicks.
- **Privacy Dashboard & Local History**: 
  - Tracks statistics like *URLs Cleaned*, *Trackers Blocked*, and *Estimated Bandwidth Saved*.
  - Detailed, interactive local history cards with individual "Open", "Copy", and "Copy Original" actions.
  - JSON Backup import and export to secure your stats locally.
- **Accessible Modern Design**: Built following **Material 3 Expressive guidelines** with a quiet, nature-inspired palette (grounded forest slates, moss charcoal, and sage green). Fully responsive bottom sheet input dialogs and a full-width ergonomic details toggle row.

---

## Tidy vs. Tidy+

Tidy's cleaning engine, history, and core privacy guarantees are fully open
source and always will be. Some convenience features that aren't essential
to the core promise are part of an optional, one-time **Tidy+** unlock —
this funds continued development without compromising what Tidy is for.

| | **Tidy** (FOSS) | **Tidy+** (one-time unlock) |
|---|---|---|
| Local URL cleaning & tracker stripping | ✅ | ✅ |
| Short-URL unwrapping | ✅ | ✅ |
| Domain whitelisting, incl. custom/regex rules | ✅ | ✅ |
| Multiple whitelist profiles | ✅ | ✅ |
| Full history & Privacy Dashboard | ✅ Unlimited | ✅ Unlimited |
| JSON backup & restore | ✅ | ✅ |
| Auto-copy & auto-close on share | — | ✅ |
| Inline text-selection cleaning | — | ✅ |
| Bulk clipboard cleaning | — | ✅ |
| Extra themes | — | ✅ |
| Available on | GitHub Releases (direct APK) | Google Play (free install, unlock via in-app purchase) |

A few things worth being upfront about:

- **The free version isn't a trial.** Everything that gives you control —
  cleaning, custom rules, whitelist profiles, full history, your own
  backups — is in the free tier, permanently, no asterisks.
- **Tidy+ is about saving time, not unlocking trust.** Auto-copy, auto-close,
  inline selection cleaning, and bulk actions exist for people who already know how the app behaves
  and just want fewer taps. Nothing in there changes what gets cleaned or
  who can see your data.
- **Tidy+ is a one-time purchase, not a subscription.** Since everything
  runs on-device with no ongoing server cost, recurring billing wouldn't be
  honest.
- **The core app's source stays GPL-licensed and forkable.** The Tidy+
  module is closed-source and Play-only, which is what makes a paid unlock
  viable at all — anyone can build the GitHub version themselves, and we'd
  rather you know that than discover it later.
- **The GitHub-distributed build will never gate features behind a
  paywall.** If something genuinely core to privacy gets added later, it
  ships free, in that build, no exceptions.

---

## Which build should I install?

Both are the same app under the hood. Pick based on how you want updates
handled:

- **GitHub Releases (APK)** — fully open source, no Play account needed.
  Updates are manual: watch the repo or check back for new releases.
- **Google Play** — same core app, plus the Tidy+ unlock if you want it.
  Updates install automatically, same as any other Play app.

Already on the GitHub build and want to move to Play later? Your data
moves with you via the existing backup/export — see below.

---

## Crash Reporting & Analytics — By Design, Not By Accident

You won't find Firebase Crashlytics, Google Analytics, or any third-party
telemetry SDK in this codebase. That's not an oversight — it's the same
"zero remote logging" principle that governs everything else in Tidy.

Instead:

- **Crashes** are caught locally using [ACRA](https://github.com/ACRA/acra),
  an open-source crash handler. If Tidy crashes, the report is written to
  your device only. On next launch, you'll see a small prompt offering to
  show you the report or share it yourself, manually. To prevent continuous popup alerts, dismissing the prompt hides it for the current app session.
- **Diagnostics & Log Access**: Users can always inspect, share, or delete the last crash report at any time via a dedicated "Diagnostics & Crashes" section in the Settings panel.
- **Usage analytics** don't exist, full stop. The Privacy Dashboard already
  gives *you* the stats that matter (URLs cleaned, trackers blocked) —
  there's no separate copy being sent to us, and no toggle anywhere that
  sends it either.
- **Remote Blocklist Updates**: Tidy periodically performs a read-only background fetch to synchronize its parameter blocklist from GitHub. This fetch transmits absolutely no client identifiers, usage statistics, or cleaned URL data. It is a one-way download of public parsing rules so your app can stay up-to-date against new trackers without needing manual updates. The app is fully functional offline on first install using its bundled fallback rules.
- **Performance monitoring** happens on our end during development (Android
  Studio Profiler, Macrobenchmark), never in the shipped app.
- On the **Play Store build only**, we rely on Android Vitals in Play
  Console — a Google-provided view of crash/ANR rates collected at the OS
  level for any app distributed through Play. This requires no SDK and no
  code in Tidy; it's not present at all in the GitHub-distributed build.

If you've ever wondered why an app this small doesn't have a "send crash
data" checkbox buried in onboarding — this is why. The absence is the point.

---

## Architecture & Codebase Structure 🏗️

The project utilizes a decoupled Kotlin Multiplatform architecture dividing core logic and the Android presentation client:

- **`:shared` (Core Logic)**:
  - `UrlCleaner`: The regex parameter scrubbing and domain whitelisting core engine.
  - `SettingsRepository`: Manages whitelists, custom blacklists, stats, and automation configurations backed by Jetpack DataStore Preferences.
  - `PlatformDataStore`: Platform-specific expected declarations for DataStore instantiation.
- **`:app` (Android Application)**:
  - `MainActivity`: Intercepts shared text intents (`ACTION_SEND`) and orchestrates background automation.
  - `Navigation`: Implements screen routing using Jetpack Navigation3.
  - `ui/main/`: Main dashboard, manual input bottom sheet, results cards, and the `MainScreenViewModel`.
  - `ui/settings/`: Management panel for whitelists, blacklists, automation switches, and custom switch styles.
  - `ui/history/`: Analytics cards, bandwidth savings estimator, import/export buttons, and the interactive historical log.
  - `data/UrlExpander`: Handles async HTTP GET-redirect resolution (following up to 5 hops with connection timeout safeguards).
  - `data/TidyTileService`: Implements the Quick Settings Tile for zero-friction background clipboard cleaning.

---

## Technical Stack 🛠️

- **Language**: Kotlin Multiplatform (KMP)
- **UI Framework**: Jetpack Compose (Compose Multiplatform)
- **Data Storage**: Jetpack DataStore Preferences
- **Navigation**: Jetpack Navigation3
- **Build System**: Gradle Kotlin DSL (`.gradle.kts`)

---

## Getting Started 💻

### Prerequisites
- JDK 17 or higher
- Android SDK (API Level 26+)
- Android Studio Ladybug (or newer)

### Build and Run
1. Clone this repository.
2. Build and install the debug application to an active emulator or connected device:
   ```bash
   ./gradlew installDebug
   ```

### Running Unit Tests
To run JVM unit tests for both shared business logic and presentation view-models:
```bash
./gradlew testDebugUnitTest
```

---

## License 📄

Tidy is open-source software licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for details.
