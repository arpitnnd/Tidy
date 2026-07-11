# Tidy 🧹

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-Multiplatform-purple.svg)](#)

Most links you share carry tracking parameters: click IDs, campaign
tags, referral codes. Small strings that quietly record where you found
a link, when you shared it, and who you sent it to. You never added
them. They're just there.

Tidy removes them before the link leaves your phone.

It's a small Android app, built carefully rather than quickly, for
people who'd rather know exactly what their phone is doing than hope
it's fine. Cleaning happens entirely on-device. The rules are yours to
read and override. Nothing about your links ever reaches a server, not
Tidy's, not anyone else's.

---

## What it does

Share a link to Tidy, or paste one in, and it comes back clean: tracking
parameters gone, short links (`bit.ly`, `t.co`) unwound to where they
actually lead, `m.`/`mobile.` subdomains dropped. It happens as you
type. No button to hunt for.

You stay in charge of the rules, not just the outcome:

- **Know what was removed, and why.** Tap any stripped parameter (on the
  dashboard, in your history, or inside the default blocklist) to see
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
- **Launch auto-clean.** Open Tidy and your clipboard link is cleaned,
  copied back, and the app is gone before it's had time to matter.
- **Share automation** (Tidy+). A shared link gets cleaned, copied, and
  Tidy closes itself, dropping you right back where you were.
- **Clean from anywhere** (Tidy+). Select a URL in any app, tap *Clean
  with Tidy* in the selection toolbar. No switching apps to do it.

---

## The one network call

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

That's the whole list. Two calls, both explainable in one sentence, both
one-way.

---

## No crash reporting, no analytics, on purpose

There's no Firebase in here. No Crashlytics, no analytics SDK, nothing
phoning home in the background.

If Tidy crashes, the report stays on your device. Find it under
**Settings → Diagnostics & Crashes**: read it, delete it, or share it
yourself through the system share sheet if you want to help. Tidy never
sends anything without you choosing to.

An app that claims zero remote logging shouldn't ask you to trust that
blindly. The code's right here. Go check.

---

## Tidy and Tidy+

Tidy is free and open source, and free isn't a trial here. Everything
that gives you control, the cleaning engine, custom rules, whitelist
profiles, unlimited history, your own backups, stays free, for good.

**Tidy+** is a one-time unlock on Google Play for people who already
trust how Tidy behaves and want a few less taps: auto-copy and
auto-close on share, cleaning from any text field, extra themes. It's
also how this stays a real, maintained project instead of an app that
quietly stops getting updates. No ads, no subscription, no data
collected to sell instead.

| | **Tidy** | **Tidy+** |
|---|---|---|
| Cleaning engine, short-link expansion | ✅ | ✅ |
| Custom rules, whitelist profiles | ✅ | ✅ |
| Unlimited history, dashboard, backup/restore | ✅ | ✅ |
| Quick Settings tile, launch auto-clean | ✅ | ✅ |
| Auto-copy & auto-close on share | — | ✅ |
| Inline text-selection cleaning | — | ✅ |
| Extra themes | — | ✅ |

## Installing

Same app, two doors in:

- **[GitHub Releases](../../releases)**: open source, no Play account
  needed. You update it yourself, whenever you check back.
- **[Google Play](#)**: free to install, updates itself, Tidy+ available
  if you want it.

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
  engine), `SettingsRepository` (DataStore-backed rules, stats, and
  automation settings).
- **`:app`**: Compose UI (Navigation3), share-intent handling, the
  Quick Settings `TileService`, and `UrlExpander` for redirect
  resolution.

**Requirements:** JDK 17+, Android SDK 26+, Android Studio Ladybug or
newer.

```bash
./gradlew installDebug        # build & install debug
./gradlew testDebugUnitTest   # run unit tests
```

---

## Security policy

If you discover a security vulnerability in Tidy, please report it privately via the **Security** tab on GitHub rather than opening a public issue. See [SECURITY.md](SECURITY.md) for details on scope and disclosure.

---

## License

Apache License 2.0. See [LICENSE](LICENSE).
