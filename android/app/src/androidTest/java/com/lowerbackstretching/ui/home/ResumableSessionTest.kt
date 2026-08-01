package com.lowerbackstretching.ui.home

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.lowerbackstretching.core.InProgressSession
import com.lowerbackstretching.core.SyntheticProgramId
import com.lowerbackstretching.data.ContentRepository
import com.lowerbackstretching.data.db.CustomRoutineEntity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The Home "Pick up where you left off" card must only appear when the saved
 * session still points at something that exists. Resolution needs the bundled
 * content JSON, so this runs instrumented rather than as a JVM test.
 */
@RunWith(AndroidJUnit4::class)
class ResumableSessionTest {
    private lateinit var content: ContentRepository

    @Before
    fun setUp() {
        content = ContentRepository(ApplicationProvider.getApplicationContext())
    }

    private fun routine(
        id: Long,
        name: String,
    ) = CustomRoutineEntity(
        id = id,
        name = name,
        stretchIdsCsv = content.stretches.take(2).joinToString(",") { it.id },
        createdAtEpochMillis = 0L,
    )

    @Test
    fun a_real_program_resolves_to_its_title() {
        val program = content.programs.first()
        val result = resumableSession(
            InProgressSession(program.id, dayNumber = 2, index = 1),
            content,
            routines = emptyList(),
        )
        assertThat(result).isNotNull()
        assertThat(result!!.title).isEqualTo(program.title)
    }

    @Test
    fun a_single_stretch_resolves_to_the_stretch_name() {
        val stretch = content.stretches.first()
        val result = resumableSession(
            InProgressSession(SyntheticProgramId.single(stretch.id), dayNumber = 0, index = 0),
            content,
            routines = emptyList(),
        )
        assertThat(result?.title).isEqualTo(stretch.name)
    }

    @Test
    fun a_custom_routine_resolves_to_its_name() {
        val result = resumableSession(
            InProgressSession(SyntheticProgramId.routine(5L), dayNumber = 0, index = 3),
            content,
            routines = listOf(routine(5L, "Morning loosener")),
        )
        assertThat(result?.title).isEqualTo("Morning loosener")
    }

    @Test
    fun a_deleted_custom_routine_resolves_to_null() {
        // This is the case that matters — the card must not dead-end.
        val result = resumableSession(
            InProgressSession(SyntheticProgramId.routine(99L), dayNumber = 0, index = 3),
            content,
            routines = listOf(routine(5L, "Morning loosener")),
        )
        assertThat(result).isNull()
    }

    @Test
    fun an_unknown_program_resolves_to_null() {
        val result = resumableSession(
            InProgressSession("program-that-was-removed", dayNumber = 1, index = 0),
            content,
            routines = emptyList(),
        )
        assertThat(result).isNull()
    }

    @Test
    fun an_unknown_stretch_resolves_to_null() {
        val result = resumableSession(
            InProgressSession(SyntheticProgramId.single("no-such-stretch"), 0, 0),
            content,
            routines = emptyList(),
        )
        assertThat(result).isNull()
    }

    @Test
    fun the_original_session_is_carried_through_unchanged() {
        val program = content.programs.first()
        val session = InProgressSession(program.id, dayNumber = 4, index = 2)
        val result = resumableSession(session, content, routines = emptyList())
        assertThat(result?.session).isEqualTo(session)
    }
}
