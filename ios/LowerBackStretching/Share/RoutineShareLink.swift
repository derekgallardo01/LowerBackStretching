import Foundation

/// A custom-scheme deep link that round-trips a custom routine across
/// devices: the recipient taps the link or scans the QR code, the OS
/// routes back to the app, and the importer creates a copy of the
/// routine locally.
///
/// Format: `lowerbackstretching://routine?name=<urlenc>&ids=<csv>`
///
/// The link is self-contained — no server is involved — so it works
/// offline and survives the original sender deleting their copy.

let routineLinkScheme = "lowerbackstretching"
let routineLinkHost = "routine"

struct SharedRoutine: Equatable {
    let name: String
    let stretchIds: [String]
}

func buildRoutineLink(name: String, stretchIds: [String]) -> String {
    var components = URLComponents()
    components.scheme = routineLinkScheme
    components.host = routineLinkHost
    components.queryItems = [
        URLQueryItem(name: "name", value: name),
        URLQueryItem(name: "ids", value: stretchIds.joined(separator: ",")),
    ]
    return components.url?.absoluteString ?? ""
}

/// Parse `link` back into a `SharedRoutine`. Returns nil if the link
/// isn't ours, is missing required params, or carries an empty id list.
func parseRoutineLink(_ link: String) -> SharedRoutine? {
    guard let components = URLComponents(string: link) else { return nil }
    guard components.scheme == routineLinkScheme, components.host == routineLinkHost else {
        return nil
    }
    let queryItems = Dictionary(
        uniqueKeysWithValues: (components.queryItems ?? []).map { ($0.name, $0.value ?? "") }
    )
    guard let rawName = queryItems["name"], let rawIds = queryItems["ids"] else { return nil }
    let trimmedName = rawName.trimmingCharacters(in: .whitespacesAndNewlines)
    let ids = rawIds.split(separator: ",").map { $0.trimmingCharacters(in: .whitespaces) }.filter { !$0.isEmpty }
    guard !trimmedName.isEmpty, !ids.isEmpty else { return nil }
    return SharedRoutine(name: trimmedName, stretchIds: ids)
}
