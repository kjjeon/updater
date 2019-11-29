package com.altimedia.updater.data.model

import com.altimedia.updater.data.model.PayloadSpec
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class PlayloadSpecTest {
   @Test
   fun `playloadSpec 생성 테스트`() {
       val payloadSpec = PayloadSpec.build {
           offset { 3 }
           properties { mutableListOf("a", "b")}
           size { 2 }
           url = "file://update.zip"
       }

       assertEquals(3, payloadSpec.offset)
       assertEquals(2, payloadSpec.size)
       assertEquals("file://update.zip", payloadSpec.url)
       val expectedList = mutableListOf("a", "b")
       assertThat(expectedList, `is`(equalTo(payloadSpec.properties)))
   }
}
