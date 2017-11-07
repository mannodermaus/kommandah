package de.mannodermaus.kommandah.views.main

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainActivityModule {
  @ContributesAndroidInjector
  abstract fun mainActivity(): MainActivity
}
