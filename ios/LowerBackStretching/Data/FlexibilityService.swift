import Foundation
import SwiftData

/// Cross-platform parity for `FlexibilityRepository` on Android.
enum FlexibilityService {

    @discardableResult
    static func record(
        sitAndReachCm: Float?,
        toeTouchCm: Float?,
        shoulderReachCm: Float?,
        in context: ModelContext
    ) -> FlexibilityTest {
        let test = FlexibilityTest(
            sitAndReachCm: sitAndReachCm,
            toeTouchCm: toeTouchCm,
            shoulderReachCm: shoulderReachCm
        )
        context.insert(test)
        try? context.save()
        return test
    }

    static func delete(_ test: FlexibilityTest, in context: ModelContext) {
        context.delete(test)
        try? context.save()
    }
}

struct FlexibilityDelta: Equatable {
    let sitAndReachCm: Float?
    let toeTouchCm: Float?
    let shoulderReachCm: Float?
}

/// Pure helper mirroring Android's flexibilityDelta. Returns nil per
/// metric if either snapshot is missing that metric.
func flexibilityDelta(latest: FlexibilityTest?, previous: FlexibilityTest?) -> FlexibilityDelta {
    guard let latest, let previous else {
        return FlexibilityDelta(sitAndReachCm: nil, toeTouchCm: nil, shoulderReachCm: nil)
    }
    return FlexibilityDelta(
        sitAndReachCm: subtract(latest.sitAndReachCm, previous.sitAndReachCm),
        toeTouchCm: subtract(latest.toeTouchCm, previous.toeTouchCm),
        shoulderReachCm: subtract(latest.shoulderReachCm, previous.shoulderReachCm)
    )
}

private func subtract(_ a: Float?, _ b: Float?) -> Float? {
    if let a, let b { return a - b }
    return nil
}
