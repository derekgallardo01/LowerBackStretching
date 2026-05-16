package com.lowerbackstretching.data.db

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class SessionDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: SessionDao

    @Before
    fun setup() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java).build()
        dao = db.sessionDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_and_count() = runBlocking {
        assertThat(dao.count().first()).isEqualTo(0)
        dao.insert(session(programId = "p1", day = 1, epochDay = 100L, millis = 1L, duration = 60))
        assertThat(dao.count().first()).isEqualTo(1)
    }

    @Test
    fun recent_returns_in_descending_order_and_respects_limit() = runBlocking {
        for (i in 1..5) {
            dao.insert(
                session(programId = "p$i", day = i, epochDay = 100L + i, millis = (i * 1000L), duration = 60)
            )
        }
        val recent = dao.recent(limit = 3).first()
        assertThat(recent).hasSize(3)
        assertThat(recent[0].programId).isEqualTo("p5")
        assertThat(recent[1].programId).isEqualTo("p4")
        assertThat(recent[2].programId).isEqualTo("p3")
    }

    @Test
    fun completedDays_returns_distinct_days_descending() = runBlocking {
        dao.insert(session(programId = "p", day = 1, epochDay = 100L, millis = 1L, duration = 60))
        dao.insert(session(programId = "p", day = 2, epochDay = 100L, millis = 2L, duration = 60))
        dao.insert(session(programId = "p", day = 3, epochDay = 102L, millis = 3L, duration = 60))
        val days = dao.completedDays().first()
        assertThat(days).containsExactly(102L, 100L).inOrder()
    }

    @Test
    fun forDay_filters_by_epochDay() = runBlocking {
        dao.insert(session(programId = "p", day = 1, epochDay = 100L, millis = 1L, duration = 60))
        dao.insert(session(programId = "p", day = 2, epochDay = 101L, millis = 2L, duration = 60))
        val day100 = dao.forDay(100L).first()
        assertThat(day100).hasSize(1)
        assertThat(day100[0].dayNumber).isEqualTo(1)
    }

    private fun session(programId: String, day: Int, epochDay: Long, millis: Long, duration: Int) =
        SessionEntity(
            programId = programId,
            dayNumber = day,
            completedAtEpochDay = epochDay,
            completedAtEpochMillis = millis,
            durationSeconds = duration,
        )
}
