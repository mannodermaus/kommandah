package de.mannodermaus.kommandah.managers.time

import dagger.Module
import dagger.Provides
import de.mannodermaus.kommandah.App
import org.threeten.bp.Clock
import javax.inject.Singleton

@Module
class TimeModule {

  @Singleton
  @Provides
  fun timeInitializer(app: App): TimeInitializer = AndroidTimeInitializer(app)

  @Singleton
  @Provides
  fun clock(): Clock = Clock.systemDefaultZone()
}
