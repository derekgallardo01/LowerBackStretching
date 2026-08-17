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
    /** Seconds into the video where the actual demo starts. Used to
     *  skip channel intros / talking heads / sponsor reads. 0 = play
     *  from the beginning. */
    val videoStartSeconds: Int = 0,
    /** A one-line summary of why this stretch helps. Optional. */
    val whyThisStretch: String? = null,
    /** Deeper how-to / anatomy / education cards shown on the detail screen. */
    val educationalCards: List<EducationalCard>? = null,
    /** Common mistakes the user should watch for. */
    val mistakesToAvoid: List<String>? = null,
    /** Body-feedback text shown as a small overlay during the player. */
    val whatYouShouldFeel: String? = null,
    /** Optional looping stick-figure animation that demonstrates the stretch.
     *  When null, the player surface shows a placeholder + "Watch demo on
     *  YouTube" link instead. */
    val animation: StretchAnimationSpec? = null,
    /** URL to a short looping MP4 video demonstrating the stretch.
     *  When present, the player renders video instead of the stick-figure
     *  animation. Falls back to animation / placeholder if the video fails
     *  to load or is absent. */
    val videoUrl: String? = null,
) : Timed

/**
 * Looping keyframe animation for a stick-figure renderer.
 *
 * The renderer interpolates between consecutive poses using eased segments and
 * wraps back to the first pose after the last — i.e. for poses [A, B] the
 * loop is A → B → A → B …, and for [A, B, C] it is A → B → C → A …
 *
 * Each [Pose.joints] entry maps a joint name to its normalized `[x, y]`
 * position in the drawing surface, where `0,0` is top-left and `1,1` is
 * bottom-right. Joint names that the renderer understands: `head`, `neck`,
 * `shoulder`, `elbow`, `hand`, `spineMid`, `hip`, `knee`, `foot`. All poses
 * in one spec should declare the same joints.
 */
@Serializable
data class StretchAnimationSpec(
    /** Total duration of one full loop through all poses, in seconds. */
    val loopSeconds: Double = 4.0,
    val poses: List<Pose>,
)

@Serializable
data class Pose(
    /** Optional human-readable label (e.g. "cow", "inhale"). Not rendered;
     *  exists for content-author orientation when reading the JSON. */
    val name: String? = null,
    val joints: Map<String, List<Double>>,
)

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
