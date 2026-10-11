import SwiftUI
import ZotMacrosCore
struct MenuView: View {
    @Bindable var model: AppModel
    @State private var search = ""
    @State private var selectedPeriod = ""
    @State private var selectedStation = ""
    private var snapshot: MenuSnapshot? { model.menu.result?.snapshot }
    private var period: MenuPeriod? { snapshot?.periods.first { $0.id == selectedPeriod } ?? snapshot?.periods.first }
    private var foods: [MenuFood] {
        (period?.foods ?? []).filter { (selectedStation.isEmpty || $0.stationId == selectedStation) && (search.isEmpty || $0.dish.name.localizedCaseInsensitiveContains(search)) }
    }
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                Text("Today · \(model.day)").foregroundStyle(ZotTheme.muted)
                Picker("Dining hall", selection: Binding(get: { model.preferences.value.hall }, set: { model.selectHall($0) })) {
                    Text("Anteatery").tag("anteatery"); Text("Brandywine").tag("brandywine")
                }.pickerStyle(.segmented).frame(minHeight: 48)
                if model.menu.loading { ProgressView("Refreshing menu…").frame(maxWidth: .infinity) }
                if let warning = model.menu.result?.warning {
                    VStack(alignment: .leading, spacing: 8) { Label(warning, systemImage: "exclamationmark.circle"); Button("Retry") { Task { await model.refreshMenu(force: true) } }.frame(minHeight: 48) }.font(.subheadline).zotSurface()
                }
                if let snapshot {
                    if snapshot.day != model.day { Label("Saved menu from \(snapshot.day) — today’s menu is unavailable.", systemImage: "clock").foregroundStyle(ZotTheme.gold) }
                    if !snapshot.supplementVerified { Text("Menu may be incomplete.").foregroundStyle(ZotTheme.gold) }
                    Text("\(model.menu.result?.cached == true ? "Saved" : "Updated") \(snapshot.fetchedAt.formatted(date: .abbreviated, time: .shortened))").font(.caption).foregroundStyle(ZotTheme.muted)
                    if snapshot.unavailableDishes > 0 { Text("Some Anteater food details were unavailable.").font(.caption).foregroundStyle(ZotTheme.muted) }
                    Picker("Meal period", selection: $selectedPeriod) { ForEach(snapshot.periods) { Text($0.name).tag($0.id) } }.frame(minHeight: 48)
                    HStack(spacing: 12) {
                        Image(systemName: "magnifyingglass").foregroundStyle(ZotTheme.muted).accessibilityHidden(true)
                        TextField("Search this menu", text: $search, prompt: Text("Search this menu").foregroundStyle(ZotTheme.muted))
                            .textFieldStyle(.plain).foregroundStyle(.white).tint(ZotTheme.gold)
                            .accessibilityLabel("Search this menu")
                    }
                    .padding(.horizontal, 16).frame(minHeight: 52)
                    .background(ZotTheme.surface, in: RoundedRectangle(cornerRadius: 16))
                    Picker("Station", selection: $selectedStation) {
                        Text("All stations").tag("")
                        ForEach(stations, id: \.0) { id, name in Text(name).tag(id) }
                    }.frame(minHeight: 48)
                    if foods.isEmpty { ContentUnavailableView("No foods found", systemImage: "fork.knife", description: Text(search.isEmpty ? "No foods are published for this selection." : "Try another search or station.")) }
                    ForEach(stations.filter { station in foods.contains { $0.stationId == station.0 } }, id: \.0) { station in
                        VStack(alignment: .leading, spacing: 12) {
                            Text(station.1).font(.title2.bold())
                            ForEach(foods.filter { $0.stationId == station.0 }) { food in
                                Button { if let period { model.open(food, period: period) } } label: {
                                    HStack(spacing: 16) {
                                        VStack(alignment: .leading, spacing: 5) {
                                            Text(food.dish.name).font(.headline).foregroundStyle(.white)
                                            Text(food.dish.serving).font(.caption).foregroundStyle(ZotTheme.muted)
                                            if Nutrient.allCases.allSatisfy({ food.dish.nutrition[$0] == nil }) { Text("Nutrition unavailable.").font(.subheadline).foregroundStyle(ZotTheme.muted) }
                                            else { Text("\(amount(food.dish.nutrition[.calories])) kcal · \(amount(food.dish.nutrition[.protein])) g protein").font(.subheadline).foregroundStyle(ZotTheme.muted) }
                                        }
                                        Spacer(minLength: 0)
                                        Image(systemName: alreadyLogged(food) ? "checkmark.circle" : "plus.circle").font(.title2).foregroundStyle(ZotTheme.gold)
                                    }.frame(minHeight: 60).contentShape(Rectangle())
                                }.buttonStyle(.plain).accessibilityHint("Choose servings and log food")
                                Divider()
                            }
                        }
                    }
                } else if !model.menu.loading, model.menu.result?.warning == nil {
                    ContentUnavailableView("Load the dining menu", systemImage: "fork.knife", description: Text("Menus come directly from Anteater API and UCI Dining."))
                    Button("Load menu") { Task { await model.refreshMenu(force: true) } }.buttonStyle(GoldButton())
                }
                Attribution()
            }.padding(20).frame(maxWidth: 720)
        }.navigationTitle("Dining menu").zotScreen().refreshable { await model.refreshMenu(force: true) }
            .task(id: model.preferences.value.hall + model.day) { if model.preferences.value.autoRefresh { await model.refreshMenu() } }
            .onChange(of: snapshot, initial: true) { _, value in
                let c = CampusClock.calendar.dateComponents([.hour, .minute], from: Date())
                selectedPeriod = choosePeriod(value?.periods ?? [], preference: model.preferences.value.meal, time: String(format: "%02d:%02d", c.hour!, c.minute!)) ?? ""
                selectedStation = ""
            }.onChange(of: selectedPeriod) { _, _ in selectedStation = "" }
    }
    private var stations: [(String, String)] {
        var names: [String: String] = [:]; for food in period?.foods ?? [] { names[food.stationId] = food.station }; return names.sorted { $0.value < $1.value }
    }
    private func alreadyLogged(_ food: MenuFood) -> Bool { model.entries(on: model.day).contains { $0.dish.id == food.dish.id && $0.hall == model.preferences.value.hall && $0.station == food.station } }
}
