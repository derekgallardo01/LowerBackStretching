package com.lowerbackstretching.core

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
    fun stretchIdFrom_round_trips_single() {
        assertThat(SyntheticProgramId.stretchIdFrom(SyntheticProgramId.single("cat-cow")))
            .isEqualTo("cat-cow")
    }

    @Test
    fun stretchIdFrom_rejects_non_single_ids() {
        assertThat(SyntheticProgramId.stretchIdFrom("routine-42")).isNull()
        assertThat(SyntheticProgramId.stretchIdFrom("lower-back-relief-7day")).isNull()
        // Bare prefix carries no stretch id.
        assertThat(SyntheticProgramId.stretchIdFrom("single-")).isNull()
    }

    @Test
    fun routineIdFrom_round_trips_routine() {
        assertThat(SyntheticProgramId.routineIdFrom(SyntheticProgramId.routine(42L))).isEqualTo(42L)
    }

    @Test
    fun routineIdFrom_rejects_non_routine_and_malformed_ids() {
        assertThat(SyntheticProgramId.routineIdFrom("single-cat-cow")).isNull()
        assertThat(SyntheticProgramId.routineIdFrom("lower-back-relief-7day")).isNull()
        assertThat(SyntheticProgramId.routineIdFrom("routine-")).isNull()
        assertThat(SyntheticProgramId.routineIdFrom("routine-abc")).isNull()
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
