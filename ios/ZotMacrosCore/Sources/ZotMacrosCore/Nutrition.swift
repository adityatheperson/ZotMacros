import Foundation
public enum Nutrient: String, CaseIterable, Codable, Sendable {
    case calories, protein, carbs, fat, saturatedFat, transFat, fiber, sugar, sodium, cholesterol, iron, calcium, vitaminA, vitaminC
    public var label: String { switch self {
    case .calories: "Calories"; case .protein: "Protein"; case .carbs: "Carbs"; case .fat: "Fats"
    case .saturatedFat: "Saturated fat"; case .transFat: "Trans fat"; case .fiber: "Fiber"; case .sugar: "Sugar"
    case .sodium: "Sodium"; case .cholesterol: "Cholesterol"; case .iron: "Iron"; case .calcium: "Calcium"
    case .vitaminA: "Vitamin A"; case .vitaminC: "Vitamin C" } }
    public var unit: String { switch self { case .calories: "kcal"; case .sodium, .cholesterol, .iron, .calcium: "mg"; case .vitaminA, .vitaminC: "IU"; default: "g" } }
    public var apiKey: String { switch self {
    case .calories: "calories"; case .protein: "proteinG"; case .carbs: "totalCarbsG"; case .fat: "totalFatG"
    case .saturatedFat: "saturatedFatG"; case .transFat: "transFatG"; case .fiber: "dietaryFiberG"; case .sugar: "sugarsG"
    case .sodium: "sodiumMg"; case .cholesterol: "cholesterolMg"; case .iron: "ironMg"; case .calcium: "calciumMg"
    case .vitaminA: "vitaminAIU"; case .vitaminC: "vitaminCIU" } }
}
public enum ZotError: Error, LocalizedError {
    case invalidQuantity, invalidData(String), http(Int)
    public var errorDescription: String? { switch self {
    case .invalidQuantity: "Enter a serving quantity greater than 0 and at most 1,000."
    case .invalidData(let reason): reason
    case .http(429): "The dining service is busy. Wait a moment, then retry."
    case .http(404): "No menu is available for this hall and date."
    case .http: "The dining service is unavailable. Please retry." } }
}
public struct Nutrition: Codable, Equatable, Sendable {
    private var values: [Nutrient: Double]
    public init(_ values: [Nutrient: Double] = [:]) { self.values = values.filter { $0.value.isFinite && $0.value >= 0 } }
    public subscript(_ key: Nutrient) -> Double? { guard let v = values[key], v.isFinite, v >= 0 else { return nil }; return v }
    public func scaled(by quantity: Double) throws -> Nutrition {
        guard quantity.isFinite, quantity > 0, quantity <= 1000 else { throw ZotError.invalidQuantity }
        return Nutrition(values.mapValues { $0 * quantity })
    }
}
public struct NutrientTotal: Equatable, Sendable {
    public let value: Double?
    public let missing: Int
    public init(value: Double?, missing: Int) { self.value = value; self.missing = missing }
    public var isPartial: Bool { missing > 0 }
}
public func aggregate(_ items: [Nutrition]) -> [Nutrient: NutrientTotal] {
    Dictionary(uniqueKeysWithValues: Nutrient.allCases.map { key in
        let known = items.compactMap { $0[key] }, sum = items.compactMap { $0[key] }.reduce(0, +)
        let value: Double? = items.isEmpty ? 0 : (known.isEmpty || !sum.isFinite ? nil : sum)
        return (key, NutrientTotal(value: value, missing: items.count - known.count + (sum.isFinite ? 0 : known.count)))
    })
}
