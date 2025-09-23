package net.programmierecke.radiodroid2.service;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.session.MediaSession;
import androidx.media3.session.SessionCommand;
import androidx.media3.session.SessionResult;

import com.google.common.util.concurrent.ListenableFuture;

import net.programmierecke.radiodroid2.RadioDroidApp;
import net.programmierecke.radiodroid2.station.DataRadioStation;

import java.util.List;

/**
 * MediaSession callback for radio playback commands.
 * Basic implementation without overriding interface methods.
 */
public class RadioSessionCallback {
    private static final String TAG = "RadioSessionCallback";
    
    private final RadioMediaSessionService service;
    
    public RadioSessionCallback(RadioMediaSessionService service) {
        this.service = service;
    }
    
    public void handlePlay(MediaItem mediaItem) {
        if (service != null && mediaItem != null) {
            // Extract station info from MediaItem and play
            String stationId = mediaItem.mediaId;
            if (stationId != null && !stationId.isEmpty()) {
                DataRadioStation station = findStationById(stationId);
                if (station != null) {
                    service.playStation(station, false);
                }
            }
        }
    }
    
    public void handlePause() {
        if (service != null) {
            service.pause();
        }
    }
    
    public void handleStop() {
        if (service != null) {
            service.stop();
        }
    }
    
    public void handleSkipToNext() {
        // Implement skip to next station logic
    }
    
    public void handleSkipToPrevious() {
        // Implement skip to previous station logic
    }
    
    private DataRadioStation findStationById(String stationId) {
        if (service != null) {
            RadioDroidApp app = (RadioDroidApp) service.getApplication();
            // Try to find station in favorites first
            DataRadioStation station = findStationInList(app.getFavouriteManager().getList(), stationId);
            if (station == null) {
                // Try history
                station = findStationInList(app.getHistoryManager().getList(), stationId);
            }
            if (station == null) {
                // Try fallback stations
                station = findStationInList(app.getFallbackStationsManager().getList(), stationId);
            }
            return station;
        }
        return null;
    }
    
    private DataRadioStation findStationInList(List<DataRadioStation> stations, String stationId) {
        for (DataRadioStation station : stations) {
            if (station.StationUuid.equals(stationId)) {
                return station;
            }
        }
        return null;
    }
}
