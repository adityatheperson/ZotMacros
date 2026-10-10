import SwiftUI
import Charts
import ZotMacrosCore
struct HistoryView: View {
    @Bindable var model: AppModel
    @State private var date = Date()
    private var day: String { CampusClock.day(at: date) }
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                DatePicker("Diary date", selection: $date, in: ...Date(), displayedComponents: .date).datePickerStyle(.compact).environment(\.timeZone, CampusClock.calendar.timeZone).frame(minHeight: 48)
                NutritionSummary(entries: model.entries(on: day), preferences: model.preferences.value)
                DiaryList(entries: model.entries(on: day), edit: model.edit, delete: model.delete)
                Text("Last seven days").font(.title2.bold())
                ForEach([Nutrient.calories, .protein], id: \.self) { nutrient in
                    VStack(alignment: .leading, spacing: 12) {
                        Text(nutrient.label).font(.headline)
                        Chart(days, id: \.self) { day in
                            let entries = model.entries(on: day), total = aggregate(entries.map(\.totals))[nutrient]!
                            if !entries.isEmpty, let value = total.value { BarMark(x: .value("Day", String(day.suffix(5))), y: .value(nutrient.unit, value)).foregroundStyle(ZotTheme.nutrient(nutrient)).opacity(total.isPartial ? 0.5 : 1) }
                        }.frame(height: 120).accessibilityHidden(true)
                        ForEach(days, id: \.self) { day in
                            let entries = model.entries(on: day), total = aggregate(entries.map(\.totals))[nutrient]!
                            HStack { Text(day); Spacer(); Text(entries.isEmpty ? "No entries" : "\(amount(total.value)) \(nutrient.unit)\(total.isPartial ? " · incomplete" : "")") }.font(.caption)
                        }
                    }.zotSurface()
                }
            }.padding(20).frame(maxWidth: 720)
        }.navigationTitle("History").zotScreen()
    }
    private var days: [String] { (-6...0).map { CampusClock.day(at: CampusClock.calendar.date(byAdding: .day, value: $0, to: date)!) } }
}
