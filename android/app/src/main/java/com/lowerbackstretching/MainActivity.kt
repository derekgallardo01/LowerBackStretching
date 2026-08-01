package com.lowerbackstretching

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lowerbackstretching.core.SharedRoutine
import com.lowerbackstretching.core.ThemeMode
import com.lowerbackstretching.core.parseRoutineLink
import com.lowerbackstretching.data.Prefs
import com.lowerbackstretching.ui.nav.AppNav
import com.lowerbackstretching.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val pendingImport = MutableStateFlow<SharedRoutine?>(null)
    private val pipHost = PictureInPictureHost()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        consumeDeepLink(intent)

        // Tell the system whether auto-PiP is armed, tracking whether a player
        // screen is currently composed.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                pipHost.pipEligible.collect { updatePipParams() }
            }
        }

        setContent {
            val prefs = remember { Prefs(this) }
            val themeMode by prefs.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val import by pendingImport.collectAsState()
            AppTheme(themeMode = themeMode) {
                CompositionLocalProvider(LocalPictureInPictureHost provides pipHost) {
                    AppNav(
                        pendingImport = import,
                        onConsumeImport = { pendingImport.value = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeDeepLink(intent)
    }

    /** Pulls the routine link out of [intent.data] if it's one of ours. */
    private fun consumeDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        val parsed = parseRoutineLink(data.toString()) ?: return
        pendingImport.value = parsed
    }

    /**
     * Called when the user presses Home / Recents while the activity is still in
     * the foreground. When the player is on screen we enter Picture-in-Picture
     * instead of stopping playback.
     *
     * On Android 12+ the entry is handled declaratively by
     * `setAutoEnterEnabled(true)` in [updatePipParams] — the system animates the
     * transition itself, which looks materially better than calling
     * `enterPictureInPictureMode` from here. This path remains for API 26–30.
     */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return
        if (!pipHost.pipEligible.value) return
        enterPictureInPictureMode(buildPipParams())
    }

    /**
     * Keep the system's PiP params in sync with whether the player is on screen.
     *
     * `setAutoEnterEnabled` (API 12L+) lets the system start the PiP animation as
     * the user swipes away, rather than after the gesture completes.
     * `setSourceRectHint` tells it which part of the window becomes the PiP
     * window, so the content morphs instead of cross-fading.
     */
    private fun updatePipParams() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        setPictureInPictureParams(buildPipParams())
    }

    private fun buildPipParams(): PictureInPictureParams {
        val builder = PictureInPictureParams
            .Builder()
            .setAspectRatio(Rational(16, 9))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setAutoEnterEnabled(pipHost.pipEligible.value)
            sourceRectHint()?.let { builder.setSourceRectHint(it) }
        }
        return builder.build()
    }

    /** Bounds of the content view, used as the PiP morph source. */
    private fun sourceRectHint(): Rect? {
        val content = findViewById<View>(android.R.id.content) ?: return null
        if (content.width == 0 || content.height == 0) return null
        val location = IntArray(2)
        content.getLocationInWindow(location)
        return Rect(
            location[0],
            location[1],
            location[0] + content.width,
            location[1] + content.height,
        )
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        pipHost.setInPip(isInPictureInPictureMode)
    }
}
