import Foundation
import SwiftUI

final class ContentStore: ObservableObject {
    let stretches: [Stretch]
    let programs: [Program]
    let glossary: [GlossaryEntry]
    private let stretchById: [String: Stretch]
    private let programById: [String: Program]

    init() {
        let s: [Stretch] = ContentStore.load("stretches.json") ?? []
        let p: [Program] = ContentStore.load("programs.json") ?? []
        let g: [GlossaryEntry] = ContentStore.load("glossary.json") ?? []
        self.stretches = s
        self.programs = p
        self.glossary = g
        self.stretchById = Dictionary(uniqueKeysWithValues: s.map { ($0.id, $0) })
        self.programById = Dictionary(uniqueKeysWithValues: p.map { ($0.id, $0) })
    }

    func stretch(id: String) -> Stretch? { stretchById[id] }
    func program(id: String) -> Program? { programById[id] }

    func stretches(for program: Program, day: Int) -> [Stretch] {
        guard let d = program.days.first(where: { $0.day == day }) else { return [] }
        return d.stretchIds.compactMap { stretchById[$0] }
    }

    func totalDurationSeconds(stretchIds: [String]) -> Int {
        stretchIds.compactMap { stretchById[$0]?.durationSeconds }.reduce(0, +)
    }

    private static func load<T: Decodable>(_ filename: String) -> T? {
        guard let url = Bundle.main.url(forResource: filename, withExtension: nil) else {
            assertionFailure("Missing \(filename) in bundle")
            return nil
        }
        do {
            let data = try Data(contentsOf: url)
            return try JSONDecoder().decode(T.self, from: data)
        } catch {
            assertionFailure("Failed to decode \(filename): \(error)")
            return nil
        }
    }
}
