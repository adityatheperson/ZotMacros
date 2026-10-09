# Verified Anteater API contract
Verified 2026-10-07 against https://anteaterapi.com/openapi.json and live responses.
Base: https://anteaterapi.com/v2/rest/
- GET dining/restaurants -> {ok,data:[{id,stations:[{id,name}]}]}
- GET dining/restaurantToday?id=anteatery&date=YYYY-MM-DD -> {ok,data:{id,periods:{periodId:{name,startTime,endTime,stationToDishes:{stationId:[dishId]}}}}}. Date is UCI local date, explicitly supplied.
- GET dining/dishes/batch?ids=comma,separated,ids -> {ok,data:[dish]}. Requests chunk at 20 IDs; duplicates removed across periods. No auth needed for tested public requests.
- Dish: id,name,description,ingredients,dietRestriction,nutritionInfo. Nutrition keys carry units: calories; proteinG,totalCarbsG,totalFatG,saturatedFatG,transFatG,dietaryFiberG,sugarsG; sodiumMg,cholesterolMg,calciumMg,ironMg; vitaminAIU,vitaminCIU. Vitamin C is labeled IU exactly as reported; no invented conversion. Nullable or malformed fields remain unknown; numeric strings accepted only if finite and nonnegative.
- Actual periods include All Day, Brunch, Afternoon Snack, Evening Snack. No hardcoded three-period filter. Null hours display no schedule claim.
- Restaurants may return 404; validation 422; server 500; handle rate-limit 429 without retries that amplify traffic.

Attribution https://docs.icssc.club/docs/developer/anteaterapi/attribution-policy requires prominent attribution near query/data surfaces, linked when possible to https://icssc.link/about-anteaterapi. App uses linked “Data from Anteater API” in Menu and Food Details and About.

Menus are cached per hall/date with fetch timestamp and warning for fallback/historical/old data. No fixtures enter production; captured today.json exists only under src/test/resources.

## Direct UCI menu supplement (1.0.1)

ZotMacros also reads the public MyDiningHub Adobe GraphQL endpoint directly from Android. No additional hosted service, account, or diary upload is involved. `SupplementSource` requests station names and meal IDs from `getLocation` / `Commerce_mealPeriods`, then `getLocationRecipes` with explicit hall, campus date, and returned meal ID. Both `simple` and `configurable { sku variants }` placements are collected. Weekly product catalogs never determine date membership; only the exact date's station map does.

UCI determines published membership for each returned Anteater meal and any additional period in UCI’s active schedule. Matching Anteater foods retain their original nutrition; missing foods use null nutrients and unavailable serving sizes. Variant nutrition is not guessed for configurable parents. Deduplication uses station and exact source identity, not food names. Configurable parents never borrow variant nutrition. This also removes stale Anteater placements from the displayed menu without altering saved diary entries. If direct UCI collection fails, the app retains the exact-date cache or shows an explicit incomplete-menu warning. Completed menus are cached for 15 minutes. Old caches without supplement verification are refreshed.

The public `ElevateAPIProd` header is the storefront's shared identifier, not a private user credential. The implementation follows the upstream collector's public request headers. Fixtures are confined to test resources. See `docs/verification/menu-2026-10-08/COMPARISON.md` for captured evidence and `docs/upstream/` for the prepared collector patch.
