package de.mannodermaus.kommandah

import dagger.Module
import dagger.Provides

@Module
class AppModule(private val app: App) {

  @Provides
  fun app() = app
}
