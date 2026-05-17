package com.lowerbackstretching.health

/**
 * Pure decision function: should we show a "you walked X steps, try a
 * cooldown stretch" card on Home? Cross-platform parity helper.
 *
 * Returns true only when:
 *   - the user opted in to reading steps,
 *   - they haven't already stretched today,
 *   - the step count is known and exceeds [threshold].
 */
fun shouldShowCooldown(
    enabledRead: Boolean,
    stretchedToday: Boolean,
    stepsToday: Long?,
    threshold: Long = 5_000L,
): Boolean {
    if (!enabledRead || stretchedToday) return false
    val steps = stepsToday ?: return false
    return steps >= threshold
}
