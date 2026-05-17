import WatchKit

/// Tiny on-wrist nudges. The watch uses these instead of audio (the
/// speaker isn't reliable on every device) so the wearer feels the
/// transition between stretches even when their wrist is down.
enum WatchHaptics {
    static func short() {
        WKInterfaceDevice.current().play(.click)
    }

    static func finish() {
        WKInterfaceDevice.current().play(.success)
    }
}
