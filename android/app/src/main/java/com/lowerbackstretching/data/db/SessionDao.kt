package com.lowerbackstretching.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert
    suspend fun insert(session: SessionEntity): Long

    @Query("SELECT * FROM sessions ORDER BY completedAtEpochMillis DESC")
    fun all(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE completedAtEpochDay = :epochDay")
    fun forDay(epochDay: Long): Flow<List<SessionEntity>>

    @Query("SELECT DISTINCT completedAtEpochDay FROM sessions ORDER BY completedAtEpochDay DESC")
    fun completedDays(): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM sessions")
    fun count(): Flow<Int>
}
