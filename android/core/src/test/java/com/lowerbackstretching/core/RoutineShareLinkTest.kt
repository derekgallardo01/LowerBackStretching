package com.lowerbackstretching.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RoutineShareLinkTest {

    @Test fun `build then parse round-trips`() {
        val link = buildRoutineLink("Morning Routine", listOf("cat-cow", "child-pose", "knee-to-chest"))
        val parsed = parseRoutineLink(link)
        assertThat(parsed).isEqualTo(
            SharedRoutine("Morning Routine", listOf("cat-cow", "child-pose", "knee-to-chest"))
        )
    }

    @Test fun `built link starts with the custom scheme`() {
        val link = buildRoutineLink("X", listOf("a"))
        assertThat(link).startsWith("$ROUTINE_LINK_SCHEME://$ROUTINE_LINK_HOST?")
    }

    @Test fun `name with spaces and unicode round-trips`() {
        val link = buildRoutineLink("Café — déjà vu", listOf("a"))
        assertThat(parseRoutineLink(link)?.name).isEqualTo("Café — déjà vu")
    }

    @Test fun `name with ampersand and equals signs round-trips`() {
        val link = buildRoutineLink("Bake & Stretch = Fun", listOf("a"))
        assertThat(parseRoutineLink(link)?.name).isEqualTo("Bake & Stretch = Fun")
    }

    @Test fun `wrong scheme returns null`() {
        assertThat(parseRoutineLink("https://example.com/routine?name=X&ids=a")).isNull()
    }

    @Test fun `wrong host returns null`() {
        assertThat(parseRoutineLink("lowerbackstretching://program?name=X&ids=a")).isNull()
    }

    @Test fun `missing name returns null`() {
        assertThat(parseRoutineLink("lowerbackstretching://routine?ids=a,b")).isNull()
    }

    @Test fun `missing ids returns null`() {
        assertThat(parseRoutineLink("lowerbackstretching://routine?name=X")).isNull()
    }

    @Test fun `empty ids string returns null`() {
        assertThat(parseRoutineLink("lowerbackstretching://routine?name=X&ids=")).isNull()
    }

    @Test fun `blank name returns null`() {
        val link = buildRoutineLink("   ", listOf("a"))
        assertThat(parseRoutineLink(link)).isNull()
    }

    @Test fun `parse trims whitespace inside the id list`() {
        val parsed = parseRoutineLink("lowerbackstretching://routine?name=X&ids=a,%20b%20,c")
        assertThat(parsed?.stretchIds).containsExactly("a", "b", "c").inOrder()
    }
}
