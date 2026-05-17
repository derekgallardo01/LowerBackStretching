import Foundation
import UserNotifications

/// Foreground notification gate. When the app is in the foreground and
/// the user has already stretched today, suppress the daily reminder
/// banner. Background delivery still occurs — iOS doesn't give us a
/// pre-delivery hook outside the foreground — but the user will still
/// see "you already did it" reflected in the app when they open it.
///
/// Installed as `UNUserNotificationCenter.current().delegate` from the
/// app's root scene.
final class NotificationDelegate: NSObject, UNUserNotificationCenterDelegate {

    static let shared = NotificationDelegate()
    private override init() {}

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        if notification.request.identifier == ReminderManager.identifier
            && alreadyStretchedToday()
        {
            completionHandler([])
            return
        }
        completionHandler([.banner, .sound, .badge])
    }

    private func alreadyStretchedToday() -> Bool {
        let last = UserDefaults.standard.integer(forKey: SettingsKeys.lastSessionEpochDay)
        return last == EpochDay.current()
    }
}
