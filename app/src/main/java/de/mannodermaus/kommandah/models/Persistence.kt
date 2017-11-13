package de.mannodermaus.kommandah.models

import io.reactivex.Single

interface ProgramInfo {
  val id: Int
  val title: String

  fun load(): Single<Program>
}
