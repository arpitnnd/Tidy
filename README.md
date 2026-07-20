# Tidy 🧹

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-Multiplatform-purple.svg)](https://kotlinlang.org/docs/multiplatform.html)

Ever copied a link and seen extra gibberish tacked onto the end,
something like `?utm_source=facebook&fbclid=AbC123xyz`? That's not
part of the real address. It's tracking information, added quietly so
companies can tell where you found the link, when you shared it, and
who you sent it to. Bit by bit, that builds a picture of you, without
ever asking.

Tidy removes that part before you share a link. What's left is the
plain, clean address, nothing extra riding along, and nothing about
you attached to it.

It's a small, free app. No account, no setup, no ads.
Everything happens right on your phone, nothing about your links is
ever sent anywhere else.

---

## What it does

Share a link to Tidy, or paste one in, and it comes back clean: tracking
parameters gone, short links (`bit.ly`, `t.co`) unwound to where they
actually lead, `m.`/`mobile.` subdomains dropped. It happens as you
type. No button to hunt for.

You stay in charge of the rules, not just the outcome:

- **Know what was removed, and why.** Tap any stripped parameter to see
  what it actually was. `fbclid`, for instance, is Facebook's way of
  tying a click back to you. Whitelist it for that one domain in a
  single tap, or flag a rule as wrong straight into a prefilled GitHub
  issue.
- **Whitelisting with nuance.** Keep `v` on `youtube.com` while
  stripping it everywhere else. Add your own rules where the defaults
  don't fit.
- **A record, entirely yours.** The Privacy Dashboard counts URLs
  cleaned, trackers blocked, and bandwidth saved, with unlimited
  history, per-entry open/copy actions, and full JSON backup and
  restore. It never leaves your phone unless you export it yourself.

### Without even opening the app

- **Quick Settings tile.** Clean whatever's on your clipboard from the
  notification shade, no app launch required.
- **Clipboard checking.** Off by default. Turn it on and choose how
  far it goes, from a quick review prompt up to fully automatic.
- **Share to Tidy.** Same choice for shared links: see the cleaned
  result, or let Tidy copy and even re-share it for you.
- **Clean from anywhere** (Tidy+). Select a URL in any app, tap *Clean
  with Tidy* in the selection toolbar. No switching apps to do it.

---

## The two network calls

Tidy's tracker blocklist lives in this repository. The app checks in
every few days for updates: a plain, read-only pull of public rules, so
it keeps catching new trackers without waiting on you to update the app.
Nothing about you or your links travels the other way. On first
install, before it's ever fetched anything, Tidy already works fully
offline using its bundled rules. Every change to the blocklist is
public: [see the history for yourself](../../commits/main/blocklist).

Short-link expansion needs the network too, by nature. Unwinding
`bit.ly/xyz` means asking where it goes. That request travels straight
from your phone to the link's own server. Nothing of Tidy's sits in
between.

That's the whole list, each one explainable in a sentence, both
one-way.

---

## No crash reporting, no analytics, on purpose

There's no Firebase in here. No Crashlytics, no analytics SDK, nothing
phoning home in the background.

If Tidy crashes, the report stays on your device. Find it under
**Settings → Diagnostics & Crashes**: read it, delete it, or share it
yourself through the system share sheet if you want to help. Tidy never
sends anything without you choosing to.

The same goes for Android's Auto Backup (Google account or ADB). Tidy blocks
these by default to prevent silent cloud uploads of your settings and
sanitisation history. You can opt in any time under **Settings → Data &
Backup**.

An app that claims zero remote logging shouldn't ask you to trust that
blindly. The code's right here. Go check.

---

## Tidy and Tidy+

Tidy is free and open source, and free isn't a trial here. Everything
that gives you control, the cleaning engine, custom rules, whitelist
profiles, unlimited history, your own backups, stays free, for good.

**Tidy+** is a one-time unlock on Google Play for people who already
trust how Tidy behaves and want a few less taps: the upper tiers of
clipboard and share automation, cleaning from any text field, extra
themes. It's also how this stays a real, maintained project instead of
an app that quietly stops getting updates. No ads, no subscription, no
data collected to sell instead.

> Tidy+ isn't live yet. The GitHub build above works fully today. Tidy+
> is finishing testing before it lands on Google Play, and this README
> will update the moment it does. Think of the table below as a preview
> of what's coming.

| | **Tidy** | **Tidy+** |
|---|---|---|
| Cleaning engine, short-link expansion | ✅ | ✅ |
| Custom rules, whitelist profiles | ✅ | ✅ |
| Unlimited history, dashboard, backup/restore | ✅ | ✅ |
| Quick Settings tile | ✅ | ✅ |
| Clipboard checking, review before copying | ✅ | ✅ |
| Clipboard checking, auto-copy or auto-clean | — | ✅ |
| Share to Tidy, view the cleaned link | ✅ | ✅ |
| Share to Tidy, auto-copy or auto-share | — | ✅ |
| Inline text-selection cleaning | — | ✅ |
| Extra themes | — | ✅ |

## Installing

Same app, two doors in:

- **[GitHub Releases](../../releases)**: open source, no Play account
  needed. You update it yourself, whenever you check back.
- **Google Play**: coming soon. Tidy+ and automatic updates will live
  here once the listing is public.

They're signed separately and install as two distinct apps. Switching
from GitHub to Play later takes one export and one restore. Tidy walks
you through it on first launch.

---

## Design

Built to Material 3 Expressive, in a quiet, nature-leaning palette:
forest slates, moss charcoal, sage green. Inputs sit low enough for a
thumb to reach without adjusting your grip. Nothing here is trying to
be noticed. A tool you'll open twenty times a day should feel like part
of the phone, not an occasion.

---

## For developers

Kotlin Multiplatform, split cleanly between core logic and the Android
client:

- **`:shared`**: `UrlCleaner` (the parameter-stripping and whitelisting
  engine), `UrlDetection` (the single loose "looks like a URL" check
  used everywhere a link needs spotting), `SettingsRepository`
  (DataStore-backed rules, stats, and tiered automation settings).
- **`:app`**: Compose UI (Navigation3), share-intent handling, the
  Quick Settings `TileService`, and `UrlExpander` for redirect
  resolution.

Written with AI-assisted tooling for code and drafting. Every line is
reviewed, tested, and shipped by a human before it reaches you.

**Requirements:** JDK 17+, Android SDK 26+, Android Studio Ladybug or
newer.

We have two build flavors, under Gradle's `distribution` dimension.
**`foss`** is what's here: no Google Play Services, nothing else needed
to build or run it, works fine even on a de-Googled phone. **`play`**
adds the paid Tidy+ features. Some of that code sits in a separate repo,
purely to protect the one-time purchase. Your data is handled exactly
the same either way. The build commands below are for the `foss` flavor.

### Building and Testing

```bash
# Compile and build the FOSS flavor APK
./gradlew :app:assembleFossDebug

# Run unit tests
./gradlew :shared:testAndroidHostTest
./gradlew :app:testFossDebugUnitTest

# Run lint checks
./gradlew lintFossDebug
```

---

## Contributing

Bug fixes, blocklist corrections, and improvements to the `foss`
flavor are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md) for setup,
code style, and how to submit changes.

---

## Security policy

If you discover a security vulnerability in Tidy, please report it
privately via the **Security** tab on GitHub rather than opening a
public issue. See [SECURITY.md](SECURITY.md) for details on scope and
disclosure.

---

## License

Apache License 2.0. See [LICENSE](LICENSE).