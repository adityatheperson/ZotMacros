import Foundation
import SwiftData
import ZotMacrosCore
public enum SchemaV1: VersionedSchema {
    public static var versionIdentifier = Schema.Version(1, 0, 0)
    public static var models: [any PersistentModel.Type] { [DiaryRecord.self, MenuRecord.self] }
    @Model public final class DiaryRecord {
        @Attribute(.unique) public var id: UUID
        public var day: String
        public var payload: Data
        public init(_ entry: DiaryEntry) throws { id = entry.id; day = entry.day; payload = try JSONEncoder().encode(entry) }
    }
    @Model public final class MenuRecord {
        @Attribute(.unique) public var key: String
        public var hall: String
        public var day: String
        public var payload: Data
        public init(_ snapshot: MenuSnapshot) throws { key = snapshot.hall + ":" + snapshot.day; hall = snapshot.hall; day = snapshot.day; payload = try JSONEncoder().encode(snapshot) }
    }
}
public enum StoreMigrationPlan: SchemaMigrationPlan {
    public static var schemas: [any VersionedSchema.Type] { [SchemaV1.self] }
    public static var stages: [MigrationStage] { [] }
}
public enum Store {
    public static func make(url: URL? = nil, inMemory: Bool = false) throws -> ModelContainer {
        let schema = Schema(versionedSchema: SchemaV1.self)
        let config: ModelConfiguration
        if let url { config = ModelConfiguration(schema: schema, url: url, cloudKitDatabase: .none) }
        else { config = ModelConfiguration(schema: schema, isStoredInMemoryOnly: inMemory, cloudKitDatabase: .none) }
        return try ModelContainer(for: schema, migrationPlan: StoreMigrationPlan.self, configurations: [config])
    }
}
