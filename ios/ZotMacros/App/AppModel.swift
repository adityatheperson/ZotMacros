import Foundation
import Observation
import SwiftData
import ZotMacrosCore
import ZotMacrosStorage
import ZotMacrosState
@MainActor @Observable final class AppModel {
    let preferences: PreferencesStore
    let diary: DiaryStore
    let menu: MenuModel
    var entries: [DiaryEntry] = []
    var day = CampusClock.day()
    var editor: FoodEditor?
    var error: String?
    var confirmation: String?
    var selectedTab = 0
    init(container: ModelContainer, preferences: PreferencesStore? = nil, diary: DiaryStore? = nil, load: (@Sendable (String, String, Bool) async throws -> MenuResult)? = nil) {
        self.preferences = preferences ?? PreferencesStore(); self.diary = diary ?? DiaryStore(container: container)
        let repository = MenuRepository(http: URLSessionHTTPClient(), cache: MenuCacheStore(modelContainer: container))
        menu = MenuModel(load: load ?? { hall, day, force in try await repository.load(hall: hall, day: day, force: force) })
        reloadDiary()
    }
    func reloadDiary() { do { entries = try diary.entries() } catch { self.error = "Couldn’t read your saved diary. \(error.localizedDescription)" } }
    func entries(on day: String) -> [DiaryEntry] { entries.filter { $0.day == day } }
    func foreground() async {
        let current = CampusClock.day()
        if current != day { day = current; menu.invalidate(hall: preferences.value.hall, day: day) }
        reloadDiary()
        if preferences.value.autoRefresh { await refreshMenu() }
    }
    func refreshMenu(force: Bool = false) async { await menu.refresh(hall: preferences.value.hall, day: day, force: force) }
    func selectHall(_ hall: String) {
        var p = preferences.value; p.hall = hall
        do { try preferences.save(p); menu.invalidate(hall: hall, day: day) } catch { self.error = error.localizedDescription }
    }
    func open(_ food: MenuFood, period: MenuPeriod) {
        editor = makeEditor(dish: food.dish, hall: preferences.value.hall, station: food.station, period: period.name, existing: nil)
    }
    func edit(_ entry: DiaryEntry) { editor = makeEditor(dish: entry.dish, hall: entry.hall, station: entry.station, period: entry.period, existing: entry) }
    private func makeEditor(dish: Dish, hall: String, station: String, period: String, existing: DiaryEntry?) -> FoodEditor {
        FoodEditor(dish: dish, hall: hall, station: station, period: period, day: day, existing: existing) { [weak self] entry, editing in
            guard let self else { throw ZotError.invalidData("Please reopen this food.") }
            if editing { try self.diary.edit(id: entry.id, quantity: entry.quantity, meal: entry.meal) }
            else { try self.diary.add(entry) }
            self.reloadDiary(); self.confirmation = editing ? "Food updated" : "Food added"
        }
    }
    func delete(_ entry: DiaryEntry) { do { try diary.delete(id: entry.id); reloadDiary() } catch { self.error = error.localizedDescription } }
    func clearDiary() { do { try diary.clear(); reloadDiary() } catch { self.error = error.localizedDescription } }
}
