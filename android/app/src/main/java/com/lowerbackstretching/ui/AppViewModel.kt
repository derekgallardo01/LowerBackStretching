package com.lowerbackstretching.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.lowerbackstretching.App
import com.lowerbackstretching.data.ContentRepository
import com.lowerbackstretching.data.CustomRoutineRepository
import com.lowerbackstretching.data.FlexibilityRepository
import com.lowerbackstretching.data.Prefs
import com.lowerbackstretching.data.ProgramProgressRepository
import com.lowerbackstretching.data.SessionRepository
import com.lowerbackstretching.health.HealthController

/**
 * Single shared ViewModel that exposes the app's repositories to every
 * Compose screen. Screens use `vm: AppViewModel = viewModel()` and read
 * whichever repositories they need.
 */
class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val appCtx: App get() = getApplication()
    val content: ContentRepository get() = appCtx.contentRepository
    val sessions: SessionRepository get() = appCtx.sessionRepository
    val customRoutines: CustomRoutineRepository get() = appCtx.customRoutineRepository
    val programProgress: ProgramProgressRepository get() = appCtx.programProgressRepository
    val flexibility: FlexibilityRepository get() = appCtx.flexibilityRepository
    val health: HealthController get() = appCtx.health
    val prefs: Prefs get() = appCtx.prefs
}
