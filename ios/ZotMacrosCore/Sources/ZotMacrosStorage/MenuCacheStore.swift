import Foundation
import SwiftData
import ZotMacrosCore
public actor MenuCacheStore: ModelActor, MenuCache {
    public nonisolated let modelContainer: ModelContainer
    public nonisolated let modelExecutor: any ModelExecutor
    public init(modelContainer: ModelContainer) {
        self.modelContainer = modelContainer
        let context = ModelContext(modelContainer)
        context.autosaveEnabled = false
        self.modelExecutor = DefaultSerialModelExecutor(modelContext: context)
    }
    public func exact(hall: String, day: String) throws -> MenuSnapshot? {
        let key = hall + ":" + day
        guard let r = try modelContext.fetch(FetchDescriptor<SchemaV1.MenuRecord>(predicate: #Predicate { $0.key == key })).first else { return nil }
        return try JSONDecoder().decode(MenuSnapshot.self, from: r.payload)
    }
    public func latest(hall: String, beforeOrOn day: String) throws -> MenuSnapshot? {
        let rows = try modelContext.fetch(FetchDescriptor<SchemaV1.MenuRecord>(predicate: #Predicate { $0.hall == hall && $0.day <= day }, sortBy: [SortDescriptor(\.day, order: .reverse)]))
        for row in rows { if let snapshot = try? JSONDecoder().decode(MenuSnapshot.self, from: row.payload) { return snapshot } }
        return nil
    }
    public func save(_ snapshot: MenuSnapshot) throws {
        modelContext.autosaveEnabled = false
        let key = snapshot.hall + ":" + snapshot.day
        do {
            let data = try JSONEncoder().encode(snapshot)
            if let row = try modelContext.fetch(FetchDescriptor<SchemaV1.MenuRecord>(predicate: #Predicate { $0.key == key })).first { row.payload = data }
            else { modelContext.insert(try SchemaV1.MenuRecord(snapshot)) }
            try modelContext.save()
        } catch { modelContext.rollback(); throw error }
    }
}
