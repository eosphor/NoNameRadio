package net.programmierecke.radiodroid2.core.domain.interfaces;

import net.programmierecke.radiodroid2.station.DataRadioStation;
import java.util.List;

public interface IStationSearchService {
    List<DataRadioStation> searchStations(String query);
    List<DataRadioStation> searchStationsByTag(String tag);
    List<DataRadioStation> searchStationsByCountry(String country);
    List<DataRadioStation> searchStationsByLanguage(String language);
    void loadTopStations(int limit);
    void loadRecentStations(int limit);
}

