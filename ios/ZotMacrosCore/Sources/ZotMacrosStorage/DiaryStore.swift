import Foundation
import SwiftData
import ZotMacrosCore
@MainActor public final class DiaryStore {
    private let context: ModelContext
    private let commit: (ModelContext) throws -> Void
    public init(container: ModelContainer, commit: @escaping (ModelContext) throws -> Void = { try $0.save() }) { context = ModelContext(container); context.autosaveEnabled = false; self.commit = commit }
    public func entries(day: String? = nil) throws -> [DiaryEntry] {
        let records = try context.fetch(FetchDescriptor<SchemaV1.DiaryRecord>())
        return try records.filter { day == nil || $0.day == day }.map { try JSONDecoder().decode(DiaryEntry.self, from: $0.payload) }.sorted { $0.createdAt < $1.createdAt }
    }
    private func record(_ id: UUID) throws -> SchemaV1.DiaryRecord? {
        try context.fetch(FetchDescriptor<SchemaV1.DiaryRecord>(predicate: #Predicate { $0.id == id })).first
    }
    @discardableResult public func add(_ entry: DiaryEntry) throws -> Bool {
        _ = try entry.dish.nutrition.scaled(by: entry.quantity)
        guard CampusClock.date(entry.day) != nil else { throw ZotError.invalidData("Invalid diary date.") }
        if try record(entry.id) != nil { return false }
        do { context.insert(try SchemaV1.DiaryRecord(entry)); try commit(context); return true } catch { context.rollback(); throw error }
    }
    public func edit(id: UUID, quantity: Double, meal: Meal) throws {
        guard let r = try record(id) else { throw ZotError.invalidData("This entry was already removed.") }
        let original = try JSONDecoder().decode(DiaryEntry.self, from: r.payload)
        let updated = try original.edited(quantity: quantity, meal: meal)
        do { r.payload = try JSONEncoder().encode(updated); try commit(context) } catch { context.rollback(); throw error }
    }
    public func delete(id: UUID) throws {
        guard let r = try record(id) else { return }
        do { context.delete(r); try commit(context) } catch { context.rollback(); throw error }
    }
    public func clear() throws {
        do { for r in try context.fetch(FetchDescriptor<SchemaV1.DiaryRecord>()) { context.delete(r) }; try commit(context) } catch { context.rollback(); throw error }
    }
}
