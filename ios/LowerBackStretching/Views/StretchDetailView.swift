import SwiftUI

struct StretchDetailView: View {
    let stretch: Stretch

    @AppStorage(SettingsKeys.durationUnit) private var durationUnitRaw: String = DurationUnit.seconds.storageValue
    private var unit: DurationUnit { DurationUnit.fromStorage(durationUnitRaw) }

    struct PracticeTarget: Hashable {
        let stretchId: String
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                YouTubeView(videoId: stretch.youtubeId, autoplay: false)
                    .aspectRatio(16.0 / 9.0, contentMode: .fit)
                    .clipShape(RoundedRectangle(cornerRadius: 12))

                HStack(spacing: 8) {
                    Text(formatDuration(stretch.durationSeconds, unit: unit))
                        .font(.caption.weight(.medium))
                        .foregroundStyle(.tint)
                    Text("·").font(.caption.weight(.medium))
                    DifficultyDots(difficulty: stretch.difficulty)
                }

                Text(BodyParts.displayList(stretch.bodyParts))
                    .font(.caption.weight(.medium))

                Text(stretch.description).font(.body)

                if let why = stretch.whyThisStretch {
                    WhyThisStretchCard(text: why)
                }

                if let cards = stretch.educationalCards, !cards.isEmpty {
                    SectionHeader("Learn more")
                    ForEach(cards, id: \.title) { card in
                        EducationalCardView(card: card)
                    }
                }

                if let mistakes = stretch.mistakesToAvoid, !mistakes.isEmpty {
                    SectionHeader("Mistakes to avoid")
                    MistakesCard(mistakes: mistakes)
                }

                NavigationLink(value: PracticeTarget(stretchId: stretch.id)) {
                    Label("Practice this stretch", systemImage: "play.fill")
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 10)
                        .background(RoundedRectangle(cornerRadius: 12).fill(Color.accentColor))
                        .foregroundStyle(.white)
                }
                .padding(.top, 8)
            }
            .padding(16)
        }
        .navigationTitle(stretch.name)
        .navigationBarTitleDisplayMode(.inline)
        .navigationDestination(for: PracticeTarget.self) { target in
            SinglePlayerView(stretchId: target.stretchId)
        }
    }
}

private struct WhyThisStretchCard: View {
    let text: String

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("Why this stretch").font(.subheadline.weight(.semibold))
            Text(text).font(.body)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(Color.accentColor.opacity(0.12), in: RoundedRectangle(cornerRadius: 16))
    }
}

private struct EducationalCardView: View {
    let card: EducationalCard

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(card.title).font(.headline)
            Text(card.body).font(.body)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(.thinMaterial, in: RoundedRectangle(cornerRadius: 14))
    }
}

private struct MistakesCard: View {
    let mistakes: [String]

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            ForEach(mistakes, id: \.self) { mistake in
                HStack(alignment: .top, spacing: 8) {
                    Text("•")
                    Text(mistake)
                }
                .font(.body)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(.thinMaterial, in: RoundedRectangle(cornerRadius: 14))
    }
}
