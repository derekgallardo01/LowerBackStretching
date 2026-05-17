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
            SyntheticProgramId.routine(uuid)
        )
    }

    func testTypeForClassifiesEachPrefix() {
        XCTAssertEqual(SyntheticProgramId.type(for: "single-cat-cow"), .single)
        XCTAssertEqual(SyntheticProgramId.type(for: "routine-00000000-0000-0000-0000-000000000001"), .routine)
        XCTAssertEqual(SyntheticProgramId.type(for: "lower-back-relief-7day"), .program)
    }

    func testSessionTypeFromStorageRoundTrips() {
        for type in SessionType.allCases {
            XCTAssertEqual(SessionType.fromStorage(type.storageValue), type)
        }
    }

    func testSessionTypeFromStorageUnknownDefaultsToProgram() {
        XCTAssertEqual(SessionType.fromStorage("nonsense"), .program)
    }
}
