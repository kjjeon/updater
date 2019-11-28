package com.altimedia.updater.data.engine

import android.util.SparseArray

/**
 * Helper class to work with update_engine's error codes.
 * Many error codes are defined in  `UpdateEngine.UpdateStatusConstants`,
 * but you can find more in system/update_engine/common/error_code.h.
 */
object UpdateEngineStatuses {

    private val STATUS_MAP = SparseArray<String>()
    /**
     * converts status code to status name
     */
    fun getStatusText(status: Int): String {
        return STATUS_MAP[status]
    }

    init {
        STATUS_MAP.put(0, "IDLE")
        STATUS_MAP.put(1, "CHECKING_FOR_UPDATE")
        STATUS_MAP.put(2, "UPDATE_AVAILABLE")
        STATUS_MAP.put(3, "DOWNLOADING")
        STATUS_MAP.put(4, "VERIFYING")
        STATUS_MAP.put(5, "FINALIZING")
        STATUS_MAP.put(6, "UPDATED_NEED_REBOOT")
        STATUS_MAP.put(7, "REPORTING_ERROR_EVENT")
        STATUS_MAP.put(8, "ATTEMPTING_ROLLBACK")
        STATUS_MAP.put(9, "DISABLED")
    }
}