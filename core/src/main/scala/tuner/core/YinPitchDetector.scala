package tuner.core

import scala.math.*

class YinPitchDetector(
    val bufferSize: Int = 2048,
    val sampleRate: Int = 44100,
    val threshold: Float = 0.12f,
    val rmsSilenceThreshold: Float = 0.01f
):
  private val halfBufferSize: Int = bufferSize / 2
  private val diffBuffer: Array[Float] = new Array[Float](halfBufferSize)
  private val cmndBuffer: Array[Float] = new Array[Float](halfBufferSize)

  def process(buffer: Array[Float], instrument: Instrument): PitchResult =
    if computeRms(buffer) < rmsSilenceThreshold then
      PitchResult.Silence
    else
      difference(buffer)
      cumulativeMeanNormalizedDifference()
      val tau = absoluteThreshold()

      if tau == -1 then
        PitchResult.NoPitch
      else
        val betterTau = parabolicInterpolation(tau)
        val frequency = sampleRate.toDouble / betterTau
        matchToInstrument(frequency, instrument)

  private def computeRms(buffer: Array[Float]): Float =
    var sum = 0.0f
    var i = 0
    while i < buffer.length do
      sum += buffer(i) * buffer(i)
      i += 1
    sqrt(sum / buffer.length).toFloat

  private def difference(buffer: Array[Float]): Unit =
    var tau = 0
    while tau < halfBufferSize do
      var sum = 0.0f
      var i = 0
      while i < halfBufferSize do
        val delta = buffer(i) - buffer(i + tau)
        sum += delta * delta
        i += 1
      diffBuffer(tau) = sum
      tau += 1

  private def cumulativeMeanNormalizedDifference(): Unit =
    cmndBuffer(0) = 1.0f
    var runningSum = 0.0f
    var tau = 1
    while tau < halfBufferSize do
      runningSum += diffBuffer(tau)
      if runningSum == 0.0f then
        cmndBuffer(tau) = 1.0f
      else
        cmndBuffer(tau) = diffBuffer(tau) * tau / runningSum
      tau += 1

  private def absoluteThreshold(): Int =
    var tau = 2
    var minTau = -1
    var minVal = Float.MaxValue

    while tau < halfBufferSize do
      if cmndBuffer(tau) < threshold then
        while tau + 1 < halfBufferSize && cmndBuffer(tau + 1) < cmndBuffer(tau) do
          tau += 1
        return tau

      if cmndBuffer(tau) < minVal then
        minVal = cmndBuffer(tau)
        minTau = tau
      tau += 1

    if minVal < 0.40f then minTau else -1

  private def parabolicInterpolation(tau: Int): Double =
    if tau <= 0 || tau >= halfBufferSize - 1 then tau.toDouble
    else
      val s0 = cmndBuffer(tau - 1)
      val s1 = cmndBuffer(tau)
      val s2 = cmndBuffer(tau + 1)
      val bottom = 2.0f * (2.0f * s1 - s0 - s2)
      if bottom == 0.0f then tau.toDouble
      else
        val delta = (s2 - s0) / bottom
        tau.toDouble + delta.toDouble

  private def matchToInstrument(frequency: Double, instrument: Instrument): PitchResult =
    val strings = instrument.strings
    var bestMatch = strings(0)
    var minDiff = abs(bestMatch.targetFrequency - frequency)
    var i = 1
    while i < strings.length do
      val diff = abs(strings(i).targetFrequency - frequency)
      if diff < minDiff then
        minDiff = diff
        bestMatch = strings(i)
      i += 1

    val cents = 1200.0 * (log(frequency / bestMatch.targetFrequency) / log(2.0))
    PitchResult.Detected(
      frequency = frequency,
      nearestString = bestMatch,
      centsDeviation = cents,
      isInTune = abs(cents) <= 4.0
    )
