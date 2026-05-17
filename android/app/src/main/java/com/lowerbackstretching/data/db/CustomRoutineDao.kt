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

    /** Visible routines: not soft-deleted. Sorted by displayOrder, newest first. */
    @Query(
        "SELECT * FROM custom_routines WHERE deletedAtEpochMillis IS NULL " +
            "ORDER BY displayOrder ASC, createdAtEpochMillis DESC"
    )
    fun all(): Flow<List<CustomRoutineEntity>>

    @Query("SELECT * FROM custom_routines WHERE id = :id")
    suspend fun byId(id: Long): CustomRoutineEntity?

    @Query("UPDATE custom_routines SET displayOrder = :order WHERE id = :id")
    suspend fun setDisplayOrder(id: Long, order: Int)

    @Query("UPDATE custom_routines SET deletedAtEpochMillis = :deletedAt WHERE id = :id")
    suspend fun setDeletedAt(id: Long, deletedAt: Long?)
}
