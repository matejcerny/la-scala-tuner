package com.guitartuner.app

import com.guitartuner.core.CoreTunerStub
import com.guitartuner.core.TunerConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ScalaCoreInteropTest {

    @Test
    fun testCanCallScalaCoreFromKotlin() {
        val greeting = CoreTunerStub.getGreeting("KotlinUnitTest")
        assertNotNull(greeting)
        assert(greeting.contains("Hello KotlinUnitTest"))

        val status = CoreTunerStub.verifyConnection("KotlinUnitTest")
        assertEquals("Scala Core received: 'KotlinUnitTest'. Status: OK", status)
    }

    @Test
    fun testCanAccessScalaConfigFromKotlin() {
        val config = TunerConfig.load()
        assertEquals("GuitarTunerCore", config.appName())
        assertEquals(44100, config.sampleRate())
        assertEquals(440.0, config.a4Frequency(), 0.0001)
    }
}
