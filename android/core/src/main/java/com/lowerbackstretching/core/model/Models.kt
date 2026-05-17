package com.lowerbackstretching.core.model

import com.lowerbackstretching.core.player.Timed
import kotlinx.serialization.Serializable

@Serializable
data class Stretch(
    val id: String,
    val name: String,
    val bodyParts: List<String>,
    override val durationSeconds: Int,
    val difficulty: String,
    val description: String,
    val youtubeId: String,
    /** A one-line summary of why this stretch helps. Optional. */
    val whyThisStretch: String? = null,
    /** Deeper how-to / anatomy / education cards shown on the detail screen. */
    val educationalCards: List<EducationalCard>? = null,
    /** Common mistakes the user should watch for. */
    val mistakesToAvoid: List<String>? = null,
    /** Body-feedback text shown as a small overlay during the player. */
    val whatYouShouldFeel: String? = null,
) : Timed

@Serializable
data class EducationalCard(
    val title: String,
    val body: String,
    /** Optional Material icon name (filled set) for visual interest. */
    val icon: String? = null,
)

@Serializable
data class Program(
    val id: String,
    val title: String,
    val category: String,
    val summary: String,
    val days: List<ProgramDay>,
)

@Serializable
data class ProgramDay(
    val day: Int,
    val title: String,
    val stretchIds: List<String>,
)

@Serializable
data class GlossaryEntry(
    val term: String,
    val definition: String,
    /** Free-form category — current values: "anatomy", "concepts". */
    val category: String,
)
