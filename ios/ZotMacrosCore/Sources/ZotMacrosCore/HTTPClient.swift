import Foundation
public protocol HTTPClient: Sendable { func send(_ request: URLRequest) async throws -> Data }
public struct URLSessionHTTPClient: HTTPClient {
    private let session: URLSession
    public init(session: URLSession = .shared) { self.session = session }
    public func send(_ request: URLRequest) async throws -> Data {
        let (data, response) = try await session.data(for: request)
        try Task.checkCancellation()
        guard let http = response as? HTTPURLResponse else { throw ZotError.invalidData("Invalid service response.") }
        guard (200..<300).contains(http.statusCode) else { throw ZotError.http(http.statusCode) }
        return data
    }
}
public protocol MenuCache: Sendable {
    func exact(hall: String, day: String) async throws -> MenuSnapshot?
    func latest(hall: String, beforeOrOn day: String) async throws -> MenuSnapshot?
    func save(_ snapshot: MenuSnapshot) async throws
}
