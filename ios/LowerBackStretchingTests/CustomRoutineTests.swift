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

    func testNewRoutineDefaultsToVisibleAndOrderZero() {
        let routine = CustomRoutine(name: "x", stretchIds: [])
        XCTAssertEqual(routine.displayOrder, 0)
        XCTAssertNil(routine.deletedAt)
    }

    func testDuplicateNameAppendsSuffixOnce() {
        XCTAssertEqual(duplicateName("Morning"), "Morning (copy)")
        XCTAssertEqual(duplicateName("Morning (copy)"), "Morning (copy)")
        XCTAssertEqual(duplicateName("  Spaced  "), "Spaced (copy)")
    }

    func testReorderAssignsSequentialDisplayOrder() {
        let a = CustomRoutine(name: "a", stretchIds: [])
        let b = CustomRoutine(name: "b", stretchIds: [])
        let c = CustomRoutine(name: "c", stretchIds: [])
        a.displayOrder = 99
        b.displayOrder = 99
        c.displayOrder = 99
        CustomRoutineService.reorder([c, a, b])
        XCTAssertEqual(c.displayOrder, 0)
        XCTAssertEqual(a.displayOrder, 1)
        XCTAssertEqual(b.displayOrder, 2)
    }

    func testSoftDeleteAndRestoreToggleDeletedAt() {
        let routine = CustomRoutine(name: "x", stretchIds: [])
        XCTAssertNil(routine.deletedAt)
        CustomRoutineService.softDelete(routine)
        XCTAssertNotNil(routine.deletedAt)
        CustomRoutineService.restore(routine)
        XCTAssertNil(routine.deletedAt)
    }
}
