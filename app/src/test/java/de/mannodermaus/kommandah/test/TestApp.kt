package de.mannodermaus.kommandah.test

import de.mannodermaus.kommandah.App
import de.mannodermaus.kommandah.AppComponent
import de.mannodermaus.kommandah.AppModule

class TestApp : App() {
  override fun createComponent(): AppComponent =
      DaggerTestAppComponent.builder()
          .appModule(AppModule(this))
          .buildTypeComponent(DaggerTestBuildTypeComponent.create())
          .build()
}
