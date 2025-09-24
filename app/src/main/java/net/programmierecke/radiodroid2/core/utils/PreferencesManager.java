package net.programmierecke.radiodroid2.core.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import java.util.Set;

/**
 * Centralized SharedPreferences manager with caching and type-safe access.
 * Replaces multiple getDefaultSharedPreferences() calls throughout the app.
 *
 * @author NoNameRadio Team
 * @version 1.0.0
 * @since 0.86.903
 */
public class PreferencesManager {
    private static volatile PreferencesManager instance;
    private final SharedPreferences prefs;

    private PreferencesManager(Context context) {
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    /**
     * Get singleton instance of PreferencesManager
     */
    public static PreferencesManager getInstance(Context context) {
        if (instance == null) {
            synchronized (PreferencesManager.class) {
                if (instance == null) {
                    instance = new PreferencesManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * Get boolean preference with default value
     */
    public boolean getBoolean(String key, boolean defValue) {
        return prefs.getBoolean(key, defValue);
    }

    /**
     * Put boolean preference
     */
    public void putBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
    }

    /**
     * Get string preference with default value
     */
    public String getString(String key, String defValue) {
        return prefs.getString(key, defValue);
    }

    /**
     * Put string preference
     */
    public void putString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    /**
     * Get int preference with default value
     */
    public int getInt(String key, int defValue) {
        return prefs.getInt(key, defValue);
    }

    /**
     * Put int preference
     */
    public void putInt(String key, int value) {
        prefs.edit().putInt(key, value).apply();
    }

    /**
     * Get long preference with default value
     */
    public long getLong(String key, long defValue) {
        return prefs.getLong(key, defValue);
    }

    /**
     * Put long preference
     */
    public void putLong(String key, long value) {
        prefs.edit().putLong(key, value).apply();
    }

    /**
     * Get float preference with default value
     */
    public float getFloat(String key, float defValue) {
        return prefs.getFloat(key, defValue);
    }

    /**
     * Put float preference
     */
    public void putFloat(String key, float value) {
        prefs.edit().putFloat(key, value).apply();
    }

    /**
     * Get string set preference with default value
     */
    public Set<String> getStringSet(String key, Set<String> defValue) {
        return prefs.getStringSet(key, defValue);
    }

    /**
     * Put string set preference
     */
    public void putStringSet(String key, Set<String> defValue) {
        prefs.edit().putStringSet(key, defValue).apply();
    }

    /**
     * Check if preference exists
     */
    public boolean contains(String key) {
        return prefs.contains(key);
    }

    /**
     * Remove preference
     */
    public void remove(String key) {
        prefs.edit().remove(key).apply();
    }

    /**
     * Clear all preferences
     */
    public void clear() {
        prefs.edit().clear().apply();
    }

    /**
     * Register preference change listener
     */
    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Unregister preference change listener
     */
    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Get all preferences (for debugging)
     */
    public java.util.Map<String, ?> getAll() {
        return prefs.getAll();
    }
}
