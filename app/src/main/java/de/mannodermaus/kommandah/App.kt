package de.mannodermaus.kommandah

import android.app.Application

class App : Application() {

  protected val component: AppComponent = createComponent()

  protected fun createComponent() = DaggerAppComponent.builder()
      .appModule(AppModule(this))
      .build()
}
