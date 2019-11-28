package com.altimedia.updater.data.model

import java.io.Serializable

data class PackageFile(
    val filename: String,  /** filename in an archive */
    val offset: Long,     /** defines beginning of update data in archive */
    val size: Long  /** size of the update data in archive */
) : Serializable