package com.lowerbackstretching.data

import com.google.common.truth.Truth.assertThat
import com.lowerbackstretching.data.db.FlexibilityTestDao
import com.lowerbackstretching.data.db.FlexibilityTestEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FlexibilityRepositoryTest {

    @Test
    fun `record writes a row with the supplied measurements`() = runTest {
        val dao = FakeDao()
        val repo = FlexibilityRepository(dao)
        repo.record(sitAndReachCm = 12.5f, toeTouchCm = -4f, shoulderReachCm = null)
        val saved = dao.inserted.last()
        assertThat(saved.sitAndReachCm).isEqualTo(12.5f)
        assertThat(saved.toeTouchCm).isEqualTo(-4f)
        assertThat(saved.shoulderReachCm).isNull()
    }

    private class FakeDao : FlexibilityTestDao {
        val inserted = mutableListOf<FlexibilityTestEntity>()
        private val rows = MutableStateFlow<List<FlexibilityTestEntity>>(emptyList())

        override suspend fun insert(test: FlexibilityTestEntity): Long {
            inserted += test
            rows.value = rows.value + test.copy(id = (inserted.size).toLong())
            return inserted.size.toLong()
        }

        override suspend fun delete(test: FlexibilityTestEntity) {
            rows.value = rows.value.filterNot { it.id == test.id }
        }

        override fun all(): Flow<List<FlexibilityTestEntity>> = rows

        override fun latest(): Flow<FlexibilityTestEntity?> =
            rows.map { list -> list.maxByOrNull { it.recordedAtEpochMillis } }
    }
}
