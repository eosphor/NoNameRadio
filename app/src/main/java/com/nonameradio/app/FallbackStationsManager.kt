package com.nonameradio.app

import android.content.Context
import com.nonameradio.app.core.architecture.IStationRepository
import com.nonameradio.app.data.repository.impl.StationRepository
import com.nonameradio.app.service.StationManager
import com.nonameradio.app.station.DataRadioStation

class FallbackStationsManager(ctx: Context?) : StationManager(ctx, StationRepository(ctx, "fallback")) {

    init {
        // Load fallback stations immediately
        loadFallbackStations()
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
