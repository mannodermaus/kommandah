package de.mannodermaus.kommandah

import dagger.Component
import de.mannodermaus.kommandah.managers.logging.LoggingInitializer
import de.mannodermaus.kommandah.managers.runtime.RuntimeModule
import de.mannodermaus.kommandah.managers.time.TimeInitializer
import de.mannodermaus.kommandah.managers.time.TimeModule

@Component(modules = arrayOf(
    AppModule::class,
    TimeModule::class,
    RuntimeModule::class
), dependencies = arrayOf(
    BuildTypeComponent::class
))
interface AppComponent {
  fun timeInitializer(): TimeInitializer
  fun loggingInitializer(): LoggingInitializer
}
