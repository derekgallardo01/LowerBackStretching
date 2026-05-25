package com.lowerbackstretching.core

/**
 * One self-reported pain rating. Pure value contract — the Room-backed
 * `PainLogEntity` in `:app` implements this so the pairing and delta
 * math here can run on either entities or test doubles without dragging
 * Room into `:core`.
 *
 * `painLevel` is 0..10 (no pain..severe). `bodyLocationTag` is a
 * `BodyZone.bodyPartTag` or null when the user skipped the body
 * diagram. `context` is one of [PRE_SESSION], [POST_SESSION].
 */
interface PainMeasurement {
    val recordedAtEpochMillis: Long
    val painLevel: Int
    val bodyLocationTag: String?
    val context: String
}

object PainContext {
    const val PRE_SESSION = "PRE_SESSION"
    const val POST_SESSION = "POST_SESSION"
}

/** A POST-session log paired with its closest preceding PRE log (if any). */
data class SessionPainPair(val pre: PainMeasurement?, val post: PainMeasurement)

/** Numeric view of a paired session — `delta` is null if no pre log was found. */
data class SessionPainDelta(val pre: Int?, val post: Int, val delta: Int?)

fun sessionPainDelta(pair: SessionPainPair): SessionPainDelta =
    SessionPainDelta(
        pre = pair.pre?.painLevel,
        post = pair.post.painLevel,
        delta = pair.pre?.let { pair.post.painLevel - it.painLevel },
    )

/**
 * Pair each POST_SESSION log with the most recent PRE_SESSION log that
 * landed within [lookbackMillis] before it. POST logs without a PRE
 * inside the window come back with `pre = null`. Order of the input
 * list doesn't matter; the result is sorted newest-POST-first.
 */
fun pairSessionPainLogs(
    logs: List<PainMeasurement>,
    lookbackMillis: Long = 60 * 60 * 1000L,
): List<SessionPainPair> {
    val pres = logs
        .filter { it.context == PainContext.PRE_SESSION }
        .sortedBy { it.recordedAtEpochMillis }
    val posts = logs
        .filter { it.context == PainContext.POST_SESSION }
        .sortedByDescending { it.recordedAtEpochMillis }
    return posts.map { post ->
        val pre = pres.lastOrNull { pre ->
            pre.recordedAtEpochMillis <= post.recordedAtEpochMillis &&
                post.recordedAtEpochMillis - pre.recordedAtEpochMillis <= lookbackMillis
        }
        SessionPainPair(pre = pre, post = post)
    }
}
