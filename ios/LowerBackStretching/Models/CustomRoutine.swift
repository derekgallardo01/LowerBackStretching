import Foundation
import SwiftData

@Model
final class CustomRoutine {
    @Attribute(.unique) var id: UUID
    var name: String
    var stretchIdsCsv: String
    var createdAt: Date

    init(name: String, stretchIds: [String]) {
        self.id = UUID()
        self.name = name.trimmingCharacters(in: .whitespacesAndNewlines)
        self.stretchIdsCsv = stretchIds.joined(separator: ",")
        self.createdAt = .now
    }

    var stretchIds: [String] {
        get { stretchIdsCsv.isEmpty ? [] : stretchIdsCsv.split(separator: ",").map(String.init) }
        set { stretchIdsCsv = newValue.joined(separator: ",") }
    }
}
