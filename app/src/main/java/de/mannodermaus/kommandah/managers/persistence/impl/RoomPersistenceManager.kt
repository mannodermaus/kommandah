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
import de.mannodermaus.kommandah.App
import de.mannodermaus.kommandah.managers.persistence.Base64Factory
import de.mannodermaus.kommandah.managers.persistence.Base64String
import de.mannodermaus.kommandah.managers.persistence.PersistenceManager
import de.mannodermaus.kommandah.models.Instruction
import de.mannodermaus.kommandah.models.Program
import de.mannodermaus.kommandah.models.ProgramInfo
import de.mannodermaus.kommandah.utils.extensions.decodeFromBase64
import de.mannodermaus.kommandah.utils.extensions.encodeToBase64
import io.reactivex.Flowable
import io.reactivex.Single

// Implementation of the app's Persistence layer
// using the Room Persistence Library:
// https://developer.android.com/topic/libraries/architecture/room.html

class RoomPersistenceManager(
    app: App,
    private val base64Factory: Base64Factory) : PersistenceManager {

  private val db: AppDatabase = Room
      .databaseBuilder(app, AppDatabase::class.java, "kommandah")
      .build()

  override fun listPrograms(): Single<out List<ProgramInfo>> {
    return db.programDao()
        .listAll()
        .map { toProgramInfo(it) }
        .toList()
  }

  override fun saveProgram(program: Program, info: ProgramInfo?): Single<ProgramInfo> =
      db.programDao()
          .insert(
              RoomTableItem(
                  id = info?.id,
                  title = info?.title ?: "untitled",
                  data = serializeProgram(program, base64Factory)))
          .map { toProgramInfo(it) }

  private fun toProgramInfo(entity: RoomTableItem): ProgramInfo =
      RoomProgramInfo(entity.id!!, entity.title, entity.data)

  private inner class RoomProgramInfo(
      override val id: Int,
      override val title: String,
      private val data: Base64String) : ProgramInfo {

    override fun load(): Single<Program> = Single.fromCallable { deserializeProgram(data, base64Factory) }
  }
}

/* Entities & Helper Functions */

@Entity(tableName = "data")
data class RoomTableItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,
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
    .map {
      val index = it.key
      val instruction = it.value
      "$index:$instruction"
    }
    .joinToString(separator = ";")
    .encodeToBase64(factory)

fun deserializeProgram(g: String, factory: Base64Factory): Program? {
  val instructions = g.decodeFromBase64(factory)
      .split(delimiters = ";")
      .map {
        val components = it.split(":")
        val index = components[0].toInt()
        val serialized = components[1]
        index to Instruction.fromString(serialized)
      }
      .associate { it }
  return Program(instructions)
}

/* DAOs */

@Dao
abstract class ProgramDao {
  @Query("SELECT * FROM data")
  abstract fun listAll(): Flowable<RoomTableItem>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract fun insertSync(entity: RoomTableItem): Long

  @Query("SELECT * FROM data WHERE id = (:id)")
  abstract fun getSync(id: Long): RoomTableItem

  @Transaction
  open fun insertAndGetSync(entity: RoomTableItem): RoomTableItem {
    val id = insertSync(entity)
    return getSync(id)
  }

  // No idea why Room doesn't support Rx for @Insert functions, but oh well.
  fun insert(entity: RoomTableItem): Single<RoomTableItem> =
      Single.fromCallable { insertAndGetSync(entity) }
}

/* Room Database */

@Database(
    version = 1,
    entities = arrayOf(RoomTableItem::class))
abstract class AppDatabase : RoomDatabase() {
  abstract fun programDao(): ProgramDao
}
