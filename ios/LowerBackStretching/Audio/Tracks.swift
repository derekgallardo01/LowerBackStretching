import Foundation

/// Catalogue of audio assets shipped with the app. Each entry maps a
/// persisted preference value to a bundle resource name and a
/// user-visible label.
///
/// `resourceName` is looked up via `Bundle.main.url(forResource:withExtension:)`
/// at play time — if the MP3 isn't in the bundle, the AudioController
/// silently falls back to no-op. See `Resources/Audio/README.md` for
/// file requirements.

enum MusicTrack: String, CaseIterable {
    case none, calm, lofi, piano

    var storageValue: String { rawValue }

    static func fromStorage(_ value: String?) -> MusicTrack {
        MusicTrack(rawValue: value ?? "") ?? .none
    }

    var resourceName: String? {
        switch self {
        case .none:  return nil
        case .calm:  return "music_calm"
        case .lofi:  return "music_lofi"
        case .piano: return "music_piano"
        }
    }

    var displayName: String {
        switch self {
        case .none:  return "None"
        case .calm:  return "Calm"
        case .lofi:  return "Lofi"
        case .piano: return "Piano"
        }
    }
}

enum AmbientTrack: String, CaseIterable {
    case none, rain, forest, ocean

    var storageValue: String { rawValue }

    static func fromStorage(_ value: String?) -> AmbientTrack {
        AmbientTrack(rawValue: value ?? "") ?? .none
    }

    var resourceName: String? {
        switch self {
        case .none:   return nil
        case .rain:   return "ambient_rain"
        case .forest: return "ambient_forest"
        case .ocean:  return "ambient_ocean"
        }
    }

    var displayName: String {
        switch self {
        case .none:   return "None"
        case .rain:   return "Rain"
        case .forest: return "Forest"
        case .ocean:  return "Ocean"
        }
    }
}

enum ChimeTrack: String, CaseIterable {
    case none, bell, ding, drop

    var storageValue: String { rawValue }

    static func fromStorage(_ value: String?) -> ChimeTrack {
        ChimeTrack(rawValue: value ?? "") ?? .none
    }

    var resourceName: String? {
        switch self {
        case .none: return nil
        case .bell: return "chime_bell"
        case .ding: return "chime_ding"
        case .drop: return "chime_drop"
        }
    }

    var displayName: String {
        switch self {
        case .none: return "None"
        case .bell: return "Bell"
        case .ding: return "Ding"
        case .drop: return "Drop"
        }
    }
}

enum AudioDefaults {
    static let musicVolume: Float = 0.4
    static let ambientVolume: Float = 0.6
}
