import Foundation
public enum DiningService {
    public static func request(path: String, query: [String: String] = [:]) throws -> URLRequest {
        var c = URLComponents(string: "https://anteaterapi.com/v2/rest/dining/" + path)!
        c.queryItems = query.sorted { $0.key < $1.key }.map { URLQueryItem(name: $0.key, value: $0.value) }
        guard let url = c.url else { throw ZotError.invalidData("Invalid menu request.") }
        return URLRequest(url: url, cachePolicy: .reloadIgnoringLocalCacheData, timeoutInterval: 30)
    }
    public static func dishRequests(ids: [String]) throws -> [URLRequest] {
        let unique = Array(Set(ids)).sorted()
        return try stride(from: 0, to: unique.count, by: 20).map { start in try request(path: "dishes/batch", query: ["ids": unique[start..<min(start + 20, unique.count)].joined(separator: ",")]) }
    }
    public static func fetch(http: any HTTPClient, hall: String, day: String, now: Date) async throws -> MenuSnapshot {
        let stations = try DiningParser.stations(data: await http.send(request(path: "restaurants")), hall: hall)
        let periods = try DiningParser.periods(data: await http.send(request(path: "restaurantToday", query: ["id": hall, "date": day])), hall: hall)
        let ids = periods.flatMap { $0.stationToDishes.values.flatMap { $0 } }
        var dishes: [String: Dish] = [:]
        for request in try dishRequests(ids: ids) { try Task.checkCancellation(); for dish in try DiningParser.dishes(data: await http.send(request)) { dishes[dish.id] = dish } }
        return MenuSnapshot(hall: hall, day: day, fetchedAt: now, periods: periods.map { p in
            MenuPeriod(id: p.id, name: p.name, start: p.start, end: p.end, foods: p.stationToDishes.keys.sorted().flatMap { station in p.stationToDishes[station]!.compactMap { id in dishes[id].map { MenuFood(dish: $0, stationId: station, station: stations[station] ?? "Station \(station)") } } })
        }, unavailableDishes: Set(ids).filter { dishes[$0] == nil }.count)
    }
}
