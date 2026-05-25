package com.lowerbackstretching.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Lightweight YouTube embed using a WebView + iframe API. Avoids the heavy
 * `YouTubeAndroidPlayerApi` dependency and works without Google Play Services.
 *
 * If the channel disables embedding, the iframe renders a "video unavailable"
 * message inside its frame - that's a content curation problem, not a code one.
 *
 * Shows a loading skeleton until the WebView signals onPageFinished, and
 * a friendly offline placeholder when the device has no internet so the
 * user still gets timer + description + body silhouette to follow.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubePlayerView(
    videoId: String,
    modifier: Modifier = Modifier,
    autoplay: Boolean = true,
    startSeconds: Int = 0,
) {
    val context = LocalContext.current
    val online = remember(videoId) { hasInternet(context) }
    var isLoading by remember(videoId) { mutableStateOf(true) }

    Box(modifier = modifier.aspectRatio(16f / 9f)) {
        if (videoId.isBlank()) {
            NoVideoOverlay()
            return@Box
        }
        if (online) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        webChromeClient = WebChromeClient()
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                            }
                        }
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
                    val startParam = if (startSeconds > 0) "&start=$startSeconds" else ""
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
                              src="https://www.youtube.com/embed/$videoId?playsinline=1&rel=0&modestbranding=1&autoplay=$auto$startParam"
                              allow="autoplay; encrypted-media; picture-in-picture"
                              allowfullscreen></iframe>
                          </div>
                        </body></html>
                    """.trimIndent()
                    web.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null)
                },
            )
            if (isLoading) {
                LoadingOverlay()
            }
        } else {
            OfflineOverlay()
        }
    }
}

@Composable
private fun LoadingOverlay() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(36.dp))
            Text(
                text = "Loading video…",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun NoVideoOverlay() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.SelfImprovement,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = "Follow the description and timer below.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun OfflineOverlay() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.SignalWifiOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = "No connection — follow the description and timer.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

private fun hasInternet(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return false
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
