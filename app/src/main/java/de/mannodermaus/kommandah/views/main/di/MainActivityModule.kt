package de.mannodermaus.kommandah.views.main.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.mannodermaus.kommandah.views.main.MainActivity

@Module
abstract class MainActivityModule {
  @ContributesAndroidInjector
  abstract fun mainActivity(): MainActivity
}
