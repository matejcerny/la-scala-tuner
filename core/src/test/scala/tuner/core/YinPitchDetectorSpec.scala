package tuner.core

import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import scala.util.Random

@RunWith(classOf[JUnitRunner])
class YinPitchDetectorSpec extends AnyFunSuite with Matchers {

  private val sampleRate = 44100
  private val bufferSize = 2048
  private val detector = new YinPitchDetector(bufferSize = bufferSize, sampleRate = sampleRate)
  private val instrument = Instrument.StandardUkulele

  private def generateSineBuffer(
      frequency: Double,
      amplitude: Float = 0.8f,
      harmonics: Seq[(Double, Float)] = Seq.empty
  ): Array[Float] = {
    val buffer = new Array[Float](bufferSize)
    var i = 0
    while (i < bufferSize) {
      var sample = amplitude * math.sin(2.0 * math.Pi * frequency * i / sampleRate).toFloat
      for ((hMult, hAmp) <- harmonics) {
        sample += hAmp * math.sin(2.0 * math.Pi * frequency * hMult * i / sampleRate).toFloat
      }
      buffer(i) = sample
      i += 1
    }
    buffer
  }

  test("Ukulele A4 (440.0 Hz) - Pure sine signal") {
    val buffer = generateSineBuffer(440.0)
    val result = detector.process(buffer, instrument)

    result match {
      case PitchResult.Detected(freq, target, cents, inTune) =>
        freq shouldBe (440.0 +- 0.3)
        target.name shouldBe "A4"
        cents shouldBe (0.0 +- 2.0)
        inTune shouldBe true
      case other =>
        fail(s"Expected PitchResult.Detected but got $other")
    }
  }

  test("Ukulele E4 (329.63 Hz) - Sine with 2nd & 3rd harmonics") {
    val buffer = generateSineBuffer(329.63, amplitude = 0.5f, harmonics = Seq((2.0, 0.25f), (3.0, 0.12f)))
    val result = detector.process(buffer, instrument)

    result match {
      case PitchResult.Detected(freq, target, cents, inTune) =>
        freq shouldBe (329.63 +- 0.5)
        target.name shouldBe "E4"
        inTune shouldBe true
      case other =>
        fail(s"Expected PitchResult.Detected but got $other")
    }
  }

  test("Ukulele C4 (261.63 Hz) - Slightly sharp signal (263.0 Hz)") {
    val buffer = generateSineBuffer(263.0)
    val result = detector.process(buffer, instrument)

    result match {
      case PitchResult.Detected(freq, target, cents, inTune) =>
        freq shouldBe (263.0 +- 0.5)
        target.name shouldBe "C4"
        cents shouldBe (9.0 +- 1.5)
      case other =>
        fail(s"Expected PitchResult.Detected but got $other")
    }
  }

  test("Ukulele G4 (392.00 Hz) - Slightly flat signal (388.0 Hz)") {
    val buffer = generateSineBuffer(388.0)
    val result = detector.process(buffer, instrument)

    result match {
      case PitchResult.Detected(freq, target, cents, inTune) =>
        freq shouldBe (388.0 +- 0.5)
        target.name shouldBe "G4"
        cents shouldBe (-17.5 +- 1.5)
      case other =>
        fail(s"Expected PitchResult.Detected but got $other")
    }
  }

  test("Silence Gating - Audio below threshold returns PitchResult.Silence") {
    val silentBuffer = Array.fill(bufferSize)(0.002f)
    val result = detector.process(silentBuffer, instrument)
    result shouldBe PitchResult.Silence
  }

  test("White Noise Rejection - Unpitched noise returns PitchResult.NoPitch") {
    val rng = new Random(42)
    val noiseBuffer = Array.fill(bufferSize)((rng.nextFloat() * 2.0f - 1.0f) * 0.1f)
    val result = detector.process(noiseBuffer, instrument)
    result shouldBe PitchResult.NoPitch
  }

  test("Performance Benchmark - Processing a 2048-sample buffer takes < 2.5 ms") {
    val buffer = generateSineBuffer(440.0)

    // Warm up JIT
    var i = 0
    while (i < 500) {
      detector.process(buffer, instrument)
      i += 1
    }

    // Benchmark over 100 iterations
    val iterations = 100
    val startTime = System.nanoTime()
    i = 0
    while (i < iterations) {
      detector.process(buffer, instrument)
      i += 1
    }
    val totalTimeNanos = System.nanoTime() - startTime
    val avgTimeMs = (totalTimeNanos / iterations.toDouble) / 1000000.0

    avgTimeMs should be < 2.5
  }
}
