# Security Policy

Tidy runs entirely on-device with no backend server, no accounts, and
no remote data storage. That shrinks the attack surface considerably,
but it doesn't shrink it to zero. If you've found a real issue, here's
how to tell us.

## Reporting a Vulnerability

Please **do not** open a public GitHub issue for security reports.
Use GitHub's private reporting instead: go to the **Security** tab of
this repository → **Report a vulnerability**. This reaches maintainers
directly without disclosing the issue publicly before a fix ships.

Include what you can:

- Which build (GitHub/foss or Google Play) and version
- Android version and device (or emulator) used
- Steps to reproduce
- What you'd expect an attacker to be able to do with it

You'll get an acknowledgment within 5 business days. This is a
one-person project, not a company security team, so response time
depends on severity and my other commitments — but every report gets
read and taken seriously.

## Scope

**In scope:**

- The Android app itself, either flavour
- Local data handling: whitelist rules, history, backups, DataStore
  preferences
- The remote blocklist sync mechanism
- Crash report handling (local storage, sharing flow)
- Anything in the Tidy+ entitlement/purchase flow

**Out of scope:**

- Behaviour of third-party sites or short-link services that URLs
  happen to resolve to — Tidy doesn't control what's on the other end
  of a link
- Attacks that require a rooted or otherwise compromised device to
  bypass local-only checks (e.g. the Tidy+ entitlement check). This is
  a known, accepted tradeoff of running with no backend to verify
  against, not a vulnerability in the usual sense
- Social engineering, physical device access, or anything not
  specific to Tidy's own code

## Supported Versions

Only the latest released version of each build (GitHub/foss and Google
Play) is supported. Please update before reporting, if possible, in
case it's already fixed.

## Disclosure

Coordinated disclosure is appreciated: give us a reasonable window to
ship a fix before any public write-up. Credit is offered in release
notes if you'd like it. There's no bug bounty program; this is an
independent project without a budget for one.

Thank you for taking the time to help keep Tidy trustworthy.
