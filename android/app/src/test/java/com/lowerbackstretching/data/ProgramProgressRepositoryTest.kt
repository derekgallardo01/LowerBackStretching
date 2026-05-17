package com.lowerbackstretching.data

import com.google.common.truth.Truth.assertThat
import com.lowerbackstretching.core.SyntheticProgramId
import com.lowerbackstretching.data.db.ProgramProgressDao
import com.lowerbackstretching.data.db.ProgramProgressEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ProgramProgressRepositoryTest {

    @Test
    fun `currentDay defaults to 1 when no record exists`() = runTest {
        val repo = ProgramProgressRepository(FakeDao())
        assertThat(repo.currentDay("lower-back-relief-7day").first()).isEqualTo(1)
    }

    @Test
    fun `advance writes nextDay one past the completed day`() = runTest {
        val dao = FakeDao()
        val repo = ProgramProgressRepository(dao)
        repo.advance("p1", completedDay = 3, totalDays = 7)
        assertThat(dao.upserts.last().programId).isEqualTo("p1")
        assertThat(dao.upserts.last().currentDay).isEqualTo(4)
    }

    @Test
    fun `advance caps at totalDays plus one`() = runTest {
        val dao = FakeDao()
        val repo = ProgramProgressRepository(dao)
        repo.advance("p1", completedDay = 7, totalDays = 7)
        assertThat(dao.upserts.last().currentDay).isEqualTo(8)

        // Even if completedDay overshoots, we never go higher than totalDays + 1.
        repo.advance("p1", completedDay = 99, totalDays = 7)
        assertThat(dao.upserts.last().currentDay).isEqualTo(8)
    }

    @Test
    fun `advance ignores synthetic single ids`() = runTest {
        val dao = FakeDao()
        val repo = ProgramProgressRepository(dao)
        repo.advance(SyntheticProgramId.single("cat-cow"), completedDay = 1, totalDays = 1)
        assertThat(dao.upserts).isEmpty()
    }

    @Test
    fun `advance ignores synthetic routine ids`() = runTest {
        val dao = FakeDao()
        val repo = ProgramProgressRepository(dao)
        repo.advance(SyntheticProgramId.routine(42L), completedDay = 1, totalDays = 1)
        assertThat(dao.upserts).isEmpty()
    }

    @Test
    fun `reset deletes the row`() = runTest {
        val dao = FakeDao()
        val repo = ProgramProgressRepository(dao)
        repo.advance("p1", completedDay = 2, totalDays = 7)
        repo.reset("p1")
        assertThat(dao.deletedIds).containsExactly("p1")
    }

    private class FakeDao : ProgramProgressDao {
        val upserts = mutableListOf<ProgramProgressEntity>()
        val deletedIds = mutableListOf<String>()
        private val rows = MutableStateFlow<List<ProgramProgressEntity>>(emptyList())

        override suspend fun upsert(progress: ProgramProgressEntity) {
            upserts += progress
            rows.value = rows.value.filterNot { it.programId == progress.programId } + progress
        }

        override fun observe(programId: String): Flow<ProgramProgressEntity?> =
            rows.map { list -> list.firstOrNull { it.programId == programId } }

        override suspend fun byId(programId: String): ProgramProgressEntity? =
            rows.value.firstOrNull { it.programId == programId }

        override suspend fun deleteFor(programId: String) {
            deletedIds += programId
            rows.value = rows.value.filterNot { it.programId == programId }
        }
    }
}
