package com.lowerbackstretching.wear.sync

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.lowerbackstretching.core.model.WatchRoutine

/**
 * Background listener service that receives updated custom routines
 * from the companion phone via the Wearable Data Layer.
 */
class WearDataListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val uri = event.dataItem.uri
                if (uri.path == WatchRoutine.DATA_LAYER_PATH) {
                    val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                    val jsonString = dataMapItem.dataMap.getString(WatchRoutine.KEY_ROUTINES_JSON)
                    if (!jsonString.isNullOrBlank()) {
                        val routines = WatchRoutine.decodeList(jsonString)
                        WatchRoutineStorage.saveRoutines(applicationContext, routines)
                    }
                }
            }
        }
    }
}
