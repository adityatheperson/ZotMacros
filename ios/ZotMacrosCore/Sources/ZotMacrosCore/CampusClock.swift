import Foundation
public enum CampusClock {
    public static var calendar: Calendar { var c = Calendar(identifier: .gregorian); c.timeZone = TimeZone(identifier: "America/Los_Angeles")!; return c }
    public static func day(at date: Date = Date()) -> String {
        let c = calendar.dateComponents([.year, .month, .day], from: date)
        return String(format: "%04d-%02d-%02d", c.year!, c.month!, c.day!)
    }
    public static func date(_ day: String) -> Date? {
        let f = DateFormatter(); f.calendar = calendar; f.locale = Locale(identifier: "en_US_POSIX"); f.timeZone = calendar.timeZone; f.dateFormat = "yyyy-MM-dd"; f.isLenient = false
        guard let d = f.date(from: day), self.day(at: d) == day else { return nil }; return d
    }
    public static func nextMidnight(after date: Date) -> Date { calendar.date(byAdding: .day, value: 1, to: calendar.startOfDay(for: date))! }
}
