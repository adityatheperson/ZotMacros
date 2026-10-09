# Build ZotMacros — Native Android MVP

## 1. Project Overview

Build a fully functional native Android application called **ZotMacros**.

**Tagline:** Fuel your day. Track your macros.

ZotMacros is a premium fitness-focused nutrition tracker designed for UC Irvine students. It integrates with the Anteater API to retrieve real-time dining hall menus, allowing students to easily log their meals and track daily calories, protein, carbohydrates, fats, and micronutrients.

The application should look and feel like a professionally designed fitness app, inspired by MacroFactor, MyFitnessPal, and Apple Fitness.

**Core experience:**

1. Open ZotMacros.
2. See today's calorie and macro progress.
3. Browse today's UCI dining hall menus.
4. Select a specific food.
5. Adjust serving quantity.
6. Add it to today's food log.
7. Instantly update all nutritional totals.

The entire food-logging process should require as few taps as possible.

## 2. Technology Stack

Build a real Android application, not a website or WebView.

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM with Repository pattern
- **HTTP:** Retrofit + OkHttp
- **JSON:** Kotlinx Serialization
- **Local database:** Room
- **Preferences:** Jetpack DataStore
- **Asynchronous operations:** Coroutines + StateFlow
- **Dependency injection:** Hilt
- **Navigation:** Navigation Compose
- **Charts:** Compose Canvas or a lightweight chart library
- **Minimum SDK:** Android 8.0 (API 26)

Ensure the app compiles and works on a Google Pixel 8 Pro.

Use clean, modular, maintainable code.

## 3. Anteater API Integration

Documentation:

https://anteaterapi.com/reference

REST base URL:

https://anteaterapi.com/v2/rest

Relevant dining endpoints:

- `GET /dining/restaurants`
- `GET /dining/restaurantToday`
- `GET /dining/dateRange`
- `GET /dining/dishes/{id}`
- `/dining/dishes/batch`
- `GET /dining/schedules`

**Before implementation, inspect the official API documentation to determine the actual parameters, response structures, and supported nutrition fields.**

### Required functionality

- Retrieve today's dining hall menu using the America/Los_Angeles timezone.
- Support The Anteatery and Brandywine.
- Organize menu items by dining hall, meal period, and food station/section where available.
- Retrieve nutritional values for individual dishes.
- Show calories, protein, carbohydrates, and fat when reported.
- Support additional micronutrients including sodium, fiber, sugar, iron, calcium, and cholesterol when available.
- Cache menus locally.
- Allow pull-to-refresh.
- Use batch requests when appropriate to minimize unnecessary API traffic.
- Correctly handle missing values and malformed responses.
- Follow Anteater API attribution requirements.

Never invent missing nutritional data. A missing value should display as unavailable, not zero.

## 4. Visual Identity

ZotMacros must have a sleek, high-end gym aesthetic.

Think premium fitness dashboard, not a generic school application.

### Brand colors

| Element | Color |
|---|---|
| Background | `#101114` |
| Cards | `#1C1E22` |
| Accent lime | `#C8F568` |
| Primary text | `#F5F5F5` |
| Secondary text | `#9A9DA3` |
| Protein | `#7BA9FF` |
| Carbs | `#F5C76B` |
| Fats | `#F08AA5` |

### Design guidelines

- Dark-mode-first interface.
- Large, bold typography for numerical values.
- Rounded cards with consistent spacing.
- Smooth animated progress indicators.
- Clean, understated icons.
- Subtle haptic feedback for food logging.
- Polished bottom sheets.
- Smooth navigation and screen transitions.
- Responsive layouts.
- Minimal visual clutter.
- No unnecessary gradients or excessive decorations.

Create a recognizable ZotMacros brand identity with a minimal stylized Z or anteater-inspired icon.

## 5. Navigation

Use four bottom navigation tabs.

**Today** — Nutrition dashboard and logged foods.

**Menu** — Today's UCI dining menus.

**History** — Previous food logs and trends.

**Settings** — Goals, preferences, and dining hall selection.

The bottom navigation bar should be compact, floating-style, dark, and premium-looking.

## 6. Today — Main Dashboard

The Today screen is the most important visual component.

### Header

Display:

- ZotMacros branding
- Current date
- Greeting such as "Let's fuel up."
- Optional small settings shortcut

### Calorie progress

Create a prominent circular calorie progress visualization.

At its center display:

- Total calories consumed
- Daily calorie goal
- Calories remaining

Example:

**1,420**\
of 2,400 kcal

980 remaining

Animate the ring whenever calories change.

Do not treat exceeding a goal as an error. Show over-goal progress clearly.

### Macro cards

Below the calorie display, show three compact cards:

**Protein** — 112 / 160 g\
**Carbs** — 145 / 250 g\
**Fats** — 48 / 75 g

Each card contains a progress bar using its corresponding accent color.

All values must be calculated using actual logged foods.

### Today's Food

Display a chronological list of logged foods, grouped into:

- Breakfast
- Lunch
- Dinner
- Snacks

Each food row should display:

- Food name
- Servings consumed
- Calories
- Protein
- Optional meal time

Support tapping to edit and swiping or using a menu to delete.

Include a visually prominent **+ Add Food** button.

When the user logs a food, all dashboard totals must update immediately.

## 7. Menu — Dining Hall Browser

Build a beautiful dining hall menu browser.

### Top controls

- Dining hall segmented selector: Anteatery / Brandywine
- Today's date
- Breakfast / Lunch / Dinner selectors
- Search bar
- Station/section filters

Only show meal periods and sections actually returned by the API.

### Menu organization

Group foods by station/section.

For example:

**The Grill**

Grilled Chicken Breast\
165 kcal · 31g protein\
[+]

**Salad Bar**

Mixed Greens\
20 kcal · 2g protein\
[+]

Example values are for UI demonstration only and must never be presented as real API values.

### Food cards

Each card should show:

- Food name
- Serving size
- Calories
- Protein
- Carbs
- Fats
- Quick-add button

Tapping the card opens its nutrition details.

Tapping the add button opens a serving selector bottom sheet.

Include search functionality across all foods in the selected dining hall and meal.

Display an unobtrusive indicator for foods already logged.

## 8. Food Details and Serving Selection

When a user selects a food, open a polished detail screen or expanded bottom sheet.

### Food information

Display:

- Name
- Dining hall
- Station
- Meal period
- Serving size
- Calories
- Protein
- Carbs
- Total fats
- Saturated fats
- Sugar
- Fiber
- Sodium
- Cholesterol
- Iron
- Calcium
- Other available nutrients
- Ingredients/allergens when reported

Group information into:

**Macronutrients**

Prominent values and visual indicators.

**Nutrition Facts**

Compact, readable micronutrient breakdown.

### Serving adjustment

Provide quick presets:

`0.5×` `1×` `1.5×` `2×` `Custom`

Users should also be able to directly enter a decimal serving quantity.

Multiply the reported per-serving nutritional values by the selected quantity.

For example, two servings of a food containing 20g protein per serving should log 40g protein.

Do not round intermediate calculation values unnecessarily.

### Add action

Include a large lime-green button:

**Add to Today — 320 kcal**

When tapped:

1. Save the entry into Room.
2. Update daily totals.
3. Provide subtle haptic confirmation.
4. Return to the previous screen or close the sheet.
5. Show a brief success confirmation.

## 9. Food Logging and Persistence

Implement a reliable local food diary.

Each entry should include:

- Unique ID
- Date
- Timestamp
- Dish ID
- Food name
- Dining hall
- Station
- Meal category
- Serving quantity
- Original serving size
- Per-serving nutritional snapshot
- Calculated nutrient totals

### Logging requirements

- Users can add multiple foods.
- Users can add the same food multiple times.
- Serving quantities can be changed.
- Entries can be deleted.
- Entries persist after app restarts.
- Daily totals are calculated automatically.
- Historical food logs must not change when the API updates nutritional data.
- Past logs must work offline.

Represent nutrients as nullable numeric values with explicit units.

Missing nutritional fields must remain unknown rather than being silently converted to zero.

When calculating a daily nutrient total, distinguish between a complete total and a partial total if some logged foods lack that nutrient.

## 10. History Screen

Implement a minimalist historical nutrition dashboard.

Features:

- Calendar/day selector.
- Previous daily nutrition totals.
- List of foods logged on each date.
- Edit and delete historical entries.
- Seven-day calorie chart.
- Seven-day protein chart.

Use the same colors as the main dashboard.

If a day has no entries, display an appropriate empty state rather than fabricated progress data.

## 11. Settings

Allow users to configure:

### Nutrition goals

- Daily calories
- Protein target
- Carbohydrate target
- Fat target

Display editable values with units.

Initialize targets with clearly labeled example defaults that users can replace.

### Dining preferences

- Preferred dining hall
- Default meal period behavior
- Auto-refresh menus

### Application preferences

- Haptic feedback toggle
- Clear local food history with confirmation
- About ZotMacros
- Anteater API attribution

Persist preferences using DataStore.

## 12. Onboarding

Create a simple onboarding flow shown only on first launch.

**Screen 1 — Welcome**

ZotMacros\
"Fuel your day. Track your macros."

A clean branded introduction.

**Screen 2 — Choose your dining hall**

Anteatery or Brandywine.

**Screen 3 — Set your goals**

Daily calories, protein, carbs, and fats.

**Screen 4 — Get started**

Enter the Today dashboard.

Onboarding should be skippable, with sensible editable placeholder goals.

Do not require account creation or login.

## 13. Offline Behavior and API Reliability

The app should remain usable even when the Anteater API is temporarily unavailable.

Requirements:

- Cache recently retrieved menus locally.
- Display a clear loading indicator when fetching menus.
- Show a retry action for failed requests.
- Display the last successful menu retrieval time.
- Identify stale menu data.
- Do not label cached historical menus as today's confirmed menu.
- Keep logged meals accessible offline.
- Never erase saved food logs due to an API failure.

Use a repository abstraction that separates remote data, cached menus, and user food logs.

## 14. Important Edge Cases

Correctly handle:

- Empty dining hall menus
- Unavailable restaurants
- Missing meal periods
- Missing food stations
- Duplicate food names
- Dishes without nutrition facts
- Inconsistent nutrient units
- Decimal serving quantities
- Multiple entries of the same dish
- App restarts
- Device offline
- API rate limits or failures
- Midnight date transitions
- Repeated taps on the add button
- Nutritional goals being exceeded

The app must never crash when a nutritional field is absent.

## 15. MVP Exclusions

Do not add unnecessary complexity.

Exclude:

- Authentication
- Cloud accounts
- Social features
- AI meal recommendations
- Barcode scanning
- Payment features
- Wearable integrations
- Weight-loss coaching
- Subscription systems
- Advertisement infrastructure

Focus on excellent execution of the core experience.

## 16. Implementation Order

Build the application incrementally.

**Phase 1 — Foundation**
- Initialize Android Studio project.
- Configure dependencies.
- Set up MVVM architecture.
- Create navigation.
- Implement design system.

**Phase 2 — API**
- Inspect Anteater API schemas.
- Implement dining repositories.
- Retrieve actual restaurant menus.
- Fetch dish nutritional data.
- Organize menus by meal and station.

**Phase 3 — Nutrition Tracking**
- Implement Room database.
- Implement serving quantity calculations.
- Build food logging logic.
- Implement automatic daily nutrient aggregation.

**Phase 4 — User Interface**
- Build Today dashboard.
- Build Menu browser.
- Build food details and quick-add sheets.
- Build History and Settings.

**Phase 5 — Polish and Testing**
- Improve animations.
- Add empty/loading/error states.
- Test food logging.
- Test persistence.
- Test API integration.
- Verify calculations.
- Test the application on an Android device.

## 17. Deliverables

Produce:

1. Complete Android Studio project.
2. Fully implemented Kotlin source code.
3. Real Anteater API integration.
4. Room database and migrations strategy.
5. All four functioning screens.
6. Responsive Jetpack Compose interface.
7. Working nutrition calculations.
8. Persistent historical food logs.
9. Unit tests for serving calculations and daily nutrient aggregation.
10. Build instructions and a debug APK if the environment supports Android builds.

Do not substitute fake sample menus for real API integration.

If the API cannot provide a requested nutrient, gracefully handle that limitation rather than fabricating values.

## 18. MVP Completion Criteria

The MVP is complete when I can install ZotMacros on my Android phone, open it, select The Anteatery or Brandywine, see today's actual menu separated by meal and section, select foods, adjust servings, log what I ate, and immediately see my total calories, protein, carbs, fats, and other available nutrients update.

I must also be able to close the app, reopen it, and retain all of my food history.

**The highest priorities are functional API integration, accurate nutrition calculations, extremely fast food logging, and a beautiful premium gym-style interface.**

Build the actual working application end-to-end. Do not stop at a UI prototype.
