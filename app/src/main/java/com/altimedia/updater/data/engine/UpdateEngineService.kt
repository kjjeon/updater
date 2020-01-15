package com.altimedia.updater.data.engine

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.os.UpdateEngine
import android.util.Log
import com.altimedia.libplatform.HiddenApi
import com.altimedia.updater.data.model.*
import com.altimedia.updater.data.model.PackageFileInfo.Companion.CARE_MAP_FILE_NAME
import com.altimedia.updater.data.model.PackageFileInfo.Companion.COMPATIBILITY_ZIP_FILE_NAME
import com.altimedia.updater.data.model.PackageFileInfo.Companion.METADATA_FILE_NAME
import com.altimedia.updater.data.model.PackageFileInfo.Companion.OTA_PACKAGE_DIR
import com.altimedia.updater.data.model.PackageFileInfo.Companion.PAYLOAD_BINARY_FILE_NAME
import com.altimedia.updater.data.model.PackageFileInfo.Companion.PAYLOAD_METADATA_FILE_NAME
import com.altimedia.updater.data.model.PackageFileInfo.Companion.PAYLOAD_PROPERTIES_FILE_NAME
import com.altimedia.updater.data.model.UpdateConfig.CREATOR.AB_INSTALL_TYPE_NON_STREAMING
import com.altimedia.updater.util.FileDownloader
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

class UpdateEngineService : IntentService(TAG) {

    /**
     * The files that should be downloaded before streaming.
     */
    private val PRE_STREAMING_FILES_SET =
        setOf(
            CARE_MAP_FILE_NAME,
            COMPATIBILITY_ZIP_FILE_NAME,
            METADATA_FILE_NAME,
            PAYLOAD_PROPERTIES_FILE_NAME
        )

    private val updateEngine: UpdateEngine = UpdateEngine()

    /**
     * This interface is used to send results from [UpdateEngineService] to
     * `MainActivity`.
     */
    interface UpdateResultCallback {
        /**
         * Invoked when files are downloaded and payload spec is constructed.
         *
         * @param resultCode  result code, values are defined in [UpdateEngineService]
         * @param payloadSpec prepared payload spec for streaming update
         */
        fun onReceiveResult(resultCode: Int, payloadSpec: PayloadSpec?)
    }

    /**
     * Starts PrepareUpdateService.
     *
     * @param context        application context
     * @param config         update config
     * @param resultCallback callback that will be called when the update is ready to be installed
     */
    fun startService(
        context: Context,
        config: UpdateConfig,
        handler: Handler,
        resultCallback: UpdateResultCallback
    ) {
        Log.d(TAG, "Starting PrepareUpdateService")
        val receiver: ResultReceiver = CallbackResultReceiver(handler, resultCallback)
        val intent = Intent(context, UpdateEngineService::class.java)
        intent.putExtra(EXTRA_PARAM_CONFIG, config)
        intent.putExtra(EXTRA_PARAM_RESULT_RECEIVER, receiver)
        context.startService(intent)
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "On handle intent is called")
        intent?.run {
            val config: UpdateConfig? = getParcelableExtra(EXTRA_PARAM_CONFIG)
            val resultReceiver = getParcelableExtra<ResultReceiver>(EXTRA_PARAM_RESULT_RECEIVER)
            config?.let {
                try {
                    val spec: PayloadSpec = execute(it)
                    resultReceiver?.send(RESULT_CODE_SUCCESS, CallbackResultReceiver.createBundle(spec)
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to prepare streaming update", e)
                    resultReceiver?.send(RESULT_CODE_ERROR, null)
                }
            }
        }
    }

    /**
     * 1. Downloads files for streaming updates.
     * 2. Makes sure required files are present.
     * 3. Checks OTA package compatibility with the device.
     * 4. Constructs [PayloadSpec] for streaming update.
     */
    @Throws(IOException::class, PreparationFailedException::class)
    private fun execute(config: UpdateConfig): PayloadSpec {
        config.abConfig?.run {
            if (verifyPayloadMetadata) {
                Log.i(TAG, "Verifying payload metadata with UpdateEngine.")
                if (!verifyPayloadMetadata(config)) {
                    throw PreparationFailedException("Payload metadata is not compatible")
                }
            }
        }
        if (config.abInstallType == AB_INSTALL_TYPE_NON_STREAMING) {
            return PayloadSpecFactory.forNonStreaming(config.getUpdatePackageFile())
        }

        downloadPreStreamingFiles(config, OTA_PACKAGE_DIR)

        val payloadBinary: PackageFile =
            UpdateConfigFactory.getPropertyFile(PAYLOAD_BINARY_FILE_NAME, config)
                ?: throw PreparationFailedException("Failed to find $PAYLOAD_BINARY_FILE_NAME in config")

        if (UpdateConfigFactory.getPropertyFile(PAYLOAD_PROPERTIES_FILE_NAME, config) == null
            || !Paths.get(OTA_PACKAGE_DIR, PAYLOAD_PROPERTIES_FILE_NAME).toFile().exists()
        ) {
            throw IOException("$PAYLOAD_PROPERTIES_FILE_NAME not found")
        }
        val compatibilityFile = Paths.get(OTA_PACKAGE_DIR, COMPATIBILITY_ZIP_FILE_NAME).toFile()
        if (compatibilityFile.isFile) {
            Log.i(TAG, "Verifying OTA package for compatibility with the device")
            if (!verifyPackageCompatibility(compatibilityFile)) {
                throw PreparationFailedException("OTA package is not compatible with this device")
            }
        }
        return PayloadSpecFactory.forStreaming(
            config.url,
            payloadBinary.offset,
            payloadBinary.size,
            Paths.get(OTA_PACKAGE_DIR, PAYLOAD_PROPERTIES_FILE_NAME).toFile()
        )
    }


    private fun verifyPayloadMetadata(config: UpdateConfig): Boolean {
        config.abConfig?.run {
            val metadataPackageFile =
                propertyFiles.firstOrNull { p -> p.filename == PAYLOAD_METADATA_FILE_NAME }
            if (metadataPackageFile == null) {
                Log.w(TAG, "ab_config.property_files doesn't contain $PAYLOAD_METADATA_FILE_NAME")
                return true
            }

            val metadataPath = Paths.get(OTA_PACKAGE_DIR, PAYLOAD_METADATA_FILE_NAME)
            try {
                Files.deleteIfExists(metadataPath)
                val d = FileDownloader(
                    config.url,
                    metadataPackageFile.offset,
                    metadataPackageFile.size,
                    metadataPath.toFile()
                )
                d.download()
            } catch (e: IOException) {
                Log.w(TAG, "Downloading $PAYLOAD_METADATA_FILE_NAME from ${config.url} failed ", e)
                return true
            }
            return try {
                updateEngine.verifyPayloadMetadata(metadataPath.toAbsolutePath().toString())
            } catch (e: Exception) {
                Log.w(TAG, "UpdateEngine#verifyPayloadMetadata failed", e)
                true
            }

        }
        return true

    }

    /**
     * Downloads files defined in [UpdateConfig.abConfig]
     * and exists in `PRE_STREAMING_FILES_SET`, and put them
     * in directory `dir`.
     *
     * @throws IOException when can't download a file
     */
    @Throws(IOException::class)
    private fun downloadPreStreamingFiles(config: UpdateConfig, dir: String) {
        Log.d(TAG, "Deleting existing files from $dir")
        for (file in PRE_STREAMING_FILES_SET) {
            Files.deleteIfExists(Paths.get(OTA_PACKAGE_DIR, file))
        }
        Log.d(TAG, "Downloading files to $dir")
        config.abConfig?.run {
            for (file in propertyFiles) {
                if (PRE_STREAMING_FILES_SET.contains(file.filename)) {
                    Log.d(TAG, "Downloading file " + file.filename)
                    val downloader = FileDownloader(
                        config.url,
                        file.offset,
                        file.size,
                        Paths.get(dir, file.filename).toFile()
                    )
                    downloader.download()
                }
            }
        }
    }

    /**
     * @param file physical location of [COMPATIBILITY_ZIP_FILE_NAME]
     * @return true if OTA package is compatible with this device
     */
    private fun verifyPackageCompatibility(file: File): Boolean {
        return try {
            HiddenApi.verifyPackageCompatibility(file)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to verify package compatibility", e)
            false
        }
    }

    /**
     * Used by [UpdateEngineService] to pass [PayloadSpec]
     * to [UpdateResultCallback.onReceiveResult].
     */
    private class CallbackResultReceiver internal constructor(
        handler: Handler?,
        private val updateResultCallback: UpdateResultCallback
    ) : ResultReceiver(handler) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
            var payloadSpec: PayloadSpec? = null
            if (resultCode == RESULT_CODE_SUCCESS) {
                payloadSpec =
                    resultData.getSerializable(BUNDLE_PARAM_PAYLOAD_SPEC) as PayloadSpec?
            }
            updateResultCallback.onReceiveResult(resultCode, payloadSpec)
        }

        companion object {
            fun createBundle(payloadSpec: PayloadSpec?): Bundle {
                val b = Bundle()
                b.putSerializable(BUNDLE_PARAM_PAYLOAD_SPEC, payloadSpec)
                return b
            }

            private const val BUNDLE_PARAM_PAYLOAD_SPEC = "payload-spec"
        }
    }

    private class PreparationFailedException internal constructor(message: String?) :
        java.lang.Exception(message)

    companion object {
        const val TAG = "UpdateEngineService"
        /**
         * UpdateResultCallback result codes.
         */
        const val RESULT_CODE_SUCCESS = 0
        const val RESULT_CODE_ERROR = 1

        /**
         * Extra params that will be sent to IntentService.
         */
        const val EXTRA_PARAM_CONFIG = "config"
        const val EXTRA_PARAM_RESULT_RECEIVER = "result-receiver"
    }
}