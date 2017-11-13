package de.mannodermaus.kommandah

import android.app.Activity
import android.app.Application
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import de.mannodermaus.kommandah.utils.di.setupInjectors
import de.mannodermaus.kommandah.managers.logging.LoggingInitializer
import de.mannodermaus.kommandah.managers.time.TimeInitializer
import javax.inject.Inject

open class App : Application(), HasActivityInjector {

  @Inject lateinit var injector: DispatchingAndroidInjector<Activity>
  @Inject lateinit var timeInitializer: TimeInitializer
  @Inject lateinit var loggingInitializer: LoggingInitializer

  val component: AppComponent = createComponent()

  open protected fun createComponent() = DaggerAppComponent.builder()
      .buildTypeComponent(DaggerBuildTypeComponent.create())
      .appModule(AppModule(this))
      .build()

  override fun onCreate() {
    super.onCreate()

    // Initialize Dependency Injection & perform one-time initialization
    this.setupInjectors()
    timeInitializer.init()
    loggingInitializer.init()
  }

  override fun activityInjector(): AndroidInjector<Activity> = injector
}
