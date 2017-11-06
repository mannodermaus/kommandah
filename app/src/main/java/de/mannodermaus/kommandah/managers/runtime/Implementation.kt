package de.mannodermaus.kommandah.managers.runtime

import dagger.Module
import dagger.Provides
import de.mannodermaus.kommandah.models.ExecutionEnvironment
import de.mannodermaus.kommandah.models.OutputEvent
import de.mannodermaus.kommandah.models.Program
import io.reactivex.Flowable
import java.util.concurrent.TimeUnit

@Module
class RuntimeModule {

  @Provides
  fun interpreter(): Interpreter = NaiveInterpreter()
}

private class NaiveInterpreter : Interpreter {
  override fun execute(program: Program, env: ExecutionEnvironment): Flowable<OutputEvent> =
      Flowable.fromIterable(program)
          .delay(env.speed.toMillis(), TimeUnit.MILLISECONDS)
          .map { OutputEvent(it) }
}
