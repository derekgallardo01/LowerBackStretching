import Foundation

struct Program: Identifiable, Codable, Hashable {
    let id: String
    let title: String
    let category: String
    let summary: String
    let days: [ProgramDay]
}

struct ProgramDay: Codable, Hashable, Identifiable {
    var id: Int { day }
    let day: Int
    let title: String
    let stretchIds: [String]
}
