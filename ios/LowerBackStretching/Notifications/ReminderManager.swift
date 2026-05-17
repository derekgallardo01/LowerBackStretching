import Foundation
import UserNotifications

enum ReminderManager {
    static let identifier = "daily_stretch_reminder"

    static func schedule(hour: Int, minute: Int) {
        cancel()

        let center = UNUserNotificationCenter.current()
        // Ask for permission lazily — only when the user actually
        // enables a reminder. If already granted/denied, this returns
        // immediately without showing a dialog.
        center.requestAuthorization(options: [.alert, .badge, .sound]) { _, _ in
            let content = UNMutableNotificationContent()
            content.title = "Time to stretch"
            content.body = "A few minutes today keeps your back happy."
            content.sound = .default

            var components = DateComponents()
            components.hour = hour
            components.minute = minute

            let trigger = UNCalendarNotificationTrigger(dateMatching: components, repeats: true)
            let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)

            center.add(request)
        }
    }

    static func cancel() {
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: [identifier])
    }

    /// Drops any reminder currently in the notification tray. Called
    /// after a session is recorded so the user isn't reminded of
    /// something they already did.
    static func clearDelivered() {
        UNUserNotificationCenter.current()
            .removeDeliveredNotifications(withIdentifiers: [identifier])
    }
}
