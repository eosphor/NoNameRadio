package com.nonameradio.app.core.di;

import android.content.Context;
import androidx.fragment.app.FragmentManager;
import com.nonameradio.app.NoNameRadioApp;
import com.nonameradio.app.core.domain.interfaces.INavigationManager;
import com.nonameradio.app.core.domain.interfaces.IPlayerService;
import com.nonameradio.app.core.domain.interfaces.IRecordingRepository;
import com.nonameradio.app.core.architecture.IStationRepository;
import com.nonameradio.app.data.repository.impl.RecordingRepository;
import com.nonameradio.app.data.repository.impl.StationRepository;
import com.nonameradio.app.presentation.navigation.NavigationManager;

public class DependencyInjector {
    private static IStationRepository stationRepository;
    private static IRecordingRepository recordingRepository;
    private static IPlayerService playerService;
    private static INavigationManager navigationManager;

    public static void initialize(Context context) {
        if (!(context instanceof NoNameRadioApp)) {
            throw new IllegalArgumentException("Context must be NoNameRadioApp instance");
        }

        NoNameRadioApp app = (NoNameRadioApp) context;

        // Initialize repositories
        stationRepository = new StationRepository(context, "default");
        recordingRepository = new RecordingRepository(app.getRecordingsManager());

        // Initialize services
        playerService = new PlayerServiceWrapper(context);

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
