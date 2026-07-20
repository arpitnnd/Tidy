# Changelog

All notable changes to Tidy will be documented in this file.

## [Unreleased]

## [1.1.0]

This release is about giving you more control over automation, catching more
trackers and short links out of the box, and making sure a blocklist update
actually does something the moment it lands.

### New

- Clipboard checking and share-to-clean now offer multiple automation
  levels instead of one fixed, all-or-nothing behaviour each, from a quick
  in-app suggestion up to fully automatic. (Tidy+ unlocks the higher
  tiers.)
- Clipboard checking is off by default now, with a dismissible callout
  above manual entry offering to turn it on, and a confirmation banner
  (with a link to Settings) once you do.
- "Clean from any app" (Tidy+): select a URL's text in any app and clean
  it directly from the system text-selection menu.
- Recognise more short-link hosts: `share.google`, `amzn.to`, `v.gd`,
  `rb.gy`, and `shrtco.de` now expand the same way `bit.ly` and `t.co`
  already did.
- Catch more tracking parameters by default: `gbraid`, `wbraid`,
  `twclid`, `ttclid`, `srsltid`, `li_fat_id`, `sc_cid`, and paired
  HubSpot email-tracking parameters are now stripped alongside the
  existing set.

### Improvements

- One adaptive bottom area on the home screen, replacing the previously
  separate manual-entry field and clipboard-suggestion banner.
- "Coming soon" next to the Get Tidy+ row is now a proper badge instead
  of text appended onto the title.
- The clear action on a cleaned link now sits on the left with Material 3
  error styling, to read as the destructive action it is.

### Bug fixes

- Clipboard checking now behaves identically whether Tidy is freshly
  opened or resumed from the background. Opening it directly used to run
  a separate code path that could silently auto-clean the clipboard and
  close the app right back out from under you; it never does that now.
- A tracker blocklist update fetched from this repository now actually
  changes what gets stripped from a link, instead of only updating the
  tooltip text shown for an already-removed parameter.
- Tapping a locked Tidy+ feature (a premium theme, an automation toggle)
  on the FOSS build now actually shows the upgrade prompt, instead of
  silently doing nothing.
- The "Back up app data" description now says what's actually backed up:
  all your Tidy data, not just history and rules.

### Internal

- Updated AGP, Kotlin, and other dependencies.
- The compiled-in default tracker list is now generated from
  `blocklist/trackers.json` at build time instead of being hand-copied,
  so it can no longer drift out of sync with what the app syncs at runtime.
- Play Billing's dependency version now lives in its own private catalog
  instead of the public version catalog, since it's only ever used by the
  closed-source `:feature-plus` module.
- APK output filenames are now set natively via AGP's `onVariants` API
  instead of a manual rename task, so Android Studio's Build Variants
  panel shows the right names too.
- The Tidy+ migration backup (still hidden behind the coming-soon flag)
  now backs up your rules and settings too, not just history. The
  restore confirmation shows real counts instead of a hardcoded zero.
- Fixed a stale Gradle wrapper checksum that was breaking CI, and bumped
  CI's GitHub Actions to their Node 24 versions ahead of GitHub dropping
  Node 20.

## [1.0.0]

First public release.

### New

- Strip tracking parameters from a link automatically, as you type, paste,
  or share it in. No button to hunt for.
- Expand short links (`bit.ly`, `t.co`) to where they actually lead, and
  drop `m.`/`mobile.` subdomains.
- Tap any removed parameter to see what it was. Whitelist it for one
  domain in a single tap, or flag a rule as wrong straight into a
  prefilled GitHub issue.
- Set your own whitelist and blocklist rules where the defaults don't fit.
- A Privacy Dashboard tracking URLs cleaned, trackers blocked, and
  bandwidth saved, backed by unlimited history with per-entry open/copy
  actions.
- Back up your rules and history to a JSON file, and restore them
  whenever you need to.
- Opt-in Android system backups (disabled by default) to prevent silent
  cloud uploads of your history or settings. Enable them from Settings →
  Data & Backup.
- Clean whatever's on your clipboard from the Quick Settings tile or on
  app launch, no need to open the full app.
- Crash reports stay on-device: read, delete, or share them yourself
  from Settings → Diagnostics & Crashes. Nothing is sent automatically.
- The tracker blocklist updates itself every few days from this
  repository's public rules. The app works fully offline from first
  install on its bundled rules.
- Free and open source under Apache 2.0. No ads, no account, no
  analytics, no crash-reporting SDK.
- Tidy+ settings are present but gated behind a "coming soon" flag
  until the Google Play listing goes live; switching from this GitHub
  build to Play later will be a single export and restore.
