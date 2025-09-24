package com.nonameradio.app.core.domain.usecase;

import com.nonameradio.app.core.domain.interfaces.IStationRepository;
import com.nonameradio.app.station.DataRadioStation;

public class SaveStationUseCase implements UseCase<DataRadioStation, Boolean> {
    private final IStationRepository stationRepository;

    public SaveStationUseCase(IStationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @Override
    public Boolean execute(DataRadioStation station) throws Exception {
        if (station == null) {
            return false;
        }

        stationRepository.saveStation(station);
        return true;
    }
}

