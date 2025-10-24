package com.nonameradio.app.core.domain.interfaces;

import com.nonameradio.app.station.DataRadioStation;
import java.util.List;

public interface IStationSearchService {
    List<DataRadioStation> searchStations(String query);
    List<DataRadioStation> searchStationsByTag(String tag);
    List<DataRadioStation> searchStationsByCountry(String country);
    List<DataRadioStation> searchStationsByLanguage(String language);
    void loadTopStations(int limit);
    void loadRecentStations(int limit);
}

