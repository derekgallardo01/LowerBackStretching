import SwiftUI
import AVKit

/// A muted, looping video player for stretch demonstrations.
///
/// Uses `AVPlayer` behind `VideoPlayer`. The video plays
/// automatically, loops forever, and has no audio. The view
/// handles lifecycle correctly: the player is released when the
/// view leaves the hierarchy.
struct StretchVideoPlayer: View {
    let videoUrl: String

    @State private var player: AVPlayer?

    var body: some View {
        VideoPlayer(player: player)
            .onAppear {
                guard let url = URL(string: videoUrl) else { return }
                let p = AVPlayer(url: url)
                p.isMuted = true
                p.play()
                // Loop the video when it reaches the end.
                NotificationCenter.default.addObserver(
                    forName: .AVPlayerItemDidPlayToEndTime,
                    object: p.currentItem,
                    queue: .main
                ) { _ in
                    p.seek(to: .zero)
                    p.play()
                }
                self.player = p
            }
            .onDisappear {
                player?.pause()
                player = nil
            }
    }
}
