package de.mannodermaus.kommandah.managers.runtime

import dagger.Module
import dagger.Provides
import de.mannodermaus.kommandah.models.ExecutionEnvironment
import de.mannodermaus.kommandah.models.OutputEvent
import de.mannodermaus.kommandah.models.Program
import io.reactivex.Flowable
import io.reactivex.rxkotlin.toFlowable
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class RuntimeModule {

  @Singleton
  @Provides
  fun interpreter(): Interpreter = NaiveInterpreter()
}

private class NaiveInterpreter : Interpreter {
  override fun execute(program: Program, env: ExecutionEnvironment): Flowable<OutputEvent> =
      program.run().toFlowable()
          .delay(env.speed.toMillis(), TimeUnit.MILLISECONDS)
}