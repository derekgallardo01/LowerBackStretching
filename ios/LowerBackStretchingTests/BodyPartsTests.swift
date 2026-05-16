import XCTest
@testable import LowerBackStretching

final class BodyPartsTests: XCTestCase {

    private func stretch(_ id: String, _ parts: [String]) -> Stretch {
        Stretch(
            id: id, name: id, bodyParts: parts,
            durationSeconds: 30, difficulty: "easy",
            description: "", youtubeId: "x",
        )
    }

    func testDisplayReplacesHyphensWithSpaces() {
        XCTAssertEqual(BodyParts.display("lower-back"), "lower back")
        XCTAssertEqual(BodyParts.display("hips"), "hips")
        XCTAssertEqual(BodyParts.display("upper-back-and-spine"), "upper back and spine")
    }

    func testDisplayListJoinsWithDotSeparatorByDefault() {
        XCTAssertEqual(BodyParts.displayList(["lower-back", "spine"]), "lower back · spine")
    }

    func testDisplayListEmpty() {
        XCTAssertEqual(BodyParts.displayList([]), "")
    }

    func testDisplayListCustomSeparator() {
        XCTAssertEqual(BodyParts.displayList(["a", "b"], separator: ", "), "a, b")
    }

    func testDistinctSortedCollectsUniqueAndSorted() {
        let stretches = [
            stretch("a", ["lower-back", "spine"]),
            stretch("b", ["calves"]),
            stretch("c", ["lower-back", "hips"]),
        ]
        XCTAssertEqual(BodyParts.distinctSorted(from: stretches), ["calves", "hips", "lower-back", "spine"])
    }

    func testFilterOptionsPrependsAll() {
        let stretches = [stretch("a", ["calves"]), stretch("b", ["hips"])]
        XCTAssertEqual(BodyParts.filterOptions(from: stretches), ["all", "calves", "hips"])
    }

    func testAllConstantValue() {
        XCTAssertEqual(BodyParts.all, "all")
    }
}
