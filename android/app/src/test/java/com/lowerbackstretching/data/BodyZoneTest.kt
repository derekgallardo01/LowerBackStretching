package com.lowerbackstretching.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BodyZoneTest {

    @Test fun `empty tags produce empty zone set`() {
        assertThat(bodyZonesForTags(emptyList())).isEmpty()
    }

    @Test fun `spine tag highlights all three back zones`() {
        assertThat(bodyZonesForTags(listOf("spine")))
            .containsExactly(BodyZone.NECK, BodyZone.UPPER_BACK, BodyZone.LOWER_BACK)
    }

    @Test fun `lower-back tag highlights only the lower back zone`() {
        assertThat(bodyZonesForTags(listOf("lower-back")))
            .containsExactly(BodyZone.LOWER_BACK)
    }

    @Test fun `groin folds into the hips zone`() {
        assertThat(bodyZonesForTags(listOf("groin")))
            .containsExactly(BodyZone.HIPS)
    }

    @Test fun `unknown tags are silently dropped`() {
        assertThat(bodyZonesForTags(listOf("core", "quads"))).isEmpty()
    }

    @Test fun `multiple tags merge into a single zone set`() {
        val zones = bodyZonesForTags(listOf("lower-back", "hamstrings", "calves"))
        assertThat(zones).containsExactly(
            BodyZone.LOWER_BACK, BodyZone.HAMSTRINGS, BodyZone.CALVES,
        )
    }

    @Test fun `duplicate tags don't multiply zones`() {
        assertThat(bodyZonesForTags(listOf("lower-back", "lower-back")))
            .containsExactly(BodyZone.LOWER_BACK)
    }
}
