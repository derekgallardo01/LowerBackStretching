package com.lowerbackstretching.core

import com.google.common.truth.Truth.assertThat
import com.lowerbackstretching.core.BodyParts
import com.lowerbackstretching.core.model.Program
import com.lowerbackstretching.core.model.ProgramDay
import com.lowerbackstretching.core.model.Stretch
import org.junit.Test

class DisplayTest {

    private fun stretch(
        id: String = "s",
        difficulty: String = "easy",
        seconds: Int = 30,
        bodyParts: List<String> = listOf("lower-back"),
    ) = Stretch(
        id = id,
        name = id,
        bodyParts = bodyParts,
        durationSeconds = seconds,
        difficulty = difficulty,
        description = "",
        youtubeId = "x",
    )

    @Test
    fun difficultyDisplay_capitalizes_first_letter() {
        assertThat(stretch(difficulty = "easy").difficultyDisplay).isEqualTo("Easy")
        assertThat(stretch(difficulty = "medium").difficultyDisplay).isEqualTo("Medium")
        assertThat(stretch(difficulty = "hard").difficultyDisplay).isEqualTo("Hard")
    }

    @Test
    fun shortSubtitle_default_seconds() {
        val s = stretch(seconds = 45, difficulty = "easy", bodyParts = listOf("lower-back", "spine"))
        assertThat(s.shortSubtitle()).isEqualTo("45s · Easy · lower back · spine")
    }

    @Test
    fun shortSubtitle_with_minutes_short() {
        val s = stretch(seconds = 90, difficulty = "medium", bodyParts = listOf("hips"))
        assertThat(s.shortSubtitle(DurationUnit.MINUTES_SHORT))
            .isEqualTo("1:30 · Medium · hips")
    }

    @Test
    fun formatDuration_seconds_appends_s() {
        assertThat(formatDuration(45, DurationUnit.SECONDS)).isEqualTo("45s")
        assertThat(formatDuration(60, DurationUnit.SECONDS)).isEqualTo("60s")
        assertThat(formatDuration(0, DurationUnit.SECONDS)).isEqualTo("0s")
    }

    @Test
    fun formatDuration_minutes_short_pads_seconds() {
        assertThat(formatDuration(0, DurationUnit.MINUTES_SHORT)).isEqualTo("0:00")
        assertThat(formatDuration(30, DurationUnit.MINUTES_SHORT)).isEqualTo("0:30")
        assertThat(formatDuration(60, DurationUnit.MINUTES_SHORT)).isEqualTo("1:00")
        assertThat(formatDuration(90, DurationUnit.MINUTES_SHORT)).isEqualTo("1:30")
        assertThat(formatDuration(125, DurationUnit.MINUTES_SHORT)).isEqualTo("2:05")
    }

    @Test
    fun filteredBy_with_ALL_returns_everything() {
        val list = listOf(stretch(id = "a"), stretch(id = "b", bodyParts = listOf("calves")))
        assertThat(list.filteredBy(BodyParts.ALL)).hasSize(2)
    }

    @Test
    fun filteredBy_narrows_to_one_body_part() {
        val list = listOf(
            stretch(id = "a", bodyParts = listOf("lower-back")),
            stretch(id = "b", bodyParts = listOf("calves")),
            stretch(id = "c", bodyParts = listOf("calves", "hamstrings")),
        )
        assertThat(list.filteredBy("calves").map { it.id }).containsExactly("b", "c").inOrder()
    }

    @Test
    fun program_subtitle_renders_day_count_and_category() {
        val program = Program(
            id = "p", title = "X", category = "lower-back",
            summary = "", days = listOf(
                ProgramDay(day = 1, title = "d1", stretchIds = listOf("a")),
                ProgramDay(day = 2, title = "d2", stretchIds = listOf("a")),
            ),
        )
        assertThat(program.subtitle).isEqualTo("2-day · lower back")
    }

    @Test
    fun programDay_headerTitle_and_subtitle() {
        val day = ProgramDay(day = 3, title = "Gentle", stretchIds = listOf("a", "b"))
        assertThat(day.headerTitle).isEqualTo("Day 3 · Gentle")
        assertThat(day.subtitle(totalSeconds = 180)).isEqualTo("2 stretches · 3 min")
    }

    @Test
    fun stretchCountSubtitle_matches_programDay_subtitle_shape() {
        assertThat(stretchCountSubtitle(stretchCount = 5, totalSeconds = 300))
            .isEqualTo("5 stretches · 5 min")
        assertThat(stretchCountSubtitle(stretchCount = 0, totalSeconds = 0))
            .isEqualTo("0 stretches · 0 min")
        // Truncates to whole minutes — the iOS mirror does the same.
        assertThat(stretchCountSubtitle(stretchCount = 3, totalSeconds = 119))
            .isEqualTo("3 stretches · 1 min")
    }
}
