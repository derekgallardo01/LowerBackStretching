import SwiftUI

struct StretchDetailView: View {
    let stretch: Stretch

    struct PracticeTarget: Hashable {
        let stretchId: String
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                YouTubeView(videoId: stretch.youtubeId, autoplay: false)
                    .aspectRatio(16.0 / 9.0, contentMode: .fit)
                    .clipShape(RoundedRectangle(cornerRadius: 12))

                Text("\(stretch.durationSeconds)s · \(stretch.difficulty)")
                    .font(.caption.weight(.medium))
                    .foregroundStyle(.tint)

                Text(BodyParts.displayList(stretch.bodyParts))
                    .font(.caption.weight(.medium))

                Text(stretch.description).font(.body)

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
