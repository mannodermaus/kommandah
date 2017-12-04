package de.mannodermaus.kommandah.managers.runtime

import de.mannodermaus.kommandah.models.ExecutionEnvironment
import de.mannodermaus.kommandah.models.ProgramOutput
import de.mannodermaus.kommandah.models.Program
import io.reactivex.Flowable

/**
 * Executor of Program instructions defined inside the app.
 */
interface Interpreter {
  fun execute(program: Program, env: ExecutionEnvironment): Flowable<ProgramOutput>
}
