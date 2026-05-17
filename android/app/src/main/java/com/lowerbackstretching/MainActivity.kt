package com.lowerbackstretching

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.lowerbackstretching.data.Prefs
import com.lowerbackstretching.data.ThemeMode
import com.lowerbackstretching.ui.nav.AppNav
import com.lowerbackstretching.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val prefs = remember { Prefs(this) }
            val themeMode by prefs.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            AppTheme(themeMode = themeMode) {
                AppNav()
            }
        }
    }
}
