package com.altimedia.updater.util

import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import kotlin.math.min

/**
 * Downloads chunk of a file from given url using `offset` and `size`,
 * and saves to a given location.
 *
 * In a real-life application this helper class should download from HTTP Server,
 * but in this sample app it will only download from a local file.
 */
class FileDownloader(
    private val mUrl: String,
    private val mOffset: Long,
    private val mSize: Long,
    private val mDestination: File
) {
    /**
     * Downloads the file with given offset and size.
     * @throws IOException when can't download the file
     */
    @Throws(IOException::class)
    fun download() {
        Log.d(
            "FileDownloader", "downloading " + mDestination.name
                    + " from " + mUrl
                    + " to " + mDestination.absolutePath
        )
        val url = URL(mUrl)
        val connection = url.openConnection()
        connection.connect()
        connection.getInputStream().use { input ->
            FileOutputStream(mDestination).use { output ->
                val skipped = input.skip(mOffset)
                if (skipped != mOffset) {
                    throw IOException(
                        "Can't download file "
                                + mUrl
                                + " with given offset "
                                + mOffset
                    )
                }
                val data = ByteArray(4096)
                var total: Long = 0
                while (total < mSize) {
                    val needToRead = min(4096, mSize - total).toInt()
                    val count = input.read(data, 0, needToRead)
                    if (count <= 0) {
                        break
                    }
                    output.write(data, 0, count)
                    total += count.toLong()
                }
                if (total != mSize) {
                    throw IOException(
                        "Can't download file "
                                + mUrl
                                + " with given size "
                                + mSize
                    )
                }
            }
        }
    }

}