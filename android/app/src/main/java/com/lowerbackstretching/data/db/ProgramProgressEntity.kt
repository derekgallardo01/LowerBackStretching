package com.lowerbackstretching.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Per-program bookmark of the next day the user should do. Inserted on
 * first completion of any day in the program; updated on each
 * subsequent completion. Synthetic program ids (single-* / routine-*)
 * never write to this table — only canned programs do.
 */
@Entity(tableName = "program_progress")
data class ProgramProgressEntity(
    @PrimaryKey val programId: String,
    val currentDay: Int,
    val updatedAtEpochMillis: Long,
)
