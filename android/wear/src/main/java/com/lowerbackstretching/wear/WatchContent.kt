package com.lowerbackstretching.wear

import android.content.Context
import com.lowerbackstretching.wear.model.WatchRoutine
import kotlinx.serialization.json.Json

/**
 * Loads the bundled `watch_routine.json` from assets. The watch app
 * doesn't have access to the phone's `stretches.json` / `programs.json`
 * — it ships a small hardcoded routine instead so it works standalone.
 */
object WatchContent {

    private val json = Json { ignoreUnknownKeys = true }

    fun loadRoutine(context: Context): WatchRoutine {
        val text = context.assets.open("watch_routine.json")
            .bufferedReader().use { it.readText() }
        return json.decodeFromString(text)
    }
}
