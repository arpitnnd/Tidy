# Contributing to Tidy

Thanks for considering a contribution. This repository contains the free, open-source FOSS app and tracker rules.
Tracker corrections, small fixes and thoughtful improvements all help keep Tidy useful.
The Play flavour requires the closed-source `:feature-plus` module.

## Choose a change

- Correct or extend [tracker rules](blocklist/trackers.v2.json).
- Fix a bug from [GitHub Issues](../../issues), or report one with reproduction steps.
- Improve the FOSS app, shared logic or reusable UI.

For larger changes, open an issue to agree the scope before implementation.
Use the [README](README.md#for-developers) for environment requirements and build commands.

## Tracker rules

The blocklist is a JSON array with `name`, `description` and `domains` fields.
Check that removing a parameter preserves the intended destination and behaviour.
Use the narrowest supported domain scope. Do not classify a parameter as tracking from its name alone.

## Code and review

- Keep changes focused. Separate unrelated fixes, refactors and formatting.
- Use British English, sentence case and the term "URL" in new copy.
- Put UI text in string or plural resources.
- Reuse existing components and follow the [design system guide](docs/design-system.md).
- Test changed behaviour and relevant failure paths. Avoid tests that only repeat the implementation.
- Explain what changed, why it matters and how you checked it in the pull request.

Use signed commits with an imperative subject of at most 50 characters, such as `fix: Preserve shared message text`.
Keep the type lowercase and start the description with a capital letter.

Before opening a pull request, run the FOSS build, host tests and lint commands in the README.
For UI changes, review the affected screenshots and device interactions too.

## Craft and documentation

Correct behaviour is the starting point. Review how a change feels in the complete flow, including clarity, continuity and accessibility.
Preserve useful feedback and context. A smaller implementation or shorter document does not establish a better experience.

The README welcomes users, fans and prospective supporters. Keep its product story, maker voice, visual examples and Tidy/Tidy+ comparison.
Correct inaccurate claims while preserving the benefit they explain. Keep availability clear.
Contributor and design guides need enough detail to apply their advice, including examples and reasons behind non-obvious decisions.
Use source references for exact implementation values without making readers reconstruct the guidance themselves.

Before submitting documentation, read the complete result against the previous version. Check what readers lose as well as what they gain.
Review tone, claims, headings, tables, images and destinations. Keep internal review notes out of public guides.

## Screenshot tests

Compose screenshot tests use AGP's native test suites:

```bash
./gradlew :app:testFossDebugScreenshotTestDefaultTestSuite
```

Previews live in `app/src/screenshotTest/kotlin`; reference images live in
`app/src/screenshotTestDefaultFossDebug/reference`.
Inspect failures in `app/build/reports/tests/testFossDebugScreenshotTestDefaultTestSuite/index.html`.
After accepting an intentional visual change, update references:

```bash
./gradlew :app:updateFossDebugScreenshotTestDefaultTestSuite
```

Commit reviewed references with their preview tests. Keep generated images, diffs and reports out of Git.
Use synthetic data. Keep the host operating system and rendering-library versions consistent when comparing images.
Screenshots check appearance; instrumented tests and device review check interaction and accessibility.

## Reports and contributions

Use the [issue templates](../../issues/new/choose) for bugs, features and tracker-rule reports.
Report vulnerabilities privately through the process in [SECURITY.md](SECURITY.md).

AI-assisted contributions have the same review requirements as other contributions.
You are responsible for understanding, testing and maintaining the work you submit.

## Licence

Contributions use the project's [Apache License 2.0](LICENSE).
