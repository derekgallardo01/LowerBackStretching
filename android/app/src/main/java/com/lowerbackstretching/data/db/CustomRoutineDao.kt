package com.lowerbackstretching.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomRoutineDao {
    @Insert
    suspend fun insert(routine: CustomRoutineEntity): Long

    @Update
    suspend fun update(routine: CustomRoutineEntity)

    @Delete
    suspend fun delete(routine: CustomRoutineEntity)

    @Query("SELECT * FROM custom_routines ORDER BY createdAtEpochMillis DESC")
    fun all(): Flow<List<CustomRoutineEntity>>

    @Query("SELECT * FROM custom_routines WHERE id = :id")
    suspend fun byId(id: Long): CustomRoutineEntity?
}
