import XCTest
@testable import LowerBackStretching

final class CustomRoutineTests: XCTestCase {

    func testStretchIdsParsesCsv() {
        let routine = CustomRoutine(name: "Test", stretchIds: ["cat-cow", "child-pose", "knee-to-chest"])
        XCTAssertEqual(routine.stretchIds, ["cat-cow", "child-pose", "knee-to-chest"])
        XCTAssertEqual(routine.stretchIdsCsv, "cat-cow,child-pose,knee-to-chest")
    }

    func testEmptyStretchIds() {
        let routine = CustomRoutine(name: "Empty", stretchIds: [])
        XCTAssertEqual(routine.stretchIds, [])
        XCTAssertEqual(routine.stretchIdsCsv, "")
    }

    func testNameIsTrimmed() {
        let routine = CustomRoutine(name: "  Morning  ", stretchIds: [])
        XCTAssertEqual(routine.name, "Morning")
    }

    func testStretchIdsSetterUpdatesCsv() {
        let routine = CustomRoutine(name: "x", stretchIds: ["a"])
        routine.stretchIds = ["b", "c"]
        XCTAssertEqual(routine.stretchIdsCsv, "b,c")
    }
}
