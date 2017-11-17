package de.mannodermaus.kommandah.test

import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.support.AndroidSupportInjectionModule
import de.mannodermaus.kommandah.AppComponent
import de.mannodermaus.kommandah.AppModule
import de.mannodermaus.kommandah.BuildTypeComponent
import de.mannodermaus.kommandah.managers.logging.DebugLoggingModule
import de.mannodermaus.kommandah.managers.runtime.RuntimeModule
import de.mannodermaus.kommandah.managers.time.TimeModule
import de.mannodermaus.kommandah.managers.viewmodel.ViewModelModule
import de.mannodermaus.kommandah.test.mocks.logging.TestLoggingModule
import de.mannodermaus.kommandah.test.mocks.persistence.TestPersistenceModule
import de.mannodermaus.kommandah.test.utils.InjectExtension
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(
    // Contributed by dagger-android
    AndroidInjectionModule::class,
    AndroidSupportInjectionModule::class,

    // Global modules
    AppModule::class,
    TimeModule::class,
    RuntimeModule::class,
    TestPersistenceModule::class,
    ViewModelModule::class

), dependencies = arrayOf(
    // Build Type-specific
    BuildTypeComponent::class
))
interface TestAppComponent : AppComponent {
  fun inject(extension: InjectExtension)
}

@Singleton
@Component(modules = arrayOf(
    TestLoggingModule::class
))
interface TestBuildTypeComponent : BuildTypeComponent
