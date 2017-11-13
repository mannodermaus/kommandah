package de.mannodermaus.kommandah.managers.persistence

import de.mannodermaus.kommandah.models.Program
import de.mannodermaus.kommandah.models.ProgramInfo
import io.reactivex.Single

interface PersistenceManager {
  fun listPrograms(): Single<out List<ProgramInfo>>
  fun saveProgram(program: Program, info: ProgramInfo?): Single<ProgramInfo>
}
