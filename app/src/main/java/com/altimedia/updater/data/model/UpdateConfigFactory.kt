package com.altimedia.updater.data.model

import android.content.Context
import android.util.Log
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class UpdateConfigFactory {

    companion object {
        const val UPDATE_CONFIGS_ROOT = "configs/"

        /**
         * @param configs update configs
         * @return list of names
         */
        fun configsToNames(configs: List<UpdateConfig>): Array<String> {
            return configs.map { it.name }.toTypedArray()
        }
        /**
         * @param context app context
         * @return configs root directory
         */
        fun getConfigsRoot(context: Context) =
            Paths.get(
                context.filesDir.toString(),
                UPDATE_CONFIGS_ROOT
            ).toString()

        /**
         * @param context application context
         * @return list of configs from directory List<UpdateConfig>
         */
        fun getUpdateConfigs(context: Context): List<UpdateConfig> {
            val root = File(
                getConfigsRoot(
                    context
                )
            )
            val configs = ArrayList<UpdateConfig>()

            if (!root.exists()) return configs

            root.listFiles()?.let{
                for (f in it) {
                    if (!f.isDirectory && f.name.endsWith(".json")) {
                        try {
                            val json = String(
                                Files.readAllBytes(f.toPath()),
                                StandardCharsets.UTF_8
                            )
                            configs.add(UpdateConfig.fromJson(json))
                        } catch (e: Exception) {
                            Log.e(
                                "UpdateConfigs",
                                "Can't read/parse config file " + f.name,
                                e
                            )
                            throw RuntimeException(
                                "Can't read/parse config file " + f.name, e
                            )
                        }
                    }
                }
                return configs
            }
            return configs
        }

        /**
         * @param filename searches by given filename
         * @param config searches in {@link UpdateConfig#getAbConfig()}
         * @return offset and size of {@code filename} in the package zip file
         *         stored as {@link UpdateConfig.PackageFile}.
         */
        fun getPropertyFile(fileName: String, config: UpdateConfig): PackageFile? =
            config.abConfig?.propertyFiles?.first { file -> fileName == file.filename }
    }
}
