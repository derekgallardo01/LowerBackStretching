import Foundation

/// Loads the bundled `watch_routine.json` from the WatchApp target's
/// resources. The watch app ships a hardcoded short routine so it
/// works without the iPhone.
enum WatchContent {

    static func loadRoutine() -> WatchRoutine {
        guard let url = Bundle.main.url(forResource: "watch_routine", withExtension: "json"),
              let data = try? Data(contentsOf: url),
              let routine = try? JSONDecoder().decode(WatchRoutine.self, from: data)
        else {
            assertionFailure("Missing watch_routine.json in bundle")
            return WatchRoutine(name: "(empty)", stretches: [])
        }
        return routine
    }
}
