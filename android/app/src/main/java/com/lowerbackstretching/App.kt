package com.lowerbackstretching

import android.app.Application
import com.lowerbackstretching.data.ContentRepository
import com.lowerbackstretching.data.CustomRoutineRepository
import com.lowerbackstretching.data.Prefs
import com.lowerbackstretching.data.ProgramProgressRepository
import com.lowerbackstretching.data.SessionRepository
import com.lowerbackstretching.data.db.AppDatabase
import com.lowerbackstretching.notifications.NotificationChannels

class App : Application() {

    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val contentRepository: ContentRepository by lazy { ContentRepository(this) }
    val sessionRepository: SessionRepository by lazy { SessionRepository(database.sessionDao()) }
    val customRoutineRepository: CustomRoutineRepository by lazy {
        CustomRoutineRepository(database.customRoutineDao())
    }
    val programProgressRepository: ProgramProgressRepository by lazy {
        ProgramProgressRepository(database.programProgressDao())
    }
    val prefs: Prefs by lazy { Prefs(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.registerAll(this)
    }
}
