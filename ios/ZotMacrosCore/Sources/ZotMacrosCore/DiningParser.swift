import Foundation
import CoreFoundation
func object(_ value: Any?) throws -> [String: Any] { guard let v = value as? [String: Any] else { throw ZotError.invalidData("Invalid menu data.") }; return v }
func array(_ value: Any?) throws -> [Any] { guard let v = value as? [Any] else { throw ZotError.invalidData("Missing menu entries.") }; return v }
func string(_ value: Any?) -> String? {
    if let s = value as? String { return s }
    if let n = value as? NSNumber, CFGetTypeID(n) != CFBooleanGetTypeID() { return n.stringValue }
    return nil
}
func nonempty(_ value: Any?) throws -> String { guard let s = string(value), !s.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { throw ZotError.invalidData("Missing menu details.") }; return s }
func root(_ data: Data) throws -> [String: Any] { try object(JSONSerialization.jsonObject(with: data)) }
public struct RemotePeriod: Sendable {
    public let id: String; public let name: String; public let start: String?; public let end: String?; public let stationToDishes: [String: [String]]
}
public enum DiningParser {
    static func payload(_ data: Data) throws -> Any { let r = try root(data); guard r["ok"] as? Bool == true, let value = r["data"], !(value is NSNull) else { throw ZotError.invalidData("The dining service could not return this menu.") }; return value }
    public static func stations(data: Data, hall: String) throws -> [String: String] {
        let rows = try array(payload(data)); guard let r = rows.compactMap({ $0 as? [String: Any] }).first(where: { string($0["id"]) == hall }) else { throw ZotError.invalidData("This dining hall is unavailable.") }
        var names: [String: String] = [:]
        for row in (r["stations"] as? [[String: Any]] ?? []) { if let id = string(row["id"]) { names[id] = (try? nonempty(row["name"])) ?? "Station \(id)" } }
        return names
    }
    public static func periods(data: Data, hall: String) throws -> [RemotePeriod] {
        let r = try object(payload(data)); guard string(r["id"]) == hall else { throw ZotError.invalidData("The service returned a different hall.") }
        return try object(r["periods"]).compactMap { id, value -> RemotePeriod? in
            guard let p = value as? [String: Any], let name = try? nonempty(p["name"]) else { return nil }
            let stations = (p["stationToDishes"] as? [String: Any] ?? [:]).mapValues { value in Array(Set((value as? [Any] ?? []).compactMap(string))).sorted() }
            return RemotePeriod(id: id, name: name, start: string(p["startTime"]), end: string(p["endTime"]), stationToDishes: stations)
        }.sorted { ($0.start ?? "99:99", $0.name) < ($1.start ?? "99:99", $1.name) }
    }
    public static func dishes(data: Data) throws -> [Dish] {
        try array(payload(data)).compactMap { value in
            guard let row = value as? [String: Any], let id = try? nonempty(row["id"]), let name = try? nonempty(row["name"]) else { return nil }
            let n = row["nutritionInfo"] as? [String: Any] ?? [:]
            var values: [Nutrient: Double] = [:]
            for key in Nutrient.allCases { if let s = string(n[key.apiKey]), let v = Double(s), v.isFinite, v >= 0 { values[key] = v } }
            let serving = [string(n["servingSize"]), string(n["servingUnit"])].compactMap { $0 }.filter { !$0.isEmpty }.joined(separator: " ")
            let restrictions = row["dietRestriction"] as? [String: Any]
            let allergens = (restrictions ?? [:]).filter { $0.key.hasPrefix("contains") && $0.value as? Bool == true }.keys.map { String($0.dropFirst(8)).replacingOccurrences(of: "([a-z])([A-Z])", with: "$1 $2", options: .regularExpression) }.sorted()
            return Dish(id: id, name: name, serving: serving.isEmpty ? "Serving size unavailable" : serving, nutrition: Nutrition(values), description: string(row["description"]) ?? "", ingredients: string(row["ingredients"]), allergens: allergens, allergensReported: restrictions != nil)
        }
    }
}
