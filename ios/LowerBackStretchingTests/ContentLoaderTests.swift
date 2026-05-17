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

    func testTotalDurationSumsKnownStretches() {
        let store = ContentStore()
        let program = store.programs.first!
        let ids = program.days.first!.stretchIds
        let expected = ids.compactMap { store.stretch(id: $0)?.durationSeconds }.reduce(0, +)
        XCTAssertEqual(store.totalDurationSeconds(stretchIds: ids), expected)
    }

    func testTotalDurationSkipsUnknownIds() {
        let store = ContentStore()
        XCTAssertEqual(store.totalDurationSeconds(stretchIds: ["nonexistent"]), 0)
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

    func testCatCowHasEducationalContent() {
        let store = ContentStore()
        let catCow = store.stretch(id: "cat-cow")!
        XCTAssertNotNil(catCow.whyThisStretch)
        XCTAssertNotNil(catCow.whatYouShouldFeel)
        XCTAssertNotNil(catCow.educationalCards)
        XCTAssertFalse(catCow.educationalCards!.isEmpty)
        XCTAssertNotNil(catCow.mistakesToAvoid)
        XCTAssertFalse(catCow.mistakesToAvoid!.isEmpty)
    }

    func testStretchesWithoutEducationalContentStillParse() {
        // Pigeon wasn't enriched in the wave-6 first cut, so all the
        // optional fields should be nil. Confirms the schema tolerates
        // partial population without throwing.
        let store = ContentStore()
        let pigeon = store.stretch(id: "pigeon")!
        XCTAssertNil(pigeon.whyThisStretch)
        XCTAssertNil(pigeon.whatYouShouldFeel)
        XCTAssertNil(pigeon.educationalCards)
        XCTAssertNil(pigeon.mistakesToAvoid)
    }

    func testGlossaryLoadsAndEveryEntryHasRequiredFields() {
        let store = ContentStore()
        XCTAssertFalse(store.glossary.isEmpty)
        for entry in store.glossary {
            XCTAssertFalse(entry.term.isEmpty)
            XCTAssertFalse(entry.definition.isEmpty)
            XCTAssertFalse(entry.category.isEmpty)
        }
    }

    func testGlossaryCategoriesAreKnownSet() {
        let store = ContentStore()
        let categories = Set(store.glossary.map(\.category))
        XCTAssertTrue(categories.contains("anatomy"))
        XCTAssertTrue(categories.contains("concepts"))
    }

    func testEveryBodyZoneHasAtLeastOneMatchingStretch() {
        // Tapping a zone on the body diagram opens a sheet listing
        // matching stretches. If any zone maps to an empty list the
        // user sees a dead-end — guard against that.
        let store = ContentStore()
        for zone in BodyZone.allCases {
            let matches = store.stretches.filter { $0.bodyParts.contains(zone.bodyPartTag) }
            XCTAssertFalse(matches.isEmpty, "No stretches match zone \(zone.displayName)")
        }
    }
}
