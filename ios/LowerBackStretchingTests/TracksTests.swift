import XCTest
@testable import LowerBackStretching

final class TracksTests: XCTestCase {

    func testMusicTrackRoundTripAllCases() {
        for track in MusicTrack.allCases {
            XCTAssertEqual(MusicTrack.fromStorage(track.storageValue), track)
        }
    }

    func testMusicTrackUnknownFallsBackToNone() {
        XCTAssertEqual(MusicTrack.fromStorage(nil), .none)
        XCTAssertEqual(MusicTrack.fromStorage("invalid"), .none)
    }

    func testAmbientTrackRoundTripAllCases() {
        for track in AmbientTrack.allCases {
            XCTAssertEqual(AmbientTrack.fromStorage(track.storageValue), track)
        }
    }

    func testChimeTrackRoundTripAllCases() {
        for track in ChimeTrack.allCases {
            XCTAssertEqual(ChimeTrack.fromStorage(track.storageValue), track)
        }
    }

    func testNoneVariantsHaveNoResourceName() {
        XCTAssertNil(MusicTrack.none.resourceName)
        XCTAssertNil(AmbientTrack.none.resourceName)
        XCTAssertNil(ChimeTrack.none.resourceName)
    }

    func testNonNoneTracksHaveResourceName() {
        for track in MusicTrack.allCases where track != .none {
            XCTAssertNotNil(track.resourceName)
        }
        for track in AmbientTrack.allCases where track != .none {
            XCTAssertNotNil(track.resourceName)
        }
        for track in ChimeTrack.allCases where track != .none {
            XCTAssertNotNil(track.resourceName)
        }
    }
}
