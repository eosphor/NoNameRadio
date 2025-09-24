package net.programmierecke.radiodroid2.core.domain.usecase;

import net.programmierecke.radiodroid2.core.domain.interfaces.IStationRepository;
import net.programmierecke.radiodroid2.station.DataRadioStation;

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

