import XCTest
import SwiftData
@testable import LowerBackStretching

final class FlexibilityTests: XCTestCase {

    private var container: ModelContainer!
    private var context: ModelContext!

    override func setUp() {
        super.setUp()
        let config = ModelConfiguration("test", isStoredInMemoryOnly: true)
        container = try! ModelContainer(for: FlexibilityTest.self, configurations: config)
        context = ModelContext(container)
    }

    func testRecordWritesARowWithTheSuppliedMeasurements() {
        FlexibilityService.record(
            sitAndReachCm: 12.5, toeTouchCm: -4, shoulderReachCm: nil, in: context
        )
        let stored = try? context.fetch(FetchDescriptor<FlexibilityTest>())
        XCTAssertEqual(stored?.count, 1)
        XCTAssertEqual(stored?.first?.sitAndReachCm, 12.5)
        XCTAssertEqual(stored?.first?.toeTouchCm, -4)
        XCTAssertNil(stored?.first?.shoulderReachCm)
    }

    func testFlexibilityDeltaReturnsNilDeltasWhenEitherSnapshotIsMissing() {
        let now = FlexibilityTest(sitAndReachCm: 10, shoulderReachCm: 5)
        XCTAssertNil(flexibilityDelta(latest: now, previous: nil).sitAndReachCm)
        XCTAssertNil(flexibilityDelta(latest: nil, previous: now).toeTouchCm)
    }

    func testFlexibilityDeltaSubtractsPerMetricAndSkipsWhenOneSideIsNil() {
        let now  = FlexibilityTest(sitAndReachCm: 12, toeTouchCm: nil, shoulderReachCm: 7)
        let prev = FlexibilityTest(sitAndReachCm: 10, toeTouchCm: 3,   shoulderReachCm: nil)
        let delta = flexibilityDelta(latest: now, previous: prev)
        XCTAssertEqual(delta.sitAndReachCm, 2)
        XCTAssertNil(delta.toeTouchCm)
        XCTAssertNil(delta.shoulderReachCm)
    }

    func testDeleteRemovesTheRow() {
        let test = FlexibilityService.record(
            sitAndReachCm: 12, toeTouchCm: nil, shoulderReachCm: nil, in: context
        )
        FlexibilityService.delete(test, in: context)
        let stored = try? context.fetch(FetchDescriptor<FlexibilityTest>())
        XCTAssertEqual(stored?.count, 0)
    }
}
