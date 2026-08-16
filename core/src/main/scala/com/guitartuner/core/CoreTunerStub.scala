package com.guitartuner.core

import com.typesafe.config.{Config, ConfigFactory}

final case class TunerConfig(appName: String, version: String, sampleRate: Int, a4Frequency: Double)

object TunerConfig {
  def load(): TunerConfig = {
    val config: Config = ConfigFactory.load()
    TunerConfig(
      appName = config.getString("app.name"),
      version = config.getString("app.version"),
      sampleRate = config.getInt("app.sampleRate"),
      a4Frequency = config.getDouble("app.pitch.a4Frequency")
    )
  }
}

object CoreTunerStub {
  def getGreeting(name: String): String = {
    val config = TunerConfig.load()
    s"Hello $name from ${config.appName} v${config.version}! Sample rate: ${config.sampleRate}Hz, A4: ${config.a4Frequency}Hz"
  }

  def verifyConnection(input: String): String = {
    s"Scala Core received: '$input'. Status: OK"
  }
}
