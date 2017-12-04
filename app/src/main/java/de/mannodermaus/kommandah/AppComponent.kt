package de.mannodermaus.kommandah

import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.support.AndroidSupportInjectionModule
import de.mannodermaus.kommandah.managers.persistence.PersistenceModule
import de.mannodermaus.kommandah.managers.runtime.RuntimeModule
import de.mannodermaus.kommandah.managers.time.TimeModule
import de.mannodermaus.kommandah.managers.viewmodel.ViewModelModule
import de.mannodermaus.kommandah.views.main.di.MainActivityModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
  // Contributed by dagger-android
  AndroidInjectionModule::class,
  AndroidSupportInjectionModule::class,

  // Global modules
  AppModule::class,
  TimeModule::class,
  RuntimeModule::class,
  PersistenceModule::class,
  ViewModelModule::class,

  // Screen-specific
  MainActivityModule::class
], dependencies = [
  // Build Type-specific
  BuildTypeComponent::class
])
interface AppComponent {
  fun inject(app: App)
}
