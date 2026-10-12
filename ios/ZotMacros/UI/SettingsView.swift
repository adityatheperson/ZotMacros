import SwiftUI
import ZotMacrosCore
import ZotMacrosStorage
struct SettingsView: View {
    @Bindable var model: AppModel
    @State private var draft = UserPreferences()
    @State private var clear = false
    @State private var error: String?
    @State private var saved = false
    var body: some View {
        Form {
            Section {
                if draft.exampleGoals { Text("These are example goals. Adjust them or accept them to personalize your tracker.").foregroundStyle(ZotTheme.muted) }
                goal("Calories (kcal)", value: $draft.calories)
                goal("Protein (g)", value: $draft.protein)
                goal("Carbs (g)", value: $draft.carbs)
                goal("Fats (g)", value: $draft.fat)
                Button(draft.exampleGoals ? "Accept goals" : "Save goals") {
                    var new = model.preferences.value
                    new.calories = draft.calories; new.protein = draft.protein
                    new.carbs = draft.carbs; new.fat = draft.fat; new.exampleGoals = false
                    if save(new) { draft = new }
                }.frame(minHeight: 48)
            } header: { Text("Daily goals").foregroundStyle(ZotTheme.muted) }
            .listRowBackground(ZotTheme.surface)
            Section("Dining preferences") {
                Picker("Preferred hall", selection: preference(\.hall)) { Text("Anteatery").tag("anteatery"); Text("Brandywine").tag("brandywine") }.accessibilityIdentifier("preferredHall")
                Picker("Default meal", selection: preference(\.meal)) { ForEach(meals, id: \.self) { Text($0).tag($0) } }
                Toggle("Refresh menus automatically", isOn: preference(\.autoRefresh))
                Toggle("Haptic feedback", isOn: preference(\.haptics))
                if saved { Label("Preferences saved", systemImage: "checkmark.circle").foregroundStyle(ZotTheme.muted) }
                if let error { Text(error).foregroundStyle(ZotTheme.gold) }
            }.listRowBackground(ZotTheme.surface)
            Section("On this iPhone") {
                Text("Your food diary stays on this device. Menu requests are sent to Anteater API and UCI Dining.").font(.footnote)
                Button("Clear local food history", role: .destructive) { clear = true }.frame(minHeight: 48)
            }.listRowBackground(ZotTheme.surface)
            Section("About ZotMacros") {
                Text("Fuel your day. Track your macros.")
                Text("An independent app for UCI students. Not affiliated with or endorsed by UC Irvine.").font(.footnote).foregroundStyle(ZotTheme.muted)
                Attribution()
            }.listRowBackground(ZotTheme.surface)
        }.scrollContentBackground(.hidden).zotScreen().toolbar(.hidden, for: .navigationBar)
            .safeAreaInset(edge: .top, spacing: 0) { ScreenHeader(title: "Settings") { EmptyView() } }
            .onAppear { draft = model.preferences.value }
            .alert("Clear food history?", isPresented: $clear) { Button("Cancel", role: .cancel) {}; Button("Clear history", role: .destructive) { model.clearDiary() } } message: { Text("This permanently removes all food entries stored on this device.") }
    }
    private var meals: [String] { ["Automatic"] + Array(Set(["Breakfast", "Lunch", "Dinner", "Brunch", "All Day", "Afternoon Snack", "Evening Snack", draft.meal] + (model.menu.result?.snapshot?.periods.map(\.name) ?? []))).filter { $0 != "Automatic" }.sorted() }
    private func goal(_ label: String, value: Binding<Double>) -> some View {
        LabeledContent(label) { TextField(label, value: value, format: .number).keyboardType(.decimalPad).multilineTextAlignment(.trailing).frame(minHeight: 48).accessibilityLabel(label) }
    }
    private func preference<Value>(_ keyPath: WritableKeyPath<UserPreferences, Value>) -> Binding<Value> {
        Binding(get: { model.preferences.value[keyPath: keyPath] }, set: { value in
            var new = model.preferences.value
            new[keyPath: keyPath] = value
            _ = save(new)
        })
    }
    @discardableResult private func save(_ new: UserPreferences) -> Bool {
        do { let oldHall = model.preferences.value.hall; try model.preferences.save(new); saved = true; error = nil; if oldHall != new.hall { model.menu.invalidate(hall: new.hall, day: model.day) }; return true }
        catch { self.error = error.localizedDescription; saved = false; return false }
    }
}
