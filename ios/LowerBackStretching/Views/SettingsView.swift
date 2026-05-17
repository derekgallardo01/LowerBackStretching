import SwiftUI

struct SettingsView: View {
    @AppStorage(SettingsKeys.reminderEnabled) private var enabled: Bool = false
    @AppStorage(SettingsKeys.reminderHour) private var hour: Int = ReminderDefaults.hour
    @AppStorage(SettingsKeys.reminderMinute) private var minute: Int = ReminderDefaults.minute
    @AppStorage(SettingsKeys.themeMode) private var themeModeRaw: String = ThemeMode.system.storageValue
    @AppStorage(SettingsKeys.durationUnit) private var durationUnitRaw: String = DurationUnit.seconds.storageValue
    @AppStorage(SettingsKeys.hapticsTransitions) private var hapticsTransitions: Bool = true
    @AppStorage(SettingsKeys.hapticsFinish) private var hapticsFinish: Bool = true

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

            Section(header: Text("Appearance")) {
                Picker("Theme", selection: Binding(
                    get: { ThemeMode.fromStorage(themeModeRaw) },
                    set: { themeModeRaw = $0.storageValue },
                )) {
                    ForEach(ThemeMode.allCases, id: \.self) { mode in
                        Text(mode.rawValue.capitalized).tag(mode)
                    }
                }
                .pickerStyle(.segmented)

                Picker("Duration display", selection: Binding(
                    get: { DurationUnit.fromStorage(durationUnitRaw) },
                    set: { durationUnitRaw = $0.storageValue },
                )) {
                    Text("Seconds").tag(DurationUnit.seconds)
                    Text("Minutes").tag(DurationUnit.minutesShort)
                }
                .pickerStyle(.segmented)
            }

            Section(header: Text("Haptics")) {
                Toggle("Stretch transitions", isOn: $hapticsTransitions)
                Toggle("Routine finish", isOn: $hapticsFinish)
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
