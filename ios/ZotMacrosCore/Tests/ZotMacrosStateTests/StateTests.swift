import Foundation
import Testing
import ZotMacrosCore
@testable import ZotMacrosState
@MainActor struct StateTests {
    @Test func testObsoleteResultCannotReplaceSelection() async {
        let model = MenuModel { hall, day, _ in
            if hall == "anteatery" { try? await Task.sleep(nanoseconds: 50_000_000) }
            return MenuResult(snapshot: MenuSnapshot(hall: hall, day: day, periods: []))
        }
        let first = Task { await model.refresh(hall: "anteatery", day: "2026-10-10", force: true) }
        await Task.yield()
        await model.refresh(hall: "brandywine", day: "2026-10-10", force: true)
        await first.value
        #expect(model.result?.snapshot?.hall == "brandywine")
    }
    @Test func testEditorRetriesAndSnapshot() async throws {
        var calls = 0
        let editor = FoodEditor(dish: Dish(id: "x", name: "Food"), hall: "anteatery", station: "Fire and Ice", period: "Lunch", day: "2026-10-10") { entry, _ in
            calls += 1
            if calls == 1 { throw ZotError.invalidData("Disk unavailable") }
        }
        #expect(await !editor.save()); #expect(editor.error != nil)
        #expect(await editor.save()); #expect(await editor.save()); #expect(calls == 2)
        #expect(editor.quantity == "1"); #expect(editor.meal == .lunch)
    }
    @Test func testPeriodChoice() {
        #expect(mealForPeriod("Brunch") == .lunch); #expect(mealForPeriod("All Day") == .snacks)
        let periods = [MenuPeriod(id: "1", name: "Lunch", start: "11:00", end: "14:00"), MenuPeriod(id: "2", name: "Afternoon Snack", start: "14:00", end: "16:00")]
        #expect(choosePeriod(periods, preference: "Automatic", time: "14:30") == "2")
        #expect(choosePeriod(periods, preference: "Lunch", time: "14:30") == "1")
    }
}

@MainActor struct CancellationTests {
    @Test func testCanceledRefreshStopsLoading() async {
        let model = MenuModel { _, _, _ in
            try? await Task.sleep(nanoseconds: 1_000_000_000)
            return MenuResult(snapshot: nil)
        }
        let task = Task { await model.refresh(hall: "anteatery", day: "2026-10-10", force: true) }
        while !model.loading { await Task.yield() }
        task.cancel(); await task.value
        #expect(!model.loading)
        #expect(model.result?.warning == nil)
    }
    @Test func testInvalidQuantityCannotSave() async {
        var calls = 0
        let editor = FoodEditor(dish: Dish(id: "x", name: "Food"), hall: "anteatery", station: "Grill", period: "Lunch", day: "2026-10-10") { _, _ in calls += 1 }
        for quantity in ["0", "NaN", "infinity", "1001", "-2", "1abc"] { editor.quantity = quantity; #expect(await !editor.save()) }
        #expect(calls == 0)
    }
}
