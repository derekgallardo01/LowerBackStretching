package com.lowerbackstretching.health

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Thin wrapper over Health Connect's [HealthConnectClient]. Every entry
 * point is safe to call regardless of whether Health Connect is
 * installed on the device — missing-provider failures are caught and
 * logged, never thrown.
 *
 * The view layer asks for permissions via [permissionsContract] /
 * [hasAllPermissions], gates writes/reads on the user pref, and calls
 * [writeStretchingSession] or [readStepsToday] when appropriate.
 */
class HealthController(private val context: Context) {

    enum class Availability { Available, ProviderUpdateRequired, NotInstalled }

    companion object {
        private const val TAG = "HealthController"

        val writePermissions: Set<String> = setOf(
            HealthPermission.getWritePermission(ExerciseSessionRecord::class),
        )
        val readPermissions: Set<String> = setOf(
            HealthPermission.getReadPermission(StepsRecord::class),
        )
        val allPermissions: Set<String> = writePermissions + readPermissions
    }

    fun availability(): Availability = when (HealthConnectClient.getSdkStatus(context)) {
        HealthConnectClient.SDK_AVAILABLE -> Availability.Available
        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> Availability.ProviderUpdateRequired
        else -> Availability.NotInstalled
    }

    private fun client(): HealthConnectClient? = try {
        if (availability() == Availability.Available) HealthConnectClient.getOrCreate(context)
        else null
    } catch (t: Throwable) {
        Log.w(TAG, "Health Connect getOrCreate failed: ${t.message}")
        null
    }

    /**
     * Returns the [androidx.activity.result.contract.ActivityResultContract]
     * the UI uses to request the union of write+read permissions.
     */
    fun permissionsContract() =
        PermissionController.createRequestPermissionResultContract()

    suspend fun hasAllPermissions(): Boolean {
        val c = client() ?: return false
        return c.permissionController.getGrantedPermissions().containsAll(allPermissions)
    }

    suspend fun hasWritePermission(): Boolean {
        val c = client() ?: return false
        return c.permissionController.getGrantedPermissions().containsAll(writePermissions)
    }

    suspend fun hasReadPermission(): Boolean {
        val c = client() ?: return false
        return c.permissionController.getGrantedPermissions().containsAll(readPermissions)
    }

    /**
     * Write a single stretching exercise session. Returns true on
     * success; false if Health Connect is unavailable, the permission
     * is missing, or the write fails.
     */
    suspend fun writeStretchingSession(start: Instant, end: Instant): Boolean {
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
                        title = "Stretching",
                    )
                )
            )
            true
        } catch (t: Throwable) {
            Log.w(TAG, "writeStretchingSession failed: ${t.message}")
            false
        }
    }

    /**
     * Sum of step counts recorded between local midnight (today) and
     * now. Returns null if Health Connect is unavailable, permission
     * is missing, or no steps have been recorded yet.
     */
    suspend fun readStepsToday(): Long? {
        val c = client() ?: return null
        if (!hasReadPermission()) return null
        val zone = ZoneId.systemDefault()
        val start = LocalDate.now(zone).atStartOfDay(zone).toInstant()
        val end = Instant.now()
        return try {
            val response = c.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                )
            )
            response[StepsRecord.COUNT_TOTAL]
        } catch (t: Throwable) {
            Log.w(TAG, "readStepsToday failed: ${t.message}")
            null
        }
    }
}
