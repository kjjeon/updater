package com.altimedia.updater.data.engine

import android.util.SparseArray
import com.altimedia.updater.dummy.UpdateEngine
import java.util.*

/**
 * Helper class to work with update_engine's error codes.
 * Many error codes are defined in  [UpdateEngine.ErrorCodeConstants],
 * but you can find more in system/update_engine/common/error_code.h.
 */
object UpdateEngineErrorCodes {
    /**
     * Error code from the update engine. Values must agree with the ones in
     * system/update_engine/common/error_code.h.
     */
    const val UNKNOWN = -1
    const val UPDATED_BUT_NOT_ACTIVE = 52
    const val USER_CANCELLED = 48

    private val CODE_TO_NAME_MAP = SparseArray<String>()
    /**
     * Completion codes returned by update engine indicating that the update
     * was successfully applied.
     */
    private val SUCCEEDED_COMPLETION_CODES: Set<Int> = HashSet(
        listOf(
            UpdateEngine.ErrorCodeConstants.SUCCESS,
            // UPDATED_BUT_NOT_ACTIVE is returned when the payload is successfully
            // applied but the device won't switch to the new slot after the next boot.
            UpdateEngine.ErrorCodeConstants.UPDATED_BUT_NOT_ACTIVE
        )
    )

    /**
     * checks if update succeeded using errorCode
     */
    fun isUpdateSucceeded(errorCode: Int): Boolean {
        return SUCCEEDED_COMPLETION_CODES.contains(errorCode)
    }

    /**
     * converts error code to error name
     */
    fun getCodeName(errorCode: Int): String {
        return CODE_TO_NAME_MAP[errorCode]
    }

    init {
        CODE_TO_NAME_MAP.put(0, "SUCCESS")
        CODE_TO_NAME_MAP.put(1, "ERROR")
        CODE_TO_NAME_MAP.put(4, "FILESYSTEM_COPIER_ERROR")
        CODE_TO_NAME_MAP.put(5, "POST_INSTALL_RUNNER_ERROR")
        CODE_TO_NAME_MAP.put(6, "PAYLOAD_MISMATCHED_TYPE_ERROR")
        CODE_TO_NAME_MAP.put(7, "INSTALL_DEVICE_OPEN_ERROR")
        CODE_TO_NAME_MAP.put(8, "KERNEL_DEVICE_OPEN_ERROR")
        CODE_TO_NAME_MAP.put(9, "DOWNLOAD_TRANSFER_ERROR")
        CODE_TO_NAME_MAP.put(10, "PAYLOAD_HASH_MISMATCH_ERROR")
        CODE_TO_NAME_MAP.put(11, "PAYLOAD_SIZE_MISMATCH_ERROR")
        CODE_TO_NAME_MAP.put(12, "DOWNLOAD_PAYLOAD_VERIFICATION_ERROR")
        CODE_TO_NAME_MAP.put(15, "NEW_ROOTFS_VERIFICATION_ERROR")
        CODE_TO_NAME_MAP.put(20, "DOWNLOAD_STATE_INITIALIZATION_ERROR")
        CODE_TO_NAME_MAP.put(26, "DOWNLOAD_METADATA_SIGNATURE_MISMATCH")
        CODE_TO_NAME_MAP.put(48, "USER_CANCELLED")
        CODE_TO_NAME_MAP.put(52, "UPDATED_BUT_NOT_ACTIVE")
    }
}