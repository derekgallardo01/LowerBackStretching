package com.lowerbackstretching.wear

import com.google.common.truth.Truth.assertThat
import com.lowerbackstretching.core.player.PlayerEngine
import com.lowerbackstretching.wear.model.WatchRoutine
import com.lowerbackstretching.wear.model.WatchStretch
import kotlinx.serialization.json.Json
import org.junit.Test

/**
 * `:wear` previously had no test sources at all, so CI's
 * `:wear:testDebugUnitTest` step was permanently, vacuously green.
 *
 * These cover the two things that can actually break the watch app without
 * anyone noticing: the bundled routine JSON failing to deserialize into
 * [WatchRoutine], and [WatchStretch] not satisfying the shared
 * [PlayerEngine] contract. Loading from `assets` needs a Context, so the JSON is
 * read from the source tree instead — same bytes that get packaged.
 */
class WatchRoutineTest {
    private val json = Json { ignoreUnknownKeys = true }

    private val routine: WatchRoutine by lazy {
        val file = java.io.File("src/main/assets/watch_routine.json")
        assertThat(file.exists()).isTrue()
        json.decodeFromString<WatchRoutine>(file.readText())
    }

    @Test
    fun bundled_routine_deserializes() {
        assertThat(routine.name).isNotEmpty()
        assertThat(routine.stretches).isNotEmpty()
    }

    @Test
    fun every_stretch_has_an_id_name_and_positive_duration() {
        for (s in routine.stretches) {
            assertThat(s.id).isNotEmpty()
            assertThat(s.name).isNotEmpty()
            assertThat(s.durationSeconds).isGreaterThan(0)
        }
    }

    @Test
    fun stretch_ids_are_unique() {
        val ids = routine.stretches.map { it.id }
        assertThat(ids).containsNoDuplicates()
    }

    @Test
    fun the_shared_engine_can_run_the_bundled_routine_to_completion() {
        val engine = PlayerEngine(routine.stretches)
        val total = routine.stretches.sumOf { it.durationSeconds }

        // The engine starts running for a non-empty routine — no togglePlay needed.
        repeat(total) { engine.tick() }

        assertThat(engine.state.value.finished).isTrue()
    }

    @Test
    fun engine_progress_advances_through_each_stretch() {
        val engine = PlayerEngine(routine.stretches)

        assertThat(engine.state.value.index).isEqualTo(0)
        repeat(routine.stretches.first().durationSeconds) { engine.tick() }
        // After the first stretch's full duration we should be on the second.
        assertThat(engine.state.value.index).isEqualTo(1)
    }

    @Test
    fun unknown_json_fields_are_ignored() {
        // WatchContent decodes with ignoreUnknownKeys, so a phone-side field
        // leaking into the watch JSON must not crash the watch app.
        val withExtra = """
            {"name":"R","stretches":[
              {"id":"a","name":"A","durationSeconds":10,"youtubeId":"xyz"}
            ]}
        """.trimIndent()
        val parsed = json.decodeFromString<WatchRoutine>(withExtra)
        assertThat(parsed.stretches).hasSize(1)
        assertThat(parsed.stretches.first().durationSeconds).isEqualTo(10)
    }
}
