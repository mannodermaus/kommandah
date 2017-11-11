package de.mannodermaus.kommandah.test.managers.runtime

import de.mannodermaus.kommandah.managers.runtime.Interpreter
import de.mannodermaus.kommandah.models.ExecutionEnvironment
import de.mannodermaus.kommandah.models.Program
import de.mannodermaus.kommandah.models.ProgramOutput
import io.reactivex.Flowable
import io.reactivex.rxkotlin.toFlowable

/**
 * Interpreter implementation for unit tests.
 * Basically disregards the throttling mechanics of the default implementation,
 * so that Programs are run "all at once".
 */
class InstantInterpreter : Interpreter {
  override fun execute(program: Program, env: ExecutionEnvironment): Flowable<ProgramOutput> =
      program.run().toFlowable()
}
