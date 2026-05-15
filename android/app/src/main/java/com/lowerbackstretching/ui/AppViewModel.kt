package com.lowerbackstretching.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.lowerbackstretching.App
import com.lowerbackstretching.data.ContentRepository
import com.lowerbackstretching.data.Prefs
import com.lowerbackstretching.data.SessionRepository

open class AppViewModel(app: Application) : AndroidViewModel(app) {
    protected val appCtx: App get() = getApplication()
    val content: ContentRepository get() = appCtx.contentRepository
    val sessions: SessionRepository get() = appCtx.sessionRepository
    val prefs: Prefs by lazy { Prefs(appCtx) }
}
