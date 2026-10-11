import Foundation
import Testing
@testable import ZotMacrosCore
struct MenuParserTests {
    @Test func testNutrientsAndMalformedEnvelope() throws {
        let data = Data(#"{"ok":true,"data":[{"id":"x","name":"Food","nutritionInfo":{"calories":0,"proteinG":"20","sodiumMg":"bad","totalFatG":-1,"vitaminCIU":4}}]}"#.utf8)
        let food = try DiningParser.dishes(data: data)[0]
        #expect(food.nutrition[.calories] == 0); #expect(food.nutrition[.protein] == 20)
        #expect(food.nutrition[.sodium] == nil); #expect(food.nutrition[.fat] == nil)
        #expect(food.nutrition[.vitaminC] == 4); #expect(Nutrient.vitaminC.unit == "IU")
        #expect(throws: (any Error).self) { try DiningParser.dishes(data: Data(#"{"ok":false,"data":[]}"#.utf8)) }
    }
    @Test func testCapturedPopcornChicken() throws {
        let url = Bundle.module.url(forResource: "uci-lunch-2026-10-08", withExtension: "json", subdirectory: "Fixtures")!
        let foods = try UciMenuParser.foods(data: Data(contentsOf: url), day: "2026-10-08", stations: ["1926": "Fire and Ice"], requireKnownStations: false)
        let chicken = try #require(foods.first { $0.name == "Boom Boom Popcorn Chicken" })
        #expect(chicken.stationId == "1926"); #expect(foods.count == 46)
        #expect(throws: (any Error).self) { try UciMenuParser.foods(data: Data(contentsOf: url), day: "2026-10-09", stations: [:]) }
        let variant = MenuFood(dish: Dish(id: chicken.aliases.last!, name: "Variant", nutrition: Nutrition([.protein: 99])), stationId: "1926", station: "Fire and Ice")
        let result = reconcile(period: MenuPeriod(id: "lunch", name: "Lunch", foods: [variant]), foods: [chicken, chicken])
        #expect(result.foods.count == 1); #expect(result.foods[0].dish == variant.dish)
    }
    @Test func testPreservesNutritionAndStationIdentity() {
        let dish = Dish(id: "x", name: "Rice", nutrition: Nutrition([.calories: 100]))
        let period = MenuPeriod(id: "l", name: "Lunch", foods: [MenuFood(dish: dish, stationId: "1", station: "One"), MenuFood(dish: Dish(id: "old", name: "Stale"), stationId: "1", station: "One")])
        let result = reconcile(period: period, foods: [SupplementFood(id: "x", name: "Rice", stationId: "1", station: "One", aliases: ["x"]), SupplementFood(id: "x", name: "Rice", stationId: "2", station: "Two", aliases: ["x"])])
        #expect(result.foods.count == 3); #expect(result.foods[0].dish == dish)
        #expect(result.foods[1].dish.id == "old"); #expect(result.foods.last?.dish.nutrition[.calories] == nil)
    }
    @Test func testScheduleOverrides() throws {
        let schedules: [[String: Any]] = [["type": "standard", "meal_periods": [["meal_period": "Lunch", "opening_hours": "Mo-Su 11:00-14:00; Sa off"]]], ["start_date": "2026-10-10", "end_date": "2026-10-10", "meal_periods": [["meal_period": "Brunch", "opening_hours": "Sa 10:00-14:00"]]]]
        #expect(try uciPeriods(schedules: schedules, day: "2026-10-10", meals: ["Lunch": 1, "Brunch": 2]).map(\.name) == ["Brunch"])
        #expect(try uciPeriods(schedules: [schedules[0]], day: "2026-10-10", meals: ["Lunch": 1]).isEmpty)
        #expect(try uciPeriods(schedules: [schedules[0]], day: "2026-10-09", meals: ["Lunch": 1]).first?.start == "11:00")
    }
}

struct DefensiveMenuTests {
    @Test func testBooleanNutritionIsUnknown() throws {
        let data = Data(#"{"ok":true,"data":[{"id":"x","name":"Food","nutritionInfo":{"calories":false,"proteinG":true}}]}"#.utf8)
        let dish = try DiningParser.dishes(data: data)[0]
        #expect(dish.nutrition[.calories] == nil); #expect(dish.nutrition[.protein] == nil)
    }
    @Test func testMissingAndHiddenUciFood() throws {
        let menu: [String: Any] = ["data": ["getLocationRecipes": ["locationRecipesMap": ["dateSkuMap": [["date": "2026-10-10", "stations": [["id": "s", "skus": ["simple": ["x", "x"], "configurable": []]]]]]], "products": ["items": [["sku": "x", "name": "Hidden", "attributes": [["name": "is_hide_from_web_menu", "value": "1"]]]]]]]]
        let data = try JSONSerialization.data(withJSONObject: menu)
        #expect(try UciMenuParser.foods(data: data, day: "2026-10-10", stations: ["s": "Station"]).isEmpty)
        #expect(throws: (any Error).self) { try UciMenuParser.foods(data: data, day: "2026-10-10", stations: [:]) }
        #expect(throws: (any Error).self) { try UciMenuParser.foods(data: Data(#"{"errors":[{}],"data":{}}"#.utf8), day: "2026-10-10", stations: [:]) }
    }
    @Test func testUnknownHoursFailAndWraparoundDays() throws {
        let schedules: [[String: Any]] = [["type":"standard", "meal_periods":[["meal_period":"Snack", "opening_hours":"Fr-Mo 15:00-17:00"]]]]
        #expect(try uciPeriods(schedules: schedules, day: "2026-10-10", meals: ["Snack": 4]).count == 1)
        #expect(try uciPeriods(schedules: schedules, day: "2026-10-13", meals: ["Snack": 4]).isEmpty)
        #expect(throws: (any Error).self) { try uciPeriods(schedules: [["type":"standard", "meal_periods":[["meal_period":"Lunch", "opening_hours":"Everyday noon"]]]], day: "2026-10-10", meals: ["Lunch": 1]) }
    }
}

struct EmptyHoursTests {
    @Test func testBlankHoursMeanInactivePeriod() throws {
        let periods = try uciPeriods(schedules: [["type": "standard", "meal_periods": [["meal_period": "Overnight", "opening_hours": ""], ["meal_period": "Lunch", "opening_hours": "Mo-Su 11:00-14:00"]]]], day: "2026-10-10", meals: ["Overnight": 31, "Lunch": 25])
        #expect(periods.map(\.name) == ["Lunch"])
    }
}

struct MetadataTests {
    @Test func testCurrentMetadataAcceptsInactivePeriods() throws {
        let url = Bundle.module.url(forResource: "uci-metadata-2026-10-10", withExtension: "json", subdirectory: "Fixtures")!
        let metadata = try UciMenuParser.metadata(data: Data(contentsOf: url), day: "2026-10-10")
        #expect(metadata.stations["1926"] == "Fire and Ice"); #expect(metadata.meals["Lunch"] == 25)
        #expect(metadata.periods.contains { $0.name == "Brunch" }); #expect(!metadata.periods.contains { $0.name == "Overnight" })
    }
}

struct MissingProductTests {
    @Test func testReferencedButUnreturnedProductDoesNotDisappearSilently() throws {
        let url = Bundle.module.url(forResource: "uci-brandywine-incomplete-2026-10-10", withExtension: "json", subdirectory: "Fixtures")!
        do {
            _ = try UciMenuParser.foods(data: Data(contentsOf: url), day: "2026-10-10", stations: [:], requireKnownStations: false)
            Issue.record("Expected incomplete response to be rejected")
        } catch {
            #expect(error.localizedDescription == "Missing UCI food details.")
        }
    }
}
