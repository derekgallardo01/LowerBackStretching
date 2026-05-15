import SwiftUI

struct SettingsView: View {
    @AppStorage(ReminderDefaults.enabledKey) private var enabled: Bool = false
    @AppStorage(ReminderDefaults.hourKey) private var hour: Int = 8
    @AppStorage(ReminderDefaults.minuteKey) private var minute: Int = 0

    @State private var pickerDate: Date = {
        var c = DateComponents(); c.hour = 8; c.minute = 0
        return Calendar.current.date(from: c) ?? .now
    }()

    var body: some View {
        Form {
            Section(header: Text("Daily reminder")) {
                Toggle("Enabled", isOn: $enabled)
                    .onChange(of: enabled) { _, on in applyReminder(on: on) }

                DatePicker("Time", selection: $pickerDate, displayedComponents: .hourAndMinute)
                    .onChange(of: pickerDate) { _, value in
                        let comps = Calendar.current.dateComponents([.hour, .minute], from: value)
                        hour = comps.hour ?? 8
                        minute = comps.minute ?? 0
                        applyReminder(on: enabled)
                    }
            }

            Section(header: Text("About")) {
                LabeledContent("Version", value: "0.1.0")
                LabeledContent("Made with", value: "SwiftUI")
            }
        }
        .navigationTitle("Settings")
        .onAppear { syncPickerFromStorage() }
    }

    private func syncPickerFromStorage() {
        var comps = DateComponents()
        comps.hour = hour
        comps.minute = minute
        if let date = Calendar.current.date(from: comps) {
            pickerDate = date
        }
    }

    private func applyReminder(on: Bool) {
        if on {
            ReminderManager.schedule(hour: hour, minute: minute)
        } else {
            ReminderManager.cancel()
        }
    }
}
