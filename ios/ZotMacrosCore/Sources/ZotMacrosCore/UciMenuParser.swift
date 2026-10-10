import Foundation
public struct SupplementFood: Sendable {
    public let id: String; public let name: String; public let stationId: String; public let station: String; public let aliases: [String]
    public init(id: String, name: String, stationId: String, station: String, aliases: [String]) { self.id = id; self.name = name; self.stationId = stationId; self.station = station; self.aliases = aliases }
}
public struct UciMetadata: Sendable { public let stations: [String: String]; public let meals: [String: Int]; public let periods: [MenuPeriod] }
public enum UciMenuParser {
    static func payload(_ data: Data) throws -> [String: Any] { let r = try root(data); guard (r["errors"] as? [Any] ?? []).isEmpty else { throw ZotError.invalidData("Incomplete UCI response.") }; return try object(r["data"]) }
    public static func metadata(data: Data, day: String) throws -> UciMetadata {
        let r = try payload(data), location = try object(r["getLocation"])
        var stations: [String: String] = [:], meals: [String: Int] = [:]
        for child in try array(object(location["commerceAttributes"])["children"]) { let o = try object(child); stations[try nonempty(o["id"])] = try nonempty(o["name"]) }
        for child in try array(r["Commerce_mealPeriods"]) { let o = try object(child); guard let id = Int(try nonempty(o["id"])) else { throw ZotError.invalidData("Invalid meal ID.") }; meals[try nonempty(o["name"])] = id }
        let schedules = try array(object(object(location["aemAttributes"])["hoursOfOperation"])["schedule"]).map { try object($0) }
        return UciMetadata(stations: stations, meals: meals, periods: try uciPeriods(schedules: schedules, day: day, meals: meals))
    }
    public static func foods(data: Data, day: String, stations: [String: String], requireKnownStations: Bool = true) throws -> [SupplementFood] {
        let menu = try object(payload(data)["getLocationRecipes"])
        if menu["locationRecipesMap"] is NSNull && menu["products"] is NSNull { return [] }
        let days = try array(object(menu["locationRecipesMap"])["dateSkuMap"]).map { try object($0) }.filter { string($0["date"]) == day }
        guard days.count == 1 else { throw ZotError.invalidData("Menu date unavailable.") }
        var products: [String: [String: Any]] = [:]
        for value in try array(object(menu["products"])["items"]) { let o = try object(value); products[try nonempty(o["sku"])] = o }
        var result: [SupplementFood] = [], seen = Set<String>()
        for value in try array(days[0]["stations"]) {
            let station = try object(value), id = try nonempty(station["id"]), skus = try object(station["skus"])
            guard !requireKnownStations || stations[id] != nil else { throw ZotError.invalidData("Unknown UCI station.") }
            let simple = try array(skus["simple"]).map { try nonempty($0) }
            var entries = simple.map { ($0, [$0]) }
            for value in try array(skus["configurable"]) { let p = try object(value), sku = try nonempty(p["sku"]), variants = try array(p["variants"]).map { try nonempty($0) }; if !variants.contains(where: simple.contains) { entries.append((sku, [sku] + variants)) } }
            for (sku, aliases) in entries {
                guard let p = products[sku] else { throw ZotError.invalidData("Missing UCI food details.") }
                let name = try nonempty(p["name"])
                let attributes = try array(p["attributes"]).map { try object($0) }
                let hidden = attributes.first { string($0["name"]) == "is_hide_from_web_menu" }.flatMap { string($0["value"]) }
                if hidden == "1" || hidden == "yes" { continue }
                if seen.insert(id + ":" + sku).inserted { result.append(SupplementFood(id: sku, name: name, stationId: id, station: stations[id] ?? "Station \(id)", aliases: aliases)) }
            }
        }
        return result
    }
}
public func reconcile(period: MenuPeriod, foods: [SupplementFood]) -> MenuPeriod {
    var copy = period, seen = Set<String>()
    copy.foods = foods.filter { seen.insert($0.stationId + ":" + $0.id).inserted }.map { food in
        period.foods.first { $0.stationId == food.stationId && ($0.dish.id == food.id || $0.dish.id == "\(food.id):station:\(food.stationId)") } ?? MenuFood(dish: Dish(id: food.id, name: food.name), stationId: food.stationId, station: food.station)
    }
    return copy
}
