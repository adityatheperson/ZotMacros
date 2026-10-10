import XCTest
import SwiftData
import ZotMacrosCore
import ZotMacrosStorage
@testable import ZotMacros
@MainActor final class AppIntegrationTests: XCTestCase {
    func testUnknownFoodCanBeLoggedAndEdited() async throws {
        let app = AppModel(container: try Store.make(inMemory: true))
        let food = MenuFood(dish: Dish(id: "unknown", name: "Unknown food"), stationId: "1926", station: "Fire and Ice")
        app.open(food, period: MenuPeriod(id: "l", name: "Lunch"))
        let editor = try XCTUnwrap(app.editor)
        let saved = await editor.save(); XCTAssertTrue(saved)
        XCTAssertEqual(app.entries.count, 1)
        let total = try XCTUnwrap(aggregate(app.entries.map(\.totals))[.calories])
        XCTAssertNil(total.value); XCTAssertEqual(total.missing, 1)
        app.edit(try XCTUnwrap(app.entries.first)); app.editor?.quantity = "2"
        let edited = await app.editor!.save(); XCTAssertTrue(edited)
        XCTAssertEqual(app.entries.count, 1); XCTAssertEqual(app.entries.first?.quantity, 2)
        XCTAssertNil(app.entries.first?.dish.nutrition[.calories])
    }
}
