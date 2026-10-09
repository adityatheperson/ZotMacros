# Anteatery lunch comparison — October 8, 2026

Source: live MyDiningHub Adobe GraphQL response; mealPeriod 25, date 2026-10-08. Collector base commit: 87c13c6ad0d44ba484dbd4a1f6a11954e4ee23fd.

| Station | Simple entries | Omitted configurable entries | Total |
|---|---:|---:|---:|
| Sizzle Grill | 6 | 6 | 12 |
| Noodle Bar | 0 | 1 | 1 |
| Home | 1 | 2 | 3 |
| Oven | 2 | 1 | 3 |
| The Twisted Root | 0 | 1 | 1 |
| Farmer's Market | 4 | 13 | 17 |
| Fire and Ice | 0 | 2 | 2 |
| Soups | 2 | 0 | 2 |
| Bakery | 5 | 0 | 5 |

Total: 20 simple + 26 configurable = 46 station placements. Deli has no lunch station mapping in this response.

## Missing foods by station

Sizzle Grill: Crispy Homestyle French Fries, American Cheese, Lettuce, Sliced Red Onions, Sliced Tomatoes, Dill Pickle Slices.

Noodle Bar: Asian Tofu Noodle Bowl.

Home: Marinated Green Beans, Jasmine Rice.

Oven: Classic Cheese Pizza.

The Twisted Root: Plant-Based Sloppy Joe.

Farmer's Market: Baby Spinach, Black Olives, Brown Lentils, Cantaloupe, Chopped Kale, Diced Pineapple, Feta Cheese Crumbles, Grape Tomatoes, Honeydew Melon, Shredded Carrots, Sliced Cucumber, Sliced Mixed Bell Peppers, Spring Salad Mix.

Fire and Ice: Jasmine Rice, Boom Boom Popcorn Chicken.

## Nutrition and identity findings

Boom Boom Popcorn Chicken is parent SKU `1923_101628_A4290_1`, station `1926` (Fire and Ice). It has no parent serving nutrition. Two variants contain different nutrition, with no parent serving basis; the MVP must not select a variant automatically. Jasmine Rice appears at both Home and Fire and Ice. Anteater stores stationId on the dish itself, so new configurable IDs in the proposed upstream patch are station-scoped. Existing simple IDs and their nutrition remain unchanged.

The native live comparison initially returned 50 foods: Anteater retained four placements absent from this raw map. The app now uses UCI membership and retains nutrition only for exact matching identities. Final native verification and release details are recorded in RESULTS-DIRECT-UCI.md.
