import Foundation
public struct Dish: Codable, Equatable, Sendable, Identifiable {
    public let id: String
    public let name: String
    public var serving: String
    public var nutrition: Nutrition
    public var description: String
    public var ingredients: String?
    public var allergens: [String]
    public var allergensReported: Bool
    public init(id: String, name: String, serving: String = "Serving size unavailable", nutrition: Nutrition = Nutrition(), description: String = "", ingredients: String? = nil, allergens: [String] = [], allergensReported: Bool = false) {
        self.id = id; self.name = name; self.serving = serving; self.nutrition = nutrition; self.description = description; self.ingredients = ingredients; self.allergens = allergens; self.allergensReported = allergensReported
    }
}
public struct MenuFood: Codable, Equatable, Sendable, Identifiable {
    public let dish: Dish; public let stationId: String; public let station: String
    public var id: String { stationId + ":" + dish.id }
    public init(dish: Dish, stationId: String, station: String) { self.dish = dish; self.stationId = stationId; self.station = station }
}
public struct MenuPeriod: Codable, Equatable, Sendable, Identifiable {
    public let id: String; public let name: String; public var start: String?; public var end: String?; public var foods: [MenuFood]
    public init(id: String, name: String, start: String? = nil, end: String? = nil, foods: [MenuFood] = []) { self.id = id; self.name = name; self.start = start; self.end = end; self.foods = foods }
}
public struct MenuSnapshot: Codable, Equatable, Sendable {
    public let hall: String; public let day: String; public var fetchedAt: Date; public var periods: [MenuPeriod]; public var unavailableDishes: Int; public var supplementVerified: Bool
    public init(hall: String, day: String, fetchedAt: Date = Date(), periods: [MenuPeriod], unavailableDishes: Int = 0, supplementVerified: Bool = false) { self.hall = hall; self.day = day; self.fetchedAt = fetchedAt; self.periods = periods; self.unavailableDishes = unavailableDishes; self.supplementVerified = supplementVerified }
}
public struct MenuResult: Sendable {
    public let snapshot: MenuSnapshot?; public let warning: String?; public let cached: Bool
    public init(snapshot: MenuSnapshot?, warning: String? = nil, cached: Bool = false) { self.snapshot = snapshot; self.warning = warning; self.cached = cached }
}
public enum Meal: String, Codable, CaseIterable, Sendable { case breakfast = "Breakfast", lunch = "Lunch", dinner = "Dinner", snacks = "Snacks" }
public struct DiaryEntry: Codable, Equatable, Sendable, Identifiable {
    public let id: UUID; public let createdAt: Date; public let dish: Dish; public let hall: String; public let station: String; public let period: String; public var meal: Meal; public var quantity: Double; public let day: String
    public init(id: UUID = UUID(), createdAt: Date = Date(), dish: Dish, hall: String, station: String, period: String, meal: Meal, quantity: Double, day: String) { self.id = id; self.createdAt = createdAt; self.dish = dish; self.hall = hall; self.station = station; self.period = period; self.meal = meal; self.quantity = quantity; self.day = day }
    public var totals: Nutrition { (try? dish.nutrition.scaled(by: quantity)) ?? Nutrition() }
    public func edited(quantity: Double, meal: Meal) throws -> DiaryEntry { _ = try dish.nutrition.scaled(by: quantity); var copy = self; copy.quantity = quantity; copy.meal = meal; return copy }
}
public func hallName(_ id: String) -> String { id == "brandywine" ? "Brandywine" : "Anteatery" }
