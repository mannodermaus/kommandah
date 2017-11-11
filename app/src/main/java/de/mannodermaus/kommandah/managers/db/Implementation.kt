package de.mannodermaus.kommandah.managers.db

import dagger.Module

@Module
class PersistenceModule {

  // TODO Prototype with SharedPreferences, or direct usage of a real impl like Room
//  @Provides @Singleton fun persistenceManager(app: App): PersistenceManager =
//      SharedPrefsPersistenceManager(app)
}
