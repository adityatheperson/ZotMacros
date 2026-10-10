import SwiftUI
import UIKit
import ZotMacrosCore
import ZotMacrosState
struct FoodSheet: View {
    @Bindable var editor: FoodEditor
    let haptics: Bool
    @Environment(\.dismiss) private var dismiss
    @FocusState private var quantityFocused: Bool
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    Text(editor.dish.name).font(.title.bold())
                    Text("\(hallName(editor.hall)) · \(editor.station) · \(editor.period)").font(.subheadline).foregroundStyle(ZotTheme.muted)
                    Text(editor.dish.serving).foregroundStyle(ZotTheme.muted)
                    if Nutrient.allCases.allSatisfy({ editor.dish.nutrition[$0] == nil }) {
                        Label("Nutrition unavailable. You can log this food; daily totals will be incomplete.", systemImage: "info.circle").foregroundStyle(ZotTheme.muted)
                    }
                    Text("\(amount(editor.totals[.calories])) kcal").font(.largeTitle.bold()).monospacedDigit()
                    Text("Serving quantity").font(.headline)
                    ViewThatFits(in: .horizontal) {
                        presets
                        ScrollView(.horizontal) { presets }
                    }
                    TextField("Servings", text: $editor.quantity).keyboardType(.decimalPad).textFieldStyle(.roundedBorder).focused($quantityFocused).frame(minHeight: 48).accessibilityIdentifier("servingQuantity")
                    if editor.parsedQuantity == nil { Text("Enter a serving quantity greater than 0 and at most 1,000.").font(.footnote).foregroundStyle(ZotTheme.gold) }
                    Picker("Log to", selection: $editor.meal) { ForEach(Meal.allCases, id: \.self) { Text($0.rawValue).tag($0) } }.frame(minHeight: 48)
                    Text("Diary date: \(editor.day)").font(.caption).foregroundStyle(ZotTheme.muted)
                    VStack(spacing: 12) {
                        ForEach(Nutrient.allCases, id: \.self) { nutrient in
                            HStack(alignment: .firstTextBaseline) { Text(nutrient.label); Spacer(); Text(editor.totals[nutrient].map { "\(amount($0)) \(nutrient.unit)" } ?? "Unavailable").multilineTextAlignment(.trailing) }.font(.subheadline)
                            if nutrient != Nutrient.allCases.last { Divider() }
                        }
                    }.zotSurface()
                    if !editor.dish.description.isEmpty { Text(editor.dish.description).font(.body) }
                    if let ingredients = editor.dish.ingredients, !ingredients.isEmpty { Text("Ingredients").font(.headline); Text(ingredients) }
                    Text("Allergens").font(.headline)
                    Text(editor.dish.allergensReported ? (editor.dish.allergens.isEmpty ? "None reported by the source." : editor.dish.allergens.joined(separator: ", ")) : "Allergen information unavailable.").foregroundStyle(ZotTheme.muted)
                    Attribution()
                }.padding(20)
            }.zotScreen()
                .safeAreaInset(edge: .bottom) {
                    VStack(spacing: 8) {
                        if let error = editor.error { Text(error).font(.footnote).foregroundStyle(ZotTheme.gold).accessibilityIdentifier("saveError") }
                        Button {
                            quantityFocused = false
                            Task { if await editor.save() { if haptics { UINotificationFeedbackGenerator().notificationOccurred(.success) }; dismiss() } }
                        } label: {
                            if editor.saving { ProgressView("Saving…").tint(ZotTheme.ink) }
                            else { Text(editor.editing ? "Save changes" : "Add to diary").accessibilityIdentifier("saveFood") }
                        }.buttonStyle(GoldButton()).disabled(editor.saving || editor.parsedQuantity == nil)
                    }.padding(16).background(ZotTheme.background)
                }
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) { Button("Close") { dismiss() }.disabled(editor.saving) }
                    ToolbarItemGroup(placement: .keyboard) { Spacer(); Button("Done") { quantityFocused = false } }
                }
        }.presentationDragIndicator(.visible).presentationDetents([.large]).interactiveDismissDisabled(editor.saving)
    }
    private var presets: some View {
        HStack { ForEach([0.5, 1, 1.5, 2], id: \.self) { value in
            Button("\(amount(value))×") { editor.quantity = String(value) }.buttonStyle(.bordered).frame(minWidth: 48, minHeight: 48)
        } }
    }
}
