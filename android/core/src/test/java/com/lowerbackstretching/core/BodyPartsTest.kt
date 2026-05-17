package com.lowerbackstretching.core

import com.google.common.truth.Truth.assertThat
import com.lowerbackstretching.core.model.Stretch
import org.junit.Test

class BodyPartsTest {

    private fun stretch(id: String, vararg parts: String) = Stretch(
        id = id, name = id, bodyParts = parts.toList(),
        durationSeconds = 30, difficulty = "easy", description = "", youtubeId = "x",
    )

    @Test
    fun display_replaces_hyphens_with_spaces() {
        assertThat(BodyParts.display("lower-back")).isEqualTo("lower back")
        assertThat(BodyParts.display("hips")).isEqualTo("hips")
        assertThat(BodyParts.display("upper-back-and-spine")).isEqualTo("upper back and spine")
    }

    @Test
    fun displayList_joins_with_dot_separator_by_default() {
        assertThat(BodyParts.displayList(listOf("lower-back", "spine")))
            .isEqualTo("lower back · spine")
    }

    @Test
    fun displayList_empty_returns_empty_string() {
        assertThat(BodyParts.displayList(emptyList())).isEmpty()
    }

    @Test
    fun displayList_with_custom_separator() {
        assertThat(BodyParts.displayList(listOf("a", "b"), separator = ", ")).isEqualTo("a, b")
    }

    @Test
    fun distinctSorted_collects_unique_parts_sorted() {
        val stretches = listOf(
            stretch("a", "lower-back", "spine"),
            stretch("b", "calves"),
            stretch("c", "lower-back", "hips"),
        )
        assertThat(BodyParts.distinctSorted(stretches))
            .containsExactly("calves", "hips", "lower-back", "spine").inOrder()
    }

    @Test
    fun filterOptions_prepends_ALL() {
        val stretches = listOf(stretch("a", "calves"), stretch("b", "hips"))
        assertThat(BodyParts.filterOptions(stretches))
            .containsExactly("all", "calves", "hips").inOrder()
    }

    @Test
    fun ALL_constant_value() {
        assertThat(BodyParts.ALL).isEqualTo("all")
    }
}
