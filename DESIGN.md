---
name: ZotMacros
description: Native Android nutrition tracking in UCI blue and gold.
colors:
  gold: "#FECC07"
  blue-background: "#103B67"
  blue-surface: "#255799"
  protein: "#65D7FF"
  fat: "#38E1ED"
  ink-on-gold: "#102A45"
  gold-container: "#715900"
  foreground: "#F5F8FF"
  muted: "#D4E5FF"
  surface-variant: "#1D4D81"
  outline: "#8AAFD7"
  outline-variant: "#3B6DA5"
  surface-highest: "#2D609F"
  surface-low: "#184573"
  error: "#FFD4D4"
  on-error: "#690005"
typography:
  display-large:
    fontFamily: "Android system sans-serif"
    fontSize: "56sp"
    fontWeight: 700
  display-medium:
    fontFamily: "Android system sans-serif"
    fontSize: "44sp"
    fontWeight: 700
  headline-large:
    fontFamily: "Android system sans-serif"
    fontSize: "30sp"
    fontWeight: 700
  headline-medium:
    fontFamily: "Android system sans-serif"
    fontSize: "26sp"
    fontWeight: 700
  title-large:
    fontFamily: "Android system sans-serif"
    fontSize: "22sp"
    fontWeight: 700
  title-medium:
    fontFamily: "Android system sans-serif"
    fontSize: "16sp"
    fontWeight: 600
  body-large:
    fontFamily: "Android system sans-serif"
    fontSize: "16sp"
    fontWeight: 400
  body-medium:
    fontFamily: "Android system sans-serif"
    fontSize: "14sp"
    fontWeight: 400
  label-large:
    fontFamily: "Android system sans-serif"
    fontSize: "14sp"
    fontWeight: 600
rounded:
  chart-bar: "4dp"
  search: "16dp"
  card: "20dp"
  navigation: "28dp"
spacing:
  step-4: "4dp"
  step-6: "6dp"
  step-8: "8dp"
  step-10: "10dp"
  step-12: "12dp"
  step-14: "14dp"
  step-16: "16dp"
  step-18: "18dp"
  step-20: "20dp"
  step-28: "28dp"
components:
  button-primary:
    backgroundColor: "{colors.gold}"
    textColor: "{colors.ink-on-gold}"
    typography: "{typography.label-large}"
  chip-selected:
    backgroundColor: "{colors.gold}"
    textColor: "{colors.ink-on-gold}"
  card:
    backgroundColor: "{colors.blue-surface}"
    textColor: "{colors.foreground}"
    rounded: "{rounded.card}"
    padding: "16dp"
  macro-card:
    backgroundColor: "{colors.blue-surface}"
    rounded: "{rounded.card}"
    padding: "12dp"
  search:
    textColor: "{colors.foreground}"
    rounded: "{rounded.search}"
  navigation:
    backgroundColor: "{colors.blue-surface}"
    rounded: "{rounded.navigation}"
  calorie-ring:
    size: "230dp"
---
# Design System: ZotMacros

## Overview

**Creative North Star: "UCI blue and gold"**

A bright, confident UCI blue-and-gold identity gives daily nutrition a clear, approachable rhythm. Blue surfaces organize information; gold marks progress, selection, and the main action. Large numbers carry the hierarchy while labels, units, and plain-language status keep the data understandable.

This is a native Android Compose and Material 3 system. The user-approved visual authority is `docs/design/approved-mockup.png`; the token values above are extracted from the implemented theme and components. Platform typography, interaction states, insets, and accessibility semantics take precedence over reproducing the illustrative mockup pixel for pixel.

**Key Characteristics:**
- UCI blue backgrounds with brighter blue cards and sheets.
- Gold primary actions and selected controls.
- Bold numerical hierarchy with compact, readable supporting text.
- Native controls, tonal grouping, and explicit nutrition status.

## Colors

The palette combines campus blue tonal surfaces, a clear gold accent, and cool macro highlights. Frontmatter is normative; `Theme.kt` maps these primitives into Material roles.

### Primary
- **Gold:** primary actions, calorie progress, carbohydrate values, and selected chips and segmented controls. Both `primary` and `secondaryContainer` use gold, with dark ink text.
- **Gold container:** the theme's deeper primary container, paired with gold content.

### Secondary
- **Protein cyan:** protein values and progress, plus the theme's secondary role.
- **Fat aqua:** fat values and progress, plus the theme's tertiary role.

### Neutral
- **Blue background:** full-screen canvas and macro progress tracks.
- **Blue surface:** cards, bottom navigation, calorie-ring track, and food sheets.
- **Surface variant, low, and highest:** supporting Material containers. Surface container and surface variant share a value; high uses blue surface.
- **Foreground and muted:** primary content and supporting content respectively.
- **Outline and outline variant:** input boundaries and subtle separators.
- **Error and on-error:** native input validation; not an indication of exceeding a nutrition goal.

**The Labeled Color Rule.** Every nutrient color is accompanied by a readable nutrient label or unit.

## Typography

Use Android system sans-serif (normally Roboto) through Compose `FontFamily.SansSerif`. The frontmatter captures the nine explicitly customized Material roles. Line height and letter spacing are not explicitly set in these custom styles; do not invent a fixed scale for them. Remaining Material roles retain library defaults.

Display medium carries calorie totals; headline large introduces screens; headline medium titles the food sheet. Title large identifies sections and macro amounts; title medium names foods and controls. Body medium supplies muted metadata, and label large supports compact values and actions. Display large is defined by the theme for larger numerical hierarchy. The brand's gold Z uses the headline role with a black font-weight override.

## Layout

Screens use vertically scrolling native lists with 20dp content padding. Typical section gaps are 16dp; History uses 18dp and Settings uses 20dp. Macro cards form three equal columns with 8dp gaps and 12dp padding. Meal and chart surfaces use 16dp padding. Supporting rows use smaller gaps from the recorded scale.

The navigation changes from a four-destination bottom bar to a rail at 600dp available width. Screen content remains centered with a 720dp maximum width. The bottom bar has 12dp horizontal and 8dp vertical outside padding. Scaffold and navigation insets protect system controls.

Food details use a scrollable modal sheet with 20dp horizontal padding and a save action outside the scrolling region. It respects keyboard and navigation insets. Primary actions have a minimum height of 52dp; the sheet action uses 54dp. Preserve minimum 48dp interactive targets even when icons appear smaller.

## Elevation & Depth

Custom cards are flat at rest: blue tonal separation and subtle dividers establish grouping, with no custom shadow vocabulary. Material sheets, dialogs, and controls retain their native elevation and scrim behavior. Do not replace those platform effects with a web shadow system.

Tab destinations crossfade with 180ms arrivals and 120ms exits. Calorie-ring and macro-bar progress share a 280ms FastOutSlowIn tween; numeric totals remain immediate and truthful. Food sheets retain their content until the native Material hide transition finishes, including after a save. All finite animations inherit Android’s animator duration scale, including Remove animations. History scrolling and control feedback retain Material behavior. Motion tokens live in `ui/Motion.kt`.

## Shapes

Rounded rectangles soften the dense information: 20dp cards, 16dp search field corners, and a 28dp bottom navigation clip. Small chart bars use 4dp corners. Buttons, chips, segmented controls, other fields, sheets, and dialogs use their Material 3 component defaults; the app does not override the global shape scale.

## Components

### Buttons
Gold filled buttons with dark ink communicate the main action. Their minimum height can expand with content and font scale. Outlined buttons support retry; text buttons support secondary actions and attribution. Native Material supplies pressed, focused, disabled, and ripple behavior. Disabled save controls remain unavailable while input is invalid or a save is in progress.

### Chips and segmented controls
Selected filter chips and dining-hall segments use gold `secondaryContainer` with dark content. Native selection semantics and Material indicators reinforce color. Meal, station, and recent-day rows scroll horizontally; serving and meal choices wrap in flow rows.

### Cards and lists
Macro cards show a colored label, bold amount, muted goal, and 6dp progress bar. Meal cards group editable food rows with subtle dividers. Menu rows stay on the blue canvas with separators and a labeled add affordance. Keep touch feedback and accessible names on interactive surfaces.

### Inputs
Outlined Material fields use visible labels for goals and serving quantities; the rounded search field uses a search icon and placeholder. Numeric fields request the decimal keyboard, mark invalid input with Material error semantics, and provide visible supporting guidance. Focused outlines use the theme primary role. Native defaults govern disabled states.

### Navigation
Today, Menu, History, and Settings retain text labels alongside icons. Compact navigation uses gold selected icon and text on blue, with its indicator matching the blue surface. The expanded rail uses Material defaults, including the theme's gold selected container. These two treatments are intentionally distinct in the implementation.

### Calorie ring and charts
The ring is 230dp across with a 13dp round-capped stroke and a 188dp text column. The visible arc clamps to a full circle; readable text still reports the true amount and neutral over-goal status. Its accessible description includes total, goal, and partial status. Seven-day charts use 90dp bar areas, readable daily values, and explicit legends for unavailable or partial data.

### Food sheet
A blue modal sheet introduces the dish, serving basis, live scaled calories and macros, serving controls, meal choice, and nutrition details. The anchored gold action summarizes its outcome. Missing nutrients read “Unavailable”; compact missing numbers can use an em dash with explanatory context. Confirmation dialogs protect destructive actions.

## Do's and Don'ts

### Do:
- Do use dp for layout and sp for text, preserving Android font scaling.
- Do pair macro colors with labels and units.
- Do show missing values and partial totals explicitly.
- Do reuse the blue surfaces and gold selection roles from the theme.
- Do keep touch targets at least 48dp and honor system and keyboard insets.

### Don't:
- Don’t restore the superseded lime palette.
- Don’t turn over-goal progress into an error state.
- Don’t add gradients, stock photography, or decorative animation.
- Don’t substitute fabricated data for empty or unavailable states.
- Don’t introduce web hover conventions as native Android requirements.
