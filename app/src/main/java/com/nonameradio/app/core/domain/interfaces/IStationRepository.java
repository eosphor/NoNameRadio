package com.nonameradio.app.core.domain.interfaces;

import com.nonameradio.app.station.DataRadioStation;
import java.util.List;

public interface IStationRepository {
    List<DataRadioStation> getStations();
    void saveStation(DataRadioStation station);
    void deleteStation(DataRadioStation station);
    DataRadioStation getStationById(String id);
    void updateStation(DataRadioStation station);
}
