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

    @Test
    fun `duplicate inserts a copy with suffix and zero id`() = runTest {
        val dao = FakeDao()
        val repo = CustomRoutineRepository(dao)
        val original = CustomRoutineEntity(
            id = 5L, name = "Morning", stretchIdsCsv = "a,b",
            createdAtEpochMillis = 100L, displayOrder = 3, deletedAtEpochMillis = null,
        )
        repo.duplicate(original)
        val saved = dao.lastInserted!!
        assertThat(saved.id).isEqualTo(0L)
        assertThat(saved.name).isEqualTo("Morning (copy)")
        assertThat(saved.stretchIdsCsv).isEqualTo("a,b")
        assertThat(saved.displayOrder).isEqualTo(0)
        assertThat(saved.deletedAtEpochMillis).isNull()
    }

    @Test
    fun `duplicate of an already-suffixed name does not double-suffix`() {
        assertThat(duplicateName("Morning (copy)")).isEqualTo("Morning (copy)")
        assertThat(duplicateName("Morning")).isEqualTo("Morning (copy)")
        assertThat(duplicateName("  Spaced  ")).isEqualTo("Spaced (copy)")
    }

    @Test
    fun `reorder assigns sequential displayOrder values`() = runTest {
        val dao = FakeDao()
        val repo = CustomRoutineRepository(dao)
        repo.reorder(listOf(10L, 20L, 30L))
        assertThat(dao.orderUpdates).containsExactly(10L to 0, 20L to 1, 30L to 2).inOrder()
    }

    @Test
    fun `softDelete sets deletedAt to non-null`() = runTest {
        val dao = FakeDao()
        val repo = CustomRoutineRepository(dao)
        val routine = CustomRoutineEntity(id = 9L, name = "x", stretchIdsCsv = "", createdAtEpochMillis = 0L)
        repo.softDelete(routine)
        val (id, ts) = dao.deletedAtUpdates.first()
        assertThat(id).isEqualTo(9L)
        assertThat(ts).isNotNull()
    }

    @Test
    fun `restore clears deletedAt`() = runTest {
        val dao = FakeDao()
        val repo = CustomRoutineRepository(dao)
        val routine = CustomRoutineEntity(id = 9L, name = "x", stretchIdsCsv = "", createdAtEpochMillis = 0L)
        repo.restore(routine)
        assertThat(dao.deletedAtUpdates.last()).isEqualTo(9L to null)
    }

    private class FakeDao : CustomRoutineDao {
        var lastInserted: CustomRoutineEntity? = null
        var lastUpdated: CustomRoutineEntity? = null
        var lastDeleted: CustomRoutineEntity? = null
        val orderUpdates = mutableListOf<Pair<Long, Int>>()
        val deletedAtUpdates = mutableListOf<Pair<Long, Long?>>()
        private val items = MutableStateFlow<List<CustomRoutineEntity>>(emptyList())

        override suspend fun insert(routine: CustomRoutineEntity): Long {
            lastInserted = routine
            return routine.id.takeIf { it > 0 } ?: 1L
        }
        override suspend fun update(routine: CustomRoutineEntity) { lastUpdated = routine }
        override suspend fun delete(routine: CustomRoutineEntity) { lastDeleted = routine }
        override fun all(): Flow<List<CustomRoutineEntity>> = items
        override suspend fun byId(id: Long): CustomRoutineEntity? = items.value.firstOrNull { it.id == id }
        override suspend fun setDisplayOrder(id: Long, order: Int) {
            orderUpdates += (id to order)
        }
        override suspend fun setDeletedAt(id: Long, deletedAt: Long?) {
            deletedAtUpdates += (id to deletedAt)
        }
    }
}
