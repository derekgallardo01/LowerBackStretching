package com.lowerbackstretching.data

import com.google.common.truth.Truth.assertThat
import com.lowerbackstretching.data.db.CustomRoutineDao
import com.lowerbackstretching.data.db.CustomRoutineEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CustomRoutineRepositoryTest {

    @Test
    fun `create joins stretchIds with comma and trims name`() = runTest {
        val dao = FakeDao()
        val repo = CustomRoutineRepository(dao)
        repo.create(name = "  Morning  ", stretchIds = listOf("cat-cow", "child-pose"))
        val saved = dao.lastInserted!!
        assertThat(saved.name).isEqualTo("Morning")
        assertThat(saved.stretchIdsCsv).isEqualTo("cat-cow,child-pose")
        assertThat(saved.stretchIds).containsExactly("cat-cow", "child-pose").inOrder()
    }

    @Test
    fun `create empty stretches saves empty csv`() = runTest {
        val dao = FakeDao()
        val repo = CustomRoutineRepository(dao)
        repo.create(name = "Empty", stretchIds = emptyList())
        assertThat(dao.lastInserted!!.stretchIdsCsv).isEqualTo("")
    }

    @Test
    fun `update rewrites name and stretches`() = runTest {
        val dao = FakeDao()
        val repo = CustomRoutineRepository(dao)
        val existing = CustomRoutineEntity(
            id = 7L, name = "Old", stretchIdsCsv = "a,b", createdAtEpochMillis = 100L,
        )
        repo.update(existing, name = "New", stretchIds = listOf("x"))
        val updated = dao.lastUpdated!!
        assertThat(updated.id).isEqualTo(7L)
        assertThat(updated.name).isEqualTo("New")
        assertThat(updated.stretchIdsCsv).isEqualTo("x")
        assertThat(updated.createdAtEpochMillis).isEqualTo(100L)
    }

    private class FakeDao : CustomRoutineDao {
        var lastInserted: CustomRoutineEntity? = null
        var lastUpdated: CustomRoutineEntity? = null
        var lastDeleted: CustomRoutineEntity? = null
        private val items = MutableStateFlow<List<CustomRoutineEntity>>(emptyList())

        override suspend fun insert(routine: CustomRoutineEntity): Long {
            lastInserted = routine
            return routine.id.takeIf { it > 0 } ?: 1L
        }
        override suspend fun update(routine: CustomRoutineEntity) { lastUpdated = routine }
        override suspend fun delete(routine: CustomRoutineEntity) { lastDeleted = routine }
        override fun all(): Flow<List<CustomRoutineEntity>> = items
        override suspend fun byId(id: Long): CustomRoutineEntity? = items.value.firstOrNull { it.id == id }
    }
}
