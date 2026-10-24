# Tidy design system

This guide describes the Compose theme, reusable components and layout conventions used in
Tidy's Android app. Use it when adding a screen or changing an existing interaction.

## Design principles

- Put the cleaned URL and the changes made to it first. Keep supporting actions easy to find.
- Use restrained surfaces and clear typography to distinguish content, controls and feedback.
- Preserve Android navigation, touch feedback, scrolling and text scaling behaviour.
- Prefer existing Compose Material components over custom imitations. Check Compose
  Multiplatform availability for reusable UI; keep platform services at screen hosts.
- Express outcomes precisely: a successful clean, an unchanged URL and a failed operation carry
  different meanings.

## Colour and typography

[TidyTheme](../app/src/main/java/com/tidy/app/theme/Theme.kt) supplies the active
`MaterialTheme.colorScheme` and typography. The default Sage Slate palette has light and dark
schemes defined in [Color.kt](../app/src/main/java/com/tidy/app/theme/Color.kt).
Read colour roles from the active theme so components also work with alternative schemes.

Light mode uses a muted slate canvas (`#CBD5E1`) with lighter off-white surfaces (`#F1F5F9`).
The canvas and neutral surface roles stay consistent across accent palettes, including Material
You. App bars and launch backgrounds follow the canvas; cards and sheets use opaque surfaces.
Accent colours continue to identify actions and emphasised containers.

The normal `TidyTheme` entry point observes app settings and entitlement state. The overload
accepting `selectedTheme`, `isPlusUnlocked` and `darkTheme` renders explicit state in previews.
Both supply the Material palette to `TidyStyleTheme`, so Styles follow live theme changes.

| Role | Use |
| --- | --- |
| `primary`, `onPrimary` | Primary actions and their foreground content. |
| `primaryContainer`, `onPrimaryContainer` | Emphasised containers and their foreground content. |
| `surface`, `onSurface` | Surfaces and primary text. |
| `onSurfaceVariant` | Supporting text and secondary icons. |
| `errorContainer`, `onErrorContainer` | Error or destructive-action containers and their foreground content. |
| `outline`, `outlineVariant` | Borders and separators. |

`SettingCard`, History cards and statistics cards use opaque `surface` backgrounds in both
light and dark mode. Their rows use `onSurface` for titles and `onSurfaceVariant` for supporting
text. Banners and selected controls use opaque semantic containers. Decorative tints are
composited over a known surface before drawing; borders, text and transition effects may
still vary their opacity. Check contrast against each rendered background.

[Type.kt](../app/src/main/java/com/tidy/app/theme/Type.kt) configures `bodyLarge` with the
platform default font, normal weight, 16 sp size, 24 sp line height and 0.5 sp letter spacing.
The remaining theme styles use Material 3 defaults. Components apply these styles and weights:

| Component text | Style | Weight |
| --- | --- | --- |
| `TidyTopAppBar` title | `titleLarge` | Bold |
| `ScreenSectionHeader` | `titleMedium` | SemiBold |
| `CardSectionLabel` | `labelLarge` | SemiBold |
| `ExpandableSettingRow` title | `bodyLarge` | SemiBold |
| `ExpandableSettingRow` summary | `bodySmall` | Style default |
| `ClickableLinkRow` label | `bodyMedium` | Medium |

## Cards and alignment

Shared tokens are defined in
[the design-system module](../design-system/src/main/java/com/tidy/designsystem/TidyCardTokens.kt).
The app's `TidyCardTokens` type alias preserves existing imports.

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
| [ExpandableSettingRow](../app/src/main/java/com/tidy/app/ui/components/ExpandableSettingRow.kt) | Full-row toggle with title, summary and an up/down chevron. `expanded` controls the decorative chevron and the row's expanded/collapsed state description; the row exposes an expand/collapse action label. The caller controls the body and state. |
| [ClickableLinkRow](../app/src/main/java/com/tidy/app/ui/components/ClickableLinkRow.kt) | Full-row action with a label and optional trailing icon, defaulting to `OpenInNew`. The icon is decorative; the caller supplies the destination action. |
| [ScreenSectionHeader and CardSectionLabel](../app/src/main/java/com/tidy/app/ui/components/SectionHeader.kt) | Primary-coloured headings at screen-section and card-group levels, exposed as accessibility headings. The screen places them relative to their content. |
| [TidyTopAppBar](../app/src/main/java/com/tidy/app/ui/components/TidyTopAppBar.kt) | Material surface top bar with title, optional back button and trailing actions. Its opaque background covers scrolling content. A null `onBackClick` hides the back button. |
| [TidyModalBottomSheet](../app/src/main/java/com/tidy/app/ui/components/TidyModalBottomSheet.kt) | Surface-coloured modal sheet with 8 dp tonal elevation and the Material drag handle. The default sheet state skips partial expansion; callers can supply a different state. Attach the provided scroll connection to the content's `nestedScroll` modifier. |
| [TierSelectorRow](../app/src/main/java/com/tidy/app/ui/components/TierSelectorRow.kt) | Option cards driven by `selected`. Their Grid normally uses equal-width columns and stacks vertically when width or font scaling leaves less than 96 dp per option after scaling. Selection uses a primary border and container tint and is exposed through radio-button semantics. Unlocked taps call `onSelect`; locked cards retain readable text, announce their premium state and call `onLockedTap`. |
| [AppIconBox](../app/src/main/java/com/tidy/app/ui/components/AppIconBox.kt) | Decorative icon container. Defaults to a 48 dp box, 24 dp icon and 12 dp corners, with primary tint over `primaryContainer` at 50% alpha. |
| [FeatureRow](../app/src/main/java/com/tidy/app/ui/components/FeatureRow.kt) | Decorative leading icon with title and supporting description. Accepts content and an outer modifier. |
| [TooltipWrapper](../app/src/main/java/com/tidy/app/ui/components/TooltipWrapper.kt) | Material plain tooltip positioned above its content. Supply a concise action label. |

The Settings toggle pattern makes the whole row the action target and gives its `Switch`
`onCheckedChange = null`. A locked row routes its tap to the Tidy+ prompt. Locked tier cards
keep their lock badge and use the same prompt; a lock indicates an available upgrade action.

## Compose Styles

The public `:design-system` library owns
[TidyStyle](../design-system/src/main/java/com/tidy/designsystem/TidyStyle.kt), an alias for
Compose's experimental `CommonStyle`, and
[TidyComponentStyles](../design-system/src/main/java/com/tidy/designsystem/TidyComponentStyles.kt).
Component defaults define surface colours, shapes, borders, dimensions and content padding.
Caller-supplied styles override those defaults. Interaction state can change borders and fills,
including selected tier cards and keyboard-focused rows.

`SettingCard`, `ExpandableSettingRow`, `ClickableLinkRow`, `TierSelectorRow`, `AppIconBox` and
`FeatureRow` accept a `style` argument. `AppIconBox` keeps separate `iconSize` and `tint` arguments;
its container geometry and fill are supplied through Styles:

```kotlin
AppIconBox(
    icon = icon,
    style = CommonStyle {
        size(40.dp)
        shape(RoundedCornerShape(10.dp))
        background(LocalTidyColours.currentValue.primaryContainer.copy(alpha = 0.4f))
    }
)
```

Import `CommonStyle` from `androidx.compose.foundation.style` and the style helpers and
`LocalTidyColours` from `com.tidy.designsystem`. Opt in to `ExperimentalFoundationStyleApi`
when using these APIs in a module that does not already enable the compiler opt-in.

For a new custom surface, compose defaults and overrides and use
[TidyStyledCard](../design-system/src/main/java/com/tidy/designsystem/TidyStyledCard.kt).
It owns clipping, optional click handling, ripple and style-controlled content padding.
For a custom layout, use `rememberTidyStyle`, attach `tidyStyleSurface` once, then the interaction
modifier, then `tidyStyleContent`. Padding remains inside the hit target. Supply the same
interaction source to the resolver and interaction modifier, and pass selected/enabled state
explicitly. Keep colour reads inside the style definition so a palette change resolves live.

Material controls use their native component APIs. Passive status labels use
[StatusBadge](../app/src/main/java/com/tidy/app/ui/components/StatusBadge.kt), a thin wrapper around
Material 3 `Badge` with semantic theme colours and the native shape and typography.
Its text adds 4 dp horizontal and 2 dp vertical padding to the native inset. Labels stay on one
line; wrapping headers move the whole badge below the heading when necessary. Action and selection chips retain their native Material semantics.
Home uses a lightly theme-tinted green success pair for removal counts, with text as the state
label. Removed-parameter action chips use Material error-container colours. The result heading
wraps its badge as a complete item when text is enlarged.
Typography and motion retain the existing Material roles and local transition definitions.

## Adaptive layouts

- Tier options use Compose `Grid` with equal fractional tracks and zero minimum track width.
  They stack when available width divided by font scale leaves less than 96 dp per option.
- History uses `LazyVerticalGrid` with a 320 dp minimum cell width, increased for larger text.
  Its content is bounded to 1000 dp; summary sections and headers span all columns. Entry details
  expand inside their cards, and parameter inspection uses the shared `ParamDetailBottomSheet`.
  Expanding one entry increases its grid row height, leaving space below shorter neighbours
  and moving subsequent rows down.
- The FOSS upgrade prompt stacks its icon header above full-width content when available
  width divided by font scale falls below 160 dp, preserving room for the padded status badge.
- About content is bounded to 650 dp. Width limits precede fill modifiers so they constrain
  expanded windows.
- Home's trust labels wrap through `FlexBox`. `MediaQuery` treats windows below 480 dp in height
  as compact and reduces the reserved hero space. The application enables MediaQuery integration
  before creating Compose content.

Scrollable screen bodies receive gestures across the full viewport, including the side margins.
Keep Home, Settings and About content bounded inside that scroll container. History centres
its bounded grid content through padding while the grid itself fills the viewport. Preserve
horizontal Scaffold insets in that padding. Home bottom controls avoid both navigation bars
and display cutouts; their surface can still draw to the screen edge.

Use window size and font scale rather than device names when adapting content. Preserve control
semantics and the existing action callbacks while changing layout.

## Visual and interaction tests

AGP's native screenshot suite renders About, Home, History and Settings at nine window sizes,
with dark mode, large text and theme variations. Home covers welcome and cleaned-result states;
History covers populated and empty states. Shared Settings/tier fixtures cover light, dark,
wide and large-text configurations. Runtime screen entry points collect application state and
pass explicit values and actions to their content composables, so previews use the real screen
without starting repositories or billing. The explicit-state theme overload and preview MediaQuery
scope keep host rendering deterministic. See
[the contributor commands](../CONTRIBUTING.md#screenshot-tests) for comparisons and intentional
reference updates.

Instrumented Compose tests verify expanded/selected states, locked actions, equal tier widths,
caller style overrides, live palette changes and large-text stacking. Screenshots check
appearance; continue device checks for scrolling, system bars, the keyboard and accessibility.
Screen-content tests also verify Copy/Share callbacks, inline History expansion and Settings rules.

## Motion and feedback

[MainScreen](../app/src/main/java/com/tidy/app/ui/main/MainScreen.kt) defines Home transitions.
The clipboard callout and enabled confirmation use matching 220 ms fade and vertical
expand/shrink transitions. Their trailing gap is inside the animated content, so it disappears
with the banner. The clipboard suggestion is part of the bottom container's `AnimatedContent`
transition, which has its own fade and size timings.

Keep a result actionable as soon as it is available. Preserve meaningful content throughout
entry and exit, and check transitions with system animations disabled. Errors and zero-removal
results must remain understandable without motion or colour cues.
Home retains the departing result during Clear and reveals completed content without a
completion-spinner delay. Successful expansion becomes unavailable, including unchanged results;
failures preserve retry. A new clean or Clear cancels a pending expansion.

The shared parameter sheet stacks its actions at full width. Report issue uses an outlined
button and Always keep uses a quieter text button, reflecting the persistent exception it creates.
About uses external Project section headings aligned with the Settings hierarchy.

Enable input actions when their required fields are non-blank, and validate on submission.
Invalid submissions use native Material text-field error styling and supportingText. Input
rows align their fields and actions at the top, so supporting feedback cannot shift neighbouring
controls, including when text is enlarged. Editing clears the feedback.
Reuse the same remembered validation result for feedback and submission.

Use a snackbar for feedback while the triggering screen is visible. A modal sheet needs its own
snackbar host inside the sheet so the message appears above the underlying screen. Keep messages
concise and bound any interpolated content. Use a toast when feedback must survive the
triggering view closing. Explicit URL expansion reports network failures in a snackbar while
keeping the cleaned result available.

## Accessibility and copy

Settings switch rows expose their checked state and switch role on the whole row. Their inner
switch is passive; a locked row instead exposes a button that opens the Tidy+ prompt. Theme rows
form a selection group with one radio-button action per row, including its label and lock indicator.
Passive radio indicators retain 12 dp padding to preserve the existing layout. Off-state switch
thumbs and borders use `onSurfaceVariant` against `surfaceVariant`.

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
section headings alongside summary cards and cleaned URL records. History entry expansion
is saved per entry, so scrolling a card out of view does not collapse it. App-bar titles stay
on one line and ellipsise when space is limited.
