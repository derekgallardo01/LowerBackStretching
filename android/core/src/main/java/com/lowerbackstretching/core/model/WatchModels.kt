package com.lowerbackstretching.core.model

import com.lowerbackstretching.core.player.Timed
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Slim stretch model tailored for watch displays and offline synchronization.
 * Contains only the identifiers and timing needed to drive the player state machine.
 */
@Serializable
data class WatchStretch(
    val id: String,
    val name: String,
    override val durationSeconds: Int,
) : Timed

/**
 * A routine packaged for watch synchronization and execution.
 */
@Serializable
data class WatchRoutine(
    val name: String,
    val stretches: List<WatchStretch>,
    val id: String = "",
) {
    val totalDurationSeconds: Int
        get() = stretches.sumOf { it.durationSeconds }

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        const val DATA_LAYER_PATH = "/synced_routines"
        const val KEY_ROUTINES_JSON = "routines_json"
        const val KEY_TIMESTAMP = "timestamp"

        fun encodeList(routines: List<WatchRoutine>): String = json.encodeToString(routines)

        fun decodeList(jsonString: String): List<WatchRoutine> =
            runCatching { json.decodeFromString<List<WatchRoutine>>(jsonString) }.getOrElse { emptyList() }
    }
}

/** Converts a full [Stretch] domain model into a slim [WatchStretch]. */
fun Stretch.toWatchStretch(): WatchStretch =
    WatchStretch(
        id = id,
        name = name,
        durationSeconds = durationSeconds,
    )
