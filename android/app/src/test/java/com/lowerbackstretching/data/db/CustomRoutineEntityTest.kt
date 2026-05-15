package com.lowerbackstretching.data.db

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CustomRoutineEntityTest {

    @Test
    fun `stretchIds parses csv`() {
        val entity = entity(csv = "cat-cow,child-pose,knee-to-chest")
        assertThat(entity.stretchIds).containsExactly("cat-cow", "child-pose", "knee-to-chest").inOrder()
    }

    @Test
    fun `empty csv is empty list`() {
        val entity = entity(csv = "")
        assertThat(entity.stretchIds).isEmpty()
    }

    @Test
    fun `blank csv is empty list`() {
        val entity = entity(csv = "   ")
        assertThat(entity.stretchIds).isEmpty()
    }

    @Test
    fun `single stretch csv`() {
        val entity = entity(csv = "cat-cow")
        assertThat(entity.stretchIds).containsExactly("cat-cow")
    }

    private fun entity(csv: String) = CustomRoutineEntity(
        id = 1L,
        name = "test",
        stretchIdsCsv = csv,
        createdAtEpochMillis = 0L,
    )
}
