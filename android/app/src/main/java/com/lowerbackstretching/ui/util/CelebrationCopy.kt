package com.lowerbackstretching.ui.util

/**
 * Warm, varied copy shown after a completed session. Picked at random
 * so users don't see the same line twice in a row. Tone: calm coach,
 * not cheerleader. Under 12 words, no exclamation marks, no emoji in
 * the line itself (the FinishedView surrounds them with its own iconography).
 */
object CelebrationCopy {
    private val lines = listOf(
        "Nice work. Your back will thank you.",
        "Done. That's another deposit in the bank.",
        "Your spine just got a little hug.",
        "One more session in the books.",
        "Lower back, happier than five minutes ago.",
        "Tiny effort, real difference.",
        "That's how a stronger back gets built.",
        "Quiet progress is still progress.",
        "Your future self is grateful.",
        "Every session adds up.",
        "Breath, movement, done.",
        "Five minutes well spent.",
        "Small move, big return.",
        "You showed up. That's the whole game.",
        "Lighter than when you started.",
    )

    /** Picks a line at random; pass the lastShown to avoid an immediate repeat. */
    fun pick(lastShown: String? = null): String {
        if (lines.size <= 1) return lines.first()
        var pick = lines.random()
        while (pick == lastShown) pick = lines.random()
        return pick
    }

    /** Exposed for tests that want to assert constraints across the bank. */
    internal fun all(): List<String> = lines
}
