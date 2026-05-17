package com.lowerbackstretching.wear.model

import kotlinx.serialization.Serializable

/**
 * Slim mirror of the phone-side [com.lowerbackstretching.core.model.Stretch].
 * The watch only needs the id, name, and duration to drive the timer;
 * the video, description, educational cards, and so on stay phone-only.
 *
 * Kept duplicated rather than extracted into a shared :core module so
 * this wave can ship without restructuring the whole project; the
 * extraction is a natural follow-up.
 */
@Serializable
data class WatchStretch(
    val id: String,
    val name: String,
    val durationSeconds: Int,
)

@Serializable
data class WatchRoutine(
    val name: String,
    val stretches: List<WatchStretch>,
)
