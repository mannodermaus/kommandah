package de.mannodermaus.kommandah.test.mocks.logging

import dagger.Module
import dagger.Provides
import de.mannodermaus.kommandah.managers.logging.LoggingInitializer
import javax.inject.Singleton

@Module
class TestLoggingModule {
  @Provides
  @Singleton
  fun loggingInitializer() = object : LoggingInitializer {
    override fun init() {
    }
  }
}
