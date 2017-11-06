package de.mannodermaus.kommandah

import dagger.Component

@Component(modules = arrayOf(
    AppModule::class
))
interface AppComponent
