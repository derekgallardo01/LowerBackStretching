import XCTest
@testable import LowerBackStretching

final class StretchDisplayTests: XCTestCase {

    private func stretch(
        difficulty: String = "easy",
        seconds: Int = 30,
        bodyParts: [String] = ["lower-back"]
    ) -> Stretch {
        Stretch(
            id: "s", name: "s", bodyParts: bodyParts,
            durationSeconds: seconds, difficulty: difficulty,
            description: "", youtubeId: "x"
        )
    }

    func testDifficultyDisplayCapitalizes() {
        XCTAssertEqual(stretch(difficulty: "easy").difficultyDisplay, "Easy")
        XCTAssertEqual(stretch(difficulty: "medium").difficultyDisplay, "Medium")
        XCTAssertEqual(stretch(difficulty: "hard").difficultyDisplay, "Hard")
    }

    func testShortSubtitleRendersDurationDifficultyAndBodyParts() {
        let s = stretch(difficulty: "easy", seconds: 45, bodyParts: ["lower-back", "spine"])
        XCTAssertEqual(s.shortSubtitle, "45s · Easy · lower back · spine")
    }

    func testFilteredByAllReturnsEverything() {
        let list = [
            stretch(bodyParts: ["lower-back"]),
            stretch(bodyParts: ["calves"]),
        ]
        XCTAssertEqual(list.filtered(by: BodyParts.all).count, 2)
    }

    func testFilteredByNarrowsToOneBodyPart() {
        let list = [
            stretch(bodyParts: ["lower-back"]),
            stretch(bodyParts: ["calves"]),
            stretch(bodyParts: ["calves", "hamstrings"]),
        ]
        XCTAssertEqual(list.filtered(by: "calves").count, 2)
    }

    func testProgramSubtitleRendersDayCountAndCategory() {
        let program = Program(
            id: "p", title: "X", category: "lower-back", summary: "",
            days: [
                ProgramDay(day: 1, title: "d1", stretchIds: ["a"]),
                ProgramDay(day: 2, title: "d2", stretchIds: ["a"]),
            ]
        )
        XCTAssertEqual(program.subtitle, "2-day · lower back")
    }

    func testProgramDayHeaderAndSubtitle() {
        let day = ProgramDay(day: 3, title: "Gentle", stretchIds: ["a", "b"])
        XCTAssertEqual(day.headerTitle, "Day 3 · Gentle")
        XCTAssertEqual(day.subtitle(totalSeconds: 180), "2 stretches · 3 min")
    }
}
