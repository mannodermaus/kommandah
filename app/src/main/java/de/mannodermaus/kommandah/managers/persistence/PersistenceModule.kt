package de.mannodermaus.kommandah.managers.persistence

import dagger.Module
import dagger.Provides
import de.mannodermaus.kommandah.App
import de.mannodermaus.kommandah.managers.persistence.impl.AndroidBase64Factory
import de.mannodermaus.kommandah.managers.persistence.impl.RoomPersistenceManager
import javax.inject.Singleton

@Module
class PersistenceModule {

  @Provides
  @Singleton
  fun persistenceManager(app: App, base64Factory: Base64Factory): PersistenceManager =
      RoomPersistenceManager(app, base64Factory)

  @Provides
  @Singleton
  fun base64Factory(): Base64Factory = AndroidBase64Factory()
}
