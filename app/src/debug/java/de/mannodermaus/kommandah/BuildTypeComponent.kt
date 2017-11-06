package de.mannodermaus.kommandah

import dagger.Component
import de.mannodermaus.kommandah.managers.logging.DebugLoggingModule
import de.mannodermaus.kommandah.managers.logging.LoggingInitializer

@Component(modules = arrayOf(
    DebugLoggingModule::class
))
interface BuildTypeComponent {
  fun loggingInitializer(): LoggingInitializer
}
