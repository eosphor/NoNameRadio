package com.nonameradio.app.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.collection.ArraySet;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nonameradio.app.ActivityMain;
import com.nonameradio.app.NoNameRadioApp;
import com.nonameradio.app.core.architecture.IStationManager;
import com.nonameradio.app.core.architecture.IStationRepository;
import com.nonameradio.app.core.architecture.Result;
import com.nonameradio.app.core.event.EventBus;
import com.nonameradio.app.station.DataRadioStation;
import com.nonameradio.app.utils.NetworkUtils;
import com.nonameradio.app.utils.StationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.debatty.java.stringsimilarity.Cosine;
import okhttp3.OkHttpClient;

/**
 * Modern station manager implementation using LiveData and EventBus.
 * Replaces the deprecated Observable pattern with modern Android architecture.
 */
public class StationManager implements IStationManager {
    private static final String TAG = "StationManager";

    public interface ModernStationStatusListener {
        void onStationStatusChanged(DataRadioStation station, boolean favourite);
    }


    private final Context context;
    private final IStationRepository repository;
    private final List<DataRadioStation> stations = new ArrayList<>();
    private final MutableLiveData<List<DataRadioStation>> stationsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);

    protected ModernStationStatusListener stationStatusListener;

    public StationManager(Context context, IStationRepository repository) {
        this.context = context;
        this.repository = repository;
        loadStations();
    }

    protected String getSaveId() {
        return "default";
    }

    protected Context getContext() {
        return context;
    }

    public void setStationStatusListener(ModernStationStatusListener stationStatusListener) {
        this.stationStatusListener = stationStatusListener;
    }

    public LiveData<List<DataRadioStation>> getStationsLiveData() {
        return stationsLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    private void notifyObservers() {
        stationsLiveData.postValue(new ArrayList<>(stations));
    }

    private void saveStationsAsync() {
        repository.save(new ArrayList<>(stations))
                .thenAccept(result -> {
                    if (!result.isSuccess()) {
                        Log.e(TAG, "Failed to save stations", result.getError());
                    }
                });
    }

    private void loadStations() {
        repository.load()
                .thenAccept(result -> {
                    if (result.isSuccess()) {
                        List<DataRadioStation> loadedStations = result.getData();
                        stations.clear();
                        for (DataRadioStation station : loadedStations) {
                            station.queue = this; // Set queue reference to this manager
                        }
                        stations.addAll(loadedStations);
                        notifyObservers();
                    } else {
                        Log.e(TAG, "Failed to load stations", result.getError());
                    }
                });
    }

    @Override
    public void add(DataRadioStation station) {
        if (station.queue == null) {
            station.queue = this; // Set queue reference to this manager
        }
        stations.add(station);
        saveStationsAsync();
        notifyObservers();

        if (stationStatusListener != null) {
            stationStatusListener.onStationStatusChanged(station, true);
        }
    }

    @Override
    public void addMultiple(List<DataRadioStation> newStations) {
        stations.addAll(newStations);
        saveStationsAsync();
        notifyObservers();
    }

    @Override
    public void replaceList(List<DataRadioStation> newStations) {
        for (DataRadioStation newStation : newStations) {
            for (int i = 0; i < stations.size(); i++) {
                if (stations.get(i).StationUuid.equals(newStation.StationUuid)) {
                    stations.set(i, newStation);
                    break;
                }
            }
        }
        saveStationsAsync();
        notifyObservers();
    }

    @Override
    public void addFront(DataRadioStation station) {
        if (station.queue == null) {
            station.queue = this; // Set queue reference to this manager
        }
        stations.add(0, station);
        saveStationsAsync();
        notifyObservers();

        if (stationStatusListener != null) {
            stationStatusListener.onStationStatusChanged(station, true);
        }
    }

    @Override
    public void addAll(List<DataRadioStation> newStations) {
        if (newStations == null) {
            return;
        }
        for (DataRadioStation station : newStations) {
            station.queue = this; // Set queue reference to this manager
        }
        stations.addAll(newStations);
    }

    @Override
    public DataRadioStation getLast() {
        if (!stations.isEmpty()) {
            return stations.get(stations.size() - 1);
        }
        return null;
    }

    @Override
    public DataRadioStation getFirst() {
        if (!stations.isEmpty()) {
            return stations.get(0);
        }
        return null;
    }

    @Override
    public DataRadioStation getById(String id) {
        for (DataRadioStation station : stations) {
            if (id.equals(station.StationUuid)) {
                return station;
            }
        }
        return null;
    }

    @Override
    public DataRadioStation getNextById(String id) {
        if (stations.isEmpty()) {
            return null;
        }

        for (int i = 0; i < stations.size() - 1; i++) {
            if (stations.get(i).StationUuid.equals(id)) {
                return stations.get(i + 1);
            }
        }
        return stations.get(0);
    }

    @Override
    public DataRadioStation getPreviousById(String id) {
        if (stations.isEmpty()) {
            return null;
        }

        for (int i = 1; i < stations.size(); i++) {
            if (stations.get(i).StationUuid.equals(id)) {
                return stations.get(i - 1);
            }
        }
        return stations.get(stations.size() - 1);
    }

    @Override
    public void moveWithoutNotify(int fromPos, int toPos) {
        Collections.rotate(stations.subList(Math.min(fromPos, toPos), Math.max(fromPos, toPos) + 1),
                          Integer.signum(fromPos - toPos));
    }

    @Override
    public void move(int fromPos, int toPos) {
        moveWithoutNotify(fromPos, toPos);
        saveStationsAsync();
        notifyObservers();
    }

    @Override
    public DataRadioStation getBestNameMatch(String query) {
        DataRadioStation bestStation = null;
        query = query.toUpperCase();
        double smallestDistance = Double.MAX_VALUE;

        Cosine distMeasure = new Cosine();
        for (DataRadioStation station : stations) {
            double distance = distMeasure.distance(station.Name.toUpperCase(), query);
            if (distance < smallestDistance) {
                bestStation = station;
                smallestDistance = distance;
            }
        }

        return bestStation;
    }

    @Override
    public int remove(String id) {
        for (int i = 0; i < stations.size(); i++) {
            DataRadioStation station = stations.get(i);
            if (station.StationUuid.equals(id)) {
                stations.remove(i);
                saveStationsAsync();
                notifyObservers();

                if (stationStatusListener != null) {
                    stationStatusListener.onStationStatusChanged(station, false);
                }

                return i;
            }
        }
        return -1;
    }

    @Override
    public void restore(DataRadioStation station, int pos) {
        station.queue = this; // Set queue reference to this manager
        stations.add(pos, station);
        saveStationsAsync();
        notifyObservers();

        if (stationStatusListener != null) {
            stationStatusListener.onStationStatusChanged(station, false);
        }
    }

    @Override
    public void clear() {
        List<DataRadioStation> oldStations = new ArrayList<>(stations);
        stations.clear();
        saveStationsAsync();
        notifyObservers();

        if (stationStatusListener != null) {
            for (DataRadioStation station : oldStations) {
                stationStatusListener.onStationStatusChanged(station, false);
            }
        }
    }

    @Override
    public int size() {
        return stations.size();
    }

    @Override
    public boolean isEmpty() {
        return stations.size() == 0;
    }

    @Override
    public boolean has(String id) {
        return getById(id) != null;
    }

    @Override
    public List<DataRadioStation> getList() {
        return Collections.unmodifiableList(stations);
    }

    @Override
    public void refreshStationsFromServer() {
        final NoNameRadioApp app = (NoNameRadioApp) context.getApplicationContext();
        final OkHttpClient httpClient = app.getHttpClient();
        final ArrayList<DataRadioStation> savedStations = new ArrayList<>(stations);

        isLoadingLiveData.postValue(true);

        // Send loading event
        EventBus.getInstance().post(new LoadingEvent(true));

        com.nonameradio.app.core.utils.AsyncExecutor.executeIOTask(
            () -> {
                ArrayList<DataRadioStation> stationsToRemove = new ArrayList<>();
                for (DataRadioStation station : savedStations) {
                    if (!station.refresh(httpClient, context) && !station.hasValidUuid() &&
                        station.RefreshRetryCount > DataRadioStation.MAX_REFRESH_RETRIES) {
                        stationsToRemove.add(station);
                    }
                }
                return stationsToRemove;
            },
            stationsToRemove -> {
                // onSuccess
                stations.removeAll(stationsToRemove);
                saveStationsAsync();
                notifyObservers();
                isLoadingLiveData.postValue(false);
                EventBus.getInstance().post(new LoadingEvent(false));
            },
            throwable -> {
                // onError
                isLoadingLiveData.postValue(false);
                EventBus.getInstance().post(new LoadingEvent(false));
                Log.e(TAG, "Failed to refresh stations", throwable);
            }
        );
    }

    // Event classes for EventBus
    public static class LoadingEvent {
        public final boolean isLoading;

        public LoadingEvent(boolean isLoading) {
            this.isLoading = isLoading;
        }
    }

    public static class StationStatusEvent {
        public final DataRadioStation station;
        public final boolean isFavourite;

        public StationStatusEvent(DataRadioStation station, boolean isFavourite) {
            this.station = station;
            this.isFavourite = isFavourite;
        }
    }
}
