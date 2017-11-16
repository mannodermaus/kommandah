package de.mannodermaus.kommandah.test.mocks.persistence

import de.mannodermaus.kommandah.managers.persistence.PersistenceManager
import de.mannodermaus.kommandah.models.Program
import de.mannodermaus.kommandah.models.ProgramInfo
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import org.threeten.bp.Clock
import org.threeten.bp.Instant

/**
 * Implementation of the PersistenceManager interface for unit tests,
 * using a
 */
class TestPersistenceManager(private val clock: Clock) : PersistenceManager {

  private var nextId = 0L

  val infos = BehaviorSubject.createDefault(linkedMapOf<Long, ProgramInfo>())
  val programs = linkedMapOf<Long, Program>()

  override fun listRecentPrograms(count: Int): Flowable<out List<ProgramInfo>> =
      infos.flatMapSingle {
        Flowable.fromIterable(it.values)
            .take(count.toLong())
            .toList()
      }
          .toFlowable(BackpressureStrategy.LATEST)

  override fun saveProgram(program: Program, id: Long?, title: String?): Single<ProgramInfo> =
      Single.fromCallable {
        if (id != null) {
          infos.value[id.toLong()]
        } else {
          val info = TestProgramInfo(id ?: nextId++, Instant.now(clock), title ?: "Untitled")
          infos.value[info.id] = info
          programs[info.id] = program
          info
        }
      }

  /* Private */

  private inner class TestProgramInfo(
      override val id: Long,
      override val updated: Instant,
      override val title: String
  ) : ProgramInfo {
    override fun load(): Single<Program> = Single.fromCallable { programs[id] }
  }
}
