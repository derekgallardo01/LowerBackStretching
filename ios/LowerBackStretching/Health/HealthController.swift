import Foundation
import HealthKit

/// Thin wrapper over `HKHealthStore`. Every entry point is safe to call
/// regardless of whether HealthKit is available — failures are caught
/// and turned into `false` / `nil`.
///
/// Cross-platform parity for `HealthController.kt` on Android.
///
/// Wiring required outside the code: HealthKit capability on the app
/// target, plus `NSHealthShareUsageDescription` and
/// `NSHealthUpdateUsageDescription` strings in Info.plist. See
/// `ios/README.md` for the exact entries.
final class HealthController {

    static let shared = HealthController()
    private init() {}

    private let store = HKHealthStore()

    private let shareTypes: Set<HKSampleType> = [
        .workoutType(),
    ]
    private let readTypes: Set<HKObjectType> = [
        HKObjectType.quantityType(forIdentifier: .stepCount)!,
    ]

    var isAvailable: Bool { HKHealthStore.isHealthDataAvailable() }

    /// Asks the user for both write (workouts) and read (steps) access.
    /// Calls back with `true` if HealthKit is available and the
    /// authorization request didn't error out — the user may still have
    /// denied individual scopes; consumers check write/read status
    /// before actually using the API.
    func requestAuthorization(_ completion: @escaping (Bool) -> Void) {
        guard isAvailable else { completion(false); return }
        store.requestAuthorization(toShare: shareTypes, read: readTypes) { granted, _ in
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

    /// Sum of step counts recorded between local midnight (today) and
    /// now. Returns nil if HealthKit is unavailable, the permission is
    /// missing, or no steps have been recorded yet.
    func readStepsToday(completion: @escaping (Int?) -> Void) {
        guard isAvailable, let stepsType = HKObjectType.quantityType(forIdentifier: .stepCount) else {
            completion(nil); return
        }
        let cal = Calendar.current
        let start = cal.startOfDay(for: .now)
        let predicate = HKQuery.predicateForSamples(withStart: start, end: .now)
        let query = HKStatisticsQuery(
            quantityType: stepsType,
            quantitySamplePredicate: predicate,
            options: .cumulativeSum
        ) { _, result, _ in
            let sum = result?.sumQuantity()?.doubleValue(for: .count())
            DispatchQueue.main.async { completion(sum.map { Int($0) }) }
        }
        store.execute(query)
    }
}
