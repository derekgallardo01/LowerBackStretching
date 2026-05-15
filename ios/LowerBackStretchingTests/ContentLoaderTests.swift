import XCTest
@testable import LowerBackStretching

final class ContentLoaderTests: XCTestCase {

    func testLoadsAllStretches() {
        let store = ContentStore()
        XCTAssertGreaterThanOrEqual(store.stretches.count, 20,
            "Expected at least 20 stretches in the bundled catalog")
    }

    func testEveryStretchHasNonEmptyFields() {
        let store = ContentStore()
        for s in store.stretches {
            XCTAssertFalse(s.id.isEmpty, "Stretch has empty id")
            XCTAssertFalse(s.name.isEmpty, "Stretch \(s.id) has empty name")
            XCTAssertGreaterThan(s.durationSeconds, 0, "Stretch \(s.id) has non-positive duration")
            XCTAssertFalse(s.bodyParts.isEmpty, "Stretch \(s.id) has empty bodyParts")
            XCTAssertFalse(s.youtubeId.isEmpty, "Stretch \(s.id) has empty youtubeId")
        }
    }

    func testEveryProgramDayReferencesValidStretches() {
        let store = ContentStore()
        let validIds = Set(store.stretches.map(\.id))
        for program in store.programs {
            XCTAssertFalse(program.days.isEmpty, "Program \(program.id) has no days")
            for day in program.days {
                for sid in day.stretchIds {
                    XCTAssertTrue(validIds.contains(sid),
                        "Program \(program.id) day \(day.day) references unknown stretch '\(sid)'")
                }
            }
        }
    }

    func testStretchAndProgramLookups() {
        let store = ContentStore()
        let first = store.stretches.first!
        XCTAssertNotNil(store.stretch(id: first.id))
        XCTAssertNil(store.stretch(id: "nonexistent-id"))

        let firstProgram = store.programs.first!
        XCTAssertNotNil(store.program(id: firstProgram.id))
        XCTAssertNil(store.program(id: "nonexistent"))
    }
}
