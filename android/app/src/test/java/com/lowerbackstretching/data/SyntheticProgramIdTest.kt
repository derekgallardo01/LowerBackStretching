package com.lowerbackstretching.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SyntheticProgramIdTest {

    @Test
    fun single_prefixes_stretch_id() {
        assertThat(SyntheticProgramId.single("cat-cow")).isEqualTo("single-cat-cow")
    }

    @Test
    fun routine_prefixes_long_id() {
        assertThat(SyntheticProgramId.routine(42L)).isEqualTo("routine-42")
    }

    @Test
    fun single_and_routine_have_distinct_prefixes() {
        assertThat(SyntheticProgramId.single("42")).isNotEqualTo(SyntheticProgramId.routine(42L))
    }
}
