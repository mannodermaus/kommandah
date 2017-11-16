package de.mannodermaus.kommandah.managers.runtime

import de.mannodermaus.kommandah.models.ExecutionEnvironment
import de.mannodermaus.kommandah.models.Program
import de.mannodermaus.kommandah.models.ProgramOutput
import io.reactivex.Flowable
import io.reactivex.rxkotlin.toFlowable
import io.reactivex.rxkotlin.zipWith
import java.util.concurrent.TimeUnit

class NaiveInterpreter : Interpreter {
  override fun execute(program: Program, env: ExecutionEnvironment): Flowable<ProgramOutput> =
      program.run().toFlowable()
          // Throttle each item using the ExecutionEnvironment
          // by zipping with an infinite interval emitter
          // whose items aren't actually used
          .zipWith(Flowable.interval(
              0, env.speed.toMillis(), TimeUnit.MILLISECONDS), { e, _ -> e })
}
