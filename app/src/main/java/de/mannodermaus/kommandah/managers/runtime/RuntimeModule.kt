package de.mannodermaus.kommandah.managers.runtime

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RuntimeModule {

  @Singleton
  @Provides
  fun interpreter(): Interpreter = NaiveInterpreter()
}
