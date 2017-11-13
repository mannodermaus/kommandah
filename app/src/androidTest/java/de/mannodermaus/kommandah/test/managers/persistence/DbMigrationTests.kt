package de.mannodermaus.kommandah.test.managers.persistence

import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory
import android.arch.persistence.room.testing.MigrationTestHelper
import android.support.test.InstrumentationRegistry
import de.mannodermaus.kommandah.managers.persistence.impl.AppDatabase
import org.junit.Rule

class DbMigrationTests {
  @Rule
  val helper = MigrationTestHelper(
      InstrumentationRegistry.getInstrumentation(),
      AppDatabase::class.qualifiedName,
      FrameworkSQLiteOpenHelperFactory())

  // Whenever the database version is upgraded,
  // insert a test to validate the migration
}
