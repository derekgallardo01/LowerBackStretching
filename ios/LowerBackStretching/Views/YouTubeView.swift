import SwiftUI
import WebKit

/// YouTube iframe embed. No third-party SDK required.
/// If a video has embedding disabled, the iframe shows "video unavailable" —
/// curate different IDs.
struct YouTubeView: UIViewRepresentable {
    let videoId: String
    var autoplay: Bool = true

    func makeUIView(context: Context) -> WKWebView {
        let config = WKWebViewConfiguration()
        config.allowsInlineMediaPlayback = true
        config.mediaTypesRequiringUserActionForPlayback = autoplay ? [] : .all

        let web = WKWebView(frame: .zero, configuration: config)
        web.scrollView.isScrollEnabled = false
        web.backgroundColor = .black
        return web
    }

    func updateUIView(_ web: WKWebView, context: Context) {
        let auto = autoplay ? 1 : 0
        let html = """
        <!DOCTYPE html>
        <html><head>
          <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no">
          <style>
            html, body { margin:0; padding:0; height:100%; background:#000; }
            .wrap { position:relative; width:100%; height:100%; }
            iframe { position:absolute; top:0; left:0; width:100%; height:100%; border:0; }
          </style>
        </head><body>
          <div class="wrap">
            <iframe
              src="https://www.youtube.com/embed/\(videoId)?playsinline=1&rel=0&modestbranding=1&autoplay=\(auto)"
              allow="autoplay; encrypted-media; picture-in-picture"
              allowfullscreen></iframe>
          </div>
        </body></html>
        """
        web.loadHTMLString(html, baseURL: URL(string: "https://www.youtube.com"))
    }
}
