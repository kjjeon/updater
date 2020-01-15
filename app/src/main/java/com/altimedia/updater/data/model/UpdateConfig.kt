package com.altimedia.updater.data.model

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*

data class UpdateConfig (
    var name: String = "",
    var url: String = "",
    var abInstallType: Int = AB_INSTALL_TYPE_NON_STREAMING,
    var abConfig: AbConfig? = null,
    var rawJson: String? = "") : Parcelable {

    /**
     * @return File object for given url
     */
    fun getUpdatePackageFile(): File {
        if (abInstallType != AB_INSTALL_TYPE_NON_STREAMING) {
            throw RuntimeException("Expected non-streaming install type")
        }
        if (!url.startsWith("file://")) {
            throw RuntimeException("url is expected to start with file://")
        }
        return File(url.substring(7, url.length))
    }

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readSerializable() as AbConfig,
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(url)
        parcel.writeInt(abInstallType)
        parcel.writeSerializable(abConfig)
        parcel.writeString(rawJson)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<UpdateConfig> {
        override fun createFromParcel(parcel: Parcel): UpdateConfig =
            UpdateConfig(parcel)
        override fun newArray(size: Int): Array<UpdateConfig?> = arrayOfNulls(size)

        const val AB_INSTALL_TYPE_NON_STREAMING = 0
        const val AB_INSTALL_TYPE_STREAMING = 1
        private const val AB_INSTALL_TYPE_NON_STREAMING_JSON = "NON_STREAMING"
        private const val AB_INSTALL_TYPE_STREAMING_JSON = "STREAMING"

        /** parse update config from json  */
        @Throws(JSONException::class)
        fun fromJson(json: String): UpdateConfig {
            val o = JSONObject(json)
            val c = UpdateConfig()
            c.name = o.getString("name")
            c.url = o.getString("url")

            when (o.getString("ab_install_type")) {
                AB_INSTALL_TYPE_NON_STREAMING_JSON -> c.abInstallType =
                    AB_INSTALL_TYPE_NON_STREAMING
                AB_INSTALL_TYPE_STREAMING_JSON -> c.abInstallType =
                    AB_INSTALL_TYPE_STREAMING
                else -> throw JSONException(
                    "Invalid type, expected either "
                            + "NON_STREAMING or STREAMING, got " + o.getString("ab_install_type")
                )
            }
            // TODO: parse only for A/B updates when non-A/B is implemented
            val ab = o.getJSONObject("ab_config")
            val forceSwitchSlot = ab.getBoolean("force_switch_slot")
            val verifyPayloadMetadata = ab.getBoolean("verify_payload_metadata")
            val propertyFiles = ArrayList<PackageFile>()
            if (ab.has("property_files")) {
                val propertyFilesJson = ab.getJSONArray("property_files")
                for (i in 0 until propertyFilesJson.length()) {
                    val p = propertyFilesJson.getJSONObject(i)
                    propertyFiles.add(
                        PackageFile(
                            p.getString("filename"),
                            p.getLong("offset"),
                            p.getLong("size")
                        )
                    )
                }
            }

            val authorization = ab.optString("authorization", "failed")

            c.abConfig = AbConfig(
                forceSwitchSlot,
                verifyPayloadMetadata,
                propertyFiles.toList(),
                if(authorization == "failed") null else authorization
            )
            c.rawJson = json
            return c
        }
    }
}