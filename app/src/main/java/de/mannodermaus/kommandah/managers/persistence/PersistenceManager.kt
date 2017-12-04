package de.mannodermaus.kommandah.managers.persistence

import de.mannodermaus.kommandah.models.Program
import de.mannodermaus.kommandah.models.PersistedProgram
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Mediator between the application's state of Program objects
 * and a persisted storage, e.g. a database or Shared Preferences.
 */
interface PersistenceManager {
  fun listRecentPrograms(count: Int): Flowable<out List<PersistedProgram>>
  fun saveProgram(program: Program, id: Long?, title: String?): Single<PersistedProgram>
}
