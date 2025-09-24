package net.programmierecke.radiodroid2.core.domain.interfaces;

import net.programmierecke.radiodroid2.station.DataRadioStation;
import java.util.List;

public interface IStationRepository {
    List<DataRadioStation> getStations();
    void saveStation(DataRadioStation station);
    void deleteStation(DataRadioStation station);
    DataRadioStation getStationById(String id);
    void updateStation(DataRadioStation station);
}
