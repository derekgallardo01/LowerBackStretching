package com.lowerbackstretching.core

/**
 * Pure-enum mirrors of the on-disk values stored in
 * [com.lowerbackstretching.data.Prefs]. Kept in :core so platform-
 * agnostic code (Display.kt's `formatDuration`, eventually the watch
 * UI) can reference them without pulling DataStore into the
 * dependency graph.
 */
enum class ThemeMode(val storageValue: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromStorage(value: String?): ThemeMode =
            entries.firstOrNull { it.storageValue == value } ?: SYSTEM
    }
}

enum class DurationUnit(val storageValue: String) {
    SECONDS("seconds"),
    MINUTES_SHORT("minutes_short");

    companion object {
        fun fromStorage(value: String?): DurationUnit =
            entries.firstOrNull { it.storageValue == value } ?: SECONDS
    }
}
