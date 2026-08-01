import Foundation
import HealthKit

/// Thin wrapper over `HKHealthStore`. Every entry point is safe to call
/// regardless of whether HealthKit is available — failures are caught
/// and turned into `false` / `nil`.
///
/// Cross-platform parity for `HealthController.kt` on Android.
///
/// Write-only: the app shares completed stretching workouts and reads
/// nothing back. This mirrors the Android side, which dropped its
/// `READ_STEPS` permission for Google Play's Minimum Scope policy; keeping
/// the two platforms aligned means the shared privacy policy stays true of
/// both. See `README.md` under "Health Connect & platform notes".
///
/// Wiring required outside the code: HealthKit capability on the app
/// target, plus an `NSHealthUpdateUsageDescription` string in Info.plist
/// (no `NSHealthShareUsageDescription` — nothing is read). See
/// `ios/README.md` for the exact entries.
final class HealthController {

    static let shared = HealthController()
    private init() {}

    private let store = HKHealthStore()

    private let shareTypes: Set<HKSampleType> = [
        .workoutType(),
    ]

    var isAvailable: Bool { HKHealthStore.isHealthDataAvailable() }

    /// Asks the user for write (workout) access. Calls back with `true` if
    /// HealthKit is available and the authorization request didn't error
    /// out — the user may still have denied the scope, so consumers check
    /// `canWriteWorkouts` before actually writing.
    func requestAuthorization(_ completion: @escaping (Bool) -> Void) {
        guard isAvailable else { completion(false); return }
        store.requestAuthorization(toShare: shareTypes, read: []) { granted, _ in
            DispatchQueue.main.async { completion(granted) }
        }
    }

    var canWriteWorkouts: Bool {
        isAvailable && store.authorizationStatus(for: .workoutType()) == .sharingAuthorized
    }

    /// Write a stretching workout. Returns true on success; false if
    /// HealthKit is unavailable, the permission is missing, or the
    /// write fails.
    func writeStretchingWorkout(start: Date, end: Date, completion: @escaping (Bool) -> Void) {
        guard canWriteWorkouts, end > start else { completion(false); return }

        let config = HKWorkoutConfiguration()
        config.activityType = .flexibility

        let builder = HKWorkoutBuilder(healthStore: store, configuration: config, device: .local())
        builder.beginCollection(withStart: start) { ok, _ in
            guard ok else { DispatchQueue.main.async { completion(false) }; return }
            builder.endCollection(withEnd: end) { ok, _ in
                guard ok else { DispatchQueue.main.async { completion(false) }; return }
                builder.finishWorkout { workout, _ in
                    DispatchQueue.main.async { completion(workout != nil) }
                }
            }
        }
    }
}
