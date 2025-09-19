package com.eosphor.nonameradio.compose.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.eosphor.nonameradio.RadioDroidApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeName: String = "light",
    val circularIcons: Boolean = false,
    val compactStyle: Boolean = false,
    val bottomNavigation: Boolean = true,
    val startupAction: String = "show_history",
    val autoPlayOnStartup: Boolean = false,
    val autoOffOnStartup: Boolean = false,
    val autoOffTimeout: String = "10",
    val loadIcons: Boolean = true,
    val iconClickTogglesFavorite: Boolean = true,
    val autoFavorite: Boolean = false,
    val showBroken: Boolean = false,
    val clickTrendIconVisible: Boolean = true,
    val singleUseTags: Boolean = false,
    val playExternal: Boolean = false,
    val warnNoWifi: Boolean = false,
    val pauseWhenNoisy: Boolean = true,
    val autoResumeOnWiredHeadset: Boolean = false,
    val autoResumeOnBluetoothA2dp: Boolean = false,
    val alarmExternal: Boolean = false,
    val alarmTimeout: String = "10",
    val recordNameFormatting: String = "default",
    val streamConnectTimeout: Int = 5,
    val streamReadTimeout: Int = 10,
    val retryTimeout: Int = 10,
    val retryDelay: Int = 100,
    val resumeWithin: Int = 60,
    val lastFmApiKey: String = "",
    val ignoreBatteryOptimization: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var sharedPreferences: android.content.SharedPreferences? = null

    fun initializeSettings(context: Context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val prefs = sharedPreferences ?: return@launch
                
                val newState = SettingsUiState(
                    themeName = prefs.getString("theme_name", "light") ?: "light",
                    circularIcons = prefs.getBoolean("circular_icons", false),
                    compactStyle = prefs.getBoolean("compact_style", false),
                    bottomNavigation = prefs.getBoolean("bottom_navigation", true),
                    startupAction = prefs.getString("startup_action", "show_history") ?: "show_history",
                    autoPlayOnStartup = prefs.getBoolean("auto_play_on_startup", false),
                    autoOffOnStartup = prefs.getBoolean("auto_off_on_startup", false),
                    autoOffTimeout = prefs.getString("auto_off_timeout", "10") ?: "10",
                    loadIcons = prefs.getBoolean("load_icons", true),
                    iconClickTogglesFavorite = prefs.getBoolean("icon_click_toggles_favorite", true),
                    autoFavorite = prefs.getBoolean("auto_favorite", false),
                    showBroken = prefs.getBoolean("show_broken", false),
                    clickTrendIconVisible = prefs.getBoolean("click_trend_icon_visible", true),
                    singleUseTags = prefs.getBoolean("single_use_tags", false),
                    playExternal = prefs.getBoolean("play_external", false),
                    warnNoWifi = prefs.getBoolean("warn_no_wifi", false),
                    pauseWhenNoisy = prefs.getBoolean("pause_when_noisy", true),
                    autoResumeOnWiredHeadset = prefs.getBoolean("auto_resume_on_wired_headset_connection", false),
                    autoResumeOnBluetoothA2dp = prefs.getBoolean("auto_resume_on_bluetooth_a2dp_connection", false),
                    alarmExternal = prefs.getBoolean("alarm_external", false),
                    alarmTimeout = prefs.getString("alarm_timeout", "10") ?: "10",
                    recordNameFormatting = prefs.getString("record_name_formatting", "default") ?: "default",
                    streamConnectTimeout = prefs.getInt("stream_connect_timeout", 5),
                    streamReadTimeout = prefs.getInt("stream_read_timeout", 10),
                    retryTimeout = prefs.getInt("settings_retry_timeout", 10),
                    retryDelay = prefs.getInt("settings_retry_delay", 100),
                    resumeWithin = prefs.getInt("settings_resume_within", 60),
                    lastFmApiKey = prefs.getString("last_fm_api_key", "") ?: "",
                    ignoreBatteryOptimization = prefs.getBoolean("ignore_battery_optimization", false),
                    isLoading = false,
                    error = null
                )
                
                _uiState.update { newState }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateTheme(themeName: String) {
        updatePreference("theme_name", themeName) {
            _uiState.update { it.copy(themeName = themeName) }
        }
    }

    fun updateCircularIcons(enabled: Boolean) {
        updatePreference("circular_icons", enabled) {
            _uiState.update { it.copy(circularIcons = enabled) }
        }
    }

    fun updateCompactStyle(enabled: Boolean) {
        updatePreference("compact_style", enabled) {
            _uiState.update { it.copy(compactStyle = enabled) }
        }
    }

    fun updateBottomNavigation(enabled: Boolean) {
        updatePreference("bottom_navigation", enabled) {
            _uiState.update { it.copy(bottomNavigation = enabled) }
        }
    }

    fun updateStartupAction(action: String) {
        updatePreference("startup_action", action) {
            _uiState.update { it.copy(startupAction = action) }
        }
    }

    fun updateAutoPlayOnStartup(enabled: Boolean) {
        updatePreference("auto_play_on_startup", enabled) {
            _uiState.update { it.copy(autoPlayOnStartup = enabled) }
        }
    }

    fun updateAutoOffOnStartup(enabled: Boolean) {
        updatePreference("auto_off_on_startup", enabled) {
            _uiState.update { it.copy(autoOffOnStartup = enabled) }
        }
    }

    fun updateAutoOffTimeout(timeout: String) {
        updatePreference("auto_off_timeout", timeout) {
            _uiState.update { it.copy(autoOffTimeout = timeout) }
        }
    }

    fun updateLoadIcons(enabled: Boolean) {
        updatePreference("load_icons", enabled) {
            _uiState.update { it.copy(loadIcons = enabled) }
        }
    }

    fun updateIconClickTogglesFavorite(enabled: Boolean) {
        updatePreference("icon_click_toggles_favorite", enabled) {
            _uiState.update { it.copy(iconClickTogglesFavorite = enabled) }
        }
    }

    fun updateAutoFavorite(enabled: Boolean) {
        updatePreference("auto_favorite", enabled) {
            _uiState.update { it.copy(autoFavorite = enabled) }
        }
    }

    fun updateShowBroken(enabled: Boolean) {
        updatePreference("show_broken", enabled) {
            _uiState.update { it.copy(showBroken = enabled) }
        }
    }

    fun updateClickTrendIconVisible(enabled: Boolean) {
        updatePreference("click_trend_icon_visible", enabled) {
            _uiState.update { it.copy(clickTrendIconVisible = enabled) }
        }
    }

    fun updateSingleUseTags(enabled: Boolean) {
        updatePreference("single_use_tags", enabled) {
            _uiState.update { it.copy(singleUseTags = enabled) }
        }
    }

    fun updatePlayExternal(enabled: Boolean) {
        updatePreference("play_external", enabled) {
            _uiState.update { it.copy(playExternal = enabled) }
        }
    }

    fun updateWarnNoWifi(enabled: Boolean) {
        updatePreference("warn_no_wifi", enabled) {
            _uiState.update { it.copy(warnNoWifi = enabled) }
        }
    }

    fun updatePauseWhenNoisy(enabled: Boolean) {
        updatePreference("pause_when_noisy", enabled) {
            _uiState.update { it.copy(pauseWhenNoisy = enabled) }
        }
    }

    fun updateAutoResumeOnWiredHeadset(enabled: Boolean) {
        updatePreference("auto_resume_on_wired_headset_connection", enabled) {
            _uiState.update { it.copy(autoResumeOnWiredHeadset = enabled) }
        }
    }

    fun updateAutoResumeOnBluetoothA2dp(enabled: Boolean) {
        updatePreference("auto_resume_on_bluetooth_a2dp_connection", enabled) {
            _uiState.update { it.copy(autoResumeOnBluetoothA2dp = enabled) }
        }
    }

    fun updateAlarmExternal(enabled: Boolean) {
        updatePreference("alarm_external", enabled) {
            _uiState.update { it.copy(alarmExternal = enabled) }
        }
    }

    fun updateAlarmTimeout(timeout: String) {
        updatePreference("alarm_timeout", timeout) {
            _uiState.update { it.copy(alarmTimeout = timeout) }
        }
    }

    fun updateRecordNameFormatting(format: String) {
        updatePreference("record_name_formatting", format) {
            _uiState.update { it.copy(recordNameFormatting = format) }
        }
    }

    fun updateStreamConnectTimeout(timeout: Int) {
        updatePreference("stream_connect_timeout", timeout) {
            _uiState.update { it.copy(streamConnectTimeout = timeout) }
        }
    }

    fun updateStreamReadTimeout(timeout: Int) {
        updatePreference("stream_read_timeout", timeout) {
            _uiState.update { it.copy(streamReadTimeout = timeout) }
        }
    }

    fun updateRetryTimeout(timeout: Int) {
        updatePreference("settings_retry_timeout", timeout) {
            _uiState.update { it.copy(retryTimeout = timeout) }
        }
    }

    fun updateRetryDelay(delay: Int) {
        updatePreference("settings_retry_delay", delay) {
            _uiState.update { it.copy(retryDelay = delay) }
        }
    }

    fun updateResumeWithin(within: Int) {
        updatePreference("settings_resume_within", within) {
            _uiState.update { it.copy(resumeWithin = within) }
        }
    }

    fun updateLastFmApiKey(key: String) {
        updatePreference("last_fm_api_key", key) {
            _uiState.update { it.copy(lastFmApiKey = key) }
        }
    }

    fun updateIgnoreBatteryOptimization(enabled: Boolean) {
        updatePreference("ignore_battery_optimization", enabled) {
            _uiState.update { it.copy(ignoreBatteryOptimization = enabled) }
        }
    }

    private inline fun <T> updatePreference(key: String, value: T, crossinline updateState: () -> Unit) {
        viewModelScope.launch {
            try {
                sharedPreferences?.edit()?.apply {
                    when (value) {
                        is String -> putString(key, value)
                        is Boolean -> putBoolean(key, value)
                        is Int -> putInt(key, value)
                        is Long -> putLong(key, value)
                        is Float -> putFloat(key, value)
                    }
                    apply()
                }
                updateState()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
