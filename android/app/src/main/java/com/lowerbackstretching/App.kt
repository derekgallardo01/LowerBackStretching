package com.lowerbackstretching

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.lowerbackstretching.data.ContentRepository
import com.lowerbackstretching.data.CustomRoutineRepository
import com.lowerbackstretching.data.SessionRepository
import com.lowerbackstretching.data.db.AppDatabase

class App : Application() {

    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val contentRepository: ContentRepository by lazy { ContentRepository(this) }
    val sessionRepository: SessionRepository by lazy { SessionRepository(database.sessionDao()) }
    val customRoutineRepository: CustomRoutineRepository by lazy {
        CustomRoutineRepository(database.customRoutineDao())
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Stretching reminders",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Daily nudges to do your stretching routine"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    companion object {
        const val REMINDER_CHANNEL_ID = "stretch_reminders"
    }
}
