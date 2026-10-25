# Tidy design system

Use this guide when adding or changing UI. It describes shared decisions and component contracts.
Source files own exact dimensions, defaults and signatures. Examples here explain how to apply the shared contracts.

## Design principles

Keep cleaning immediate, results readable and actions predictable. Use Material controls where they fit.
Preserve context during transitions. Add decoration only when it helps users understand the interface.
Craft includes the complete flow: clear outcomes, thoughtful hierarchy, meaningful feedback and accessible actions.
Distinguish a changed URL, an unchanged result and a failed operation. Each needs an honest explanation.
Keep the calm visual character while making primary actions and results easy to find.

## Colour and typography

Use semantic roles from `MaterialTheme`, not literal colours at call sites.
The theme separates the canvas, cards and nested containers in light and dark modes.
Content surfaces are opaque. Resolve decorative tints over a known surface before drawing them.
Scrims, borders and transition fades can use transparency.

| Role | Purpose |
| --- | --- |
| `primary`, `onPrimary` | Primary actions and their content. |
| `primaryContainer`, `onPrimaryContainer` | Emphasised containers and their content. |
| `surface`, `onSurface` | Content surfaces and primary text. |
| `onSurfaceVariant` | Supporting text and secondary icons. |
| `errorContainer`, `onErrorContainer` | Errors and destructive actions. |
| `outline`, `outlineVariant` | Borders and separators. |

Clean statuses use muted green. Removed-parameter chips use muted red and open an explanation.
These semantic colours stay consistent across accent themes. Pair colour with text or another clear state indicator.

Use Material typography roles. Keep section and row headings semibold, with supporting text visually secondary.

| Text role | Material style | Weight |
| --- | --- | --- |
| App-bar title | `titleLarge` | Bold |
| Screen section | `titleMedium` | SemiBold |
| Card group | `labelLarge` | SemiBold |
| Expandable row title | `bodyLarge` | SemiBold |
| Row summary | `bodySmall` | Default |
| Destination row | `bodyMedium` | Medium |

See [Theme.kt](../app/src/main/java/com/tidy/app/theme/Theme.kt) and
[Type.kt](../app/src/main/java/com/tidy/app/theme/Type.kt) for palette and type definitions.

## Components and ownership

| Component | Use and contract |
| --- | --- |
| `SettingCard` | Passive group. The caller owns content, spacing and state. |
| `ExpandableSettingRow` | Full-row toggle. The caller owns `expanded`, `onToggle` and the expanded body. |
| `ClickableLinkRow` | Full-row destination action. Its trailing icon is decorative. |
| `ScreenSectionHeader`, `CardSectionLabel` | Accessible headings for screen sections and card groups. |
| `TidyTopAppBar` | Opaque bar that covers scrolling content. |
| `TidyModalBottomSheet` | Shared modal surface. Attach its provided nested-scroll connection to the content. |
| `TierSelectorRow` | Equal-width choices that stack when text needs space. Locked choices open the upgrade prompt. |
| `StatusBadge`, `CleanStatusBadge` | Passive status labels. Keep each badge intact when its surrounding header wraps. |
| `RemovedParameterChip` | One accessible action that opens a parameter explanation. |

Find implementations in [UI components](../app/src/main/java/com/tidy/app/ui/components).
Use existing components before introducing a new wrapper.

Switch and radio rows expose one action across the whole row. Their inner controls are passive.
Locked controls remain readable and open the same upgrade prompt. Decorative icons have no separate action or spoken label.

## Spacing and surfaces

Use [TidyCardTokens](../design-system/src/main/java/com/tidy/designsystem/TidyCardTokens.kt)
for named card roles. Do not reuse a token for an unrelated role just because its current value matches.

Card padding and row padding establish a shared content edge. Align headings, expanded content and dividers to that edge.
Keep expanded content outside the header's touch feedback. Let text grow without moving neighbouring actions vertically.

The public `:design-system` library owns custom surface defaults through
[TidyComponentStyles](../design-system/src/main/java/com/tidy/designsystem/TidyComponentStyles.kt).
`TidyTheme` supplies live palette roles through `TidyStyleTheme`.

Apply component defaults before caller styles. Read colours inside the style so theme changes resolve live.
Keep Material controls on their native APIs.

For custom surfaces, prefer `TidyStyledCard`. For a custom layout, apply `tidyStyleSurface`,
then the interaction modifier, then `tidyStyleContent`. This keeps padding inside the touch target.
Pass the same interaction source to the style resolver and interaction modifier.
Supply selected and enabled state explicitly.
Use `CommonStyle` from `androidx.compose.foundation.style` and helpers from `com.tidy.designsystem`.
Enable `ExperimentalFoundationStyleApi` where the module does not already opt in.

A passive switch inside a clickable row uses `onCheckedChange = null`. The row owns its action, checked state and switch role.
A locked row exposes an upgrade action instead. Theme rows form one selection group, with one radio action per row.

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

## Adaptive layouts

Adapt to window size and font scale, not device names. Let labels wrap and containers grow.
Move complete actions or badges to another row when space is limited.

Scrollable bodies accept gestures across the viewport, including side margins.
Bound the content inside the scroll container. Preserve system-bar, keyboard and display-cutout insets without applying them twice.

History uses a bounded adaptive grid with inline details. Entry expansion survives scrolling out of view.
Tier choices stack when their labels need more room. Home reduces its hero space in short windows.
These patterns keep existing actions available without adding separate navigation for wider windows.
Expanded History cards increase their grid row height. Review the space below shorter neighbours with real expanded content.
Keep Copy original, Copy and Open intact when actions wrap. A wide layout must not hide choices at large text sizes.

## Motion and feedback

Make results actionable as soon as they are available. Keep departing content visible until its transition finishes.
Put a disappearing element's spacing inside its transition. Verify the flow with system animations disabled.

Enable input actions when required fields are non-blank. Validate on submission and clear feedback when the input changes.
Use native text-field error styling and supporting text. Top-align input fields and adjacent actions.

Use a snackbar while the triggering screen remains visible. Give modal sheets their own snackbar host inside the sheet.
Use a toast only when feedback must outlive the triggering view. Keep messages concise and bound interpolated text.
Failed URL expansion must leave the cleaned result available and permit a retry.
After successful expansion, including an unchanged destination, make expansion unavailable for that result.
A new clean or Clear cancels pending expansion. Preserve the departing result through Clear without delaying the next usable result.

## Accessibility and copy

- Expose headings and selected, checked and expanded states to accessibility services.
- Keep one accessible action per control. Verify keyboard focus and activation.
- Check long URLs, narrow windows and 200% text. Preserve native touch targets.
- Check contrast in light and dark modes across supported themes.
- Keep UI text in string or plural resources. Use sentence case, British English and the term "URL".
- State each action and result directly. Do not rely on colour or motion alone.

## Verification

Use host screenshots for appearance across sizes, themes and text scales.
Use instrumented tests for actions, semantics and state. Check system bars, keyboard behaviour and spoken accessibility on a device.
See [Contributing](../CONTRIBUTING.md#screenshot-tests) for screenshot commands and reference review.

Screen content composables accept explicit state and callbacks. Previews use these same components without starting repositories or billing.
Preserve that separation when adding new screens or fixtures.
