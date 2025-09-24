package com.nonameradio.app

import android.content.Context
import com.nonameradio.app.station.DataRadioStation

class FallbackStationsManager(ctx: Context?) : StationSaveManager(ctx) {

    init {
        // Load fallback stations immediately
        loadFallbackStations()
    }

    override fun getSaveId(): String {
        return "fallback" // Different save ID to not conflict with regular stations
    }

    private fun loadFallbackStations() {
        try {
            val str = getContext().resources
                    .openRawResource(R.raw.fallback_stations)
                    .bufferedReader()
                    .use { it.readText() }
            val arr = DataRadioStation.DecodeJson(str)
            // Use the modern API instead of direct list manipulation
            addAll(arr)
        } catch (e: Exception) {
            android.util.Log.e("FallbackStationsManager", "Failed to load fallback stations", e)
        }
    }
}
