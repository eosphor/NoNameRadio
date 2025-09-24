package net.programmierecke.radiodroid2.core.domain.usecase;

import net.programmierecke.radiodroid2.core.domain.interfaces.IStationRepository;
import net.programmierecke.radiodroid2.station.DataRadioStation;
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

