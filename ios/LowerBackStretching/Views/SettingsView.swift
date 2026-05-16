import SwiftUI

struct SettingsView: View {
    @AppStorage(ReminderDefaults.enabledKey) private var enabled: Bool = false
    @AppStorage(ReminderDefaults.hourKey) private var hour: Int = 8
    @AppStorage(ReminderDefaults.minuteKey) private var minute: Int = 0

    @State private var pickerDate: Date = .now

    var body: some View {
        Form {
            Section(header: Text("Daily reminder")) {
                Toggle("Enabled", isOn: $enabled)
                    .onChange(of: enabled) { _, on in
                        ReminderController.apply(enabled: on, hour: hour, minute: minute)
                    }

                DatePicker("Time", selection: $pickerDate, displayedComponents: .hourAndMinute)
                    .onChange(of: pickerDate) { _, value in
                        let comps = Calendar.current.dateComponents([.hour, .minute], from: value)
                        let h = comps.hour ?? 8
                        let m = comps.minute ?? 0
                        ReminderController.apply(enabled: enabled, hour: h, minute: m)
                    }
            }

            Section(header: Text("About")) {
                LabeledContent("Version", value: "0.1.0")
                LabeledContent("Made with", value: "SwiftUI")
            }
        }
        .navigationTitle("Settings")
        .onAppear { pickerDate = makeTime(hour: hour, minute: minute) }
    }

    private func makeTime(hour: Int, minute: Int) -> Date {
        let comps = DateComponents(hour: hour, minute: minute)
        return Calendar.current.date(from: comps) ?? .now
    }
}
