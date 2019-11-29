package com.altimedia.updater.data.model

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class PayloadSpecFactory {

    companion object {
        /**
         * The payload PAYLOAD_ENTRY is stored in the zip package to comply with the Android OTA package
         * format. We want to find out the offset of the entry, so that we can pass it over to the A/B
         * updater without making an extra copy of the payload.
         *
         *
         * According to Android docs, the entries are listed in the order in which they appear in the
         * zip file. So we enumerate the entries to identify the offset of the payload file.
         * http://developer.android.com/reference/java/util/zip/ZipFile.html#entries()
         */
        @Throws(IOException::class)
        fun forNonStreaming(packageFile: File): PayloadSpec {
            var payloadFound = false
            var payloadOffset: Long = 0
            var payloadSize: Long = 0
            val properties: MutableList<String> =
                ArrayList()
            ZipFile(packageFile).use { zip ->
                val entries = zip.entries()
                var offset: Long = 0
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val name = entry.name
                    // Zip local file header has 30 bytes + filename + sizeof extra field.
                    // https://en.wikipedia.org/wiki/Zip_(file_format)
                    val extraSize =
                        if (entry.extra == null) 0 else entry.extra.size.toLong()
                    offset += 30 + name.length + extraSize
                    if (entry.isDirectory) {
                        continue
                    }
                    val length = entry.compressedSize
                    if (PackageFileInfo.PAYLOAD_BINARY_FILE_NAME == name) {
                        if (entry.method != ZipEntry.STORED) {
                            throw IOException("Invalid compression method.")
                        }
                        payloadFound = true
                        payloadOffset = offset
                        payloadSize = length
                    } else if (PackageFileInfo.PAYLOAD_PROPERTIES_FILE_NAME == name) {
                        val inputStream = zip.getInputStream(entry)
                        if (inputStream != null) {
                            val br =
                                BufferedReader(InputStreamReader(inputStream))
                            var line: String?
                            while (br.readLine().also { line = it } != null) {
                                properties.add(line!!)
                            }
                        }
                    }
                    offset += length
                }
            }
            if (!payloadFound) {
                throw IOException("Failed to find payload entry in the given package.")
            }
            return PayloadSpec.build  {
                url {"file://" + packageFile.absolutePath }
                offset { payloadOffset }
                size { payloadSize }
                properties{ properties }
            }
        }

        /**
         * Creates a [PayloadSpec] for streaming update.
         */
        @Throws(IOException::class)
        fun forStreaming(
            updateUrl: String,
            offset: Long,
            size: Long,
            propertiesFile: File
        ): PayloadSpec {
            return PayloadSpec.build {
                url { updateUrl }
                offset{ offset }
                size { size }
                properties { Files.readAllLines(propertiesFile.toPath()) }
            }
        }

        /**
         * Converts an [PayloadSpec] to a string.
         */
        fun specToString(payloadSpec: PayloadSpec) =
            "<PayloadSpec url=${payloadSpec.url}, offset=${payloadSpec.offset}, size=${payloadSpec.size}, properties=${payloadSpec.properties.toTypedArray().contentToString()}>"

        }
}