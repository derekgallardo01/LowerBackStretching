import Foundation
import SwiftData

/// One row per flexibility self-test snapshot. Each measurement is
/// optional so the user can skip individual tests. Values stored in
/// centimeters.
@Model
final class FlexibilityTest {
    var recordedAt: Date
    var sitAndReachCm: Float?
    var toeTouchCm: Float?
    var shoulderReachCm: Float?

    init(
        sitAndReachCm: Float? = nil,
        toeTouchCm: Float? = nil,
        shoulderReachCm: Float? = nil,
        recordedAt: Date = .now
    ) {
        self.recordedAt = recordedAt
        self.sitAndReachCm = sitAndReachCm
        self.toeTouchCm = toeTouchCm
        self.shoulderReachCm = shoulderReachCm
    }
}
