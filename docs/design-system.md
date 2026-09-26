# Tidy design system

This guide describes the Compose theme, reusable components and layout conventions used in
Tidy's Android app. Use it when adding a screen or changing an existing interaction.

## Design principles

- Put the cleaned URL and the changes made to it first. Keep supporting actions easy to find.
- Use restrained surfaces and clear typography to distinguish content, controls and feedback.
- Preserve Android navigation, touch feedback, scrolling and text scaling behaviour.
- Express outcomes precisely: a successful clean, an unchanged URL and a failed operation carry
  different meanings.

## Colour and typography

[TidyTheme](../app/src/main/java/com/tidy/app/theme/Theme.kt) supplies the active
`MaterialTheme.colorScheme` and typography. The default Sage Slate palette has light and dark
schemes defined in [Color.kt](../app/src/main/java/com/tidy/app/theme/Color.kt).
Read colour roles from the active theme so components also work with alternative schemes.

| Role | Use |
| --- | --- |
| `primary`, `onPrimary` | Primary actions and their foreground content. |
| `primaryContainer`, `onPrimaryContainer` | Emphasised containers and their foreground content. |
| `surface`, `onSurface` | Surfaces and primary text. |
| `onSurfaceVariant` | Supporting text and secondary icons. |
| `errorContainer`, `onErrorContainer` | Error or destructive-action containers and their foreground content. |
| `outline`, `outlineVariant` | Borders and separators. |

`SettingCard` uses `surfaceVariant` at 40% alpha; its rows use `onSurface` for titles and
`onSurfaceVariant` for supporting text. Check contrast against the rendered background when
using translucent fills or reduced text opacity.

[Type.kt](../app/src/main/java/com/tidy/app/theme/Type.kt) configures `bodyLarge` with the
platform default font, normal weight, 16 sp size, 24 sp line height and 0.5 sp letter spacing.
The remaining theme styles use Material 3 defaults. Components apply these styles and weights:

| Component text | Style | Weight |
| --- | --- | --- |
| `TidyTopAppBar` title | `titleLarge` | Bold |
| `ScreenSectionHeader` | `titleMedium` | ExtraBold |
| `CardSectionLabel` | `labelLarge` | Bold |
| `ExpandableSettingRow` title | `bodyLarge` | SemiBold |
| `ExpandableSettingRow` summary | `bodySmall` | Style default |
| `ClickableLinkRow` label | `bodyMedium` | Medium |

## Cards and alignment

[SettingCard](../app/src/main/java/com/tidy/app/ui/components/SettingCard.kt) provides the
full-width shape and background. Its caller provides the content layout, padding and spacing.
The card is a passive group; actions belong to its rows.

[TidyCardTokens](../app/src/main/java/com/tidy/app/theme/DesignTokens.kt) defines dimensions
for Settings and About cards:

| Token | Value | Use |
| --- | --- | --- |
| `cornerRadius` | 24 dp | Outer card corners. |
| `contentPadding` | 12 dp | Padding around the card's content column. |
| `contentSpacing` | 8 dp | Spacing between children in a standard card column. |
| `rowCornerRadius` | 12 dp | Rounded outline of interactive rows. |
| `rowHorizontalPadding` | 8 dp | Row content inset and alignment of headings, expanded content and dividers. |
| `rowVerticalPadding` | 12 dp | Top and bottom padding of standard action rows. |

A standard row's content starts 20 dp from the card edge: 12 dp of card content padding plus
8 dp of row padding. Clip the row before applying `clickable`, then apply its content padding,
so the entire row responds to touch and the ripple follows the rounded outline.

Theme selection rows account for the radio control's own space: they use no leading padding,
8 dp trailing padding and 8 dp vertical padding. The About destination group uses a denser
4 dp gap between children. These layouts retain the shared card and row corner shapes.

Use these tokens for their named card roles. Other components, such as tier cards, have their
own geometry even where a numeric value happens to match.

### Composing an expandable card

The screen owns `expanded` and updates it through `onToggle`. Resolve `title`, `summary` and
`description` from string resources at the call site. The expanded body is supplied by the
caller; `ExpandableSettingRow` renders the header and chevron.

```kotlin
SettingCard {
    Column(
        modifier = Modifier.padding(TidyCardTokens.contentPadding),
        verticalArrangement = Arrangement.spacedBy(TidyCardTokens.contentSpacing)
    ) {
        ExpandableSettingRow(
            title = title,
            summary = summary,
            expanded = expanded,
            onToggle = onToggle
        )
        if (expanded) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    horizontal = TidyCardTokens.rowHorizontalPadding
                )
            )
        }
    }
}
```

## Reusable components

| Component | Behaviour and use |
| --- | --- |
| [ExpandableSettingRow](../app/src/main/java/com/tidy/app/ui/components/ExpandableSettingRow.kt) | Full-row toggle with title, summary and an up/down chevron. `expanded` controls the chevron and its expand/collapse description. The caller controls the body and state. |
| [ClickableLinkRow](../app/src/main/java/com/tidy/app/ui/components/ClickableLinkRow.kt) | Full-row action with a label and optional trailing icon, defaulting to `OpenInNew`. The icon is decorative; the caller supplies the destination action. |
| [ScreenSectionHeader and CardSectionLabel](../app/src/main/java/com/tidy/app/ui/components/SectionHeader.kt) | Primary-coloured headings at screen-section and card-group levels. The screen places them relative to their content. |
| [TidyTopAppBar](../app/src/main/java/com/tidy/app/ui/components/TidyTopAppBar.kt) | Transparent top bar with title, optional back button and trailing actions. A null `onBackClick` hides the back button. |
| [TidyModalBottomSheet](../app/src/main/java/com/tidy/app/ui/components/TidyModalBottomSheet.kt) | Surface-coloured modal sheet with 8 dp tonal elevation and the Material drag handle. The default sheet state skips partial expansion; callers can supply a different state. Attach the provided scroll connection to the content's `nestedScroll` modifier. |
| [TierSelectorRow](../app/src/main/java/com/tidy/app/ui/components/TierSelectorRow.kt) | Equal-width option cards driven by `selected`. Selection uses a primary border and container tint. Unlocked taps call `onSelect`; locked cards remain tappable and call `onLockedTap`. |
| [AppIconBox](../app/src/main/java/com/tidy/app/ui/components/AppIconBox.kt) | Decorative icon container. Defaults to a 48 dp box, 24 dp icon and 12 dp corners, with primary tint over `primaryContainer` at 50% alpha. |
| [FeatureRow](../app/src/main/java/com/tidy/app/ui/components/FeatureRow.kt) | Decorative leading icon with title and supporting description. Accepts content and an outer modifier. |
| [TooltipWrapper](../app/src/main/java/com/tidy/app/ui/components/TooltipWrapper.kt) | Material plain tooltip positioned above its content. Supply a concise action label. |

The Settings toggle pattern makes the whole row the action target and gives its `Switch`
`onCheckedChange = null`. A locked row routes its tap to the Tidy+ prompt. Locked tier cards
keep their lock badge and use the same prompt; a lock indicates an available upgrade action.

## Motion and feedback

[MainScreen](../app/src/main/java/com/tidy/app/ui/main/MainScreen.kt) defines Home transitions.
The clipboard callout and enabled confirmation use matching 220 ms fade and vertical
expand/shrink transitions. Their trailing gap is inside the animated content, so it disappears
with the banner. The clipboard suggestion is part of the bottom container's `AnimatedContent`
transition, which has its own fade and size timings.

Keep a result actionable as soon as it is available. Preserve meaningful content throughout
entry and exit, and check transitions with system animations disabled. Errors and zero-removal
results must remain understandable without motion or colour cues.

Use a snackbar for feedback while the triggering screen is visible. A modal sheet needs its own
snackbar host inside the sheet so the message appears above the underlying screen. Keep messages
concise and bound any interpolated content. Use a toast when feedback must survive the
triggering view closing.

## Accessibility and copy

When changing a component or screen:

- Check TalkBack reading order, action labels and the announcement of selected, expanded and
  checked states. Keep decorative icons unlabelled when adjacent text already carries their meaning.
- Use Material controls with usable touch targets and verify keyboard focus and activation.
- Allow text to wrap and containers to grow with font scaling. Check long URLs and narrow layouts.
- Verify contrast in light and dark appearances and across the themes the component supports.
  Pair colour with text, shape or icon changes where it communicates a state.
- Keep user-facing text in [string and plural resources](../app/src/main/res/values/strings.xml).
  Use sentence case, British English and the term "URL". Describe each action and outcome directly.

[SettingsScreen](../app/src/main/java/com/tidy/app/ui/settings/SettingsScreen.kt) and
[AboutScreen](../app/src/main/java/com/tidy/app/ui/about/AboutScreen.kt) show the card patterns
in context. [HistoryScreen](../app/src/main/java/com/tidy/app/ui/history/HistoryScreen.kt) shows
section headings alongside summary cards and cleaned URL records.
