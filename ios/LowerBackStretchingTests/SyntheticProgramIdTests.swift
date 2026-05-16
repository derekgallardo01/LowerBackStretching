import XCTest
@testable import LowerBackStretching

final class SyntheticProgramIdTests: XCTestCase {

    func testSinglePrefixesStretchId() {
        XCTAssertEqual(SyntheticProgramId.single("cat-cow"), "single-cat-cow")
    }

    func testRoutinePrefixesUUID() {
        let uuid = UUID(uuidString: "00000000-0000-0000-0000-000000000001")!
        XCTAssertEqual(SyntheticProgramId.routine(uuid), "routine-00000000-0000-0000-0000-000000000001")
    }

    func testSingleAndRoutineHaveDistinctPrefixes() {
        let uuid = UUID()
        XCTAssertNotEqual(
            SyntheticProgramId.single(uuid.uuidString),
            SyntheticProgramId.routine(uuid),
        )
    }
}
