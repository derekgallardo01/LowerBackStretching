package com.lowerbackstretching.sync

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.lowerbackstretching.core.model.WatchRoutine
import com.lowerbackstretching.core.model.toWatchStretch
import com.lowerbackstretching.data.ContentRepository
import com.lowerbackstretching.data.CustomRoutineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Manages synchronizing custom routines and programs from the phone app
 * to paired Wear OS devices using the Wearable Data Layer API.
 */
class WearDataSyncManager(
    private val context: Context,
    private val customRoutineRepository: CustomRoutineRepository,
    private val contentRepository: ContentRepository,
) {
    private val dataClient by lazy { Wearable.getDataClient(context) }
    private val nodeClient by lazy { Wearable.getNodeClient(context) }

    /** Returns true if at least one paired Wear OS watch is reachable. */
    suspend fun hasConnectedWearNodes(): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                val nodes = Tasks.await(nodeClient.connectedNodes)
                nodes.isNotEmpty()
            }.getOrDefault(false)
        }

    /**
     * Packages all active custom routines and publishes them as a DataItem
     * at `/synced_routines`.
     *
     * @return Result with the number of synchronized routines.
     */
    suspend fun syncAllCustomRoutines(): Result<Int> =
        withContext(Dispatchers.IO) {
            runCatching {
                val entities = customRoutineRepository.all().first()
                val watchRoutines = entities.map { entity ->
                    val stretches = entity.stretchIds.mapNotNull { stretchId ->
                        contentRepository.stretch(stretchId)?.toWatchStretch()
                    }
                    WatchRoutine(
                        id = entity.id.toString(),
                        name = entity.name,
                        stretches = stretches,
                    )
                }

                val jsonString = WatchRoutine.encodeList(watchRoutines)
                val putDataMapReq = PutDataMapRequest.create(WatchRoutine.DATA_LAYER_PATH).apply {
                    dataMap.putString(WatchRoutine.KEY_ROUTINES_JSON, jsonString)
                    dataMap.putLong(WatchRoutine.KEY_TIMESTAMP, System.currentTimeMillis())
                }
                val putDataReq = putDataMapReq.asPutDataRequest().setUrgent()

                Tasks.await(dataClient.putDataItem(putDataReq))
                watchRoutines.size
            }
        }
}
