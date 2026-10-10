import Foundation
import Testing
@testable import ZotMacrosCore
struct NutritionTests {
    @Test func testUnknownAndZero() throws {
        let n = Nutrition([.calories: 0, .protein: 20, .fat: -1, .carbs: .infinity])
        #expect(n[.calories] == 0); #expect(n[.fat] == nil); #expect(n[.carbs] == nil)
        #expect(try n.scaled(by: 1.5)[.protein] == 30)
    }
    @Test func testPartialTotals() {
        #expect(aggregate([Nutrition([.protein: 20]), Nutrition()])[.protein] == NutrientTotal(value: 20, missing: 1))
        #expect(aggregate([Nutrition()])[.protein]?.value == nil)
        #expect(aggregate([])[.protein]?.value == 0)
    }
    @Test func testQuantityAndOverflow() throws {
        for q in [0, -1, Double.infinity, .nan, 1000.1] { #expect(throws: (any Error).self) { try Nutrition().scaled(by: q) } }
        #expect(abs(try Nutrition([.protein: 0.1]).scaled(by: 1.5)[.protein]! - 0.15) < 1e-14)
        _ = try Nutrition().scaled(by: 1000)
        #expect(try Nutrition([.protein: .greatestFiniteMagnitude]).scaled(by: 2)[.protein] == nil)
        #expect(aggregate([Nutrition([.protein: .greatestFiniteMagnitude]), Nutrition([.protein: .greatestFiniteMagnitude])])[.protein]?.value == nil)
    }
    @Test func testSnapshotEditing() throws {
        let entry = DiaryEntry(dish: Dish(id: "x", name: "Food", nutrition: Nutrition([.protein: 20])), hall: "anteatery", station: "Grill", period: "Lunch", meal: .lunch, quantity: 1, day: "2026-10-10")
        let updated = try entry.edited(quantity: 2, meal: .dinner)
        #expect(updated.dish.nutrition[.protein] == 20); #expect(updated.totals[.protein] == 40)
        #expect(updated.id == entry.id); #expect(updated.day == entry.day)
    }
}
struct CampusClockTests {
    @Test func testMidnightAndDST() {
        let iso = ISO8601DateFormatter()
        #expect(CampusClock.day(at: iso.date(from: "2026-10-10T06:59:59Z")!) == "2026-10-09")
        #expect(CampusClock.day(at: iso.date(from: "2026-10-10T07:00:00Z")!) == "2026-10-10")
        for stamp in ["2026-03-08T08:00:00Z", "2026-11-01T07:00:00Z"] {
            let date = iso.date(from: stamp)!, next = CampusClock.nextMidnight(after: iso.date(from: stamp)!)
            #expect(next > date)
            #expect(CampusClock.day(at: next) == (stamp.hasPrefix("2026-03") ? "2026-03-09" : "2026-11-02"))
        }
    }
}
