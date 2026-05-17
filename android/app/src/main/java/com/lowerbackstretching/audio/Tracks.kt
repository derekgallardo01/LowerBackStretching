package com.lowerbackstretching.audio

/**
 * Catalogue of audio assets shipped with the app. Each entry maps a
 * persisted preference value to a `res/raw/` resource name and a
 * user-visible label.
 *
 * `resName` is looked up via `resources.getIdentifier(...)` at play
 * time — if the file is missing from `res/raw/`, the AudioController
 * silently falls back to no-op (the build still succeeds; the feature
 * just plays nothing). See `android/app/AUDIO_FILES.md` for file requirements.
 */

enum class MusicTrack(
    val storageValue: String,
    val resName: String?,
    val displayName: String,
) {
    NONE("none", null, "None"),
    CALM("calm", "music_calm", "Calm"),
    LOFI("lofi", "music_lofi", "Lofi"),
    PIANO("piano", "music_piano", "Piano");

    companion object {
        fun fromStorage(value: String?): MusicTrack =
            entries.firstOrNull { it.storageValue == value } ?: NONE
    }
}

enum class AmbientTrack(
    val storageValue: String,
    val resName: String?,
    val displayName: String,
) {
    NONE("none", null, "None"),
    RAIN("rain", "ambient_rain", "Rain"),
    FOREST("forest", "ambient_forest", "Forest"),
    OCEAN("ocean", "ambient_ocean", "Ocean");

    companion object {
        fun fromStorage(value: String?): AmbientTrack =
            entries.firstOrNull { it.storageValue == value } ?: NONE
    }
}

enum class ChimeTrack(
    val storageValue: String,
    val resName: String?,
    val displayName: String,
) {
    NONE("none", null, "None"),
    BELL("bell", "chime_bell", "Bell"),
    DING("ding", "chime_ding", "Ding"),
    DROP("drop", "chime_drop", "Drop");

    companion object {
        fun fromStorage(value: String?): ChimeTrack =
            entries.firstOrNull { it.storageValue == value } ?: NONE
    }
}

object AudioDefaults {
    const val MUSIC_VOLUME = 0.4f
    const val AMBIENT_VOLUME = 0.6f
}
