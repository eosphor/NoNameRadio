package com.nonameradio.app.utils;

import android.content.Context;
import android.util.Log;

import com.nonameradio.app.players.PlayStationTask;
import com.nonameradio.app.players.selector.PlayerSelectorDialog;
import com.nonameradio.app.players.selector.PlayerType;
import com.nonameradio.app.service.MediaSessionUtil;
import com.nonameradio.app.station.DataRadioStation;

import androidx.fragment.app.FragmentManager;

import com.nonameradio.app.NoNameRadioApp;

/**
 * Business logic utilities for radio stations.
 * Contains station-related operations extracted from Utils.java.
 */
public class StationUtils {
    private static final String TAG = "StationUtils";

    /**
     * Show player selection dialog for station
     */
    public static void showPlaySelection(final NoNameRadioApp app,
                                       final DataRadioStation station,
                                       final FragmentManager fragmentManager) {
        try {
            // Simplified implementation - will be expanded later
            play(app, station);
        } catch (Exception e) {
            Log.e(TAG, "Error showing player selection", e);
        }
    }

    /**
     * Play station with metered connection warning
     */
    public static void playAndWarnIfMetered(NoNameRadioApp app,
                                          DataRadioStation station,
                                          PlayerType playerType,
                                          Runnable playFunc) {
        try {
            // Simplified implementation - always play for now
            playFunc.run();
        } catch (Exception e) {
            Log.e(TAG, "Error checking metered connection", e);
            playFunc.run(); // Fallback to play
        }
    }

    /**
     * Play station directly
     */
    public static void play(final NoNameRadioApp app, final DataRadioStation station) {
        try {
            Log.d(TAG, "Playing station: " + station.Name);

            // Start background service if needed
            com.nonameradio.app.service.MediaSessionUtil.startService(app);

            // Simplified play - will use existing Utils.play method
            com.nonameradio.app.Utils.play(app, station);

        } catch (Exception e) {
            Log.e(TAG, "Error playing station: " + station.Name, e);
        }
    }

    /**
     * Show metered connection warning dialog
     */
    private static void showMeteredConnectionDialog(NoNameRadioApp app,
                                                  DataRadioStation station,
                                                  PlayerType playerType,
                                                  Runnable playFunc) {
        try {
            // This would show an AlertDialog warning about metered connection
            // For now, just log and proceed
            Log.w(TAG, "Playing on metered connection: " + station.Name);
            playFunc.run();
        } catch (Exception e) {
            Log.e(TAG, "Error showing metered connection dialog", e);
        }
    }

    /**
     * Validate station data
     */
    public static boolean isValidStation(DataRadioStation station) {
        if (station == null) return false;

        // Check required fields
        if (station.Name == null || station.Name.trim().isEmpty()) {
            return false;
        }

        if (station.StreamUrl == null || station.StreamUrl.trim().isEmpty()) {
            return false;
        }

        // Validate URL format
        if (!isValidUrl(station.StreamUrl)) {
            return false;
        }

        return true;
    }

    /**
     * Basic URL validation
     */
    private static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        // Basic checks
        return url.startsWith("http://") || url.startsWith("https://") ||
               url.startsWith("rtmp://") || url.startsWith("rtsp://");
    }

    /**
     * Get station display name
     */
    public static String getDisplayName(DataRadioStation station) {
        if (station == null) return "Unknown Station";

        String name = station.Name;
        if (name == null || name.trim().isEmpty()) {
            name = "Unknown Station";
        }

        // Add country if available
        if (station.Country != null && !station.Country.trim().isEmpty()) {
            name += " (" + station.Country + ")";
        }

        return name;
    }

    /**
     * Check if station supports streaming
     */
    public static boolean supportsStreaming(DataRadioStation station) {
        if (!isValidStation(station)) {
            return false;
        }

        // Check bitrate
        if (station.Bitrate > 0 && station.Bitrate < 32) {
            Log.w(TAG, "Low bitrate station: " + station.Name + " (" + station.Bitrate + " kbps)");
            return false;
        }

        // Check for broken stations
        if (!station.Working) {
            return false;
        }

        return true;
    }
}
