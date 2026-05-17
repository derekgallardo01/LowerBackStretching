import XCTest
@testable import LowerBackStretching

final class PlayerEngineTests: XCTestCase {

    private func stretch(_ id: String, _ seconds: Int) -> Stretch {
        Stretch(
            id: id, name: id, bodyParts: ["lower-back"],
            durationSeconds: seconds, difficulty: "easy",
            description: "", youtubeId: "x"
        )
    }

    func testInitialStateLoadsFirstStretch() {
        let engine = PlayerEngine(stretches: [stretch("a", 10), stretch("b", 20)])
        XCTAssertEqual(engine.snapshot.index, 0)
        XCTAssertEqual(engine.snapshot.remainingSeconds, 10)
        XCTAssertTrue(engine.snapshot.running)
        XCTAssertFalse(engine.snapshot.finished)
        XCTAssertEqual(engine.snapshot.current?.id, "a")
    }

    func testEmptyListMarksFinished() {
        let engine = PlayerEngine(stretches: [])
        XCTAssertTrue(engine.snapshot.finished)
        XCTAssertNil(engine.snapshot.current)
    }

    func testTickDecrementsRemaining() {
        let engine = PlayerEngine(stretches: [stretch("a", 3)])
        _ = engine.tick()
        XCTAssertEqual(engine.snapshot.remainingSeconds, 2)
        _ = engine.tick()
        XCTAssertEqual(engine.snapshot.remainingSeconds, 1)
    }

    func testTickAtOneAdvancesToNext() {
        let engine = PlayerEngine(stretches: [stretch("a", 1), stretch("b", 5)])
        let finished = engine.tick()
        XCTAssertFalse(finished)
        XCTAssertEqual(engine.snapshot.index, 1)
        XCTAssertEqual(engine.snapshot.remainingSeconds, 5)
    }

    func testTickPastLastFinishes() {
        let engine = PlayerEngine(stretches: [stretch("a", 1)])
        let finished = engine.tick()
        XCTAssertTrue(finished)
        XCTAssertTrue(engine.snapshot.finished)
        XCTAssertFalse(engine.snapshot.running)
    }

    func testTickWhenPausedIsNoop() {
        let engine = PlayerEngine(stretches: [stretch("a", 10)])
        engine.togglePlay()
        _ = engine.tick()
        _ = engine.tick()
        XCTAssertEqual(engine.snapshot.remainingSeconds, 10)
    }

    func testNextSkipsToFollowing() {
        let engine = PlayerEngine(stretches: [stretch("a", 30), stretch("b", 45)])
        _ = engine.next()
        XCTAssertEqual(engine.snapshot.index, 1)
        XCTAssertEqual(engine.snapshot.remainingSeconds, 45)
    }

    func testNextOnLastFinishes() {
        let engine = PlayerEngine(stretches: [stretch("a", 10)])
        let finished = engine.next()
        XCTAssertTrue(finished)
        XCTAssertTrue(engine.snapshot.finished)
    }

    func testPreviousAtZeroStays() {
        let engine = PlayerEngine(stretches: [stretch("a", 10), stretch("b", 20)])
        engine.previous()
        XCTAssertEqual(engine.snapshot.index, 0)
        XCTAssertEqual(engine.snapshot.remainingSeconds, 10)
    }

    func testPreviousResetsRemaining() {
        let engine = PlayerEngine(stretches: [stretch("a", 10), stretch("b", 20)])
        _ = engine.next()
        _ = engine.tick()
        engine.previous()
        XCTAssertEqual(engine.snapshot.index, 0)
        XCTAssertEqual(engine.snapshot.remainingSeconds, 10)
    }

    func testPreviousUnfinishesAfterFinish() {
        let engine = PlayerEngine(stretches: [stretch("a", 10)])
        _ = engine.next()
        engine.previous()
        XCTAssertFalse(engine.snapshot.finished)
        XCTAssertTrue(engine.snapshot.running)
    }

    func testTogglePlayFlipsRunning() {
        let engine = PlayerEngine(stretches: [stretch("a", 10)])
        XCTAssertTrue(engine.snapshot.running)
        engine.togglePlay()
        XCTAssertFalse(engine.snapshot.running)
        engine.togglePlay()
        XCTAssertTrue(engine.snapshot.running)
    }

    func testProgressGrows() {
        let engine = PlayerEngine(stretches: [stretch("a", 10)])
        XCTAssertEqual(engine.snapshot.progress, 0, accuracy: 1e-6)
        _ = engine.tick()
        XCTAssertEqual(engine.snapshot.progress, 0.1, accuracy: 1e-6)
        _ = engine.tick()
        XCTAssertEqual(engine.snapshot.progress, 0.2, accuracy: 1e-6)
    }

    func testProgressZeroDurationStretch() {
        let engine = PlayerEngine(stretches: [stretch("a", 0)])
        XCTAssertEqual(engine.snapshot.progress, 0)
    }

    func testTotalDurationSums() {
        let engine = PlayerEngine(stretches: [stretch("a", 10), stretch("b", 20), stretch("c", 30)])
        XCTAssertEqual(engine.totalDurationSeconds, 60)
    }

    func testNextAfterFinishedIsNoop() {
        let engine = PlayerEngine(stretches: [stretch("a", 10)])
        _ = engine.next()
        let before = engine.snapshot
        let again = engine.next()
        XCTAssertFalse(again)
        XCTAssertEqual(engine.snapshot, before)
    }

    func testFullWalkthroughFinishesAtTotalDuration() {
        let engine = PlayerEngine(stretches: [stretch("a", 2), stretch("b", 3)])
        var finishedOnLast = false
        for _ in 0..<5 { finishedOnLast = engine.tick() }
        XCTAssertTrue(finishedOnLast)
        XCTAssertTrue(engine.snapshot.finished)
    }

    func testFinishedEventFiresWithTotalDurationViaTick() {
        let engine = PlayerEngine(stretches: [stretch("a", 2), stretch("b", 3)])
        XCTAssertNil(engine.finishedEvent)
        for _ in 0..<5 { _ = engine.tick() }
        XCTAssertEqual(engine.finishedEvent?.totalDurationSeconds, 5)
    }

    func testFinishedEventFiresViaNext() {
        let engine = PlayerEngine(stretches: [stretch("a", 10)])
        XCTAssertNil(engine.finishedEvent)
        _ = engine.next()
        XCTAssertEqual(engine.finishedEvent?.totalDurationSeconds, 10)
    }

    func testStartIndexSeeksToGivenStretch() {
        let engine = PlayerEngine(
            stretches: [stretch("a", 10), stretch("b", 20), stretch("c", 30)],
            startIndex: 1
        )
        XCTAssertEqual(engine.snapshot.index, 1)
        XCTAssertEqual(engine.snapshot.remainingSeconds, 20)
        XCTAssertEqual(engine.snapshot.current?.id, "b")
    }

    func testStartIndexNegativeIsClampedToZero() {
        let engine = PlayerEngine(stretches: [stretch("a", 10)], startIndex: -3)
        XCTAssertEqual(engine.snapshot.index, 0)
    }

    func testStartIndexPastEndIsClamped() {
        let engine = PlayerEngine(
            stretches: [stretch("a", 10), stretch("b", 20)],
            startIndex: 99
        )
        XCTAssertEqual(engine.snapshot.index, 1)
    }

    func testRoutineProgressIsZeroAtFirstFrame() {
        let engine = PlayerEngine(stretches: [stretch("a", 10), stretch("b", 20)])
        XCTAssertEqual(engine.snapshot.routineProgress, 0, accuracy: 1e-6)
    }

    func testRoutineProgressAdvancesProportionallyWithinAStretch() {
        let engine = PlayerEngine(stretches: [stretch("a", 10), stretch("b", 30)])
        _ = engine.tick() // 1s of 40 total
        XCTAssertEqual(engine.snapshot.routineProgress, 1.0 / 40.0, accuracy: 1e-6)
    }

    func testRoutineProgressJumpsWhenAStretchCompletes() {
        let engine = PlayerEngine(stretches: [stretch("a", 10), stretch("b", 30)])
        for _ in 0..<10 { _ = engine.tick() } // finish first stretch
        // 10 of 40 seconds elapsed.
        XCTAssertEqual(engine.snapshot.routineProgress, 0.25, accuracy: 1e-6)
    }

    func testRoutineProgressIsOneWhenFinished() {
        let engine = PlayerEngine(stretches: [stretch("a", 2)])
        for _ in 0..<2 { _ = engine.tick() }
        XCTAssertTrue(engine.snapshot.finished)
        XCTAssertEqual(engine.snapshot.routineProgress, 1.0)
    }

    func testRoutineProgressZeroForZeroDurationStretches() {
        let engine = PlayerEngine(stretches: [stretch("a", 0), stretch("b", 0)])
        XCTAssertEqual(engine.snapshot.routineProgress, 0)
    }
}
