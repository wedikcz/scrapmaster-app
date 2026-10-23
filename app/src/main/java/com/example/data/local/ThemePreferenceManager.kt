package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
  DARK,
  LIGHT,
  SYSTEM
}

class ThemePreferenceManager(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("scrapmaster_prefs", Context.MODE_PRIVATE)

  private val _themeMode = MutableStateFlow(loadSavedTheme())
  val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

  private fun loadSavedTheme(): ThemeMode {
    val raw = prefs.getString(KEY_THEME_MODE, ThemeMode.DARK.name) ?: ThemeMode.DARK.name
    return try {
      ThemeMode.valueOf(raw)
    } catch (_: Exception) {
      ThemeMode.DARK
    }
  }

  fun setThemeMode(mode: ThemeMode) {
    prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    _themeMode.value = mode
  }

  fun toggleTheme() {
    val nextMode = when (_themeMode.value) {
      ThemeMode.DARK -> ThemeMode.LIGHT
      ThemeMode.LIGHT -> ThemeMode.DARK
      ThemeMode.SYSTEM -> ThemeMode.DARK
    }
    setThemeMode(nextMode)
  }

  companion object {
    private const val KEY_THEME_MODE = "key_app_theme_mode"

    @Volatile
    private var INSTANCE: ThemePreferenceManager? = null

    fun getInstance(context: Context): ThemePreferenceManager {
      return INSTANCE ?: synchronized(this) {
        val instance = ThemePreferenceManager(context.applicationContext)
        INSTANCE = instance
        instance
      }
    }
  }
}
