package com.altimedia.updater.util

import android.content.Context
import androidx.test.filters.SmallTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.altimedia.updater.test.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Tests for [FileDownloader]
 */
@RunWith(AndroidJUnit4ClassRunner::class)
@SmallTest
class FileDownloaderTest {

    @Rule
    @JvmField
    val thrown:ExpectedException? = ExpectedException.none()

    private lateinit var mTestContext: Context
    private lateinit var mTargetContext: Context

    @Before
    fun setUp() {
        mTestContext =
            InstrumentationRegistry.getInstrumentation().context
        mTargetContext = InstrumentationRegistry.getInstrumentation()
            .targetContext
    }

    @Test
    @Throws(Exception::class)
    fun download_downloadsChunkOfZip() { // Prepare the target file
        val packageFile = Paths
            .get(mTargetContext.cacheDir.absolutePath, "ota.zip")
            .toFile()
        Files.deleteIfExists(packageFile.toPath())
        Files.copy(
            mTestContext.resources.openRawResource(R.raw.ota_002_package),
            packageFile.toPath()
        )
        val url = "file://" + packageFile.absolutePath
        // prepare where to download
        val outFile = Paths
            .get(mTargetContext.cacheDir.absolutePath, "care_map.txt")
            .toFile()
        Files.deleteIfExists(outFile.toPath())
        // download a chunk of ota.zip
        val downloader = FileDownloader(url, 1674, 12, outFile)
        downloader.download()
        val downloadedContent = Files.readAllLines(outFile.toPath()).joinToString("\n")
        // archive contains text files with uppercase filenames
        assertEquals("CARE_MAP-TXT", downloadedContent)
    }
}