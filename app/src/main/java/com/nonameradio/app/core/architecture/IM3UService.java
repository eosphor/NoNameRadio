package com.nonameradio.app.core.architecture;

import com.nonameradio.app.station.DataRadioStation;

import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for M3U playlist operations.
 * Handles saving and loading M3U files asynchronously.
 */
public interface IM3UService {

    /**
     * Saves stations to M3U file
     */
    CompletableFuture<Result<Void>> saveM3U(String filePath, String fileName, List<DataRadioStation> stations);

    /**
     * Loads stations from M3U file
     */
    CompletableFuture<Result<List<DataRadioStation>>> loadM3U(String filePath, String fileName);

    /**
     * Loads stations from M3U reader (for SAF operations)
     */
    CompletableFuture<Result<List<DataRadioStation>>> loadM3UFromReader(Reader reader, String displayName);

    /**
     * Gets the save directory path
     */
    String getSaveDir();
}
