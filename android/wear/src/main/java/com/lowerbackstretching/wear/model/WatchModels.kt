package com.lowerbackstretching.wear.model

import com.lowerbackstretching.core.player.Timed
import kotlinx.serialization.Serializable

/**
 * Slim mirror of the phone-side [com.lowerbackstretching.core.model.Stretch].
 * The watch only needs the id, name, and duration to drive the timer;
 * the video, description, educational cards, and so on stay phone-only.
 *
 * Implements [Timed] so the shared [com.lowerbackstretching.core.player.PlayerEngine]
 * can drive either kind of routine.
 */
@Serializable
data class WatchStretch(
    val id: String,
    val name: String,
    override val durationSeconds: Int,
) : Timed

@Serializable
data class WatchRoutine(
    val name: String,
    val stretches: List<WatchStretch>,
)
