package de.mannodermaus.kommandah.managers.logging

import dagger.Module
import dagger.Provides

@Module
class ReleaseLoggingModule {

  @Provides
  fun loggingInitializer(): LoggingInitializer = ReleaseLoggingInitializer()
}

private class ReleaseLoggingInitializer : LoggingInitializer {
  override fun init() {
    // TODO Install Crashlytics Tree & Analytics
  }
}
