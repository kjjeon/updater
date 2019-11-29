package com.altimedia.updater.data.model

/** Utility class for an OTA package.  */
class PackageFileInfo {

    companion object {
        /**
         * Directory used to perform updates.
         */
        const val OTA_PACKAGE_DIR = "/data/ota_package"
        /**
         * update payload, it will be passed to `UpdateEngine#applyPayload`.
         */
        const val PAYLOAD_BINARY_FILE_NAME = "payload.bin"
        /**
         * Currently, when calling `UpdateEngine#applyPayload` to perform actions
         * that don't require network access (e.g. change slot), update_engine still
         * talks to the server to download/verify file.
         * `update_engine` might throw error when rebooting if `UpdateEngine#applyPayload`
         * is not supplied right headers and tokens.
         * This behavior might change in future android versions.
         *
         * To avoid extra network request in `update_engine`, this file has to be
         * downloaded and put in `OTA_PACKAGE_DIR`.
         */
        const val PAYLOAD_METADATA_FILE_NAME = "payload_metadata.bin"
        const val PAYLOAD_PROPERTIES_FILE_NAME = "payload_properties.txt"
        /** The zip entry in an A/B OTA package, which will be used by update_verifier.  */
        const val CARE_MAP_FILE_NAME = "care_map.txt"
        const val METADATA_FILE_NAME = "metadata"
        /**
         * The zip file that claims the compatibility of the update package to check against the Android
         * framework to ensure that the package can be installed on the device.
         */
        const val COMPATIBILITY_ZIP_FILE_NAME = "compatibility.zip"
    }
}


