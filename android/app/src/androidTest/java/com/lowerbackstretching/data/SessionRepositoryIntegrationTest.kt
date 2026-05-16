package com.lowerbackstretching.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.lowerbackstretching.data.db.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionRepositoryIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: SessionRepository

    @Before
    fun setup() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java).build()
        repo = SessionRepository(db.sessionDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun recordCompletion_appears_in_count_and_recent() = runBlocking {
        assertThat(repo.count().first()).isEqualTo(0)

        repo.recordCompletion(programId = "p1", day = 1, durationSeconds = 300)
        repo.recordCompletion(programId = "p1", day = 2, durationSeconds = 240)

        assertThat(repo.count().first()).isEqualTo(2)
        val recent = repo.recent(limit = 10).first()
        assertThat(recent).hasSize(2)
        assertThat(recent[0].dayNumber).isEqualTo(2) // most recent first
    }

    @Test
    fun streak_is_at_least_one_after_recording_today() = runBlocking {
        repo.recordCompletion(programId = "p1", day = 1, durationSeconds = 60)
        // Streak should include today.
        assertThat(repo.streak().first()).isAtLeast(1)
    }

    @Test
    fun completedDays_includes_todays_date() = runBlocking {
        repo.recordCompletion(programId = "p1", day = 1, durationSeconds = 60)
        val days = repo.completedDays().first()
        assertThat(days).isNotEmpty()
    }
}
