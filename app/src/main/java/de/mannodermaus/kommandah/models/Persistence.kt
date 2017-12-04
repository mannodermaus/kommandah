package de.mannodermaus.kommandah.models

import io.reactivex.Single
import org.threeten.bp.Instant

interface PersistedProgram {
  val id: Long
  val updated: Instant
  val title: String

  fun load(): Single<Program>
}
