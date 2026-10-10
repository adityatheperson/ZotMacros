import SwiftUI
import ZotMacrosStorage
struct OnboardingView: View {
    let preferences: PreferencesStore
    @State private var draft = UserPreferences()
    @State private var error: String?
    var body: some View {
        NavigationStack {
            Form {
                Section {
                    Text("ZotMacros").font(.largeTitle.bold()).foregroundStyle(ZotTheme.gold)
                    Text("Fuel your day. Track your macros.").font(.title3)
                    Text("Browse real dining menus, log your servings, and keep your diary on your iPhone.")
                }
                Section("Choose your dining hall") {
                    Picker("Dining hall", selection: $draft.hall) { Text("Anteatery").tag("anteatery"); Text("Brandywine").tag("brandywine") }.pickerStyle(.segmented)
                }
                Section("Example daily goals") {
                    Text("These are examples, not personalized recommendations. You can change them in Settings.").font(.footnote)
                    goal("Calories (kcal)", value: $draft.calories); goal("Protein (g)", value: $draft.protein)
                    goal("Carbs (g)", value: $draft.carbs); goal("Fats (g)", value: $draft.fat)
                    Button("Accept goals and get started") { finish(accept: true) }.frame(minHeight: 48)
                }
                if let error { Text(error).foregroundStyle(ZotTheme.gold) }
            }.scrollContentBackground(.hidden).zotScreen().navigationTitle("Welcome")
                .toolbar { ToolbarItem(placement: .cancellationAction) { Button("Skip for now") { finish(accept: false) } } }
        }.interactiveDismissDisabled().onAppear { draft = preferences.value }
    }
    private func goal(_ label: String, value: Binding<Double>) -> some View { LabeledContent(label) { TextField(label, value: value, format: .number).keyboardType(.decimalPad).multilineTextAlignment(.trailing).frame(minHeight: 48).accessibilityLabel(label) } }
    private func finish(accept: Bool) { var p = accept ? draft : preferences.value; p.onboarded = true; p.exampleGoals = !accept; do { try preferences.save(p) } catch { self.error = error.localizedDescription } }
}
