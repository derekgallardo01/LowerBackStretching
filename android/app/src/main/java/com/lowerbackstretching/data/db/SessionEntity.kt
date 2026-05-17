package com.lowerbackstretching.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programId: String,
    val dayNumber: Int,
    val completedAtEpochDay: Long,
    val completedAtEpochMillis: Long,
    val durationSeconds: Int,
    /** `SessionType.storageValue` — "program" / "single" / "routine". */
    val type: String = "program",
)
