import SwiftUI
import SwiftData
import ZotMacrosStorage
@main struct ZotMacrosApp: App {
    @State private var model: AppModel?
    @State private var startupError: String?
    var body: some Scene {
        WindowGroup {
            Group {
                if let model { RootView(model: model) }
                else if let startupError {
                    ContentUnavailableView {
                        Label("Your diary couldn’t open", systemImage: "externaldrive.badge.exclamationmark")
                    } description: { Text(startupError + " Your saved data has not been deleted.") }
                    actions: { Button("Retry", action: openStore) }
                } else { ProgressView("Opening ZotMacros…").task { openStore() } }
            }
            .tint(ZotTheme.gold).preferredColorScheme(.dark)
        }
    }
    @MainActor private func openStore() {
        do { model = AppModel(container: try Store.make()); startupError = nil }
        catch { startupError = error.localizedDescription }
    }
}
