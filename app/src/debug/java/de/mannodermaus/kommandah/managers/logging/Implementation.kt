package de.mannodermaus.kommandah.managers.logging

import dagger.Module
import dagger.Provides
import timber.log.Timber

@Module
class DebugLoggingModule {

  @Provides
  fun loggingInitializer(): LoggingInitializer = DebugLoggingInitializer()
}

private class DebugLoggingInitializer : LoggingInitializer {
  override fun init() {
    Timber.plant(Timber.DebugTree())
  }
}
