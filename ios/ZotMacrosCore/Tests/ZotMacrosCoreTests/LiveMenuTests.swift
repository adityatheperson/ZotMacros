import Foundation
import Testing
@testable import ZotMacrosCore
struct LiveMenuTests {
    @Test(.enabled(if: ProcessInfo.processInfo.environment["ZOT_LIVE_MENU"] == "1"))
    func testCurrentMenuMembershipAndNutrition() async throws {
        let day = ProcessInfo.processInfo.environment["ZOT_LIVE_DAY"] ?? CampusClock.day()
        let hall = ProcessInfo.processInfo.environment["ZOT_LIVE_HALL"] ?? "anteatery"
        let http = TracingHTTP()
        let original = try await DiningService.fetch(http: http, hall: hall, day: day, now: Date())
        let complete = try await UciSupplementService.complete(original, http: http)
        #expect(complete.supplementVerified); #expect(complete.day == day)
        for period in complete.periods {
            #expect(Set(period.foods.map(\.id)).count == period.foods.count)
            for food in period.foods {
                if let old = original.periods.first(where: { $0.id == period.id })?.foods.first(where: { $0.id == food.id }) { #expect(old.dish.nutrition == food.dish.nutrition) }
            }
            let groups = Dictionary(grouping: period.foods, by: \.station).mapValues(\.count)
            print("LIVE \(hall) \(day) \(period.name): \(period.foods.count) foods, stations \(groups.sorted { $0.key < $1.key })")
        }
        let output = FileManager.default.temporaryDirectory.appendingPathComponent("zot-ios-live-menu.json")
        try JSONEncoder().encode(complete).write(to: output)
    }
}

struct FallbackLiveTests {
    @Test(.enabled(if: ProcessInfo.processInfo.environment["ZOT_LIVE_MENU"] == "1"))
    func testPublicRepositoryRemainsUsable() async throws {
        let day = ProcessInfo.processInfo.environment["ZOT_LIVE_DAY"] ?? CampusClock.day()
        let hall = ProcessInfo.processInfo.environment["ZOT_LIVE_HALL"] ?? "anteatery"
        let repo = MenuRepository(http: URLSessionHTTPClient(), cache: MemoryCache())
        let result = try await repo.load(hall: hall, day: day, force: true)
        let snapshot = try #require(result.snapshot)
        #expect(snapshot.hall == hall); #expect(snapshot.day == day)
        if !snapshot.supplementVerified { #expect(result.warning?.contains("Some foods may be missing") == true) }
        print("LIVE repository \(hall) \(day): verified=\(snapshot.supplementVerified), periods=\(snapshot.periods.count), warning=\(result.warning ?? "none")")
    }
}

private struct TracingHTTP: HTTPClient {
    func send(_ request: URLRequest) async throws -> Data {
        let data = try await URLSessionHTTPClient().send(request)
        if request.url?.host == "api.elevate-dxp.com" {
            try data.write(to: FileManager.default.temporaryDirectory.appendingPathComponent("zot-ios-last-uci-response.json"))
        }
        return data
    }
}
