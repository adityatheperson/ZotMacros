import Foundation
import Testing
import SwiftData
import ZotMacrosCore
@testable import ZotMacrosStorage
@MainActor struct PersistenceTests {
    @Test func testSaveReopenEditAndDelete() throws {
        let url = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString + ".store")
        defer { for suffix in ["", "-shm", "-wal"] { try? FileManager.default.removeItem(atPath: url.path + suffix) } }
        let entry = DiaryEntry(dish: Dish(id: "x", name: "Food", nutrition: Nutrition([.protein: 20])), hall: "anteatery", station: "Grill", period: "Lunch", meal: .lunch, quantity: 1, day: "2026-10-10")
        do {
            let store = DiaryStore(container: try Store.make(url: url))
            #expect(try store.add(entry)); #expect(try !store.add(entry)); #expect(try store.entries().count == 1)
        }
        let reopened = DiaryStore(container: try Store.make(url: url))
        #expect(try reopened.entries().first == entry)
        try reopened.edit(id: entry.id, quantity: 2, meal: .dinner)
        #expect(try reopened.entries().first?.totals[.protein] == 40)
        #expect(try reopened.entries().first?.dish.nutrition[.protein] == 20)
        try reopened.delete(id: entry.id); #expect(try reopened.entries().isEmpty)
    }
    @Test func testFailedSaveRollsBackAndRetryIsSingle() throws {
        let container = try Store.make(inMemory: true)
        var fail = true
        let store = DiaryStore(container: container, commit: { context in if fail { throw ZotError.invalidData("Disk unavailable") }; try context.save() })
        let entry = DiaryEntry(dish: Dish(id: "x", name: "Food"), hall: "anteatery", station: "Grill", period: "Lunch", meal: .lunch, quantity: 1, day: "2026-10-10")
        #expect(throws: (any Error).self) { try store.add(entry) }
        #expect(try store.entries().isEmpty)
        fail = false; #expect(try store.add(entry)); #expect(try !store.add(entry)); #expect(try store.entries().count == 1)
    }
    @Test func testPreferences() throws {
        let suite = UUID().uuidString
        let defaults = UserDefaults(suiteName: suite)!
        defer { defaults.removePersistentDomain(forName: suite) }
        let store = PreferencesStore(defaults: defaults)
        #expect(store.value.exampleGoals); #expect(store.value.calories == 2400)
        var p = store.value; p.calories = .infinity
        #expect(throws: (any Error).self) { try store.save(p) }
        p.calories = 2300; p.exampleGoals = false; p.hall = "brandywine"; try store.save(p)
        #expect(PreferencesStore(defaults: defaults).value == p)
    }
}

@MainActor struct CacheIsolationTests {
    @Test func testCorruptCacheDoesNotAffectDiary() async throws {
        let container = try Store.make(inMemory: true), store = DiaryStore(container: container)
        let entry = DiaryEntry(dish: Dish(id: "x", name: "Saved"), hall: "anteatery", station: "Grill", period: "Lunch", meal: .lunch, quantity: 1, day: "2026-10-10")
        try store.add(entry)
        let context = ModelContext(container)
        let cache = try SchemaV1.MenuRecord(MenuSnapshot(hall: "anteatery", day: "2026-10-10", periods: []))
        cache.payload = Data("corrupt".utf8); context.insert(cache); try context.save()
        let adapter = MenuCacheStore(modelContainer: container)
        await #expect(throws: (any Error).self) { try await adapter.exact(hall: "anteatery", day: "2026-10-10") }
        #expect(try store.entries() == [entry])
    }
    @Test func testUnreadableStoreIsNotRecreated() throws {
        let url = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString + ".store")
        let bytes = Data("This is not a database".utf8); try bytes.write(to: url)
        defer { try? FileManager.default.removeItem(at: url) }
        #expect(throws: (any Error).self) { try Store.make(url: url) }
        #expect(try Data(contentsOf: url) == bytes)
    }
}
