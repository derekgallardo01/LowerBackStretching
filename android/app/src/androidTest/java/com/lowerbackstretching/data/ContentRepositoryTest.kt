package com.lowerbackstretching.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContentRepositoryTest {

    private val repo = ContentRepository(InstrumentationRegistry.getInstrumentation().targetContext)

    @Test
    fun stretches_load_with_required_fields() {
        assertThat(repo.stretches).isNotEmpty()
        for (s in repo.stretches) {
            assertThat(s.id).isNotEmpty()
            assertThat(s.name).isNotEmpty()
            assertThat(s.durationSeconds).isGreaterThan(0)
            assertThat(s.bodyParts).isNotEmpty()
            assertThat(s.youtubeId).isNotEmpty()
        }
    }

    @Test
    fun every_program_day_references_a_real_stretch() {
        val validIds = repo.stretches.map { it.id }.toSet()
        for (program in repo.programs) {
            assertThat(program.days).isNotEmpty()
            for (day in program.days) {
                for (sid in day.stretchIds) {
                    assertThat(validIds).contains(sid)
                }
            }
        }
    }

    @Test
    fun stretchesFor_returns_resolved_stretches_in_order() {
        val program = repo.programs.first()
        val day1 = program.days.first()
        val resolved = repo.stretchesFor(program, day1.day)
        assertThat(resolved).hasSize(day1.stretchIds.size)
        for ((i, sid) in day1.stretchIds.withIndex()) {
            assertThat(resolved[i].id).isEqualTo(sid)
        }
    }

    @Test
    fun lookups_by_id_work_and_return_null_for_unknown() {
        val first = repo.stretches.first()
        assertThat(repo.stretch(first.id)).isEqualTo(first)
        assertThat(repo.stretch("nonexistent")).isNull()

        val firstProgram = repo.programs.first()
        assertThat(repo.program(firstProgram.id)).isEqualTo(firstProgram)
        assertThat(repo.program("nonexistent")).isNull()
    }
}
