package com.lowerbackstretching

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.lowerbackstretching.data.Prefs
import com.lowerbackstretching.core.ThemeMode
import com.lowerbackstretching.share.SharedRoutine
import com.lowerbackstretching.share.parseRoutineLink
import com.lowerbackstretching.ui.nav.AppNav
import com.lowerbackstretching.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private val pendingImport = MutableStateFlow<SharedRoutine?>(null)
    private val pipHost = PictureInPictureHost()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        consumeDeepLink(intent)

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
     * Called when the user presses Home / Recents while the activity
     * is still in the foreground. When the player is on screen we
     * enter Picture-in-Picture instead of stopping playback.
     */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        if (!pipHost.pipEligible.value) return
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .build()
        enterPictureInPictureMode(params)
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        pipHost.setInPip(isInPictureInPictureMode)
    }
}
