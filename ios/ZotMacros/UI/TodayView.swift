import SwiftUI
struct TodayView: View {
    @Bindable var model: AppModel
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                NutritionSummary(entries: model.entries(on: model.day), preferences: model.preferences.value)
                HStack { Text("Today’s food").font(.title2.bold()); Spacer(); Button("History") { model.selectedTab = 2 }.frame(minHeight: 48) }
                DiaryList(entries: model.entries(on: model.day), edit: model.edit, delete: model.delete)
                if let confirmation = model.confirmation { Label(confirmation, systemImage: "checkmark.circle").font(.subheadline).foregroundStyle(ZotTheme.muted) }
                Button { model.selectedTab = 1 } label: { Label("Add food", systemImage: "plus") }.buttonStyle(GoldButton())
            }.padding(20).frame(maxWidth: 720)
        }.zotScreen().toolbar(.hidden, for: .navigationBar)
            .safeAreaInset(edge: .top, spacing: 0) { ScreenHeader(title: "Let’s fuel up.") { HeaderDate(day: model.day) } }
    }
}
