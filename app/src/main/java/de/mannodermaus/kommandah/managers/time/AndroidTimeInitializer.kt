package de.mannodermaus.kommandah.managers.time

import com.jakewharton.threetenabp.AndroidThreeTen
import de.mannodermaus.kommandah.App

class AndroidTimeInitializer(val app: App) : TimeInitializer {
  override fun init() {
    AndroidThreeTen.init(app)
  }
}
