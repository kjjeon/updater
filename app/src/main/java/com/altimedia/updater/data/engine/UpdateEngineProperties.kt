package com.altimedia.updater.data.engine

/**
 * Utility class for properties that will be passed to `UpdateEngine#applyPayload`.
 */

class UpdateEngineProperties {

    companion object {
        /**
         * The property indicating that the update engine should not switch slot
         * when the device reboots.
         */
        const val PROPERTY_DISABLE_SWITCH_SLOT_ON_REBOOT = "SWITCH_SLOT_ON_REBOOT=0"
        /**
         * The property to skip post-installation.
         * https://source.android.com/devices/tech/ota/ab/#post-installation
         */
        const val PROPERTY_SKIP_POST_INSTALL = "RUN_POST_INSTALL=0"
    }
}