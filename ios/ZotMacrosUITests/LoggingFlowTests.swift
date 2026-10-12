import XCTest
import UIKit
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
    private func capture(_ name: String) {
        let attachment = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        attachment.name = name; attachment.lifetime = .keepAlways; add(attachment)
    }
    func testNavigation() {
        let app = launch()
        for tab in ["Menu", "History", "Settings", "Today"] { app.tabBars.buttons[tab].tap(); XCTAssertTrue(app.staticTexts["screenTitle"].waitForExistence(timeout: 3)); capture("normal-" + tab) }
        XCTAssertTrue(app.buttons["Add food"].exists)
    }
    func testPreferredHallPersistsWithoutSaveButton() {
        let app = launch()
        app.tabBars.buttons["Settings"].tap()
        let hall = app.buttons["preferredHall"]
        scrollTo(hall, app: app); hall.tap()
        app.buttons["Brandywine"].tap()
        app.tabBars.buttons["Menu"].tap()
        XCTAssertTrue(app.buttons["hall-brandywine"].waitForExistence(timeout: 3))
        XCTAssertTrue(app.buttons["hall-brandywine"].isSelected)
        app.tabBars.buttons["Settings"].tap()
        scrollTo(hall, app: app)
        XCTAssertTrue(hall.label.contains("Brandywine") || (hall.value as? String) == "Brandywine")
        app.terminate(); app.launchArguments = []; app.launch()
        app.tabBars.buttons["Menu"].tap()
        XCTAssertTrue(app.buttons["hall-brandywine"].waitForExistence(timeout: 3))
        XCTAssertTrue(app.buttons["hall-brandywine"].isSelected)
    }
    func testMealPeriodTabs() {
        let app = launch(); menu(app)
        let lunch = app.buttons["period-lunch"], breakfast = app.buttons["period-breakfast"]
        XCTAssertTrue(lunch.waitForExistence(timeout: 3)); XCTAssertTrue(lunch.isSelected)
        breakfast.tap(); XCTAssertTrue(breakfast.isSelected)
        XCTAssertTrue(app.staticTexts["No foods found"].waitForExistence(timeout: 3))
        lunch.tap(); XCTAssertTrue(lunch.isSelected)
        XCTAssertTrue(app.staticTexts["Known lunch food"].waitForExistence(timeout: 3))
        capture("meal-tabs-lunch")
    }
    func testDiningHallSelection() {
        let app = launch(); menu(app)
        let anteatery = app.buttons["hall-anteatery"], brandywine = app.buttons["hall-brandywine"]
        XCTAssertTrue(anteatery.isSelected)
        brandywine.tap(); XCTAssertTrue(brandywine.isSelected); XCTAssertFalse(anteatery.isSelected)
        capture("hall-brandywine")
        anteatery.tap(); XCTAssertTrue(anteatery.isSelected)
        capture("hall-anteatery")
    }
    func testReducedMotionNavigation() throws {
        try XCTSkipUnless(UIAccessibility.isReduceMotionEnabled, "Enable Simulator Reduce Motion to exercise this flow.")
        let app = launch(["reset"])
        for tab in ["Menu", "History", "Today"] {
            app.tabBars.buttons[tab].tap()
            XCTAssertTrue(app.staticTexts["screenTitle"].waitForExistence(timeout: 3))
        }
        capture("reduce-motion-today")
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
        XCTAssertTrue(save.isHittable); capture("large-text-keyboard-food-sheet"); save.tap()
        XCTAssertTrue(app.staticTexts["saveError"].waitForExistence(timeout: 3))
        XCTAssertTrue(save.isHittable); save.tap()
        XCTAssertTrue(app.staticTexts["screenTitle"].waitForExistence(timeout: 3))
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
