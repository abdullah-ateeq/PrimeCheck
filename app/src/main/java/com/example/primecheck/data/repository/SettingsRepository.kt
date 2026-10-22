package com.example.primecheck.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.primecheck.model.AppTheme
import com.example.primecheck.model.CheckMode
import com.example.primecheck.model.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(
    private val prefs: SharedPreferences
) {
    constructor(context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<UserSettings> = _settingsFlow.asStateFlow()

    companion object {
        private const val PREFS_NAME = "prime_check_settings"
        private const val KEY_THEME = "theme"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_HAPTICS = "haptics"
        private const val KEY_ANIMATIONS = "animations"
        private const val KEY_CONFIRM_DELETE = "confirm_delete"
        private const val KEY_SHOW_FACTORS = "show_factors"
        private const val KEY_SHOW_FACTOR_PAIRS = "show_factor_pairs"
        private const val KEY_SHOW_CALC_STEPS = "show_calc_steps"
        private const val KEY_LAST_INPUT = "last_input"
        private const val KEY_LAST_MODE = "last_mode"
    }

    fun loadSettings(): UserSettings {
        val themeStr = prefs.getString(KEY_THEME, AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
        val theme = try {
            AppTheme.valueOf(themeStr)
        } catch (e: Exception) {
            AppTheme.SYSTEM
        }
        return UserSettings(
            theme = theme,
            dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, true),
            hapticFeedback = prefs.getBoolean(KEY_HAPTICS, true),
            animations = prefs.getBoolean(KEY_ANIMATIONS, true),
            confirmDelete = prefs.getBoolean(KEY_CONFIRM_DELETE, true),
            showFactors = prefs.getBoolean(KEY_SHOW_FACTORS, true),
            showFactorPairs = prefs.getBoolean(KEY_SHOW_FACTOR_PAIRS, true),
            showCalculationSteps = prefs.getBoolean(KEY_SHOW_CALC_STEPS, true)
        )
    }

    fun updateSettings(settings: UserSettings) {
        prefs.edit()
            .putString(KEY_THEME, settings.theme.name)
            .putBoolean(KEY_DYNAMIC_COLOR, settings.dynamicColor)
            .putBoolean(KEY_HAPTICS, settings.hapticFeedback)
            .putBoolean(KEY_ANIMATIONS, settings.animations)
            .putBoolean(KEY_CONFIRM_DELETE, settings.confirmDelete)
            .putBoolean(KEY_SHOW_FACTORS, settings.showFactors)
            .putBoolean(KEY_SHOW_FACTOR_PAIRS, settings.showFactorPairs)
            .putBoolean(KEY_SHOW_CALC_STEPS, settings.showCalculationSteps)
            .apply()
        _settingsFlow.value = settings
    }

    fun resetSettings(): UserSettings {
        val defaultSettings = UserSettings()
        updateSettings(defaultSettings)
        return defaultSettings
    }

    fun getLastInput(): String {
        return prefs.getString(KEY_LAST_INPUT, "") ?: ""
    }

    fun setLastInput(input: String) {
        prefs.edit().putString(KEY_LAST_INPUT, input).apply()
    }

    fun getLastMode(): CheckMode {
        val modeStr = prefs.getString(KEY_LAST_MODE, CheckMode.SINGLE.name) ?: CheckMode.SINGLE.name
        return try {
            CheckMode.valueOf(modeStr)
        } catch (e: Exception) {
            CheckMode.SINGLE
        }
    }

    fun setLastMode(mode: CheckMode) {
        prefs.edit().putString(KEY_LAST_MODE, mode.name).apply()
    }
}
