package com.lowerbackstretching.core

import com.google.common.truth.Truth.assertThat
import com.lowerbackstretching.core.model.Stretch
import com.lowerbackstretching.core.model.WatchRoutine
import com.lowerbackstretching.core.model.WatchStretch
import com.lowerbackstretching.core.model.toWatchStretch
import org.junit.Test

class WatchModelsTest {

    @Test
    fun toWatchStretch_extractsMinimalTimedFields() {
        val fullStretch = Stretch(
            id = "cat-cow",
            name = "Cat-Cow",
            bodyParts = listOf("back", "core"),
            durationSeconds = 45,
            difficulty = "easy",
            description = "Gentle flow",
            youtubeId = "vid123",
            whyThisStretch = "Warms up spine",
        )

        val watchStretch = fullStretch.toWatchStretch()
        assertThat(watchStretch.id).isEqualTo("cat-cow")
        assertThat(watchStretch.name).isEqualTo("Cat-Cow")
        assertThat(watchStretch.durationSeconds).isEqualTo(45)
    }

    @Test
    fun watchRoutine_calculatesTotalDuration() {
        val routine = WatchRoutine(
            id = "1",
            name = "Morning Flow",
            stretches = listOf(
                WatchStretch("s1", "Stretch 1", 30),
                WatchStretch("s2", "Stretch 2", 45),
                WatchStretch("s3", "Stretch 3", 60),
            ),
        )

        assertThat(routine.totalDurationSeconds).isEqualTo(135)
    }

    @Test
    fun watchRoutine_jsonSerializationRoundTrip() {
        val routines = listOf(
            WatchRoutine(
                id = "101",
                name = "Quick Back Relief",
                stretches = listOf(
                    WatchStretch("child-pose", "Child's Pose", 30),
                    WatchStretch("cat-cow", "Cat-Cow", 30),
                ),
            ),
            WatchRoutine(
                id = "102",
                name = "Leg Stretches",
                stretches = listOf(
                    WatchStretch("hamstring", "Hamstring Stretch", 45),
                ),
            ),
        )

        val json = WatchRoutine.encodeList(routines)
        assertThat(json).isNotEmpty()

        val decoded = WatchRoutine.decodeList(json)
        assertThat(decoded).isEqualTo(routines)
    }

    @Test
    fun watchRoutine_decodeMalformedJsonReturnsEmptyList() {
        val decoded = WatchRoutine.decodeList("{ malformed json }")
        assertThat(decoded).isEmpty()
    }
}
