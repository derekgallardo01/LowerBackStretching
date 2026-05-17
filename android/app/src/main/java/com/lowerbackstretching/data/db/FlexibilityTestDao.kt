package com.lowerbackstretching.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FlexibilityTestDao {
    @Insert
    suspend fun insert(test: FlexibilityTestEntity): Long

    @Delete
    suspend fun delete(test: FlexibilityTestEntity)

    @Query("SELECT * FROM flexibility_tests ORDER BY recordedAtEpochMillis DESC")
    fun all(): Flow<List<FlexibilityTestEntity>>

    @Query("SELECT * FROM flexibility_tests ORDER BY recordedAtEpochMillis DESC LIMIT 1")
    fun latest(): Flow<FlexibilityTestEntity?>
}
