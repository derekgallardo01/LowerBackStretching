package com.lowerbackstretching.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_routines")
data class CustomRoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val stretchIdsCsv: String,
    val createdAtEpochMillis: Long,
) {
    val stretchIds: List<String>
        get() = if (stretchIdsCsv.isBlank()) emptyList() else stretchIdsCsv.split(",")
}
