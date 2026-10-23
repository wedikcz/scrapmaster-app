package com.example.mcp

import android.content.Context
import com.example.data.repository.WorkshopRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.security.SecureRandom

enum class McpServerState {
  STOPPED,
  RUNNING,
  ERROR
}

data class McpLogEntry(
  val timestamp: Long = System.currentTimeMillis(),
  val client: String,
  val method: String,
  val status: String,
  val details: String
)

class LocalMcpServer(
  private val context: Context,
  private val repository: WorkshopRepository,
  private val scope: CoroutineScope
) {
  private val prefs = context.getSharedPreferences("scrapmaster_mcp_prefs", Context.MODE_PRIVATE)

  private var serverSocket: ServerSocket? = null
  private var serverJob: Job? = null
  val port = 8765

  private val _serverState = MutableStateFlow(McpServerState.STOPPED)
  val serverState: StateFlow<McpServerState> = _serverState.asStateFlow()

  private val _authToken = MutableStateFlow(loadOrGenerateToken())
  val authToken: StateFlow<String> = _authToken.asStateFlow()

  private val _logs = MutableStateFlow<List<McpLogEntry>>(emptyList())
  val logs: StateFlow<List<McpLogEntry>> = _logs.asStateFlow()

  private fun generateNewRawToken(): String {
    val bytes = ByteArray(24)
    SecureRandom().nextBytes(bytes)
    val token = bytes.joinToString("") { "%02x".format(it) }
    prefs.edit().putString("mcp_bearer_token", token).apply()
    return token
  }

  private fun loadOrGenerateToken(): String {
    val existing = prefs.getString("mcp_bearer_token", null)
    if (existing != null) return existing
    return generateNewRawToken()
  }

  fun regenerateToken(): String {
    val token = generateNewRawToken()
    _authToken.value = token
    return token
  }

  fun startServer() {
    if (_serverState.value == McpServerState.RUNNING) return

    serverJob = scope.launch(Dispatchers.IO) {
      try {
        serverSocket = ServerSocket(port)
        _serverState.value = McpServerState.RUNNING
        addLog("SYSTEM", "START", "200", "MCP Server naslouchá na portu $port")

        while (isActive && serverSocket?.isClosed == false) {
          val clientSocket = try {
            serverSocket?.accept() ?: break
          } catch (_: Exception) {
            break
          }
          launch(Dispatchers.IO) {
            handleClient(clientSocket)
          }
        }
      } catch (e: Exception) {
        _serverState.value = McpServerState.ERROR
        addLog("SYSTEM", "ERROR", "500", e.message ?: "Chyba spuštění")
      }
    }
  }

  fun stopServer() {
    try {
      serverSocket?.close()
      serverJob?.cancel()
    } catch (_: Exception) {}
    _serverState.value = McpServerState.STOPPED
    addLog("SYSTEM", "STOP", "200", "MCP Server byl zastaven")
  }

  private suspend fun handleClient(socket: Socket) = withContext(Dispatchers.IO) {
    try {
      val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
      val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream()), true)

      var line = reader.readLine() ?: return@withContext
      val parts = line.split(" ")
      val method = parts.getOrNull(0) ?: "GET"
      val path = parts.getOrNull(1) ?: "/"

      var contentLength = 0
      var authHeader: String? = null

      while (reader.readLine().also { line = it } != null) {
        if (line.isEmpty()) break
        val lower = line.lowercase()
        if (lower.startsWith("content-length:")) {
          contentLength = line.substring(15).trim().toIntOrNull() ?: 0
        } else if (lower.startsWith("authorization:")) {
          authHeader = line.substring(14).trim()
        }
      }

      val body = if (contentLength > 0) {
        val buffer = CharArray(contentLength)
        reader.read(buffer, 0, contentLength)
        String(buffer)
      } else ""

      // CORS Preflight
      if (method.equals("OPTIONS", ignoreCase = true)) {
        writer.print("HTTP/1.1 204 No Content\r\n")
        writer.print("Access-Control-Allow-Origin: *\r\n")
        writer.print("Access-Control-Allow-Methods: POST, GET, OPTIONS\r\n")
        writer.print("Access-Control-Allow-Headers: Authorization, Content-Type\r\n\r\n")
        writer.flush()
        socket.close()
        return@withContext
      }

      // Check Bearer token authorization
      val expectedToken = "Bearer ${_authToken.value}"
      if (authHeader == null || authHeader != expectedToken) {
        addLog(socket.inetAddress.hostAddress ?: "client", method, "401", "Neplatný nebo chybějící token")
        val errorJson = JSONObject().apply {
          put("error", "Unauthorized")
          put("message", "Vyžadován platný token 'Authorization: Bearer <token>'")
        }.toString()

        writer.print("HTTP/1.1 401 Unauthorized\r\n")
        writer.print("Content-Type: application/json\r\n")
        writer.print("Access-Control-Allow-Origin: *\r\n")
        writer.print("Content-Length: ${errorJson.toByteArray().size}\r\n\r\n")
        writer.print(errorJson)
        writer.flush()
        socket.close()
        return@withContext
      }

      // Handle MCP JSON-RPC
      val responseJson = handleMcpJsonRpc(body)
      addLog(socket.inetAddress.hostAddress ?: "client", "POST /mcp", "200", "Úspěšné volání MCP")

      writer.print("HTTP/1.1 200 OK\r\n")
      writer.print("Content-Type: application/json\r\n")
      writer.print("Access-Control-Allow-Origin: *\r\n")
      writer.print("Content-Length: ${responseJson.toByteArray().size}\r\n\r\n")
      writer.print(responseJson)
      writer.flush()
    } catch (_: Exception) {
    } finally {
      try { socket.close() } catch (_: Exception) {}
    }
  }

  suspend fun executeDirectToolCall(toolName: String, params: JSONObject): JSONObject {
    return when (toolName) {
      "search_inventory" -> {
        val q = params.optString("query", "").lowercase()
        val allItems = repository.allItems.first()
        val matched = allItems.filter { it.name.lowercase().contains(q) || it.category.lowercase().contains(q) }
        val arr = JSONArray()
        matched.forEach { item ->
          arr.put(JSONObject().apply {
            put("id", item.id)
            put("name", item.name)
            put("category", item.category)
            put("quantity", item.quantity)
            put("location", item.location)
            put("packageType", item.packageType)
          })
        }
        JSONObject().put("status", "success").put("count", matched.size).put("items", arr)
      }
      "adjust_stock" -> {
        val itemId = params.optLong("itemId", 0L)
        val delta = params.optInt("delta", 0)
        repository.adjustQuantity(itemId, delta)
        JSONObject().put("status", "success").put("message", "Zásoba upravena o $delta")
      }
      "list_projects" -> {
        val projects = repository.allProjects.first()
        val arr = JSONArray()
        projects.forEach { p ->
          arr.put(JSONObject().apply {
            put("id", p.id)
            put("title", p.title)
            put("status", p.status)
            put("neededParts", p.neededParts)
          })
        }
        JSONObject().put("status", "success").put("projects", arr)
      }
      "lookup_pinout" -> {
        val model = params.optString("model", "")
        val p = repository.findPinoutByModel(model)
        if (p != null) {
          JSONObject().apply {
            put("status", "success")
            put("model", p.componentModel)
            put("package", p.packageType)
            put("pinCount", p.pinCount)
            put("pinsJson", p.pinsJson)
          }
        } else {
          JSONObject().put("status", "not_found").put("message", "Model $model nebyl nalezen")
        }
      }
      "query_rag" -> {
        val q = params.optString("query", "")
        val chunks = repository.searchRagChunks(q, 3)
        val arr = JSONArray()
        chunks.forEach { c ->
          arr.put(JSONObject().apply {
            put("source", c.sourceTitle)
            put("section", c.section)
            put("citation", c.citation)
            put("text", c.text)
          })
        }
        JSONObject().put("status", "success").put("results", arr)
      }
      else -> JSONObject().put("status", "error").put("message", "Neznámý MCP nástroj: $toolName")
    }
  }

  private suspend fun handleMcpJsonRpc(body: String): String {
    return try {
      val req = JSONObject(body)
      val id = req.opt("id") ?: 1
      val method = req.optString("method", "")
      val params = req.optJSONObject("params") ?: JSONObject()

      when (method) {
        "initialize" -> {
          JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", id)
            put("result", JSONObject().apply {
              put("protocolVersion", "2024-11-05")
              put("capabilities", JSONObject().apply {
                put("tools", JSONObject().apply { put("listChanged", true) })
              })
              put("serverInfo", JSONObject().apply {
                put("name", "ScrapMaster MCP Workshop Server")
                put("version", "2.0.0")
              })
            })
          }.toString()
        }
        "tools/list" -> {
          val toolsArray = JSONArray().apply {
            put(JSONObject().apply {
              put("name", "search_inventory")
              put("description", "Hledat součástky a stav zásob v inventáři dílny")
              put("inputSchema", JSONObject().apply {
                put("type", "object")
                put("properties", JSONObject().apply {
                  put("query", JSONObject().apply { put("type", "string"); put("description", "Hledaný název nebo kategorie") })
                })
              })
            })
            put(JSONObject().apply {
              put("name", "adjust_stock")
              put("description", "Zvýšit nebo snížit zásobu součástky (+1 nebo -1)")
              put("inputSchema", JSONObject().apply {
                put("type", "object")
                put("properties", JSONObject().apply {
                  put("itemId", JSONObject().apply { put("type", "number") })
                  put("delta", JSONObject().apply { put("type", "number") })
                })
              })
            })
            put(JSONObject().apply {
              put("name", "list_projects")
              put("description", "Získat seznam běžících dílenských projektů a potřebných dílů")
            })
            put(JSONObject().apply {
              put("name", "lookup_pinout")
              put("description", "Vyhledat pinout a zapojení integrovaného obvodu")
              put("inputSchema", JSONObject().apply {
                put("type", "object")
                put("properties", JSONObject().apply {
                  put("model", JSONObject().apply { put("type", "string") })
                })
              })
            })
            put(JSONObject().apply {
              put("name", "query_rag")
              put("description", "Sémantické vyhledávání v datasheetech a naskenovaných schématech s přesnými citacemi")
              put("inputSchema", JSONObject().apply {
                put("type", "object")
                put("properties", JSONObject().apply {
                  put("query", JSONObject().apply { put("type", "string") })
                })
              })
            })
          }

          JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", id)
            put("result", JSONObject().apply { put("tools", toolsArray) })
          }.toString()
        }
        "tools/call" -> {
          val toolName = params.optString("name", "")
          val args = params.optJSONObject("arguments") ?: JSONObject()
          val toolResult = executeDirectToolCall(toolName, args)

          JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", id)
            put("result", JSONObject().apply {
              put("content", JSONArray().apply {
                put(JSONObject().apply {
                  put("type", "text")
                  put("text", toolResult.toString(2))
                })
              })
            })
          }.toString()
        }
        else -> {
          JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", id)
            put("error", JSONObject().apply {
              put("code", -32601)
              put("message", "Method not found: $method")
            })
          }.toString()
        }
      }
    } catch (e: Exception) {
      JSONObject().apply {
        put("jsonrpc", "2.0")
        put("error", JSONObject().apply {
          put("code", -32700)
          put("message", "Parse error: ${e.message}")
        })
      }.toString()
    }
  }

  private fun addLog(client: String, method: String, status: String, details: String) {
    val newEntry = McpLogEntry(
      client = client,
      method = method,
      status = status,
      details = details
    )
    _logs.value = (_logs.value + newEntry).takeLast(25)
  }
}
