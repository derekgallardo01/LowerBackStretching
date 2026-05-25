package com.lowerbackstretching.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PainLogDao {
    @Insert
    suspend fun insert(log: PainLogEntity): Long

    @Delete
    suspend fun delete(log: PainLogEntity)

    @Query("SELECT * FROM pain_logs ORDER BY recordedAtEpochMillis DESC")
    fun all(): Flow<List<PainLogEntity>>

    @Query("SELECT * FROM pain_logs ORDER BY recordedAtEpochMillis DESC LIMIT 1")
    fun latest(): Flow<PainLogEntity?>

    @Query(
        "SELECT EXISTS(SELECT 1 FROM pain_logs " +
            "WHERE context = 'PRE_SESSION' AND recordedAtEpochMillis >= :sinceEpochMillis)"
    )
    suspend fun hasPreLogSince(sinceEpochMillis: Long): Boolean
}
