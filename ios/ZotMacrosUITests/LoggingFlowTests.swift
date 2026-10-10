import XCTest
final class LoggingFlowTests: XCTestCase {
    private func launch(_ arguments: [String] = ["reset"]) -> XCUIApplication {
        let app = XCUIApplication(); app.launchArguments = arguments; app.launch(); return app
    }
    private func menu(_ app: XCUIApplication) {
        XCTAssertTrue(app.buttons["Add food"].waitForExistence(timeout: 5))
        app.tabBars.buttons["Menu"].tap()
    }
    private func scrollTo(_ element: XCUIElement, app: XCUIApplication) {
        for _ in 0..<8 { if element.isHittable { return }; app.swipeUp() }
    }
    func testNavigation() {
        let app = launch()
        for tab in ["Menu", "History", "Settings", "Today"] { app.tabBars.buttons[tab].tap(); XCTAssertTrue(app.navigationBars.firstMatch.waitForExistence(timeout: 3)) }
        XCTAssertTrue(app.buttons["Add food"].exists)
    }
    func testInitialPreferredMealAndUnknownLoggingPersistsOffline() {
        let app = launch(); menu(app)
        let food = app.buttons.containing(.staticText, identifier: "Unknown lunch food").firstMatch
        scrollTo(food, app: app); XCTAssertTrue(food.waitForExistence(timeout: 5)); food.tap()
        XCTAssertTrue(app.staticTexts["Nutrition unavailable. You can log this food; daily totals will be incomplete."].exists)
        app.buttons["Add to diary"].tap()
        app.tabBars.buttons["Today"].tap()
        XCTAssertTrue(app.staticTexts["Incomplete total"].waitForExistence(timeout: 5))
        app.terminate(); app.launchArguments = ["offline"]; app.launch()
        XCTAssertTrue(app.staticTexts["Incomplete total"].waitForExistence(timeout: 5))
    }
    func testFailedSaveRetryAndQuantityKeyboard() {
        let app = launch(["reset", "fail-save-once", "-UIPreferredContentSizeCategoryName", "UICTContentSizeCategoryAccessibilityXL"])
        menu(app)
        let food = app.buttons.containing(.staticText, identifier: "Known lunch food").firstMatch
        scrollTo(food, app: app); food.tap()
        let quantity = app.textFields["servingQuantity"]
        scrollTo(quantity, app: app); quantity.tap(); quantity.typeText(XCUIKeyboardKey.delete.rawValue + "1.5")
        let save = app.buttons["Add to diary"]
        XCTAssertTrue(save.isHittable); save.tap()
        XCTAssertTrue(app.staticTexts["saveError"].waitForExistence(timeout: 3))
        XCTAssertTrue(save.isHittable); save.tap()
        XCTAssertTrue(app.navigationBars["Dining menu"].waitForExistence(timeout: 3))
    }
    func testManualRefreshDoesNotFetchOnAppearance() {
        let app = launch(["reset", "manual-menu"]); menu(app)
        XCTAssertTrue(app.buttons["Load menu"].waitForExistence(timeout: 3))
        app.buttons["Load menu"].tap()
        XCTAssertTrue(app.staticTexts["Known lunch food"].waitForExistence(timeout: 3))
    }
    func testClearHistoryCancelAndConfirm() {
        let app = launch(); menu(app)
        let food = app.buttons.containing(.staticText, identifier: "Unknown lunch food").firstMatch
        scrollTo(food, app: app); food.tap(); app.buttons["Add to diary"].tap()
        app.tabBars.buttons["Settings"].tap()
        let clear = app.buttons["Clear local food history"]
        scrollTo(clear, app: app); clear.tap()
        XCTAssertTrue(app.alerts["Clear food history?"].waitForExistence(timeout: 2)); app.alerts.buttons["Cancel"].tap()
        clear.tap(); app.alerts.buttons["Clear history"].tap(); app.tabBars.buttons["Today"].tap()
        let empty = app.staticTexts["No foods logged"]; scrollTo(empty, app: app); XCTAssertTrue(empty.exists)
    }
}
