package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DatasheetEntity
import com.example.data.local.entity.ItemEntity
import com.example.data.local.entity.PinoutEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.ScanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
  @Query("SELECT * FROM inventory_items ORDER BY updatedAt DESC")
  fun getAllItems(): Flow<List<ItemEntity>>

  @Query("SELECT * FROM inventory_items WHERE id = :id")
  suspend fun getItemById(id: Long): ItemEntity?

  @Query("SELECT * FROM inventory_items WHERE quantity <= minStock ORDER BY quantity ASC")
  fun getLowStockItems(): Flow<List<ItemEntity>>

  @Query("SELECT COUNT(*) FROM inventory_items")
  fun getItemCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertItem(item: ItemEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(items: List<ItemEntity>)

  @Update
  suspend fun updateItem(item: ItemEntity)

  @Query("UPDATE inventory_items SET quantity = MAX(0, quantity + :delta), updatedAt = :time WHERE id = :id")
  suspend fun adjustQuantity(id: Long, delta: Int, time: Long = System.currentTimeMillis())

  @Delete
  suspend fun deleteItem(item: ItemEntity)

  @Query("DELETE FROM inventory_items WHERE id = :id")
  suspend fun deleteItemById(id: Long)
}

@Dao
interface ScanDao {
  @Query("SELECT * FROM scans ORDER BY timestamp DESC")
  fun getAllScans(): Flow<List<ScanEntity>>

  @Query("SELECT * FROM scans ORDER BY timestamp DESC LIMIT 5")
  fun getRecentScans(): Flow<List<ScanEntity>>

  @Query("SELECT COUNT(*) FROM scans")
  fun getScanCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertScan(scan: ScanEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(scans: List<ScanEntity>)

  @Delete
  suspend fun deleteScan(scan: ScanEntity)

  @Query("DELETE FROM scans WHERE id = :id")
  suspend fun deleteScanById(id: Long)
}

@Dao
interface ProjectDao {
  @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
  fun getAllProjects(): Flow<List<ProjectEntity>>

  @Query("SELECT * FROM projects WHERE status = :status ORDER BY updatedAt DESC")
  fun getProjectsByStatus(status: String): Flow<List<ProjectEntity>>

  @Query("SELECT COUNT(*) FROM projects")
  fun getProjectCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProject(project: ProjectEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(projects: List<ProjectEntity>)

  @Update
  suspend fun updateProject(project: ProjectEntity)

  @Query("UPDATE projects SET status = :newStatus, updatedAt = :time WHERE id = :id")
  suspend fun updateStatus(id: Long, newStatus: String, time: Long = System.currentTimeMillis())

  @Delete
  suspend fun deleteProject(project: ProjectEntity)

  @Query("DELETE FROM projects WHERE id = :id")
  suspend fun deleteProjectById(id: Long)
}

@Dao
interface PinoutDao {
  @Query("SELECT * FROM pinouts ORDER BY componentModel ASC")
  fun getAllPinouts(): Flow<List<PinoutEntity>>

  @Query("SELECT * FROM pinouts WHERE componentModel LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
  fun searchPinouts(query: String): Flow<List<PinoutEntity>>

  @Query("SELECT * FROM pinouts WHERE componentModel = :model LIMIT 1")
  suspend fun findByModel(model: String): PinoutEntity?

  @Query("SELECT COUNT(*) FROM pinouts")
  fun getPinoutCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPinout(pinout: PinoutEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(pinouts: List<PinoutEntity>)

  @Delete
  suspend fun deletePinout(pinout: PinoutEntity)
}

@Dao
interface DatasheetDao {
  @Query("SELECT * FROM datasheets ORDER BY title ASC")
  fun getAllDatasheets(): Flow<List<DatasheetEntity>>

  @Query("SELECT * FROM datasheets WHERE id = :id")
  suspend fun getDatasheetById(id: Long): DatasheetEntity?

  @Query("SELECT COUNT(*) FROM datasheets")
  fun getDatasheetCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDatasheet(datasheet: DatasheetEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(datasheets: List<DatasheetEntity>)

  @Delete
  suspend fun deleteDatasheet(datasheet: DatasheetEntity)
}
