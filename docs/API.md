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
