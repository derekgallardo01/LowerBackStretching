import SwiftUI

/// "Please see a doctor before stretching" advisory. Shown two ways:
///   - As an onboarding overlay after the user taps "One or more applies"
///     on the SafetyCheckPage (passes `onSeenDoctor` / `onContinueAnyway`
///     to advance the pager).
///   - As a standalone screen from Settings → Safety check (both buttons
///     just pop the stack).
///
/// Mirrors Android's `RedFlagAdvisoryScreen`.
struct SafetyAdvisoryView: View {
    var onSeenDoctor: () -> Void = {}
    var onContinueAnyway: () -> Void = {}

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                ZStack {
                    Circle()
                        .fill(Color.red.opacity(0.15))
                        .frame(width: 96, height: 96)
                    Image(systemName: "cross.case.fill")
                        .font(.system(size: 44))
                        .foregroundStyle(.red)
                }
                .padding(.top, 16)

                Text("Please see a doctor before stretching.")
                    .font(.title2.weight(.semibold))
                    .multilineTextAlignment(.center)

                Text("What you described could be a sign of something that needs medical attention. Stretching can help most back pain, but symptoms like numbness, leg pain, or loss of control should be evaluated first.")
                    .font(.body)
                    .multilineTextAlignment(.center)

                Text("This isn't a diagnosis — just a reminder. If you've already been cleared by a clinician, you can continue.")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)

                VStack(spacing: 12) {
                    Button {
                        onSeenDoctor()
                    } label: {
                        Text("I've already seen a doctor")
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 8)
                    }
                    .buttonStyle(.borderedProminent)
                    .accessibilityIdentifier("safetySeenDoctor")

                    Button {
                        onContinueAnyway()
                    } label: {
                        Text("Continue anyway")
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 8)
                    }
                    .buttonStyle(.bordered)
                    .accessibilityIdentifier("safetyContinueAnyway")
                }
                .padding(.top, 8)
            }
            .padding(24)
        }
        .navigationTitle("Safety check")
        .navigationBarTitleDisplayMode(.inline)
    }
}

/// First-page onboarding spread that lists the red flags as bullets and
/// hands off to the advisory screen if the user taps "One or more
/// applies". The "None of these apply" button is the normal "Next"
/// button on the onboarding pager — the parent relabels it.
struct SafetyCheckPage: View {
    var onOneApplies: () -> Void

    var body: some View {
        VStack(spacing: 20) {
            ZStack {
                Circle()
                    .fill(Color.accentColor.opacity(0.18))
                    .frame(width: 96, height: 96)
                Image(systemName: "cross.case.fill")
                    .font(.system(size: 44))
                    .foregroundStyle(.tint)
            }

            Text("Before we start — a quick safety check")
                .font(.title2.weight(.semibold))
                .multilineTextAlignment(.center)

            Text("Stretching helps most back pain, but some symptoms need a doctor's eyes first.")
                .font(.body)
                .multilineTextAlignment(.center)
                .foregroundStyle(.secondary)

            VStack(alignment: .leading, spacing: 10) {
                ForEach(RedFlagCatalog.all) { flag in
                    HStack(alignment: .top, spacing: 10) {
                        Image(systemName: "circle.fill")
                            .font(.system(size: 6))
                            .padding(.top, 7)
                            .foregroundStyle(.tint)
                        Text(flag.text).font(.body)
                    }
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(16)
            .background(.thinMaterial, in: RoundedRectangle(cornerRadius: 16))

            Button("One or more applies", action: onOneApplies)
                .font(.body.weight(.medium))
                .accessibilityIdentifier("safetyOneApplies")
        }
        .padding(.horizontal, 24)
    }
}
