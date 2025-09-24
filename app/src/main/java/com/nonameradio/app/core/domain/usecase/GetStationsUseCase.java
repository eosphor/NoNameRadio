package com.nonameradio.app.core.domain.usecase;

import com.nonameradio.app.core.domain.interfaces.IStationRepository;
import com.nonameradio.app.station.DataRadioStation;
import java.util.List;

public class GetStationsUseCase implements UseCase<Void, List<DataRadioStation>> {
    private final IStationRepository stationRepository;

    public GetStationsUseCase(IStationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @Override
    public List<DataRadioStation> execute(Void params) throws Exception {
        return stationRepository.getStations();
    }
}

