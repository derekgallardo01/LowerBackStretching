package com.lowerbackstretching.health

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import com.lowerbackstretching.R
import java.time.Instant

/**
 * Thin wrapper over Health Connect's [HealthConnectClient]. Every entry
 * point is safe to call regardless of whether Health Connect is
 * installed on the device — missing-provider failures are caught and
 * logged, never thrown.
 *
 * The view layer asks for permissions via [permissionsContract] /
 * [hasWritePermission], gates writes on the user pref, and calls
 * [writeStretchingSession] when appropriate.
 */
class HealthController(
    private val context: Context,
) {
    enum class Availability { Available, ProviderUpdateRequired, NotInstalled }

    companion object {
        private const val TAG = "HealthController"

        val writePermissions: Set<String> = setOf(
            HealthPermission.getWritePermission(ExerciseSessionRecord::class),
        )
    }

    fun availability(): Availability =
        when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE -> Availability.Available
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> Availability.ProviderUpdateRequired
            else -> Availability.NotInstalled
        }

    private fun client(): HealthConnectClient? =
        try {
            if (availability() == Availability.Available) {
                HealthConnectClient.getOrCreate(context)
            } else {
                null
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Health Connect getOrCreate failed: ${t.message}")
            null
        }

    /**
     * Returns the [androidx.activity.result.contract.ActivityResultContract]
     * the UI uses to request [writePermissions].
     */
    fun permissionsContract() = PermissionController.createRequestPermissionResultContract()

    suspend fun hasWritePermission(): Boolean {
        val c = client() ?: return false
        return c.permissionController.getGrantedPermissions().containsAll(writePermissions)
    }

    /**
     * Write a single stretching exercise session. Returns true on
     * success; false if Health Connect is unavailable, the permission
     * is missing, or the write fails.
     */
    suspend fun writeStretchingSession(
        start: Instant,
        end: Instant,
    ): Boolean {
        val c = client() ?: return false
        if (!hasWritePermission()) return false
        if (!end.isAfter(start)) return false
        return try {
            c.insertRecords(
                listOf(
                    ExerciseSessionRecord(
                        startTime = start,
                        startZoneOffset = null,
                        endTime = end,
                        endZoneOffset = null,
                        exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_STRETCHING,
                        title = context.getString(R.string.health_session_title),
                    ),
                ),
            )
            true
        } catch (t: Throwable) {
            Log.w(TAG, "writeStretchingSession failed: ${t.message}")
            false
        }
    }
}
