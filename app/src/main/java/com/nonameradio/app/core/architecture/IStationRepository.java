package com.nonameradio.app.core.architecture;

import com.nonameradio.app.station.DataRadioStation;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Repository interface for station persistence operations.
 * Handles saving and loading station data asynchronously.
 */
public interface IStationRepository {

    /**
     * Saves all stations to persistent storage
     */
    CompletableFuture<Result<Void>> save(List<DataRadioStation> stations);

    /**
     * Loads all stations from persistent storage
     */
    CompletableFuture<Result<List<DataRadioStation>>> load();

    /**
     * Gets the save identifier for this repository
     */
    String getSaveId();
}
