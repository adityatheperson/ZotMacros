import Foundation
public func uciPeriods(schedules: [[String: Any]], day: String, meals: [String: Int]) throws -> [MenuPeriod] {
    guard let date = CampusClock.date(day) else { throw ZotError.invalidData("Invalid campus date.") }
    let weekday = (CampusClock.calendar.component(.weekday, from: date) + 5) % 7
    guard let schedule = schedules.first(where: { if let start = string($0["start_date"]), let end = string($0["end_date"]) { return day >= start && day <= end }; return false }) ?? schedules.first(where: { string($0["type"]) == "standard" }) else { throw ZotError.invalidData("UCI schedule unavailable.") }
    let days = ["Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"]
    return try array(schedule["meal_periods"]).compactMap { value in
        let p = try object(value), name = try nonempty(p["meal_period"])
        guard let raw = p["opening_hours"] as? String else { throw ZotError.invalidData("Missing UCI hours.") }
        var hours: [String]?
        for block in raw.split(separator: ";") {
            let parts = block.split(whereSeparator: { $0.isWhitespace }).map(String.init)
            guard parts.count == 2 else { throw ZotError.invalidData("Unrecognized UCI hours.") }
            let range = parts[0].split(separator: "-").map(String.init)
            guard (1...2).contains(range.count), let start = days.firstIndex(of: range[0]), let end = days.firstIndex(of: range.last!) else { throw ZotError.invalidData("Unrecognized UCI days.") }
            if start <= end ? (start...end).contains(weekday) : (weekday >= start || weekday <= end) {
                if parts[1] == "off" { hours = nil } else {
                    let times = parts[1].split(separator: "-").map(String.init)
                    guard times.count == 2, times.allSatisfy({ $0.range(of: #"^([01]\d|2[0-3]):[0-5]\d$"#, options: .regularExpression) != nil }) else { throw ZotError.invalidData("Unrecognized UCI times.") }
                    hours = times
                }
            }
        }
        guard let hours else { return nil }; guard let id = meals[name] else { throw ZotError.invalidData("Unknown UCI meal.") }
        return MenuPeriod(id: "uci:\(id)", name: name, start: hours[0], end: hours[1])
    }
}
