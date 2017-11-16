package de.mannodermaus.kommandah.test.mocks.persistence

import dagger.Module
import dagger.Provides
import de.mannodermaus.kommandah.managers.persistence.Base64Factory
import de.mannodermaus.kommandah.managers.persistence.PersistenceManager
import org.threeten.bp.Clock
import javax.inject.Singleton

@Module
class TestPersistenceModule {

  @Provides @Singleton
  fun persistenceManager(clock: Clock): PersistenceManager = TestPersistenceManager(clock)

  @Provides @Singleton
  fun base64Factory(): Base64Factory = Java8Base64Factory()
}
