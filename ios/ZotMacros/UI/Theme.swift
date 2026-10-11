import SwiftUI
import ZotMacrosCore
enum ZotTheme {
    static let background = Color(red: 16/255, green: 59/255, blue: 103/255)
    static let surface = Color(red: 37/255, green: 87/255, blue: 153/255)
    static let gold = Color(red: 254/255, green: 204/255, blue: 7/255)
    static let ink = Color(red: 16/255, green: 42/255, blue: 69/255)
    static let muted = Color(red: 212/255, green: 229/255, blue: 255/255)
    static func nutrient(_ n: Nutrient) -> Color { switch n { case .protein: Color(red: 101/255, green: 215/255, blue: 1); case .fat: Color(red: 56/255, green: 225/255, blue: 237/255); default: gold } }
}
struct GoldButton: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label.font(.headline).frame(maxWidth: .infinity, minHeight: 52)
            .foregroundStyle(ZotTheme.ink).background(ZotTheme.gold.opacity(configuration.isPressed ? 0.75 : 1), in: RoundedRectangle(cornerRadius: 16))
    }
}
extension View {
    func zotSurface() -> some View { padding(16).background(ZotTheme.surface, in: RoundedRectangle(cornerRadius: 20)) }
    func zotScreen() -> some View { navigationBarTitleDisplayMode(.inline).background(ZotTheme.background).toolbarBackground(ZotTheme.background, for: .navigationBar).toolbarBackground(.visible, for: .navigationBar) }
}
func amount(_ value: Double?) -> String { value.map { $0.formatted(.number.precision(.fractionLength(0...1))) } ?? "Unavailable" }
