package com.lowerbackstretching.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CooldownSuggestionTest {

    @Test fun `off when the user hasn't opted in to reading steps`() {
        assertThat(
            shouldShowCooldown(enabledRead = false, stretchedToday = false, stepsToday = 9_000)
        ).isFalse()
    }

    @Test fun `off when the user already stretched today`() {
        assertThat(
            shouldShowCooldown(enabledRead = true, stretchedToday = true, stepsToday = 9_000)
        ).isFalse()
    }

    @Test fun `off when steps are unknown (null)`() {
        assertThat(
            shouldShowCooldown(enabledRead = true, stretchedToday = false, stepsToday = null)
        ).isFalse()
    }

    @Test fun `off below the threshold`() {
        assertThat(
            shouldShowCooldown(enabledRead = true, stretchedToday = false, stepsToday = 4_999)
        ).isFalse()
    }

    @Test fun `on at the threshold`() {
        assertThat(
            shouldShowCooldown(enabledRead = true, stretchedToday = false, stepsToday = 5_000)
        ).isTrue()
    }

    @Test fun `on above the threshold`() {
        assertThat(
            shouldShowCooldown(enabledRead = true, stretchedToday = false, stepsToday = 12_345)
        ).isTrue()
    }

    @Test fun `threshold is configurable`() {
        assertThat(
            shouldShowCooldown(
                enabledRead = true, stretchedToday = false, stepsToday = 2_000,
                threshold = 1_500,
            )
        ).isTrue()
    }
}
