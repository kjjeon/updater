package com.altimedia.updater.data.model

import android.content.Context
import androidx.test.filters.SmallTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.altimedia.updater.test.R
import com.google.common.io.CharStreams
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import java.io.IOException
import java.io.InputStreamReader

/**
 * Tests for [UpdateConfig]
 */
@RunWith(AndroidJUnit4ClassRunner::class)
@SmallTest
class UpdateConfigTest {

    @Rule
    @JvmField
    val thrown: ExpectedException? = ExpectedException.none()

    private lateinit var mContext: Context
    private lateinit var mTargetContext: Context
    private lateinit var mJsonStreaming001: String

    @Before
    @Throws(Exception::class)
    fun setUp() {
        mContext = InstrumentationRegistry.getInstrumentation().context
        mTargetContext = InstrumentationRegistry.getInstrumentation().targetContext
        mJsonStreaming001 = readResource(R.raw.update_config_001_stream)
    }

    @Test
    @Throws(Exception::class)
    fun fromJson_parsesNonStreaming() {
        val config: UpdateConfig =
            UpdateConfig.fromJson(JSON_NON_STREAMING)
        assertEquals("name is parsed", "vip update", config.name)
        assertEquals(
            "stores raw json",
            JSON_NON_STREAMING,
            config.rawJson
        )
        Assert.assertSame(
            "type is parsed",
            UpdateConfig.AB_INSTALL_TYPE_NON_STREAMING,
            config.abInstallType
        )
        assertEquals("url is parsed", "file:///my-builds/a.zip", config.url)
    }

    @Test
    @Throws(Exception::class)
    fun fromJson_parsesStreaming() {
        val config: UpdateConfig = UpdateConfig.fromJson(mJsonStreaming001)

        assertEquals("streaming-001", config.name)
        assertEquals("http://foo.bar/update.zip", config.url)
        Assert.assertSame(UpdateConfig.AB_INSTALL_TYPE_STREAMING, config.abInstallType)
        val abConfig = config.abConfig
        assertNotNull(abConfig)
        assertEquals(
            "payload.bin",
            abConfig!!.propertyFiles[0].filename
        )
        assertEquals(195, abConfig.propertyFiles[0].offset)
        assertEquals(8, abConfig.propertyFiles[0].size)
        Assert.assertTrue(abConfig.forceSwitchSlot)

    }

    @Throws(Exception::class)
    @Test
    fun updatePackageFile_throwsErrorIfStreaming() {
        val config: UpdateConfig = UpdateConfig.fromJson(mJsonStreaming001)
        thrown?.expect(RuntimeException::class.java)
        config.getUpdatePackageFile()
    }

    @Throws(Exception::class)
    @Test
    fun updatePackageFile_throwsErrorIfNotAFile() {
        val json = ("{"
                + " \"name\": \"upd\", \"url\": \"http://foo.bar\","
                + " \"ab_install_type\": \"NON_STREAMING\","
                + " \"ab_config\": {"
                + "     \"force_switch_slot\": false,"
                + "     \"verify_payload_metadata\": false } }")
        val config: UpdateConfig = UpdateConfig.fromJson(json)
        thrown?.expect(RuntimeException::class.java)
        config.getUpdatePackageFile()
    }

    @Throws(Exception::class)
    @Test
    fun updatePackageFile_works() {
        val c: UpdateConfig =
            UpdateConfig.fromJson(JSON_NON_STREAMING)
        val updatePackageFile = c.getUpdatePackageFile()
        assertNotNull(updatePackageFile)
        assertEquals("/my-builds/a.zip", updatePackageFile!!.absolutePath)
    }

    @Throws(IOException::class)
    private fun readResource(id: Int) =
        CharStreams.toString(InputStreamReader(mContext.resources.openRawResource(id)))

    companion object {
        private const val JSON_NON_STREAMING = ("{"
                + " \"name\": \"vip update\", \"url\": \"file:///my-builds/a.zip\","
                + " \"ab_install_type\": \"NON_STREAMING\","
                + " \"ab_config\": {"
                + "     \"force_switch_slot\": false,"
                + "     \"verify_payload_metadata\": false } }")
    }
}