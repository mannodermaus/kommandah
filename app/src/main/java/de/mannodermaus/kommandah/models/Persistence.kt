package de.mannodermaus.kommandah.models

import io.reactivex.Single
import org.threeten.bp.Instant

interface ProgramInfo {
  val id: Long
  val updated: Instant
  val title: String

  fun load(): Single<Program>
}
