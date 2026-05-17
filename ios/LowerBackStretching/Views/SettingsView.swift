import SwiftUI

struct SettingsView: View {
    @AppStorage(SettingsKeys.reminderEnabled) private var enabled: Bool = false
    @AppStorage(SettingsKeys.reminderHour) private var hour: Int = ReminderDefaults.hour
    @AppStorage(SettingsKeys.reminderMinute) private var minute: Int = ReminderDefaults.minute
    @AppStorage(SettingsKeys.themeMode) private var themeModeRaw: String = ThemeMode.system.storageValue
    @AppStorage(SettingsKeys.durationUnit) private var durationUnitRaw: String = DurationUnit.seconds.storageValue
    @AppStorage(SettingsKeys.hapticsTransitions) private var hapticsTransitions: Bool = true
    @AppStorage(SettingsKeys.hapticsFinish) private var hapticsFinish: Bool = true
    @AppStorage(SettingsKeys.musicTrack) private var musicTrackRaw: String = MusicTrack.none.storageValue
    @AppStorage(SettingsKeys.musicVolume) private var musicVolume: Double = Double(AudioDefaults.musicVolume)
    @AppStorage(SettingsKeys.ambientTrack) private var ambientTrackRaw: String = AmbientTrack.none.storageValue
    @AppStorage(SettingsKeys.ambientVolume) private var ambientVolume: Double = Double(AudioDefaults.ambientVolume)
    @AppStorage(SettingsKeys.chimeTrack) private var chimeTrackRaw: String = ChimeTrack.none.storageValue

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

            Section(header: Text("Audio")) {
                Picker("Music", selection: Binding(
                    get: { MusicTrack.fromStorage(musicTrackRaw) },
                    set: { musicTrackRaw = $0.storageValue },
                )) {
                    ForEach(MusicTrack.allCases, id: \.self) { t in
                        Text(t.displayName).tag(t)
                    }
                }
                VStack(alignment: .leading) {
                    Text("Music volume")
                    Slider(value: $musicVolume, in: 0...1)
                }

                Picker("Ambient", selection: Binding(
                    get: { AmbientTrack.fromStorage(ambientTrackRaw) },
                    set: { ambientTrackRaw = $0.storageValue },
                )) {
                    ForEach(AmbientTrack.allCases, id: \.self) { t in
                        Text(t.displayName).tag(t)
                    }
                }
                VStack(alignment: .leading) {
                    Text("Ambient volume")
                    Slider(value: $ambientVolume, in: 0...1)
                }

                Picker("Chime on transition", selection: Binding(
                    get: { ChimeTrack.fromStorage(chimeTrackRaw) },
                    set: { chimeTrackRaw = $0.storageValue },
                )) {
                    ForEach(ChimeTrack.allCases, id: \.self) { t in
                        Text(t.displayName).tag(t)
                    }
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
