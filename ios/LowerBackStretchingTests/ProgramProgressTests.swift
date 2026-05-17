import XCTest
import SwiftData
@testable import LowerBackStretching

final class ProgramProgressTests: XCTestCase {

    private var container: ModelContainer!
    private var context: ModelContext!

    override func setUp() {
        super.setUp()
        let config = ModelConfiguration("test", isStoredInMemoryOnly: true)
        container = try! ModelContainer(for: ProgramProgress.self, configurations: config)
        context = ModelContext(container)
    }

    func testCurrentDayDefaultsToOneWhenNoRecord() {
        XCTAssertEqual(ProgramProgressService.currentDay(for: "p1", in: []), 1)
    }

    func testAdvanceWritesNextDayOnePastCompletedDay() {
        ProgramProgressService.advance(programId: "p1", completedDay: 3, totalDays: 7, in: context)
        let stored = try? context.fetch(FetchDescriptor<ProgramProgress>())
        XCTAssertEqual(stored?.first?.currentDay, 4)
    }

    func testAdvanceCapsAtTotalDaysPlusOne() {
        ProgramProgressService.advance(programId: "p1", completedDay: 7, totalDays: 7, in: context)
        ProgramProgressService.advance(programId: "p1", completedDay: 99, totalDays: 7, in: context)
        let stored = try? context.fetch(FetchDescriptor<ProgramProgress>())
        XCTAssertEqual(stored?.first?.currentDay, 8)
    }

    func testAdvanceIgnoresSyntheticSingleIds() {
        ProgramProgressService.advance(
            programId: SyntheticProgramId.single("cat-cow"),
            completedDay: 1, totalDays: 1, in: context
        )
        let stored = try? context.fetch(FetchDescriptor<ProgramProgress>())
        XCTAssertEqual(stored?.count, 0)
    }

    func testAdvanceIgnoresSyntheticRoutineIds() {
        ProgramProgressService.advance(
            programId: SyntheticProgramId.routine(UUID()),
            completedDay: 1, totalDays: 1, in: context
        )
        let stored = try? context.fetch(FetchDescriptor<ProgramProgress>())
        XCTAssertEqual(stored?.count, 0)
    }

    func testResetDeletesTheRow() {
        ProgramProgressService.advance(programId: "p1", completedDay: 2, totalDays: 7, in: context)
        ProgramProgressService.reset(programId: "p1", in: context)
        let stored = try? context.fetch(FetchDescriptor<ProgramProgress>())
        XCTAssertEqual(stored?.count, 0)
    }

    func testAdvanceOverwritesExistingRecord() {
        ProgramProgressService.advance(programId: "p1", completedDay: 2, totalDays: 7, in: context)
        ProgramProgressService.advance(programId: "p1", completedDay: 5, totalDays: 7, in: context)
        let stored = try? context.fetch(FetchDescriptor<ProgramProgress>())
        XCTAssertEqual(stored?.count, 1)
        XCTAssertEqual(stored?.first?.currentDay, 6)
    }
}
