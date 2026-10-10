import SwiftUI
import ZotMacrosCore
import ZotMacrosStorage
struct NutritionSummary: View {
    let entries: [DiaryEntry]
    let preferences: UserPreferences
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @Environment(\.dynamicTypeSize) private var typeSize
    private var totals: [Nutrient: NutrientTotal] { aggregate(entries.map(\.totals)) }
    var body: some View {
        VStack(spacing: 20) {
            let calories = totals[.calories]!
            ZStack {
                Circle().stroke(ZotTheme.surface, lineWidth: 13)
                Circle().trim(from: 0, to: min(1, max(0, (calories.value ?? 0) / preferences.calories)))
                    .stroke(ZotTheme.gold, style: StrokeStyle(lineWidth: 13, lineCap: .round)).rotationEffect(.degrees(-90))
                    .animation(reduceMotion ? nil : .easeOut(duration: 0.28), value: calories.value)
                VStack(spacing: 5) {
                    Text(calories.value == nil ? "Unavailable" : amount(calories.value)).font(.largeTitle.bold()).monospacedDigit()
                    Text("of \(amount(preferences.calories)) kcal").font(.subheadline)
                    if calories.isPartial { Text("Incomplete total").foregroundStyle(ZotTheme.gold).font(.subheadline) }
                    else if let value = calories.value {
                        Text(value > preferences.calories ? "\(amount(value - preferences.calories)) over goal" : "\(amount(preferences.calories - value)) remaining").font(.subheadline).foregroundStyle(ZotTheme.muted)
                    }
                }.padding(24).multilineTextAlignment(.center)
            }.frame(maxWidth: 300).aspectRatio(1, contentMode: .fit)
                .accessibilityElement(children: .combine)
            if preferences.exampleGoals { Text("Example goals · Personalize in Settings").font(.footnote).foregroundStyle(ZotTheme.muted) }
            let columns = typeSize.isAccessibilitySize ? 1 : 3
            LazyVGrid(columns: Array(repeating: GridItem(.flexible(), alignment: .leading), count: columns), spacing: 8) {
                ForEach([Nutrient.protein, .carbs, .fat], id: \.self) { nutrient in
                    let total = totals[nutrient]!, goal = preferences.goal(nutrient)
                    VStack(alignment: .leading, spacing: 8) {
                        Text(nutrient.label).font(.subheadline).foregroundStyle(ZotTheme.nutrient(nutrient))
                        Text(amount(total.value)).font(.title2.bold()).monospacedDigit()
                        Text("of \(amount(goal)) \(nutrient.unit)").font(.caption).foregroundStyle(ZotTheme.muted)
                        ProgressView(value: min(1, max(0, (total.value ?? 0) / goal))).tint(ZotTheme.nutrient(nutrient))
                            .animation(reduceMotion ? nil : .easeOut(duration: 0.28), value: total.value)
                        if total.isPartial { Text("Incomplete").font(.caption).foregroundStyle(ZotTheme.gold) }
                    }.frame(maxWidth: .infinity, alignment: .leading).zotSurface()
                }
            }
            if totals.values.contains(where: \.isPartial) {
                Label("Some logged foods have unavailable nutrition. Totals include only reported values.", systemImage: "info.circle")
                    .font(.footnote).foregroundStyle(ZotTheme.muted).frame(maxWidth: .infinity, alignment: .leading)
            }
        }
    }
}
struct DiaryList: View {
    let entries: [DiaryEntry]
    let edit: (DiaryEntry) -> Void
    let delete: (DiaryEntry) -> Void
    @State private var pendingDelete: DiaryEntry?
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            if entries.isEmpty { ContentUnavailableView("No foods logged", systemImage: "fork.knife", description: Text("Choose a food from the dining menu to get started.")) }
            ForEach(Meal.allCases, id: \.self) { meal in
                let group = entries.filter { $0.meal == meal }
                if !group.isEmpty {
                    VStack(alignment: .leading, spacing: 12) {
                        Text(meal.rawValue).font(.headline)
                        ForEach(group) { entry in
                            HStack {
                                Button { edit(entry) } label: {
                                    VStack(alignment: .leading, spacing: 5) {
                                        Text(entry.dish.name).font(.headline).foregroundStyle(.white)
                                        Text("\(amount(entry.quantity)) servings · \(amount(entry.totals[.calories])) kcal").font(.subheadline).foregroundStyle(ZotTheme.muted)
                                    }.frame(maxWidth: .infinity, minHeight: 48, alignment: .leading)
                                }.buttonStyle(.plain).accessibilityHint("Edit servings or meal")
                                Menu {
                                    Button("Edit", systemImage: "pencil") { edit(entry) }
                                    Button("Delete", systemImage: "trash", role: .destructive) { pendingDelete = entry }
                                } label: { Image(systemName: "ellipsis").frame(width: 48, height: 48) }.accessibilityLabel("Actions for \(entry.dish.name)")
                            }
                            if entry.id != group.last?.id { Divider() }
                        }
                    }.zotSurface()
                }
            }
        }.alert("Delete food?", isPresented: Binding(get: { pendingDelete != nil }, set: { if !$0 { pendingDelete = nil } })) {
            Button("Cancel", role: .cancel) { pendingDelete = nil }
            Button("Delete", role: .destructive) { if let entry = pendingDelete { delete(entry) }; pendingDelete = nil }
        } message: { Text("Remove \(pendingDelete?.dish.name ?? "this food") from your diary?") }
    }
}
struct Attribution: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Link("Data from Anteater API", destination: URL(string: "https://icssc.link/about-anteaterapi")!).frame(minHeight: 48)
            Link("Menu supplemented from UCI Dining", destination: URL(string: "https://uci.mydininghub.com/")!).frame(minHeight: 48)
        }.font(.footnote)
    }
}
