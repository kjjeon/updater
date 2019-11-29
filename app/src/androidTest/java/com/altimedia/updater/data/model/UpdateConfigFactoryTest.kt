package com.altimedia.updater.data.model

import androidx.test.filters.SmallTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.altimedia.updater.data.model.UpdateConfig
import com.altimedia.updater.data.model.UpdateConfigFactory
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
@SmallTest
class UpdateConfigsTest {

    @Rule
    @JvmField
    val thrown: ExpectedException? = ExpectedException.none()

    @Test
    fun configsToNames_extractsNames() {
        val configs: List<UpdateConfig> = listOf(
            UpdateConfig("blah", "http://", UpdateConfig.AB_INSTALL_TYPE_NON_STREAMING),
            UpdateConfig("blah 2", "http://", UpdateConfig.AB_INSTALL_TYPE_STREAMING)
        )
        val names: Array<String> = UpdateConfigFactory.configsToNames(configs)
        Assert.assertArrayEquals(arrayOf("blah", "blah 2"), names)
    }
}