import SwiftUI
import SwiftData

/// History of pain ratings, organized as:
///   1. Latest rating card (newest entry, always shown if non-empty)
///   2. Session deltas — pre/post pairs within the lookback window
///   3. All ratings — every entry, newest first
///
/// Mirrors the Android `PainHistoryScreen`.
struct PainHistoryView: View {
    @Query(sort: \PainLog.recordedAt, order: .reverse) private var logs: [PainLog]

    private var pairs: [SessionPainPair] {
        pairSessionPainLogs(logs.map { $0 as PainMeasurement })
    }

    var body: some View {
        Group {
            if logs.isEmpty {
                EmptyPainState()
            } else {
                ScrollView {
                    VStack(alignment: .leading, spacing: 12) {
                        SectionHeader("Latest").padding(.top, 4)
                        LatestPainCard(latest: logs.first!)

                        if !pairs.isEmpty {
                            SectionHeader("Session deltas").padding(.top, 8)
                            ForEach(pairs.indices, id: \.self) { idx in
                                let pair = pairs[idx]
                                SessionDeltaRow(
                                    delta: sessionPainDelta(pair),
                                    recordedAt: pair.post.recordedAt,
                                    locationTag: pair.post.bodyLocationTag ?? pair.pre?.bodyLocationTag
                                )
                            }
                        }

                        if logs.count > 1 {
                            SectionHeader("All ratings").padding(.top, 8)
                            ForEach(logs.dropFirst(), id: \.persistentModelID) { entry in
                                PainHistoryRow(entry: entry)
                            }
                        }
                    }
                    .padding(16)
                }
            }
        }
        .navigationTitle("Pain log")
        .navigationBarTitleDisplayMode(.inline)
    }
}

private struct EmptyPainState: View {
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "heart.fill")
                .font(.system(size: 40))
                .foregroundStyle(.tint)
            Text("No ratings yet").font(.title3.weight(.semibold))
            Text("Your next session will ask you how things feel.")
                .font(.body)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
        }
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

private struct LatestPainCard: View {
    let latest: PainLog
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(formatDate(latest.recordedAt))
                .font(.subheadline.weight(.semibold))
            HStack {
                Text(contextLabel(latest.context))
                    .font(.body)
                Spacer()
                Text("\(latest.painLevel)/10")
                    .font(.title2.weight(.semibold))
                    .foregroundStyle(.tint)
            }
            if let tag = latest.bodyLocationTag {
                Text("Where: \(displayName(forTag: tag))")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(.thinMaterial, in: RoundedRectangle(cornerRadius: 16))
    }
}

private struct SessionDeltaRow: View {
    let delta: SessionPainDelta
    let recordedAt: Date
    let locationTag: String?

    private var deltaText: String {
        let pre = delta.pre.map(String.init) ?? "?"
        let post = String(delta.post)
        guard let d = delta.delta else { return "\(pre) → \(post)" }
        let sign = d > 0 ? "+" : ""
        return "\(pre) → \(post)   (\(sign)\(d))"
    }

    private var improvement: Bool { (delta.delta ?? 0) < 0 }

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(formatDate(recordedAt))
                .font(.caption.weight(.semibold))
            HStack {
                Text(locationTag.map(displayName(forTag:)) ?? "—")
                    .font(.body)
                Spacer()
                Text(deltaText)
                    .font(.body.monospacedDigit())
                    .foregroundStyle(improvement ? Color.accentColor : Color.primary)
            }
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(.thinMaterial, in: RoundedRectangle(cornerRadius: 12))
    }
}

private struct PainHistoryRow: View {
    let entry: PainLog

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(formatDate(entry.recordedAt))
                .font(.caption.weight(.semibold))
            HStack {
                Text(rowSubtitle(entry))
                    .font(.caption)
                    .foregroundStyle(.secondary)
                Spacer()
                Text("\(entry.painLevel)/10")
                    .font(.body)
            }
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(.thinMaterial, in: RoundedRectangle(cornerRadius: 12))
    }

    private func rowSubtitle(_ e: PainLog) -> String {
        var parts: [String] = [contextLabel(e.context)]
        if let tag = e.bodyLocationTag { parts.append(displayName(forTag: tag)) }
        return parts.joined(separator: " · ")
    }
}

private func contextLabel(_ raw: String) -> String {
    switch raw {
    case PainContext.preSession: return "Before session"
    case PainContext.postSession: return "After session"
    default: return raw
    }
}

private func displayName(forTag tag: String) -> String {
    if let zone = BodyZone.allCases.first(where: { $0.bodyPartTag == tag }) {
        return zone.displayName
    }
    return tag.replacingOccurrences(of: "-", with: " ")
}

private func formatDate(_ date: Date) -> String {
    let f = DateFormatter()
    f.dateStyle = .medium
    f.timeStyle = .short
    return f.string(from: date)
}
