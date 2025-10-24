package com.nonameradio.app;

import android.content.Context;
import android.content.Intent;

import com.nonameradio.app.core.architecture.IStationRepository;
import com.nonameradio.app.core.event.EventBus;
import com.nonameradio.app.data.repository.impl.StationRepository;
import com.nonameradio.app.service.StationManager;
import com.nonameradio.app.station.DataRadioStation;

/**
 * Favourite stations manager.
 * Extends StationManager with favourite-specific functionality.
 */
public class FavouriteManager extends StationManager {

    public FavouriteManager(Context ctx) {
        super(ctx, new StationRepository(ctx, "favourites"));
    }

    @Override
    protected String getSaveId() {
        return "favourites";
    }

    @Override
    public void add(DataRadioStation station) {
        super.add(station);

        // Send event for UI updates
        EventBus.getInstance().post(new StationStatusEvent(station, true));
    }

    @Override
    public int remove(String id) {
        int position = super.remove(id);
        if (position >= 0) {
            DataRadioStation removedStation = getById(id);
            if (removedStation != null) {
                EventBus.getInstance().post(new StationStatusEvent(removedStation, false));
            }
        }
        return position;
    }

    @Override
    public void clear() {
        // Get copy of current stations before clearing
        java.util.List<DataRadioStation> oldStations = new java.util.ArrayList<>(getList());
        super.clear();

        // Send events for all removed stations
        for (DataRadioStation station : oldStations) {
            EventBus.getInstance().post(new StationStatusEvent(station, false));
        }
    }

    /**
     * Checks if a station is in favourites
     */
    public boolean isFavourite(String stationId) {
        return has(stationId);
    }

    /**
     * Toggles favourite status for a station
     */
    public boolean toggleFavourite(DataRadioStation station) {
        if (has(station.StationUuid)) {
            remove(station.StationUuid);
            return false;
        } else {
            add(station);
            return true;
        }
    }

    // Backward compatibility methods (deprecated but still used by some fragments)
    @Deprecated
    public void addObserver(java.util.Observer observer) {
        // Deprecated: Use getStationsLiveData().observe() instead
        // Kept for backward compatibility during migration
    }

    @Deprecated
    public void deleteObserver(java.util.Observer observer) {
        // Deprecated: LiveData observers are automatically cleaned up
        // Kept for backward compatibility during migration
    }

    @Deprecated
    public void Save() {
        // Deprecated: Saving is automatic
    }

    @Deprecated
    public void notifyObservers() {
        // Deprecated: LiveData updates automatically
    }

    @Deprecated
    public void SaveM3U(String filePath, String fileName) {
        // Not implemented for favourites, but kept for compatibility
    }

    @Deprecated
    public void LoadM3U(String filePath, String fileName) {
        // Not implemented for favourites, but kept for compatibility
    }

    @Deprecated
    public void LoadM3USimple(java.io.Reader reader, String displayName) {
        // Not implemented for favourites, but kept for compatibility
    }

    @Deprecated
    public void SaveM3UWriter(java.io.Writer writer) {
        // Not implemented for favourites, but kept for compatibility
    }

    @Deprecated
    public void updateShortcuts() {
        // Not implemented, but kept for compatibility
    }

    // For backward compatibility with fragments
    public java.util.List<DataRadioStation> getListStations() {
        return getList();
    }
}