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
                    var new = draft; new.exampleGoals = false
                    save(new)
                }.frame(minHeight: 48)
            } header: { Text("Daily goals") }
            Section("Dining preferences") {
                Picker("Preferred hall", selection: $draft.hall) { Text("Anteatery").tag("anteatery"); Text("Brandywine").tag("brandywine") }
                Picker("Default meal", selection: $draft.meal) { ForEach(meals, id: \.self) { Text($0).tag($0) } }
                Toggle("Refresh menus automatically", isOn: $draft.autoRefresh)
                Toggle("Haptic feedback", isOn: $draft.haptics)
                Button("Save preferences") { save(draft) }.frame(minHeight: 48)
                if saved { Label("Preferences saved", systemImage: "checkmark.circle").foregroundStyle(ZotTheme.muted) }
                if let error { Text(error).foregroundStyle(ZotTheme.gold) }
            }
            Section("On this iPhone") {
                Text("Your food diary stays on this device. Menu requests are sent to Anteater API and UCI Dining.").font(.footnote)
                Button("Clear local food history", role: .destructive) { clear = true }.frame(minHeight: 48)
            }
            Section("About ZotMacros") {
                Text("Fuel your day. Track your macros.")
                Text("An independent app for UCI students. Not affiliated with or endorsed by UC Irvine.").font(.footnote).foregroundStyle(ZotTheme.muted)
                Attribution()
            }
        }.scrollContentBackground(.hidden).zotScreen().navigationTitle("Settings")
            .onAppear { draft = model.preferences.value }
            .alert("Clear food history?", isPresented: $clear) { Button("Cancel", role: .cancel) {}; Button("Clear history", role: .destructive) { model.clearDiary() } } message: { Text("This permanently removes all food entries stored on this device.") }
    }
    private var meals: [String] { ["Automatic"] + Array(Set(["Breakfast", "Lunch", "Dinner", "Brunch", "All Day", "Afternoon Snack", "Evening Snack", draft.meal] + (model.menu.result?.snapshot?.periods.map(\.name) ?? []))).filter { $0 != "Automatic" }.sorted() }
    private func goal(_ label: String, value: Binding<Double>) -> some View {
        LabeledContent(label) { TextField(label, value: value, format: .number).keyboardType(.decimalPad).multilineTextAlignment(.trailing).frame(minHeight: 48).accessibilityLabel(label) }
    }
    private func save(_ new: UserPreferences) {
        do { let oldHall = model.preferences.value.hall; try model.preferences.save(new); draft = new; saved = true; error = nil; if oldHall != new.hall { model.menu.invalidate(hall: new.hall, day: model.day) } }
        catch { self.error = error.localizedDescription; saved = false }
    }
}
