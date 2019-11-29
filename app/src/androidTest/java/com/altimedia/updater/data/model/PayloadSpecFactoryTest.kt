package com.altimedia.updater.data.model

import android.content.Context
import androidx.test.filters.SmallTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.altimedia.updater.data.model.PackageFileInfo.Companion.PAYLOAD_BINARY_FILE_NAME
import com.google.common.base.Charsets
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import com.altimedia.updater.test.R
import org.hamcrest.CoreMatchers
import org.junit.Assert

/**
 * Tests if PayloadSpecs parses update package zip file correctly.
 */
@RunWith(AndroidJUnit4ClassRunner::class)
@SmallTest
class PayloadSpecFactoryTest {
    private lateinit var testDir: File
    private lateinit var targetContext: Context
    private lateinit var testContext: Context

    @Rule
    @JvmField
    val thrown: ExpectedException? = ExpectedException.none()

    @Before
    fun setUp() {
        targetContext = InstrumentationRegistry.getInstrumentation()
            .targetContext
        testContext =
            InstrumentationRegistry.getInstrumentation().context
        testDir = targetContext.cacheDir
    }

    @Test
    @Throws(Exception::class)
    fun forNonStreaming_works() { // Prepare the target file
        val packageFile = Paths
            .get(targetContext.cacheDir.absolutePath, "ota.zip")
            .toFile()
        Files.deleteIfExists(packageFile.toPath())
        Files.copy(
            testContext.resources.openRawResource(R.raw.ota_002_package),
            packageFile.toPath()
        )
        val spec: PayloadSpec = PayloadSpecFactory.forNonStreaming(packageFile)
        assertEquals("correct url", "file://" + packageFile.absolutePath, spec.url)
        assertEquals(
            "correct payload offset",
            30L + PAYLOAD_BINARY_FILE_NAME.length, spec.offset
        )
        assertEquals("correct payload size", 1392, spec.size)
        assertEquals(4, spec.properties.size)
        assertEquals(
            "FILE_HASH=sEAK/NMbU7GGe01xt55FsPafIPk8IYyBOAd6SiDpiMs=",
            spec.properties[0]
        )
    }

    @Test
    @Throws(Exception::class)
    fun forNonStreaming_IOException() {
        thrown?.expect(IOException::class.java)
        PayloadSpecFactory.forNonStreaming(File("/fake/news.zip"))
    }

    @Test
    @Throws(Exception::class)
    fun forStreaming_works() {
        val url = "http://a.com/b.zip"
        val offset: Long = 45
        val size: Long = 200
        val propertiesFile = createMockPropertiesFile()
        val spec: PayloadSpec = PayloadSpecFactory.forStreaming(url, offset, size, propertiesFile)
        assertEquals("same url", url, spec.url)
        assertEquals("same offset", offset, spec.offset)
        assertEquals("same size", size, spec.size)
        val expectedList = mutableListOf("k1=val1", "key2=val2")
        Assert.assertThat(
            expectedList,
            CoreMatchers.`is`(CoreMatchers.equalTo(spec.properties))
        )
//        assertArrayEquals(
//            "correct properties",
//            arrayOf("k1=val1", "key2=val2"),
//            spec.properties.toArray(arrayOfNulls<String>(0))
//        )
    }

    @Throws(IOException::class)
    private fun createMockPropertiesFile(): File {
        val propertiesFile =
            File(testDir, PackageFileInfo.PAYLOAD_PROPERTIES_FILE_NAME)
        com.google.common.io.Files.asCharSink(propertiesFile, Charsets.UTF_8)
            .write(PROPERTIES_CONTENTS)
        return propertiesFile
    }

    companion object {
        private const val PROPERTIES_CONTENTS = "k1=val1\nkey2=val2"
    }
}