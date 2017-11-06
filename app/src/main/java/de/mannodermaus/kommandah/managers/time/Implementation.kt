package de.mannodermaus.kommandah.managers.time

import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.Module
import dagger.Provides
import de.mannodermaus.kommandah.App

@Module
class TimeModule {

  @Provides
  fun timeInitializer(app: App): TimeInitializer = AndroidTimeInitializer(app)
}

class AndroidTimeInitializer(val app: App) : TimeInitializer {
  override fun init() {
    AndroidThreeTen.init(app)
  }
}
