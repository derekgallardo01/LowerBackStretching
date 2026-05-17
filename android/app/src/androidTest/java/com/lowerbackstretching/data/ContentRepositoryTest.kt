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
    fun totalDurationSeconds_sums_known_stretches() {
        val program = repo.programs.first()
        val ids = program.days.first().stretchIds
        val expected = ids.sumOf { repo.stretch(it)!!.durationSeconds }
        assertThat(repo.totalDurationSeconds(ids)).isEqualTo(expected)
    }

    @Test
    fun totalDurationSeconds_skips_unknown_ids() {
        assertThat(repo.totalDurationSeconds(listOf("nonexistent"))).isEqualTo(0)
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

    @Test
    fun cat_cow_has_educational_content() {
        val catCow = repo.stretch("cat-cow")!!
        assertThat(catCow.whyThisStretch).isNotNull()
        assertThat(catCow.whatYouShouldFeel).isNotNull()
        assertThat(catCow.educationalCards).isNotNull()
        assertThat(catCow.educationalCards!!).isNotEmpty()
        assertThat(catCow.mistakesToAvoid).isNotNull()
        assertThat(catCow.mistakesToAvoid!!).isNotEmpty()
    }

    @Test
    fun stretches_without_educational_content_still_parse() {
        // pigeon was not enriched in the wave-6 first cut, so all the
        // optional fields should be null. This confirms the schema
        // tolerates partial population without throwing.
        val pigeon = repo.stretch("pigeon")!!
        assertThat(pigeon.whyThisStretch).isNull()
        assertThat(pigeon.whatYouShouldFeel).isNull()
        assertThat(pigeon.educationalCards).isNull()
        assertThat(pigeon.mistakesToAvoid).isNull()
    }

    @Test
    fun glossary_loads_and_every_entry_has_required_fields() {
        assertThat(repo.glossary).isNotEmpty()
        for (entry in repo.glossary) {
            assertThat(entry.term).isNotEmpty()
            assertThat(entry.definition).isNotEmpty()
            assertThat(entry.category).isNotEmpty()
        }
    }

    @Test
    fun glossary_categories_are_known_set() {
        val categories = repo.glossary.map { it.category }.toSet()
        assertThat(categories).containsAtLeast("anatomy", "concepts")
    }

    @Test
    fun every_body_zone_has_at_least_one_matching_stretch() {
        // Tapping a zone on the body diagram opens a sheet listing
        // matching stretches. If any zone maps to an empty list the
        // user sees a dead-end — guard against that.
        for (zone in BodyZone.entries) {
            val matches = repo.stretches.filter { it.bodyParts.contains(zone.bodyPartTag) }
            assertThat(matches).isNotEmpty()
        }
    }
}
