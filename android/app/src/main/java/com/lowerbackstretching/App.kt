package com.lowerbackstretching

import android.app.Application
import com.lowerbackstretching.data.ContentRepository
import com.lowerbackstretching.data.CustomRoutineRepository
import com.lowerbackstretching.data.FlexibilityRepository
import com.lowerbackstretching.data.PainLogRepository
import com.lowerbackstretching.data.Prefs
import com.lowerbackstretching.data.ProgramProgressRepository
import com.lowerbackstretching.data.SessionRepository
import com.lowerbackstretching.data.db.AppDatabase
import com.lowerbackstretching.health.HealthController
import com.lowerbackstretching.notifications.NotificationChannels
import com.lowerbackstretching.sync.NoopSyncBackend
import com.lowerbackstretching.sync.SyncBackend
import com.lowerbackstretching.sync.SyncController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

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
    val flexibilityRepository: FlexibilityRepository by lazy {
        FlexibilityRepository(database.flexibilityTestDao())
    }
    val painLogRepository: PainLogRepository by lazy {
        PainLogRepository(database.painLogDao())
    }
    val health: HealthController by lazy { HealthController(this) }
    val prefs: Prefs by lazy { Prefs(this) }

    /** Swap to a real implementation (FirebaseSyncBackend, etc.) when ready. */
    val syncBackend: SyncBackend by lazy { NoopSyncBackend() }
    val sync: SyncController by lazy { SyncController(syncBackend, prefs) }
    val wearSync: com.lowerbackstretching.sync.WearDataSyncManager by lazy {
        com.lowerbackstretching.sync.WearDataSyncManager(this, customRoutineRepository, contentRepository)
    }

    /** Scope for app-lifetime background work. Never cancelled — the process outlives it. */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.registerAll(this)

        // Warm the content cache off the main thread. ContentRepository parses
        // stretches.json / programs.json / glossary.json in `by lazy` blocks, and
        // the first touch used to come from composition (Home's program list),
        // putting an assets read + JSON decode on the first frame. stretches.json
        // carries ~2,600 lines of animation keyframes, so that is not cheap.
        //
        // `by lazy` defaults to LazyThreadSafetyMode.SYNCHRONIZED, so if the UI
        // does get there first it simply waits for this to finish rather than
        // parsing twice.
        appScope.launch {
            contentRepository.stretches
            contentRepository.programs
            contentRepository.glossary
        }
    }
}
