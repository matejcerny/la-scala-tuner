package tuner.core

case class StringTarget(name: String, targetFrequency: Double)

case class Instrument(name: String, strings: IndexedSeq[StringTarget])

object Instrument:
  val StandardUkulele: Instrument = Instrument(
    "Ukulele",
    IndexedSeq(
      StringTarget("G4", 392.00),
      StringTarget("C4", 261.63),
      StringTarget("E4", 329.63),
      StringTarget("A4", 440.00)
    )
  )

sealed trait PitchResult

object PitchResult:
  case object Silence extends PitchResult
  case object NoPitch extends PitchResult

  case class Detected(
      frequency: Double,
      nearestString: StringTarget,
      centsDeviation: Double,
      isInTune: Boolean
  ) extends PitchResult
