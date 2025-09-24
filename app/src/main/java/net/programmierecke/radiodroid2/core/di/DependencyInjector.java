package net.programmierecke.radiodroid2.core.di;

import android.content.Context;
import androidx.fragment.app.FragmentManager;
import net.programmierecke.radiodroid2.RadioDroidApp;
import net.programmierecke.radiodroid2.StationSaveManager;
import net.programmierecke.radiodroid2.core.domain.interfaces.INavigationManager;
import net.programmierecke.radiodroid2.core.domain.interfaces.IPlayerService;
import net.programmierecke.radiodroid2.core.domain.interfaces.IRecordingRepository;
import net.programmierecke.radiodroid2.core.domain.interfaces.IStationRepository;
import net.programmierecke.radiodroid2.data.repository.impl.RecordingRepository;
import net.programmierecke.radiodroid2.data.repository.impl.StationRepository;
import net.programmierecke.radiodroid2.presentation.navigation.NavigationManager;

public class DependencyInjector {
    private static IStationRepository stationRepository;
    private static IRecordingRepository recordingRepository;
    private static IPlayerService playerService;
    private static INavigationManager navigationManager;

    public static void initialize(Context context) {
        if (!(context instanceof RadioDroidApp)) {
            throw new IllegalArgumentException("Context must be RadioDroidApp instance");
        }

        RadioDroidApp app = (RadioDroidApp) context;

        // Initialize repositories
        StationSaveManager stationSaveManager = new StationSaveManager(context);
        stationRepository = new StationRepository(stationSaveManager);
        recordingRepository = new RecordingRepository(app.getRecordingsManager());

        // Initialize services
        // PlayerService wrapper will be initialized later when needed
        playerService = null;

        // Initialize managers
        navigationManager = null; // Will be initialized per activity
    }

    public static INavigationManager createNavigationManager(FragmentManager fragmentManager, Context context) {
        return new NavigationManager(fragmentManager, context);
    }

    public static IStationRepository getStationRepository() {
        if (stationRepository == null) {
            throw new IllegalStateException("DependencyInjector not initialized. Call initialize() first.");
        }
        return stationRepository;
    }

    public static IRecordingRepository getRecordingRepository() {
        if (recordingRepository == null) {
            throw new IllegalStateException("DependencyInjector not initialized. Call initialize() first.");
        }
        return recordingRepository;
    }

    public static IPlayerService getPlayerService() {
        if (playerService == null) {
            throw new IllegalStateException("DependencyInjector not initialized. Call initialize() first.");
        }
        return playerService;
    }
}
