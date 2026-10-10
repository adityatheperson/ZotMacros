import Foundation
public enum UciSupplementService {
    static let locationQuery = #"query($locationUrlKey:String!){getLocation(campusUrlKey:"campus",locationUrlKey:$locationUrlKey){commerceAttributes{children{id name}} aemAttributes{hoursOfOperation{schedule}}} Commerce_mealPeriods {id name}}"#
    static let menuQuery = #"query($locationUrlKey:String!,$date:String!,$mealPeriod:Int,$viewType:Commerce_MenuViewType!){getLocationRecipes(campusUrlKey:"campus",locationUrlKey:$locationUrlKey,date:$date,mealPeriod:$mealPeriod,viewType:$viewType){locationRecipesMap{dateSkuMap{date stations{id skus{simple configurable{sku variants}}}}} products{items{sku name attributes{name value}}}}}"#
    public static func request(query: String, variables: [String: Any]) throws -> URLRequest {
        var c = URLComponents(string: "https://api.elevate-dxp.com/api/mesh/c087f756-cc72-4649-a36f-3a41b700c519/graphql")!
        c.queryItems = [URLQueryItem(name: "query", value: query), URLQueryItem(name: "variables", value: String(data: try JSONSerialization.data(withJSONObject: variables, options: .sortedKeys), encoding: .utf8)!)]
        guard let url = c.url else { throw ZotError.invalidData("Invalid UCI request.") }
        var r = URLRequest(url: url, cachePolicy: .reloadIgnoringLocalCacheData, timeoutInterval: 30)
        for (key, value) in ["store": "ch_uci_en", "magento-store-code": "ch_uci", "magento-website-code": "ch_uci", "magento-store-view-code": "ch_uci_en", "x-api-key": "ElevateAPIProd", "Origin": "https://uci.mydininghub.com", "Referer": "https://uci.mydininghub.com/"] { r.setValue(value, forHTTPHeaderField: key) }
        return r
    }
    public static func complete(_ snapshot: MenuSnapshot, http: any HTTPClient) async throws -> MenuSnapshot {
        let location = snapshot.hall == "anteatery" ? "the-anteatery" : "brandywine"
        let metadata = try UciMenuParser.metadata(data: await http.send(request(query: locationQuery, variables: ["locationUrlKey": location])), day: snapshot.day)
        let periods = (snapshot.periods + metadata.periods.filter { fresh in !snapshot.periods.contains { $0.name == fresh.name } }).sorted { ($0.start ?? "99:99", $0.name) < ($1.start ?? "99:99", $1.name) }
        var completed: [MenuPeriod] = []
        for period in periods {
            try Task.checkCancellation()
            guard let meal = metadata.meals[period.name] else { throw ZotError.invalidData("Unknown UCI meal period.") }
            let data = try await http.send(request(query: menuQuery, variables: ["locationUrlKey": location, "date": snapshot.day, "mealPeriod": meal, "viewType": "WEEKLY"]))
            completed.append(reconcile(period: period, foods: try UciMenuParser.foods(data: data, day: snapshot.day, stations: metadata.stations)))
        }
        var result = snapshot; result.periods = completed; result.supplementVerified = true; return result
    }
}
