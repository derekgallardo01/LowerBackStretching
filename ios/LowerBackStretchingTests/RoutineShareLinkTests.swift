import XCTest
@testable import LowerBackStretching

final class RoutineShareLinkTests: XCTestCase {

    func testBuildThenParseRoundTrips() {
        let link = buildRoutineLink(name: "Morning Routine", stretchIds: ["cat-cow", "child-pose", "knee-to-chest"])
        XCTAssertEqual(
            parseRoutineLink(link),
            SharedRoutine(name: "Morning Routine", stretchIds: ["cat-cow", "child-pose", "knee-to-chest"])
        )
    }

    func testBuiltLinkStartsWithTheCustomScheme() {
        let link = buildRoutineLink(name: "X", stretchIds: ["a"])
        XCTAssertTrue(link.hasPrefix("\(routineLinkScheme)://\(routineLinkHost)?"))
    }

    func testNameWithSpacesAndUnicodeRoundTrips() {
        let link = buildRoutineLink(name: "Café — déjà vu", stretchIds: ["a"])
        XCTAssertEqual(parseRoutineLink(link)?.name, "Café — déjà vu")
    }

    func testNameWithAmpersandAndEqualsSignsRoundTrips() {
        let link = buildRoutineLink(name: "Bake & Stretch = Fun", stretchIds: ["a"])
        XCTAssertEqual(parseRoutineLink(link)?.name, "Bake & Stretch = Fun")
    }

    func testWrongSchemeReturnsNil() {
        XCTAssertNil(parseRoutineLink("https://example.com/routine?name=X&ids=a"))
    }

    func testWrongHostReturnsNil() {
        XCTAssertNil(parseRoutineLink("lowerbackstretching://program?name=X&ids=a"))
    }

    func testMissingNameReturnsNil() {
        XCTAssertNil(parseRoutineLink("lowerbackstretching://routine?ids=a,b"))
    }

    func testMissingIdsReturnsNil() {
        XCTAssertNil(parseRoutineLink("lowerbackstretching://routine?name=X"))
    }

    func testEmptyIdsStringReturnsNil() {
        XCTAssertNil(parseRoutineLink("lowerbackstretching://routine?name=X&ids="))
    }

    func testBlankNameReturnsNil() {
        let link = buildRoutineLink(name: "   ", stretchIds: ["a"])
        XCTAssertNil(parseRoutineLink(link))
    }

    func testParseTrimsWhitespaceInsideTheIdList() {
        let parsed = parseRoutineLink("lowerbackstretching://routine?name=X&ids=a,%20b%20,c")
        XCTAssertEqual(parsed?.stretchIds, ["a", "b", "c"])
    }
}
