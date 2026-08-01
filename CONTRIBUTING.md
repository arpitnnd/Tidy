# Contributing to Tidy

Thanks for considering it. This repository holds the free, open source
**`foss`** flavour of Tidy: the cleaning engine, the Android client, and
the tracker blocklist. Tidy+ (the paid Google Play flavour) lives in a
separate, closed-source repository, so anything specific to it isn't
something a PR here can touch — see [Tidy and Tidy+](README.md#tidy-and-tidy) in the
README for how the two are split.

## Ways to contribute

- **Fix or extend the tracker blocklist.** [`blocklist/trackers.v2.json`](blocklist/trackers.v2.json)
  is a plain JSON array of `{ "name": "...", "description": "...", "domains": [...] }`
  entries. Adding a missing tracker, correcting a wrong description, or
  removing a stale one is one of the most useful contributions and
  doesn't require touching any Kotlin.
- **Fix a bug.** Check [open issues](../../issues) first in case it's
  already tracked.
- **Improve the `foss` flavour.** New cleaning rules, UI fixes, or
  general polish to `:shared` or `:app`.

If you're planning something larger than a small fix, open an issue
first to talk it through before writing code — saves both of us time
if the direction needs adjusting.

## Development setup

Build instructions, the `foss`/`play` flavour split, and module layout
are documented in the README under
[For developers](README.md#for-developers). In short:

```bash
./gradlew :app:assembleFossDebug
./gradlew :shared:testAndroidHostTest
./gradlew :app:testFossDebugUnitTest
./gradlew lintFossDebug
```

You only need the `foss` flavour to work on anything in this repo —
`play` requires the closed-source `:feature-plus` module and isn't
buildable here.

## Code style

- British English throughout: code comments, UI strings, commit
  messages, docs (`customise`, not `customize`; `colour`, not
  `color`).
- Match the existing Compose patterns already in the file you're
  touching rather than introducing a new one — this codebase leans on
  a handful of shared components (`TidyModalBottomSheet`,
  `SettingCard`, `TooltipWrapper`, etc.) instead of one-off UI.
- No unrelated reformatting or renaming in the same PR as a
  behavioural change — keep diffs reviewable.

## Commit messages and PRs

- Keep commits scoped to one change; a bug fix and a refactor belong
  in separate commits.
- Write commit subjects in imperative mood (`fix: `, `feat: `,
  `refactor: `, `docs: ` prefixes match this repo's history — `git log`
  for examples).
- Run the build and tests above before opening a PR. CI runs the same
  checks, but catching it locally first is faster for both of us.

## Reporting bugs and security issues

- **Bugs**: open a [GitHub issue](../../issues/new/choose) with the
  bug report template.
- **Security vulnerabilities**: do not open a public issue — see
  [SECURITY.md](SECURITY.md) for private reporting instructions.

## A note on AI-assisted contributions

This project itself is built with AI-assisted tooling, reviewed and
tested by a human before anything ships (see the README's
[For developers](README.md#for-developers) section). AI-assisted PRs
are welcome on the same terms: you're responsible for understanding
and standing behind everything you submit, the same as if you'd
written it yourself.

## License

By contributing, you agree your contribution is licensed under this
project's [Apache License 2.0](LICENSE).
