package com.lowerbackstretching.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Lightweight YouTube embed using a WebView + iframe API. Avoids the heavy
 * `YouTubeAndroidPlayerApi` dependency and works without Google Play Services.
 *
 * If the channel disables embedding, the iframe renders a "video unavailable"
 * message - that's a content curation problem, not a code one.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubePlayerView(
    videoId: String,
    modifier: Modifier = Modifier,
    autoplay: Boolean = true,
) {
    AndroidView(
        modifier = modifier.aspectRatio(16f / 9f),
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                webChromeClient = WebChromeClient()
                settings.apply {
                    javaScriptEnabled = true
                    mediaPlaybackRequiresUserGesture = !autoplay
                    domStorageEnabled = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                }
            }
        },
        update = { web ->
            val auto = if (autoplay) 1 else 0
            val html = """
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
                      src="https://www.youtube.com/embed/$videoId?playsinline=1&rel=0&modestbranding=1&autoplay=$auto"
                      allow="autoplay; encrypted-media; picture-in-picture"
                      allowfullscreen></iframe>
                  </div>
                </body></html>
            """.trimIndent()
            web.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null)
        },
    )
}
