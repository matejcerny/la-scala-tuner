package com.guitartuner.core

import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

@RunWith(classOf[JUnitRunner])
class CoreTunerStubSpec extends AnyFunSuite with Matchers {

  test("TunerConfig should load default configuration from application.conf") {
    val config = TunerConfig.load()
    config.appName shouldBe "GuitarTunerCore"
    config.sampleRate shouldBe 44100
    config.a4Frequency shouldBe 440.0
  }

  test("CoreTunerStub should return expected verification string") {
    val result = CoreTunerStub.verifyConnection("KotlinUI")
    result shouldBe "Scala Core received: 'KotlinUI'. Status: OK"
  }
}
