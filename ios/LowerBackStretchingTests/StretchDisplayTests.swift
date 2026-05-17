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

    func testShortSubtitleDefaultSeconds() {
        let s = stretch(difficulty: "easy", seconds: 45, bodyParts: ["lower-back", "spine"])
        XCTAssertEqual(s.shortSubtitle(), "45s · Easy · lower back · spine")
    }

    func testShortSubtitleWithMinutesShort() {
        let s = stretch(difficulty: "medium", seconds: 90, bodyParts: ["hips"])
        XCTAssertEqual(s.shortSubtitle(unit: .minutesShort), "1:30 · Medium · hips")
    }

    func testFormatDurationSecondsAppendsS() {
        XCTAssertEqual(formatDuration(45, unit: .seconds), "45s")
        XCTAssertEqual(formatDuration(60, unit: .seconds), "60s")
        XCTAssertEqual(formatDuration(0, unit: .seconds), "0s")
    }

    func testFormatDurationMinutesShortPadsSeconds() {
        XCTAssertEqual(formatDuration(0, unit: .minutesShort), "0:00")
        XCTAssertEqual(formatDuration(30, unit: .minutesShort), "0:30")
        XCTAssertEqual(formatDuration(60, unit: .minutesShort), "1:00")
        XCTAssertEqual(formatDuration(90, unit: .minutesShort), "1:30")
        XCTAssertEqual(formatDuration(125, unit: .minutesShort), "2:05")
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

    func testStretchCountSubtitleMatchesProgramDayShape() {
        XCTAssertEqual(stretchCountSubtitle(stretchCount: 5, totalSeconds: 300), "5 stretches · 5 min")
        XCTAssertEqual(stretchCountSubtitle(stretchCount: 0, totalSeconds: 0), "0 stretches · 0 min")
        // Truncates to whole minutes — the Android mirror does the same.
        XCTAssertEqual(stretchCountSubtitle(stretchCount: 3, totalSeconds: 119), "3 stretches · 1 min")
    }
}
