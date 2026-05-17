import UIKit

/// Thin wrapper around UIKit haptic generators. Gated by the caller —
/// pass the user's preference for the relevant event before calling.
enum Haptics {

    static func short() {
        let generator = UIImpactFeedbackGenerator(style: .light)
        generator.impactOccurred()
    }

    static func finish() {
        let generator = UINotificationFeedbackGenerator()
        generator.notificationOccurred(.success)
    }
}
