package de.mannodermaus.kommandah.managers.runtime

import dagger.Module
import dagger.Provides
import de.mannodermaus.kommandah.models.ExecutionEnvironment
import de.mannodermaus.kommandah.models.ProgramOutput
import de.mannodermaus.kommandah.models.Program
import io.reactivex.Flowable
import io.reactivex.rxkotlin.toFlowable
import io.reactivex.rxkotlin.zipWith
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class RuntimeModule {

  @Singleton
  @Provides
  fun interpreter(): Interpreter = NaiveInterpreter()
}

private class NaiveInterpreter : Interpreter {
  override fun execute(program: Program, env: ExecutionEnvironment): Flowable<ProgramOutput> =
      program.run().toFlowable()
          // Throttle each item using the ExecutionEnvironment
          // by zipping with an infinite interval emitter
          // whose items aren't actually used
          .zipWith(Flowable.interval(
              env.speed.toMillis(), TimeUnit.MILLISECONDS), { e, _ -> e })
          .onErrorReturn { ProgramOutput.Error(it) }
}
