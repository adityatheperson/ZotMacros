import Foundation
import Testing
@testable import ZotMacrosCore
actor MemoryCache: MenuCache {
    var saved: MenuSnapshot?
    init(_ value: MenuSnapshot? = nil) { saved = value }
    func exact(hall: String, day: String) -> MenuSnapshot? { saved?.hall == hall && saved?.day == day ? saved : nil }
    func latest(hall: String, beforeOrOn day: String) -> MenuSnapshot? { saved?.hall == hall && saved!.day <= day ? saved : nil }
    func save(_ snapshot: MenuSnapshot) { saved = snapshot }
}
actor FailingHTTP: HTTPClient {
    var calls = 0
    let error: Error
    init(_ error: Error = ZotError.http(429)) { self.error = error }
    func send(_ request: URLRequest) async throws -> Data { calls += 1; throw error }
}
struct MenuRepositoryTests {
    @Test func testFreshCacheSkipsNetwork() async throws {
        let snapshot = MenuSnapshot(hall: "anteatery", day: "2026-10-10", periods: [], supplementVerified: true)
        let http = FailingHTTP(), cache = MemoryCache(snapshot)
        let result = try await MenuRepository(http: http, cache: cache).load(hall: "anteatery", day: "2026-10-10", force: false)
        #expect(result.snapshot == snapshot); #expect(result.cached); #expect(await http.calls == 0)
    }
    @Test func testRateLimitRetainsHistoricalDate() async throws {
        let snapshot = MenuSnapshot(hall: "anteatery", day: "2026-10-09", periods: [], supplementVerified: true)
        let http = FailingHTTP(), cache = MemoryCache(snapshot)
        let result = try await MenuRepository(http: http, cache: cache).load(hall: "anteatery", day: "2026-10-10", force: true)
        #expect(result.snapshot?.day == "2026-10-09"); #expect(result.warning?.contains("busy") == true); #expect(await http.calls == 1)
    }
    @Test func testCancellationPropagates() async {
        let repository = MenuRepository(http: FailingHTTP(CancellationError()), cache: MemoryCache())
        await #expect(throws: CancellationError.self) { try await repository.load(hall: "anteatery", day: "2026-10-10", force: true) }
    }
    @Test func testRequestsAreExplicitAndBatched() throws {
        let requests = try DiningService.dishRequests(ids: (0..<45).map(String.init) + ["0"])
        #expect(requests.count == 3)
        #expect(URLComponents(url: requests[0].url!, resolvingAgainstBaseURL: false)?.queryItems?.first?.value?.split(separator: ",").count == 20)
        let day = try DiningService.request(path: "restaurantToday", query: ["id": "anteatery", "date": "2026-10-10"])
        #expect(day.url!.absoluteString.contains("date=2026-10-10"))
        let uci = try UciSupplementService.request(query: "query", variables: ["date": "2026-10-10"])
        #expect(uci.value(forHTTPHeaderField: "store") == "ch_uci_en")
    }
}

actor PartialHTTP: HTTPClient {
    func send(_ request: URLRequest) async throws -> Data {
        if request.url!.host == "api.elevate-dxp.com" { throw ZotError.http(503) }
        if request.url!.path.hasSuffix("restaurants") { return Data(#"{"ok":true,"data":[{"id":"anteatery","stations":[]}]}"#.utf8) }
        return Data(#"{"ok":true,"data":{"id":"anteatery","periods":{}}}"#.utf8)
    }
}
struct SupplementFailureTests {
    @Test func testExactDateCacheSurvivesSupplementFailure() async throws {
        let snapshot = MenuSnapshot(hall: "anteatery", day: "2026-10-10", fetchedAt: Date(timeIntervalSince1970: 0), periods: [], supplementVerified: true)
        let cache = MemoryCache(snapshot), repo = MenuRepository(http: PartialHTTP(), cache: cache)
        let result = try await repo.load(hall: "anteatery", day: "2026-10-10", force: true)
        #expect(result.snapshot == snapshot); #expect(result.cached); #expect(result.warning?.contains("supplement unavailable") == true)
        #expect(await cache.saved == snapshot)
    }
    @Test func testIncompleteMenuIsNotSavedAsVerified() async throws {
        let cache = MemoryCache(), repo = MenuRepository(http: PartialHTTP(), cache: cache)
        let result = try await repo.load(hall: "anteatery", day: "2026-10-10", force: true)
        #expect(result.snapshot?.supplementVerified == false); #expect(result.warning?.contains("Some foods may be missing") == true)
        #expect(await cache.saved == nil)
    }
}
