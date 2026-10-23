package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.DatasheetDao
import com.example.data.local.dao.ItemDao
import com.example.data.local.dao.PinoutDao
import com.example.data.local.dao.ProjectDao
import com.example.data.local.dao.ScanDao
import com.example.data.local.entity.DatasheetEntity
import com.example.data.local.entity.ItemEntity
import com.example.data.local.entity.PinoutEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.ScanEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    ItemEntity::class,
    ScanEntity::class,
    ProjectEntity::class,
    PinoutEntity::class,
    DatasheetEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class ScrapMasterDatabase : RoomDatabase() {

  abstract fun itemDao(): ItemDao
  abstract fun scanDao(): ScanDao
  abstract fun projectDao(): ProjectDao
  abstract fun pinoutDao(): PinoutDao
  abstract fun datasheetDao(): DatasheetDao

  companion object {
    @Volatile
    private var INSTANCE: ScrapMasterDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): ScrapMasterDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          ScrapMasterDatabase::class.java,
          "scrapmaster_workshop.db"
        )
          .fallbackToDestructiveMigration(dropAllTables = true)
          .addCallback(DatabaseCallback(scope))
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback(
      private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            populateDatabase(database)
          }
        }
      }

      suspend fun populateDatabase(database: ScrapMasterDatabase) {
        database.itemDao().insertAll(WorkshopSeedData.getInitialItems())
        database.pinoutDao().insertAll(WorkshopSeedData.getInitialPinouts())
        database.projectDao().insertAll(WorkshopSeedData.getInitialProjects())
        database.datasheetDao().insertAll(WorkshopSeedData.getInitialDatasheets())
        database.scanDao().insertAll(WorkshopSeedData.getInitialScans())
      }
    }
  }
}
