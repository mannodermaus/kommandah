package de.mannodermaus.kommandah.test.mocks.persistence

import de.mannodermaus.kommandah.managers.persistence.PersistenceManager
import de.mannodermaus.kommandah.models.Program
import de.mannodermaus.kommandah.models.ProgramInfo
import io.reactivex.Single

class TestPersistenceManager : PersistenceManager {
  override fun listPrograms(): Single<List<ProgramInfo>> {
    TODO("not implemented")
  }

  override fun saveProgram(program: Program, info: ProgramInfo?): Single<ProgramInfo> {
    TODO("not implemented")
  }
}
