# Changelog

All notable changes to Tidy will be documented in this file.

## [1.0.0]

First public release.

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
