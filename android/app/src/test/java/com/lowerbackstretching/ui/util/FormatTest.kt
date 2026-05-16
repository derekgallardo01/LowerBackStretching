package com.lowerbackstretching.ui.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FormatTest {

    @Test
    fun zero_padded_hour_and_minute() {
        assertThat(formatTime(8, 0)).isEqualTo("08:00")
        assertThat(formatTime(0, 5)).isEqualTo("00:05")
        assertThat(formatTime(9, 7)).isEqualTo("09:07")
    }

    @Test
    fun two_digit_values_pass_through() {
        assertThat(formatTime(13, 45)).isEqualTo("13:45")
        assertThat(formatTime(23, 59)).isEqualTo("23:59")
    }

    @Test
    fun midnight() {
        assertThat(formatTime(0, 0)).isEqualTo("00:00")
    }
}
