package com.nonameradio.app.data.repository.impl;

import com.nonameradio.app.StationSaveManager;
import com.nonameradio.app.core.domain.interfaces.IStationRepository;
import com.nonameradio.app.station.DataRadioStation;
import java.util.ArrayList;
import java.util.List;

public class StationRepository implements IStationRepository {
    private final StationSaveManager stationSaveManager;

    public StationRepository(StationSaveManager stationSaveManager) {
        this.stationSaveManager = stationSaveManager;
    }

    @Override
    public List<DataRadioStation> getStations() {
        return stationSaveManager.getList();
    }

    @Override
    public void saveStation(DataRadioStation station) {
        if (station != null) {
            stationSaveManager.add(station);
        }
    }

    @Override
    public void deleteStation(DataRadioStation station) {
        if (station != null) {
            stationSaveManager.remove(station.StationId);
        }
    }

    @Override
    public DataRadioStation getStationById(String id) {
        if (id == null) return null;
        return stationSaveManager.getById(id);
    }

    @Override
    public void updateStation(DataRadioStation station) {
        if (station != null) {
            // StationSaveManager handles updates automatically when station is modified
            // No additional action needed
        }
    }
}
