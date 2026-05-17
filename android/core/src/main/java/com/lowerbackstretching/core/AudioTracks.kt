package com.lowerbackstretching.core

/**
 * Catalogue of audio assets the app can play. Pure data — the Android
 * `AudioController` in `:app/audio/` is the consumer that looks up
 * `resName` via `resources.getIdentifier(...)` at play time, falling
 * back to no-op when the matching file is missing from `res/raw/`. See
 * `android/app/AUDIO_FILES.md` for the bundling contract.
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
