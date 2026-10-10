// swift-tools-version: 5.9
import PackageDescription
import Foundation
// Command Line Tools lacks SwiftData's compiler plugin; core/state remain testable.
let coreOnly = ProcessInfo.processInfo.environment["ZOT_CORE_ONLY"] == "1"
var products: [Product] = [.library(name: "ZotMacrosCore", targets: ["ZotMacrosCore"]), .library(name: "ZotMacrosState", targets: ["ZotMacrosState"])]
var targets: [Target] = [
    .target(name: "ZotMacrosCore"),
    .target(name: "ZotMacrosState", dependencies: ["ZotMacrosCore"]),
    .testTarget(name: "ZotMacrosCoreTests", dependencies: ["ZotMacrosCore"], resources: [.copy("Fixtures")]),
    .testTarget(name: "ZotMacrosStateTests", dependencies: ["ZotMacrosState"])
]
if !coreOnly {
    products.append(.library(name: "ZotMacrosStorage", targets: ["ZotMacrosStorage"]))
    targets += [.target(name: "ZotMacrosStorage", dependencies: ["ZotMacrosCore"]), .testTarget(name: "ZotMacrosStorageTests", dependencies: ["ZotMacrosStorage"])]
}
let package = Package(name: "ZotMacrosCore", platforms: [.macOS(.v14), .iOS(.v17)], products: products, targets: targets)
