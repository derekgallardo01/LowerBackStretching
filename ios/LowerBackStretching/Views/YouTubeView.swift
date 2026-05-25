import SwiftUI
import WebKit

/// YouTube embed via WKWebView using the YT IFrame Player API.
///
/// Uses the official IFrame API JS (not a bare iframe) plus a real mobile
/// Safari User-Agent so YouTube's embedder verification — tightened in
/// July 2025 and the cause of "Error 152" failures on stock-WebView embeds —
/// treats the player as a normal embed.
struct YouTubeView: UIViewRepresentable {
    let videoId: String
    var autoplay: Bool = true
    var startSeconds: Int = 0

    private static let mobileSafariUA =
        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) " +
        "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 " +
        "Mobile/15E148 Safari/604.1"

    func makeUIView(context: Context) -> WKWebView {
        let config = WKWebViewConfiguration()
        config.allowsInlineMediaPlayback = true
        config.mediaTypesRequiringUserActionForPlayback = autoplay ? [] : .all

        let web = WKWebView(frame: .zero, configuration: config)
        web.scrollView.isScrollEnabled = false
        web.backgroundColor = .black
        // Default WKWebView UA trips YouTube's embedder verification (Error 152).
        web.customUserAgent = Self.mobileSafariUA
        return web
    }

    func updateUIView(_ web: WKWebView, context: Context) {
        let loadKey = "\(videoId)|\(startSeconds)|\(autoplay)"
        if context.coordinator.lastLoadKey == loadKey { return }
        context.coordinator.lastLoadKey = loadKey

        let auto = autoplay ? 1 : 0
        let html = """
        <!DOCTYPE html>
        <html><head>
          <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no">
          <style>
            html, body { margin:0; padding:0; height:100%; background:#000; overflow:hidden; }
            #player { width:100%; height:100%; }
          </style>
        </head><body>
          <div id="player"></div>
          <script src="https://www.youtube.com/iframe_api"></script>
          <script>
            function onYouTubeIframeAPIReady() {
              new YT.Player('player', {
                videoId: '\(videoId)',
                playerVars: {
                  playsinline: 1,
                  rel: 0,
                  modestbranding: 1,
                  autoplay: \(auto),
                  start: \(startSeconds),
                  enablejsapi: 1,
                  origin: 'https://www.youtube.com',
                  widget_referrer: 'https://www.youtube.com'
                }
              });
            }
          </script>
        </body></html>
        """
        web.loadHTMLString(html, baseURL: URL(string: "https://www.youtube.com"))
    }

    func makeCoordinator() -> Coordinator { Coordinator() }

    final class Coordinator {
        var lastLoadKey: String?
    }
}
