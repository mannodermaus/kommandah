package de.mannodermaus.kommandah.managers.persistence

import de.mannodermaus.kommandah.models.Program
import de.mannodermaus.kommandah.models.ProgramInfo
import io.reactivex.Flowable
import io.reactivex.Single

interface PersistenceManager {
  fun listRecentPrograms(count: Int): Flowable<out List<ProgramInfo>>
  fun saveProgram(program: Program, id: Long?, title: String?): Single<ProgramInfo>
}
