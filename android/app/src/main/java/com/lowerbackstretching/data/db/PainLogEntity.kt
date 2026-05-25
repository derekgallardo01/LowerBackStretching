package com.lowerbackstretching.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lowerbackstretching.core.PainMeasurement

/**
 * One row per self-reported pain rating. Two contexts in v1: a
 * PRE_SESSION rating captured (at most once per day) before the player
 * starts and a POST_SESSION rating captured at the end of the same
 * session. POST rows carry the [sessionId] of the SessionEntity they
 * belong to; PRE rows leave it null and are matched up to a POST by
 * timestamp proximity in `:core`.
 *
 * Implements the pure [PainMeasurement] contract so the pairing math
 * in `:core` can run on either entities or test doubles.
 */
@Entity(
    tableName = "pain_logs",
    indices = [
        Index("recordedAtEpochMillis"),
        Index("sessionId"),
    ],
)
data class PainLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    override val recordedAtEpochMillis: Long,
    override val painLevel: Int,
    override val bodyLocationTag: String?,
    override val context: String,
    val sessionId: Long?,
) : PainMeasurement
