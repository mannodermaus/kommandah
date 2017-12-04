package de.mannodermaus.kommandah.managers.persistence.impl

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Database
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Query
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.Transaction
import android.arch.persistence.room.TypeConverter
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.Update
import de.mannodermaus.kommandah.App
import de.mannodermaus.kommandah.R
import de.mannodermaus.kommandah.managers.persistence.Base64Factory
import de.mannodermaus.kommandah.managers.persistence.Base64String
import de.mannodermaus.kommandah.managers.persistence.PersistenceManager
import de.mannodermaus.kommandah.models.Instruction
import de.mannodermaus.kommandah.models.PersistedProgram
import de.mannodermaus.kommandah.models.Program
import de.mannodermaus.kommandah.utils.extensions.decodeFromBase64
import de.mannodermaus.kommandah.utils.extensions.encodeToBase64
import io.reactivex.Flowable
import io.reactivex.Single
import org.threeten.bp.Clock
import org.threeten.bp.Instant

// Implementation of the app's Persistence layer
// using the Room Persistence Library:
// https://developer.android.com/topic/libraries/architecture/room.html

class RoomPersistenceManager(
    private val app: App,
    private val base64Factory: Base64Factory,
    private val clock: Clock
) : PersistenceManager {

  private val db: AppDatabase = Room
      .databaseBuilder(app, AppDatabase::class.java, "kommandah")
      .build()

  override fun listRecentPrograms(count: Int): Flowable<out List<PersistedProgram>> {
    return db.programDao()
        .listAll(count)
        .flatMapSingle {
          // Convert each emission's database items into the
          // type exposed to consumers of the API
          Flowable.fromIterable(it)
              .map { it.toPersistedProgram() }
              .toList()
        }
  }

  override fun saveProgram(program: Program, id: Long?, title: String?): Single<PersistedProgram> =
      db.programDao()
          .upsert(
              RoomTableItem(
                  id = id,
                  updated = Instant.now(clock),
                  title = title ?: app.getString(R.string.main_untitledprogram),
                  data = serializeProgram(program, base64Factory)))
          .map { it.toPersistedProgram() }

  private inner class RoomPersistedProgram(
      override val id: Long,
      override val updated: Instant,
      override val title: String,
      private val data: Base64String) : PersistedProgram {

    override fun load(): Single<Program> = Single.fromCallable { deserializeProgram(data, base64Factory) }
  }

  private fun RoomTableItem.toPersistedProgram(): PersistedProgram =
      RoomPersistedProgram(this.id!!, this.updated, this.title, this.data)
}

/* Entities & Helper Functions */

@Entity(tableName = "data")
data class RoomTableItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long?,
    @ColumnInfo
    val updated: Instant,
    @ColumnInfo
    val title: String,
    @ColumnInfo
    val data: String)

/**
 * Serialized format of each instruction: "<index>:<serialized>",
 * e.g. "0:PUSH 12" for "Push(12)" at Line 0
 * or "4:PRINT" for "Print" at Line 4.
 *
 * Multiple instructions are joined together by semicolon ';'.
 */
fun serializeProgram(p: Program, factory: Base64Factory): String = p.instructions
    .filter { it.value != null }
    .map {
      val index = it.key
      val instruction = it.value
      "$index:$instruction"
    }
    .joinToString(separator = ";")
    .encodeToBase64(factory)

/**
 * Converts a string previously serialized by [serializeProgram]
 * back to its [Program] representation.
 */
fun deserializeProgram(g: String, factory: Base64Factory): Program? {
  val instructions = if (g.isNotEmpty()) {
    g.decodeFromBase64(factory)
        .split(delimiters = *arrayOf(";"))
        .map {
          val components = it.split(":")
          val index = components[0].toInt()
          val serialized = components[1]
          index to Instruction.fromString(serialized)
        }
        .associate { it }

  } else {
    emptyMap()
  }
  return Program(instructions)
}

/* Type Converters */

class RoomTypeConverters {
  @TypeConverter
  fun toInstant(value: Long): Instant = Instant.ofEpochMilli(value)

  @TypeConverter
  fun fromInstant(instant: Instant): Long = instant.toEpochMilli()
}


/* DAOs */

@Dao
abstract class ProgramDao {
  @Query("SELECT * FROM data ORDER BY updated DESC LIMIT (:limit)")
  abstract fun listAll(limit: Int): Flowable<List<RoomTableItem>>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  protected abstract fun insertSync(entity: RoomTableItem): Long

  @Update(onConflict = OnConflictStrategy.IGNORE)
  protected abstract fun updateSync(entity: RoomTableItem): Int

  @Query("SELECT * FROM data WHERE id = (:id)")
  protected abstract fun getSync(id: Long): RoomTableItem

  @Transaction
  protected open fun upsertSync(entity: RoomTableItem): RoomTableItem {
    val idToQuery = if (entity.id != null) {
      updateSync(entity)
      entity.id
    } else {
      insertSync(entity)
    }

    return getSync(idToQuery)
  }

  // No idea why Room doesn't support Rx for @Insert functions, but oh well.
  fun upsert(entity: RoomTableItem): Single<RoomTableItem> =
      Single.fromCallable { upsertSync(entity) }
}

/* Room Database */

@Database(
    version = 1,
    entities = [RoomTableItem::class])
@TypeConverters(RoomTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun programDao(): ProgramDao
}
