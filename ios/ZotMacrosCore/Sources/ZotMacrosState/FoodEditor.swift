import Foundation
import Observation
import ZotMacrosCore
@MainActor @Observable public final class FoodEditor: Identifiable {
    public let id: UUID
    public let dish: Dish
    public let hall: String
    public let station: String
    public let period: String
    public let day: String
    public let editing: Bool
    public var quantity: String
    public var meal: Meal
    public private(set) var saving = false
    public private(set) var saved = false
    public private(set) var error: String?
    private let createdAt: Date
    private let commit: (DiaryEntry, Bool) async throws -> Void
    public init(dish: Dish, hall: String, station: String, period: String, day: String, existing: DiaryEntry? = nil, commit: @escaping (DiaryEntry, Bool) async throws -> Void) {
        id = existing?.id ?? UUID(); self.dish = existing?.dish ?? dish; self.hall = hall; self.station = station; self.period = period; self.day = existing?.day ?? day; editing = existing != nil
        quantity = existing.map { String($0.quantity) } ?? "1"; meal = existing?.meal ?? mealForPeriod(period); createdAt = existing?.createdAt ?? Date(); self.commit = commit
    }
    public var parsedQuantity: Double? {
        let formatter = NumberFormatter(); formatter.locale = .current; formatter.numberStyle = .decimal
        let normalized = quantity.trimmingCharacters(in: .whitespacesAndNewlines).replacingOccurrences(of: formatter.decimalSeparator ?? ".", with: ".")
        guard let q = Double(normalized), q.isFinite, q > 0, q <= 1000 else { return nil }; return q
    }
    public var totals: Nutrition { parsedQuantity.flatMap { try? dish.nutrition.scaled(by: $0) } ?? Nutrition() }
    public func save() async -> Bool {
        if saved { return true }; guard !saving else { return false }
        guard let q = parsedQuantity else { error = ZotError.invalidQuantity.localizedDescription; return false }
        saving = true; error = nil; defer { saving = false }
        do { try await commit(DiaryEntry(id: id, createdAt: createdAt, dish: dish, hall: hall, station: station, period: period, meal: meal, quantity: q, day: day), editing); saved = true; return true }
        catch { self.error = "Couldn’t save this food. \(error.localizedDescription)"; return false }
    }
}
