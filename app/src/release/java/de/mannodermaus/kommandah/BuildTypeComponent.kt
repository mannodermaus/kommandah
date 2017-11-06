package de.mannodermaus.kommandah

import dagger.Component
import de.mannodermaus.kommandah.managers.logging.LoggingInitializer
import de.mannodermaus.kommandah.managers.logging.ReleaseLoggingModule

@Component(modules = arrayOf(
    ReleaseLoggingModule::class
))
interface BuildTypeComponent {
  fun loggingInitializer(): LoggingInitializer
}
