import XCTest
@testable import LowerBackStretching

final class BodyZoneTests: XCTestCase {

    func testEmptyTagsProduceEmptyZoneSet() {
        XCTAssertTrue(bodyZones(forTags: []).isEmpty)
    }

    func testSpineTagHighlightsAllThreeBackZones() {
        XCTAssertEqual(
            bodyZones(forTags: ["spine"]),
            [.neck, .upperBack, .lowerBack]
        )
    }

    func testLowerBackTagHighlightsOnlyTheLowerBackZone() {
        XCTAssertEqual(bodyZones(forTags: ["lower-back"]), [.lowerBack])
    }

    func testGroinFoldsIntoTheHipsZone() {
        XCTAssertEqual(bodyZones(forTags: ["groin"]), [.hips])
    }

    func testUnknownTagsAreSilentlyDropped() {
        XCTAssertTrue(bodyZones(forTags: ["core", "quads"]).isEmpty)
    }

    func testMultipleTagsMergeIntoASingleZoneSet() {
        XCTAssertEqual(
            bodyZones(forTags: ["lower-back", "hamstrings", "calves"]),
            [.lowerBack, .hamstrings, .calves]
        )
    }

    func testDuplicateTagsDoNotMultiplyZones() {
        XCTAssertEqual(
            bodyZones(forTags: ["lower-back", "lower-back"]),
            [.lowerBack]
        )
    }
}
