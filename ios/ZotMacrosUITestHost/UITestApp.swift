// Compiled only into the dedicated UI test host, never the production app.
import SwiftUI
import SwiftData
import ZotMacrosCore
import ZotMacrosStorage
@main struct UITestApp: App {
    @State private var model: AppModel
    init() {
        let arguments = ProcessInfo.processInfo.arguments
        let defaults = UserDefaults(suiteName: "com.zotmacros.ios.ui-tests")!
        let url = FileManager.default.temporaryDirectory.appendingPathComponent("zot-ui-tests.store")
        if arguments.contains("reset") {
            defaults.removePersistentDomain(forName: "com.zotmacros.ios.ui-tests")
            for suffix in ["", "-shm", "-wal"] { try? FileManager.default.removeItem(atPath: url.path + suffix) }
        }
        do {
            let container = try Store.make(url: url), preferences = PreferencesStore(defaults: defaults)
            var p = preferences.value; p.onboarded = true; p.meal = "Lunch"; p.autoRefresh = !arguments.contains("manual-menu"); try preferences.save(p)
            var failNext = arguments.contains("fail-save-once")
            let diary = DiaryStore(container: container) { context in
                if failNext { failNext = false; throw ZotError.invalidData("Test save failure") }; try context.save()
            }
            _model = State(initialValue: AppModel(container: container, preferences: preferences, diary: diary, load: { hall, day, _ in
                if arguments.contains("offline") { return MenuResult(snapshot: nil, warning: "Offline test") }
                let known = MenuFood(dish: Dish(id: "known", name: "Known lunch food", serving: "1 serving", nutrition: Nutrition([.calories: 200, .protein: 20, .carbs: 15, .fat: 7])), stationId: "1", station: "The Grill")
                let unknown = MenuFood(dish: Dish(id: "unknown", name: "Unknown lunch food"), stationId: "1926", station: "Fire and Ice")
                return MenuResult(snapshot: MenuSnapshot(hall: hall, day: day, periods: [MenuPeriod(id: "breakfast", name: "Breakfast", foods: []), MenuPeriod(id: "lunch", name: "Lunch", foods: [known, unknown])], supplementVerified: true))
            }))
        } catch { fatalError("UI test store setup failed: \(error)") }
    }
    var body: some Scene { WindowGroup { RootView(model: model).tint(ZotTheme.gold).preferredColorScheme(.dark) } }
}
