import Foundation

struct Stretch: Identifiable, Codable, Hashable {
    let id: String
    let name: String
    let bodyParts: [String]
    let durationSeconds: Int
    let difficulty: String
    let description: String
    let youtubeId: String
}
