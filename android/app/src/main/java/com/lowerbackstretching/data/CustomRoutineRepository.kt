package com.lowerbackstretching.data

import com.lowerbackstretching.data.db.CustomRoutineDao
import com.lowerbackstretching.data.db.CustomRoutineEntity
import kotlinx.coroutines.flow.Flow

class CustomRoutineRepository(private val dao: CustomRoutineDao) {

    fun all(): Flow<List<CustomRoutineEntity>> = dao.all()

    suspend fun byId(id: Long): CustomRoutineEntity? = dao.byId(id)

    suspend fun create(name: String, stretchIds: List<String>): Long =
        dao.insert(
            CustomRoutineEntity(
                name = name.trim(),
                stretchIdsCsv = stretchIds.joinToString(","),
                createdAtEpochMillis = System.currentTimeMillis(),
            )
        )

    suspend fun update(routine: CustomRoutineEntity, name: String, stretchIds: List<String>) {
        dao.update(routine.copy(name = name.trim(), stretchIdsCsv = stretchIds.joinToString(",")))
    }

    suspend fun delete(routine: CustomRoutineEntity) = dao.delete(routine)
}
