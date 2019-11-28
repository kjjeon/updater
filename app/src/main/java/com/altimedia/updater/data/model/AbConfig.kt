package com.altimedia.updater.data.model

import java.io.Serializable

data class AbConfig (
    val forceSwitchSlot: Boolean,
    val verifyPayloadMetadata: Boolean,
    val propertyFiles: List<PackageFile>,
    val authorization: String?
) : Serializable