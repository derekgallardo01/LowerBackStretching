import Foundation
import UserNotifications

enum ReminderManager {
    private static let identifier = "daily_stretch_reminder"

    static func schedule(hour: Int, minute: Int) {
        cancel()

        let content = UNMutableNotificationContent()
        content.title = "Time to stretch"
        content.body = "A few minutes today keeps your back happy."
        content.sound = .default

        var components = DateComponents()
        components.hour = hour
        components.minute = minute

        let trigger = UNCalendarNotificationTrigger(dateMatching: components, repeats: true)
        let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)

        UNUserNotificationCenter.current().add(request)
    }

    static func cancel() {
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: [identifier])
    }
}
