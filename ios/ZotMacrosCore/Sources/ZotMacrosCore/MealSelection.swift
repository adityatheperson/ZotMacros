import Foundation
public func mealForPeriod(_ name: String) -> Meal {
    let name = name.lowercased()
    if name.contains("breakfast") { return .breakfast }
    if name.contains("lunch") || name.contains("brunch") { return .lunch }
    if name.contains("dinner") { return .dinner }
    return .snacks
}
public func choosePeriod(_ periods: [MenuPeriod], preference: String, time: String) -> String? {
    if preference != "Automatic", let p = periods.first(where: { $0.name.caseInsensitiveCompare(preference) == .orderedSame }) { return p.id }
    return periods.first { p in guard let start = p.start, let end = p.end else { return false }; return start <= time && time < end }?.id ?? periods.first?.id
}
