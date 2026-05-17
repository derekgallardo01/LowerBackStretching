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

    /** Copy a routine with a " (copy)" name suffix. Returns the new id. */
    suspend fun duplicate(routine: CustomRoutineEntity): Long =
        dao.insert(
            routine.copy(
                id = 0,
                name = duplicateName(routine.name),
                createdAtEpochMillis = System.currentTimeMillis(),
                displayOrder = 0,
                deletedAtEpochMillis = null,
            )
        )

    /**
     * Assign [orderedRoutineIds] indices 0..n-1 as their `displayOrder`.
     * The caller provides the full desired order; ids not in the list
     * are not touched.
     */
    suspend fun reorder(orderedRoutineIds: List<Long>) {
        orderedRoutineIds.forEachIndexed { index, id ->
            dao.setDisplayOrder(id, index)
        }
    }

    suspend fun softDelete(routine: CustomRoutineEntity) {
        dao.setDeletedAt(routine.id, System.currentTimeMillis())
    }

    suspend fun restore(routine: CustomRoutineEntity) {
        dao.setDeletedAt(routine.id, null)
    }
}

internal fun duplicateName(original: String): String {
    val trimmed = original.trim()
    return if (trimmed.endsWith("(copy)")) trimmed else "$trimmed (copy)"
}
