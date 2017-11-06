package de.mannodermaus.kommandah

import android.app.Application

class App : Application() {

  protected val component: AppComponent = createComponent()

  protected fun createComponent() = DaggerAppComponent.builder()
      .buildTypeComponent(DaggerBuildTypeComponent.create())
      .appModule(AppModule(this))
      .build()

  override fun onCreate() {
    super.onCreate()

    // One-Time Initialization
    component.timeInitializer().init()
    component.loggingInitializer().init()
  }
}
