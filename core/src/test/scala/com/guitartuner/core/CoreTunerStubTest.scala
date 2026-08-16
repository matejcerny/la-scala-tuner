package com.guitartuner.core

import org.junit.Test
import org.junit.Assert._

class CoreTunerStubTest {

  @Test
  def testTunerConfigLoad(): Unit = {
    val config = TunerConfig.load()
    assertEquals("GuitarTunerCore", config.appName)
    assertEquals(44100, config.sampleRate)
    assertEquals(440.0, config.a4Frequency, 0.0001)
  }

  @Test
  def testVerifyConnection(): Unit = {
    val result = CoreTunerStub.verifyConnection("KotlinUI")
    assertEquals("Scala Core received: 'KotlinUI'. Status: OK", result)
  }
}
