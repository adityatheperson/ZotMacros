import Foundation
import Observation
import ZotMacrosCore
@MainActor @Observable public final class MenuModel {
    public private(set) var result: MenuResult?
    public private(set) var loading = false
    public private(set) var hall = "anteatery"
    public private(set) var day = ""
    private var generation = UUID()
    private var request: Task<MenuResult, Error>?
    private let load: @Sendable (String, String, Bool) async throws -> MenuResult
    public init(load: @escaping @Sendable (String, String, Bool) async throws -> MenuResult) { self.load = load }
    public func invalidate(hall: String, day: String) { request?.cancel(); generation = UUID(); self.hall = hall; self.day = day; result = nil; loading = false }
    public func refresh(hall: String, day: String, force: Bool) async {
        request?.cancel()
        let token = UUID(); generation = token
        if self.hall != hall || self.day != day { result = nil }
        self.hall = hall; self.day = day; loading = true
        defer { if generation == token { loading = false; request = nil } }
        let load = self.load
        let task = Task { try await load(hall, day, force) }; request = task
        do {
            let value = try await withTaskCancellationHandler(operation: { try await task.value }, onCancel: { task.cancel() })
            guard generation == token, !Task.isCancelled else { return }
            result = value
        } catch {
            guard generation == token else { return }
            if !(error is CancellationError), !Task.isCancelled { result = MenuResult(snapshot: result?.snapshot, warning: error.localizedDescription, cached: result?.snapshot != nil) }
        }
    }
}
