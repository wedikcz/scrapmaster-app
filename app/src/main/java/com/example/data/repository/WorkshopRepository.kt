package com.example.data.repository

import com.example.data.local.ScrapMasterDatabase
import com.example.data.local.WorkshopSeedData
import com.example.data.local.entity.DatasheetEntity
import com.example.data.local.entity.ItemEntity
import com.example.data.local.entity.PinoutEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.ScanEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class RagSearchResult(
  val sourceTitle: String,
  val fileName: String,
  val section: String,
  val chunkIndex: Int,
  val text: String,
  val citation: String,
  val relevanceScore: Float
)

class WorkshopRepository(private val database: ScrapMasterDatabase) {

  private val itemDao = database.itemDao()
  private val scanDao = database.scanDao()
  private val projectDao = database.projectDao()
  private val pinoutDao = database.pinoutDao()
  private val datasheetDao = database.datasheetDao()

  // Items
  val allItems: Flow<List<ItemEntity>> = itemDao.getAllItems()
  val lowStockItems: Flow<List<ItemEntity>> = itemDao.getLowStockItems()
  val itemCount: Flow<Int> = itemDao.getItemCount()

  suspend fun insertItem(item: ItemEntity): Long = withContext(Dispatchers.IO) {
    itemDao.insertItem(item)
  }

  suspend fun updateItem(item: ItemEntity) = withContext(Dispatchers.IO) {
    itemDao.updateItem(item)
  }

  suspend fun adjustQuantity(id: Long, delta: Int) = withContext(Dispatchers.IO) {
    itemDao.adjustQuantity(id, delta)
  }

  suspend fun deleteItem(id: Long) = withContext(Dispatchers.IO) {
    itemDao.deleteItemById(id)
  }

  // Scans
  val allScans: Flow<List<ScanEntity>> = scanDao.getAllScans()
  val recentScans: Flow<List<ScanEntity>> = scanDao.getRecentScans()
  val scanCount: Flow<Int> = scanDao.getScanCount()

  suspend fun insertScan(scan: ScanEntity): Long = withContext(Dispatchers.IO) {
    scanDao.insertScan(scan)
  }

  suspend fun deleteScan(id: Long) = withContext(Dispatchers.IO) {
    scanDao.deleteScanById(id)
  }

  // Projects
  val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()
  val projectCount: Flow<Int> = projectDao.getProjectCount()

  fun getProjectsByStatus(status: String): Flow<List<ProjectEntity>> =
    projectDao.getProjectsByStatus(status)

  suspend fun insertProject(project: ProjectEntity): Long = withContext(Dispatchers.IO) {
    projectDao.insertProject(project)
  }

  suspend fun updateProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
    projectDao.updateProject(project)
  }

  suspend fun updateProjectStatus(id: Long, newStatus: String) = withContext(Dispatchers.IO) {
    projectDao.updateStatus(id, newStatus)
  }

  suspend fun deleteProject(id: Long) = withContext(Dispatchers.IO) {
    projectDao.deleteProjectById(id)
  }

  // Pinouts
  val allPinouts: Flow<List<PinoutEntity>> = pinoutDao.getAllPinouts()
  val pinoutCount: Flow<Int> = pinoutDao.getPinoutCount()

  fun searchPinouts(query: String): Flow<List<PinoutEntity>> =
    pinoutDao.searchPinouts(query)

  suspend fun findPinoutByModel(model: String): PinoutEntity? = withContext(Dispatchers.IO) {
    pinoutDao.findByModel(model)
  }

  suspend fun insertPinout(pinout: PinoutEntity): Long = withContext(Dispatchers.IO) {
    pinoutDao.insertPinout(pinout)
  }

  // Datasheets & Local RAG
  val allDatasheets: Flow<List<DatasheetEntity>> = datasheetDao.getAllDatasheets()
  val datasheetCount: Flow<Int> = datasheetDao.getDatasheetCount()

  suspend fun insertDatasheet(datasheet: DatasheetEntity): Long = withContext(Dispatchers.IO) {
    datasheetDao.insertDatasheet(datasheet)
  }

  suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
    val items = itemDao.getAllItems().first()
    if (items.isEmpty()) {
      itemDao.insertAll(WorkshopSeedData.getInitialItems())
      pinoutDao.insertAll(WorkshopSeedData.getInitialPinouts())
      projectDao.insertAll(WorkshopSeedData.getInitialProjects())
      datasheetDao.insertAll(WorkshopSeedData.getInitialDatasheets())
      scanDao.insertAll(WorkshopSeedData.getInitialScans())
    }
  }

  // Local RAG Retrieval Engine over Datasheet Chunks
  suspend fun searchRagChunks(query: String, topK: Int = 3): List<RagSearchResult> =
    withContext(Dispatchers.IO) {
      val datasheets = datasheetDao.getAllDatasheets().first()
      val queryTokens = query.lowercase().split("\\s+".toRegex()).filter { it.length > 2 }
      val results = mutableListOf<RagSearchResult>()

      for (doc in datasheets) {
        try {
          val chunksArray = JSONArray(doc.chunksJson)
          for (i in 0 until chunksArray.length()) {
            val chunkObj = chunksArray.getJSONObject(i)
            val chunkIndex = chunkObj.optInt("chunkIndex", i + 1)
            val section = chunkObj.optString("section", "Oddíl $chunkIndex")
            val text = chunkObj.optString("text", "")

            var score = 0f
            val lowerText = text.lowercase()
            val lowerSection = section.lowercase()
            val lowerTitle = doc.title.lowercase()

            for (token in queryTokens) {
              if (lowerText.contains(token)) score += 2.0f
              if (lowerSection.contains(token)) score += 3.0f
              if (lowerTitle.contains(token)) score += 4.0f
            }

            if (score > 0f) {
              val citation = "[zdroj: ${doc.fileName}, část $chunkIndex]"
              results.add(
                RagSearchResult(
                  sourceTitle = doc.title,
                  fileName = doc.fileName,
                  section = section,
                  chunkIndex = chunkIndex,
                  text = text,
                  citation = citation,
                  relevanceScore = score
                )
              )
            }
          }
        } catch (_: Exception) {
          // Skip malformed chunk JSON
        }
      }

      results.sortedByDescending { it.relevanceScore }.take(topK)
    }
}
