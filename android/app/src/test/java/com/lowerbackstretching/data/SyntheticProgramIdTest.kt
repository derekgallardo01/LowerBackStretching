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

    @Test
    fun typeFor_classifies_each_prefix() {
        assertThat(SyntheticProgramId.typeFor("single-cat-cow")).isEqualTo(SessionType.SINGLE)
        assertThat(SyntheticProgramId.typeFor("routine-42")).isEqualTo(SessionType.ROUTINE)
        assertThat(SyntheticProgramId.typeFor("lower-back-relief-7day")).isEqualTo(SessionType.PROGRAM)
    }

    @Test
    fun SessionType_fromStorage_round_trips() {
        for (type in SessionType.entries) {
            assertThat(SessionType.fromStorage(type.storageValue)).isEqualTo(type)
        }
    }

    @Test
    fun SessionType_fromStorage_unknown_defaults_to_program() {
        assertThat(SessionType.fromStorage("nonsense")).isEqualTo(SessionType.PROGRAM)
    }
}
