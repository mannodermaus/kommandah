package de.mannodermaus.kommandah.managers.db

import de.mannodermaus.kommandah.models.Program
import de.mannodermaus.kommandah.models.ProgramInfo
import io.reactivex.Single

interface PersistenceManager {
  fun listPrograms(): Single<List<ProgramInfo>>
  fun loadProgram(info: ProgramInfo): Single<Program>
  fun saveProgram(program: Program, info: ProgramInfo?): Single<ProgramInfo>
}
