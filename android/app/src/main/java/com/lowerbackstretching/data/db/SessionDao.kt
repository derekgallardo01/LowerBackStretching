package com.lowerbackstretching.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun insert(session: SessionEntity): Long

    // Order by timestamp, then id, so rows written within the same millisecond
    // still have a stable, insertion-order tie-break ("most recent first").
    @Query("SELECT * FROM sessions ORDER BY completedAtEpochMillis DESC, id DESC")
    fun all(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions ORDER BY completedAtEpochMillis DESC, id DESC LIMIT :limit")
    fun recent(limit: Int): Flow<List<SessionEntity>>

    // One-shot, non-observing reads. The Flow queries above emit their initial
    // value asynchronously via Room's InvalidationTracker, which makes them a
    // poor fit for a point-in-time read right after a write: the first emission
    // can race the write and observe a stale result (flaky on faster devices).
    // These suspend queries return the committed state directly.
    @Query("SELECT COUNT(*) FROM sessions")
    suspend fun countNow(): Int

    @Query("SELECT * FROM sessions ORDER BY completedAtEpochMillis DESC, id DESC LIMIT :limit")
    suspend fun recentNow(limit: Int): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE completedAtEpochDay = :epochDay")
    fun forDay(epochDay: Long): Flow<List<SessionEntity>>

    @Query("SELECT DISTINCT completedAtEpochDay FROM sessions ORDER BY completedAtEpochDay DESC")
    fun completedDays(): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM sessions")
    fun count(): Flow<Int>

    @Query("SELECT SUM(durationSeconds) FROM sessions")
    fun totalDurationSeconds(): Flow<Int?>
}
