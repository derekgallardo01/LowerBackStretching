package com.lowerbackstretching.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: ProgramProgressEntity)

    @Query("SELECT * FROM program_progress WHERE programId = :programId LIMIT 1")
    fun observe(programId: String): Flow<ProgramProgressEntity?>

    @Query("SELECT * FROM program_progress WHERE programId = :programId LIMIT 1")
    suspend fun byId(programId: String): ProgramProgressEntity?

    @Query("DELETE FROM program_progress WHERE programId = :programId")
    suspend fun deleteFor(programId: String)
}
