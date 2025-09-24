package net.programmierecke.radiodroid2.data.repository.impl;

import net.programmierecke.radiodroid2.StationSaveManager;
import net.programmierecke.radiodroid2.core.domain.interfaces.IStationRepository;
import net.programmierecke.radiodroid2.station.DataRadioStation;
import java.util.ArrayList;
import java.util.List;

public class StationRepository implements IStationRepository {
    private final StationSaveManager stationSaveManager;

    public StationRepository(StationSaveManager stationSaveManager) {
        this.stationSaveManager = stationSaveManager;
    }

    @Override
    public List<DataRadioStation> getStations() {
        // Since listStations is not public, we need to work around this
        // For now, return empty list - this needs to be implemented properly
        return new ArrayList<>();
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
