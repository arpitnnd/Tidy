# Changelog

All notable changes to Tidy will be documented in this file.

## [Unreleased]

## [1.4.0]

Tidy 1.4.0 refreshes the everyday experience of cleaning and sharing links. A new light palette,
clearer results and layouts that adapt to your screen make Home, History and Settings easier to use.
Controls are easier to navigate with a screen reader, and larger text stays readable without crowding
controls. Sharing keeps the words around your links intact, and clipboard cleaning responds more
reliably.

### New

- You can open Tidy's privacy policy, terms of use and software licence from About.

### Improvements

- Light mode uses a muted slate background with lighter off-white cards. Content surfaces are opaque
  in both modes, and the launch background matches the app.
- Home's title uses the theme colour. Result cards have a flat background and more space around
  expanded details.
- Settings titles, descriptions and section headings have more space. Privacy labels on Home also
  have clearer gaps.
- Home and History use muted green status badges. Removed parameters appear in red with an
  information icon. Badges wrap at large text sizes, and results stay visible during animations.
- Parameter details give the explanation more emphasis than the Always keep action. About section
  headings now match Settings.
- Screen readers announce whether Settings controls are on, selected or expanded. Theme rows have
  a single action, and section headings make navigation easier.
- Locked options and switches in the off state have clearer contrast. Automation choices stack at
  larger text sizes or in narrow windows.
- History arranges records in multiple columns in wider windows. Home uses a smaller header in short
  windows, and its privacy labels wrap when space is limited.
- Screen headers have solid backgrounds that hide scrolling content beneath them. Long titles stay
  on one line and end with an ellipsis.
- Link and rule fields explain invalid input after submission. Actions sit beside the fields.
- Settings and clipboard prompts use shorter, clearer text. Backup settings explain what you agree
  to and what happens to earlier backups.
- Scrollable screens accept swipes from their side margins while keeping content centred.

### Bug fixes

- History keeps its previous data if a save fails. Clear and Undo report failures instead of
  claiming success, and saved timestamps remain stable after reload.
- Tidy+ text-selection cleaning preserves surrounding punctuation and repeated links.
- History actions wrap at larger text sizes, keeping Copy original, Copy and Open readable.
- The Get Tidy+ row stays readable while showing Coming soon.
- Removed UTM and other pattern-matched parameters show their explanations in Home and History.
- Clipboard suggestions and automatic cleaning work more reliably when you return to Tidy.
- Copy and Share preserve text around a link received from another app, including punctuation and
  email addresses. Only the first detected link is replaced. Later links remain unchanged, and
  History stores only links.
- After successful expansion, the expansion action is disabled even if the link stays unchanged.
  Failed requests can be retried. Clearing or replacing the input prevents old expansion results
  from appearing.
- Fresh installations use bundled rules without an automatic first-session network request.
- The confirmation before clearing History explains when you can undo it. Cleared entries are
  discarded when the undo prompt ends or you leave History.
- Expanded History entries keep their state when scrolled out of view and back.
- Manually expanding a link shows an error if the network request fails.
- Home input and result controls stay clear of camera cutouts in landscape.
- You can share complete backups through Android's share menu. Backup sharing can only access
  exported backups, keeping other app files private.
- If you enable Android backups, they restore History, rules and settings. Diagnostic files and
  temporary purchase data stay out of these backups.

### Internal

- Added a shared Compose Styles layer for custom component surfaces and theme-aware overrides.
- Updated Kotlin, Gradle, Android Gradle Plugin and Compose dependencies, and added native AGP
  screenshot test suites.
- Aligned Java and Kotlin bytecode targets and CI builds on Java 25.
- Added deployment for the privacy and terms website from its maintained source.
- Play bundle exports keep the original build output, so repeat builds and Android Studio can still
  find it.
- Tidy+ checkout uses the active billing connection, and all premium themes check purchase status.

## [1.3.0]

Sharing into Tidy now feels immediate, even when a short URL takes time to expand. The home screen
moves more smoothly, and restoring history keeps your lifetime totals accurate.

### New

- You can now check for blocklist updates from Settings.

### Improvements

- Shared URLs appear straight away, with a loading indicator while short URLs expand.
- The home screen header shrinks as you scroll. Input, suggestions, and results transition more
  smoothly.
- Tidy checks for blocklist updates when you return to the app or share a URL. Settings shows
  when it last checked.
- Tidy now removes `cid` from Samsung URLs and `taid` from XDA URLs.
- Lifetime statistics now use natural singular and plural wording, and clipboard prompts have
  more consistent borders.

### Bug fixes

- Email addresses are no longer mistaken for URLs.
- Restoring history now updates your lifetime statistics. Full backups keep those totals too.
- Sharing to Tidy now opens one Tidy card in Recents, separate from the app you shared from.
- Clipboard suggestions disappear when you enter or clean the same URL.

### Internal

- Updated Gradle, Android Gradle Plugin, Compose, Navigation 3, and supporting AndroidX libraries.
- Scoped Android backups to user data and excluded local crash reports.
- Enabled parallel Gradle task execution.

## [1.2.0]

A follow-up release: broader default tracker coverage with safer domain-scoping,
two new cleaning rules, and a set of fixes to automation and clipboard behaviour
that had regressed or never quite worked as intended.

### New

- Comprehensive default tracking-parameter coverage: major ad networks (Google
  Ads, Facebook, Microsoft, Yandex, X, TikTok, LinkedIn, Snapchat), campaign
  platforms (Mailchimp, HubSpot, Matomo, Marketo, ConvertKit), affiliate
  network click IDs (Awin/Zanox, Impact Radius, Rebounce, and others), social
  telemetry (Spotify, Instagram, YouTube), and e-commerce tracking (Amazon,
  AliExpress, eBay). Parameters that are generic on non-tracking sites (such
  as Amazon's `ref`, YouTube's `feature`, or eBay's `campid`) stay
  domain-scoped so non-tracking sites are never broken.
- Amazon short-link recognition now matches domain patterns like `amzn.*`
  instead of listing every one out, so `amzn.in`, `amzn.eu`, `a.co`, `a.to`,
  and `z.cn` all expand the same way `amzn.to` already did.
- Expanded regional domain coverage for e-commerce tracking rules across Amazon
  (`amazon.ie`, `amazon.co.za`, `amazon.com.be`), eBay (`ebay.fr`, `ebay.it`,
  `ebay.es`, `ebay.nl`, `ebay.at`, `ebay.ch`, `ebay.ie`, `ebay.*`), and AliExpress
  (`aliexpress.us`, `aliexpress.ru`, `aliexpress.fr`, `aliexpress.*`).
- New opt-in setting (on by default): drop a trailing slash from a cleaned
  URL's path.
- Recognise `we.tl` WeTransfer share links for expansion.

### Improvements

- The clipboard suggestion now reflects any link on the clipboard, not only
  ones cleaning would actually change, so it's predictable: what you see is
  always what's really there, whether or not it needs cleaning.

### Bug fixes

- The clipboard suggestion no longer stops checking permanently after the
  first time you return from Settings or History: it used to work once,
  then silently go quiet for the rest of the session (and after rotating the
  screen), even when a genuinely new link was on the clipboard.
- Clearing a cleaned result under the automatic-clean tier (Tidy+) no longer
  immediately re-cleans the same clipboard content and adds a duplicate
  history entry; automatic cleaning that finds surrounding text around a
  link now keeps that text instead of discarding it; and sharing, or
  closing instead of sharing, after a clean no longer gets silently skipped
  just because the clipboard already happened to match.
- A domain whitelisted to "skip entirely" is now genuinely skipped: it no
  longer has "m." stripped from its host, and a whitelisted short link no
  longer triggers an outbound network request to expand it.
- UTM parameter stripping no longer depends on the tracker list containing a
  `utm_*` entry; it's an unconditional rule again.
- Short-link expansion no longer permanently stops resolving a link after
  one failed attempt (e.g. while offline), no longer follows a redirect to
  a non-http(s) target, and correctly reads the host of a URL that carries
  a username/password.
- A dismissed "Get Tidy+" prompt no longer reappears after visiting Settings
  or History, or after rotating the screen.
- Sharing the same URL to Tidy twice in a row now cleans it both times,
  instead of silently doing nothing the second time.
- Expanding a short link no longer adds a second history entry and
  double-counts it in your stats.
- The crash-report sheet's "don't ask again" is now honoured on a cold
  start, not just after the preference had already loaded once.

### Internal

- Refactored `TidyTileService` Quick Settings tile implementation to run DataStore
  reads and URL cleaning on an asynchronous coroutine scope instead of blocking
  the main thread.
- The remote blocklist now syncs from a versioned path, so already-installed
  versions of Tidy keep syncing successfully instead of silently failing
  forever the moment this release's blocklist schema reaches them.
- Updated AGP, the Navigation 3 library, and the CI JDK.

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
