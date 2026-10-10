import Foundation
public struct MenuRepository: Sendable {
    private let http: any HTTPClient
    private let cache: any MenuCache
    private let now: @Sendable () -> Date
    public init(http: any HTTPClient, cache: any MenuCache, now: @escaping @Sendable () -> Date = { Date() }) { self.http = http; self.cache = cache; self.now = now }
    public func load(hall: String, day: String, force: Bool = false) async throws -> MenuResult {
        guard ["anteatery", "brandywine"].contains(hall), CampusClock.date(day) != nil else { throw ZotError.invalidData("Invalid hall or campus date.") }
        let exact = try? await cache.exact(hall: hall, day: day)
        if !force, let exact, exact.supplementVerified, (0..<900).contains(now().timeIntervalSince(exact.fetchedAt)) { return MenuResult(snapshot: exact, cached: true) }
        do {
            let anteater = try await DiningService.fetch(http: http, hall: hall, day: day, now: now())
            let snapshot: MenuSnapshot
            do { snapshot = try await UciSupplementService.complete(anteater, http: http) }
            catch {
                try checkCancellation(error)
                return MenuResult(snapshot: exact ?? anteater, warning: exact == nil ? "UCI menu supplement unavailable. Some foods may be missing; retry to refresh." : "UCI menu supplement unavailable. Showing saved menu; retry to refresh.", cached: exact != nil)
            }
            try Task.checkCancellation()
            do { try await cache.save(snapshot) } catch { try checkCancellation(error); return MenuResult(snapshot: snapshot, warning: "Menu loaded, but could not be saved for offline use.") }
            return MenuResult(snapshot: snapshot)
        } catch {
            try checkCancellation(error)
            var fallback = exact
            if fallback == nil { fallback = try? await cache.latest(hall: hall, beforeOrOn: day) }
            return MenuResult(snapshot: fallback, warning: (error as? ZotError)?.errorDescription ?? "Can’t reach the dining service. Check your connection and retry.", cached: fallback != nil)
        }
    }
    private func checkCancellation(_ error: Error) throws {
        if error is CancellationError || (error as? URLError)?.code == .cancelled { throw CancellationError() }; try Task.checkCancellation()
    }
}
