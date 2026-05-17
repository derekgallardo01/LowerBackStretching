package com.lowerbackstretching.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SettingsTest {

    @Test
    fun themeMode_fromStorage_round_trips_every_value() {
        for (mode in ThemeMode.entries) {
            assertThat(ThemeMode.fromStorage(mode.storageValue)).isEqualTo(mode)
        }
    }

    @Test
    fun themeMode_fromStorage_falls_back_to_system_for_unknown_or_null() {
        assertThat(ThemeMode.fromStorage(null)).isEqualTo(ThemeMode.SYSTEM)
        assertThat(ThemeMode.fromStorage("")).isEqualTo(ThemeMode.SYSTEM)
        assertThat(ThemeMode.fromStorage("invalid")).isEqualTo(ThemeMode.SYSTEM)
    }

    @Test
    fun durationUnit_fromStorage_round_trips_every_value() {
        for (unit in DurationUnit.entries) {
            assertThat(DurationUnit.fromStorage(unit.storageValue)).isEqualTo(unit)
        }
    }

    @Test
    fun durationUnit_fromStorage_falls_back_to_seconds_for_unknown_or_null() {
        assertThat(DurationUnit.fromStorage(null)).isEqualTo(DurationUnit.SECONDS)
        assertThat(DurationUnit.fromStorage("")).isEqualTo(DurationUnit.SECONDS)
        assertThat(DurationUnit.fromStorage("invalid")).isEqualTo(DurationUnit.SECONDS)
    }
}
