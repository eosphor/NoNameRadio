package com.nonameradio.app;

import android.content.Context;

import com.nonameradio.app.core.architecture.IStationRepository;
import com.nonameradio.app.data.repository.impl.StationRepository;
import com.nonameradio.app.service.StationManager;
import com.nonameradio.app.station.DataRadioStation;

/**
 * History stations manager.
 * Extends StationManager with history-specific functionality.
 * Maintains a limited history of recently played stations.
 */
public class HistoryManager extends StationManager {
    private static final int MAX_HISTORY_SIZE = 50;

    public HistoryManager(Context ctx) {
        super(ctx, new StationRepository(ctx, "history"));
    }

    @Override
    protected String getSaveId() {
        return "history";
    }

    @Override
    public void add(DataRadioStation station) {
        // Remove if already exists (to move to front)
        remove(station.StationUuid);

        // Add to front
        super.addFront(station);

        // Trim history if too large
        trimHistory();
    }

    @Override
    public void addFront(DataRadioStation station) {
        // Remove if already exists
        remove(station.StationUuid);

        // Add to front
        super.addFront(station);

        // Trim history if too large
        trimHistory();
    }

    private void trimHistory() {
        while (size() > MAX_HISTORY_SIZE) {
            // Remove oldest (last) station
            java.util.List<DataRadioStation> stations = getList();
            if (!stations.isEmpty()) {
                DataRadioStation oldest = stations.get(stations.size() - 1);
                remove(oldest.StationUuid);
            }
        }
    }

    // Backward compatibility methods
    public void addObserver(java.util.Observer observer) {
        // Not needed with LiveData, but kept for compatibility
    }

    public void deleteObserver(java.util.Observer observer) {
        // Not needed with LiveData, but kept for compatibility
    }

    public void Save() {
        // Saving is automatic, but kept for compatibility
    }

    public void notifyObservers() {
        // Not needed with LiveData, but kept for compatibility
    }

    public void SaveM3U(String filePath, String fileName) {
        // Not implemented for history, but kept for compatibility
    }

    public void LoadM3U(String filePath, String fileName) {
        // Not implemented for history, but kept for compatibility
    }

    public void LoadM3USimple(java.io.Reader reader, String displayName) {
        // Not implemented for history, but kept for compatibility
    }

    public void SaveM3UWriter(java.io.Writer writer) {
        // Not implemented for history, but kept for compatibility
    }

    // For backward compatibility with fragments
    public java.util.List<DataRadioStation> getListStations() {
        return getList();
    }
}