package com.lowerbackstretching.data

import com.google.common.truth.Truth.assertThat
import com.lowerbackstretching.core.PainContext
import com.lowerbackstretching.data.db.PainLogDao
import com.lowerbackstretching.data.db.PainLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class PainLogRepositoryTest {

    @Test
    fun `recordPre writes a row tagged PRE_SESSION with null sessionId`() = runTest {
        val dao = FakeDao()
        val repo = PainLogRepository(dao)
        val id = repo.recordPre(painLevel = 6, bodyLocationTag = "lower-back")
        val saved = dao.inserted.single()
        assertThat(id).isEqualTo(1L)
        assertThat(saved.painLevel).isEqualTo(6)
        assertThat(saved.bodyLocationTag).isEqualTo("lower-back")
        assertThat(saved.context).isEqualTo(PainContext.PRE_SESSION)
        assertThat(saved.sessionId).isNull()
    }

    @Test
    fun `recordPost writes a row tagged POST_SESSION linked to a session`() = runTest {
        val dao = FakeDao()
        val repo = PainLogRepository(dao)
        repo.recordPost(painLevel = 4, bodyLocationTag = null, sessionId = 42L)
        val saved = dao.inserted.single()
        assertThat(saved.painLevel).isEqualTo(4)
        assertThat(saved.bodyLocationTag).isNull()
        assertThat(saved.context).isEqualTo(PainContext.POST_SESSION)
        assertThat(saved.sessionId).isEqualTo(42L)
    }

    @Test
    fun `hasPreLoggedToday returns false on a clean store`() = runTest {
        val dao = FakeDao()
        val repo = PainLogRepository(dao)
        assertThat(repo.hasPreLoggedToday()).isFalse()
    }

    @Test
    fun `hasPreLoggedToday returns true after recordPre on the same day`() = runTest {
        val dao = FakeDao()
        val repo = PainLogRepository(dao)
        repo.recordPre(painLevel = 5, bodyLocationTag = null)
        assertThat(repo.hasPreLoggedToday()).isTrue()
    }

    @Test
    fun `hasPreLoggedToday queries only PRE_SESSION rows`() = runTest {
        val dao = FakeDao()
        // Seed a POST log inside today's window — must NOT count.
        dao.inserted += PainLogEntity(
            id = 1, recordedAtEpochMillis = System.currentTimeMillis(),
            painLevel = 4, bodyLocationTag = null,
            context = PainContext.POST_SESSION, sessionId = 99,
        )
        val repo = PainLogRepository(dao)
        assertThat(repo.hasPreLoggedToday()).isFalse()
    }

    @Test
    fun `hasPreLoggedToday respects the start-of-day boundary`() = runTest {
        val dao = FakeDao()
        // A PRE log from yesterday must NOT count as today.
        val yesterdayMidday = LocalDate.now().minusDays(1)
            .atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        dao.inserted += PainLogEntity(
            id = 1, recordedAtEpochMillis = yesterdayMidday,
            painLevel = 5, bodyLocationTag = null,
            context = PainContext.PRE_SESSION, sessionId = null,
        )
        val repo = PainLogRepository(dao)
        assertThat(repo.hasPreLoggedToday()).isFalse()
    }

    @Test
    fun `delete removes the row from the flow`() = runTest {
        val dao = FakeDao()
        val repo = PainLogRepository(dao)
        repo.recordPre(painLevel = 6, bodyLocationTag = null)
        val toDelete = dao.inserted.single()
        repo.delete(toDelete.copy(id = 1L))
        assertThat(dao.inserted).isEmpty()
    }

    private class FakeDao : PainLogDao {
        val inserted = mutableListOf<PainLogEntity>()
        private val rows = MutableStateFlow<List<PainLogEntity>>(emptyList())

        override suspend fun insert(log: PainLogEntity): Long {
            val newId = (inserted.size + 1).toLong()
            val stored = log.copy(id = newId)
            inserted += stored
            rows.value = rows.value + stored
            return newId
        }

        override suspend fun delete(log: PainLogEntity) {
            inserted.removeIf { it.id == log.id }
            rows.value = rows.value.filterNot { it.id == log.id }
        }

        override fun all(): Flow<List<PainLogEntity>> =
            rows.map { list -> list.sortedByDescending { it.recordedAtEpochMillis } }

        override fun latest(): Flow<PainLogEntity?> =
            rows.map { list -> list.maxByOrNull { it.recordedAtEpochMillis } }

        override suspend fun hasPreLogSince(sinceEpochMillis: Long): Boolean =
            inserted.any {
                it.context == PainContext.PRE_SESSION &&
                    it.recordedAtEpochMillis >= sinceEpochMillis
            }
    }
}
