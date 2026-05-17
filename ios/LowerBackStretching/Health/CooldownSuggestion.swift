import Foundation

/// Pure decision function: should we show a "you walked X steps, try a
/// cooldown stretch" card on Home? Cross-platform parity helper.
///
/// Returns true only when:
///   - the user opted in to reading steps,
///   - they haven't already stretched today,
///   - the step count is known and exceeds `threshold`.
func shouldShowCooldown(
    enabledRead: Bool,
    stretchedToday: Bool,
    stepsToday: Int?,
    threshold: Int = 5_000
) -> Bool {
    guard enabledRead, !stretchedToday else { return false }
    guard let steps = stepsToday else { return false }
    return steps >= threshold
}
