import Foundation
import SwiftData

/// The three measurements captured by the flexibility self-test, in
/// centimeters. Pure value contract — the SwiftData-backed
/// `FlexibilityTest` model conforms to this so the delta math in
/// `FlexibilityService` compares snapshots without depending on the
/// `@Model` storage layer. Mirrors Android's `FlexibilityMeasurement`
/// interface in `:core/Flexibility.kt`.
protocol FlexibilityMeasurement {
    var sitAndReachCm: Float? { get }
    var toeTouchCm: Float? { get }
    var shoulderReachCm: Float? { get }
}

/// One row per flexibility self-test snapshot. Each measurement is
/// optional so the user can skip individual tests. Values stored in
/// centimeters.
@Model
final class FlexibilityTest: FlexibilityMeasurement {
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
