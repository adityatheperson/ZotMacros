import SwiftUI
import ZotMacrosState
struct RootView: View {
    @Bindable var model: AppModel
    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var direction = 1.0
    var body: some View {
        TabView(selection: $model.selectedTab) {
            destination(0) { TodayView(model: model) }.tabItem { Label("Today", systemImage: "house") }.tag(0)
            destination(1) { MenuView(model: model) }.tabItem { Label("Menu", systemImage: "fork.knife") }.tag(1)
            destination(2) { HistoryView(model: model) }.tabItem { Label("History", systemImage: "chart.bar") }.tag(2)
            destination(3) { SettingsView(model: model) }.tabItem { Label("Settings", systemImage: "gearshape") }.tag(3)
        }
        .toolbarBackground(ZotTheme.surface, for: .tabBar).toolbarBackground(.visible, for: .tabBar)
        .onChange(of: model.selectedTab) { old, new in direction = new >= old ? 1 : -1 }
        .sheet(item: $model.editor) { editor in FoodSheet(editor: editor, haptics: model.preferences.value.haptics) }
        .sheet(isPresented: Binding(get: { !model.preferences.value.onboarded }, set: { _ in })) { OnboardingView(preferences: model.preferences) }
        .alert("Something needs attention", isPresented: Binding(get: { model.error != nil }, set: { if !$0 { model.error = nil } })) { Button("OK") { model.error = nil } } message: { Text(model.error ?? "") }
        .task(id: scenePhase) {
            guard scenePhase == .active else { return }
            await model.foreground()
            while !Task.isCancelled {
                let seconds = max(1, ZotMacrosCore.CampusClock.nextMidnight(after: Date()).timeIntervalSinceNow)
                do { try await Task.sleep(for: .seconds(seconds)); await model.foreground() } catch { return }
            }
        }
    }
    private func destination<Content: View>(_ tab: Int, @ViewBuilder content: () -> Content) -> some View {
        NavigationStack { content() }
            .modifier(TabEntrance(active: model.selectedTab == tab, direction: direction, reduceMotion: reduceMotion))
    }
}
import ZotMacrosCore
private struct TabEntrance: ViewModifier {
    let active: Bool; let direction: Double; let reduceMotion: Bool
    @State private var visible = false
    func body(content: Content) -> some View {
        content.opacity(visible ? 1 : 0).offset(x: visible || reduceMotion ? 0 : direction * 28)
            .onAppear { animate() }.onChange(of: active) { _, value in if value { animate() } else { visible = false } }
    }
    private func animate() { visible = false; withAnimation(reduceMotion ? .easeOut(duration: 0.12) : .easeOut(duration: 0.3)) { visible = true } }
}
