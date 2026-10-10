import Foundation
import Observation
import ZotMacrosCore
public struct UserPreferences: Codable, Equatable, Sendable {
    public var onboarded = false
    public var calories = 2400.0, protein = 160.0, carbs = 250.0, fat = 75.0
    public var hall = "anteatery", meal = "Automatic"
    public var autoRefresh = true, haptics = true, exampleGoals = true
    public init() {}
    public func goal(_ nutrient: Nutrient) -> Double { switch nutrient { case .calories: calories; case .protein: protein; case .carbs: carbs; case .fat: fat; default: 0 } }
    public func validate() throws {
        guard [calories, protein, carbs, fat].allSatisfy({ $0.isFinite && $0 > 0 && $0 <= 100_000 }), ["anteatery", "brandywine"].contains(hall) else { throw ZotError.invalidData("Enter goals greater than 0 and no more than 100,000.") }
    }
}
@MainActor @Observable public final class PreferencesStore {
    public private(set) var value: UserPreferences
    private let defaults: UserDefaults
    public init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
        if let data = defaults.data(forKey: "zot.preferences.v1"), let decoded = try? JSONDecoder().decode(UserPreferences.self, from: data), (try? decoded.validate()) != nil { value = decoded } else { value = UserPreferences() }
    }
    public func save(_ newValue: UserPreferences) throws { try newValue.validate(); let data = try JSONEncoder().encode(newValue); defaults.set(data, forKey: "zot.preferences.v1"); value = newValue }
}
