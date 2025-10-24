package com.nonameradio.app;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;

import com.nonameradio.app.core.architecture.IM3UService;
import com.nonameradio.app.core.architecture.IStationRepository;
import com.nonameradio.app.core.architecture.Result;
import com.nonameradio.app.data.repository.impl.StationRepository;
import com.nonameradio.app.service.M3UService;
import com.nonameradio.app.service.StationManager;
import com.nonameradio.app.station.DataRadioStation;

import java.io.Reader;
import java.util.List;

/**
 * Legacy StationSaveManager for backward compatibility.
 * This class now delegates to the modern StationManager and related services.
 * Migrated from Observable to full LiveData delegation.
 * 
 * @deprecated Use StationManager directly for new code.
 */
@Deprecated
public class StationSaveManager {
    public interface StationStatusListener {
        void onStationStatusChanged(DataRadioStation station, boolean favourite);
    }

    private final Context context;
    private final StationManager stationManager;
    private final IM3UService m3uService;
    private final IStationRepository repository;

    protected StationStatusListener stationStatusListener;

    public StationSaveManager(Context ctx) {
        this.context = ctx;
        this.repository = new StationRepository(ctx, getSaveId());
        this.stationManager = new StationManager(ctx, repository) {
            @Override
            protected String getSaveId() {
                return StationSaveManager.this.getSaveId();
            }

            @Override
            public void setStationStatusListener(ModernStationStatusListener stationStatusListener) {
                StationSaveManager.this.stationStatusListener = (StationStatusListener) stationStatusListener;
                super.setStationStatusListener(stationStatusListener);
            }
        };
        this.m3uService = new M3UService(ctx);
    }

    protected String getSaveId() {
        return "default";
    }

    protected void setStationStatusListener(StationStatusListener stationStatusListener) {
        this.stationStatusListener = stationStatusListener;
        // Create adapter for the new interface
        com.nonameradio.app.service.StationManager.ModernStationStatusListener adapter =
            new com.nonameradio.app.service.StationManager.ModernStationStatusListener() {
                @Override
                public void onStationStatusChanged(DataRadioStation station, boolean favourite) {
                    stationStatusListener.onStationStatusChanged(station, favourite);
                }
            };
        this.stationManager.setStationStatusListener(adapter);
    }

    // Access to modern services for migration
    public StationManager getStationManager() {
        return stationManager;
    }

    public IM3UService getM3UService() {
        return m3uService;
    }

    public IStationRepository getRepository() {
        return repository;
    }

    public LiveData<List<DataRadioStation>> getStationsLiveData() {
        return stationManager.getStationsLiveData();
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return stationManager.getIsLoadingLiveData();
    }

    // For backward compatibility with subclasses
    protected Context getContext() {
        return context;
    }

    public void add(DataRadioStation station) {
        stationManager.add(station);
    }

    public void addMultiple(List<DataRadioStation> stations) {
        stationManager.addMultiple(stations);
    }

    public void replaceList(List<DataRadioStation> stations) {
        stationManager.replaceList(stations);
    }

    public void addFront(DataRadioStation station) {
        stationManager.addFront(station);
    }

    public void addAll(List<DataRadioStation> stations) {
        stationManager.addAll(stations);
    }

    public DataRadioStation getLast() {
        return stationManager.getLast();
    }

    public DataRadioStation getFirst() {
        return stationManager.getFirst();
    }

    public DataRadioStation getById(String id) {
        return stationManager.getById(id);
    }

    public DataRadioStation getNextById(String id) {
        return stationManager.getNextById(id);
    }

    public DataRadioStation getPreviousById(String id) {
        return stationManager.getPreviousById(id);
    }

    public void moveWithoutNotify(int fromPos, int toPos) {
        stationManager.moveWithoutNotify(fromPos, toPos);
    }

    public void move(int fromPos, int toPos) {
        stationManager.move(fromPos, toPos);
    }

    public DataRadioStation getBestNameMatch(String query) {
        return stationManager.getBestNameMatch(query);
    }

    public int remove(String id) {
        return stationManager.remove(id);
    }

    public void restore(DataRadioStation station, int pos) {
        stationManager.restore(station, pos);
    }

    public void clear() {
        stationManager.clear();
    }

    public int size() {
        return stationManager.size();
    }

    public boolean isEmpty() {
        return stationManager.isEmpty();
    }

    public boolean has(String id) {
        return stationManager.has(id);
    }

    public List<DataRadioStation> getList() {
        return stationManager.getList();
    }

    public void refreshStationsFromServer() {
        stationManager.refreshStationsFromServer();
    }

    /**
     * Loading is now handled automatically by StationManager
     */
    void Load() {
        // Loading is handled by StationManager constructor
        Log.w("StationSaveManager", "Load() is deprecated. Loading is handled automatically.");
    }

    /**
     * Saving is now handled automatically by StationManager
     */
    void Save() {
        // Saving is handled automatically by StationManager
        Log.w("StationSaveManager", "Save() is deprecated. Saving is handled automatically.");
    }

    public static String getSaveDir() {
        return M3UService.getSaveDirStatic();
    }

    public void SaveM3U(final String filePath, final String fileName) {
        Toast toast = Toast.makeText(context, context.getResources().getString(R.string.notify_save_playlist_now, filePath, fileName), Toast.LENGTH_LONG);
        toast.show();

        m3uService.saveM3U(filePath, fileName, getList())
                .thenAccept(result -> {
                    if (result.isSuccess()) {
                        Log.i("SAVE", "OK");
                        Toast successToast = Toast.makeText(context, context.getResources().getString(R.string.notify_save_playlist_ok, filePath, fileName), Toast.LENGTH_LONG);
                        successToast.show();
                    } else {
                        Log.i("SAVE", "NOK");
                        Toast errorToast = Toast.makeText(context, context.getResources().getString(R.string.notify_save_playlist_nok, filePath, fileName), Toast.LENGTH_LONG);
                        errorToast.show();
                    }
                })
                .exceptionally(throwable -> {
                    Log.i("SAVE", "NOK - Exception: " + throwable.getMessage());
                    Toast exceptionToast = Toast.makeText(context, context.getResources().getString(R.string.notify_save_playlist_nok, filePath, fileName), Toast.LENGTH_LONG);
                    exceptionToast.show();
                    return null;
                });
    }

    public void SaveM3USimple(final String filePath, final String fileName) {
        // Same implementation as SaveM3U for backward compatibility
        SaveM3U(filePath, fileName);
    }

    public void LoadM3U(final String filePath, final String fileName) {
        Toast toast = Toast.makeText(context, context.getResources().getString(R.string.notify_load_playlist_now, filePath, fileName), Toast.LENGTH_LONG);
        toast.show();

        m3uService.loadM3U(filePath, fileName)
                .thenAccept(result -> {
                    if (result.isSuccess()) {
                        List<DataRadioStation> stations = result.getData();
                        Log.i("LOAD", "Loaded " + stations.size() + " stations");
                        addMultiple(stations);
                        Toast successToast = Toast.makeText(context, context.getResources().getString(R.string.notify_load_playlist_ok, stations.size(), filePath, fileName), Toast.LENGTH_LONG);
                        successToast.show();
                    } else {
                        Log.e("LOAD", "Load failed");
                        Toast errorToast = Toast.makeText(context, context.getResources().getString(R.string.notify_load_playlist_nok, filePath, fileName), Toast.LENGTH_LONG);
                        errorToast.show();
                    }
                    // LiveData notification happens automatically via StationManager
                })
                .exceptionally(throwable -> {
                    Log.e("LOAD", "Load failed - Exception: " + throwable.getMessage());
                    Toast exceptionToast = Toast.makeText(context, context.getResources().getString(R.string.notify_load_playlist_nok, filePath, fileName), Toast.LENGTH_LONG);
                    exceptionToast.show();
                    // LiveData notification happens automatically via StationManager
                    return null;
                });
    }

    public void LoadM3USimple(final Reader reader, final String displayName) {
        // Use SAF-specific string resource for import notification
        Toast toast = Toast.makeText(context, context.getResources().getString(R.string.notify_load_playlist_now_saf, displayName), Toast.LENGTH_LONG);
        toast.show();

        m3uService.loadM3UFromReader(reader, displayName)
                .thenAccept(result -> {
                    if (result.isSuccess()) {
                        List<DataRadioStation> stations = result.getData();
                        Log.i("LOAD", "Loaded " + stations.size() + " stations");
                        Toast successToast = Toast.makeText(context, context.getResources().getString(R.string.notify_load_playlist_ok_nf, stations.size()), Toast.LENGTH_LONG);
                        addMultiple(stations);
                        successToast.show();
                    } else {
                        Log.e("LOAD", "Load failed");
                        Toast errorToast = Toast.makeText(context, context.getResources().getString(R.string.notify_load_playlist_nok, displayName, ""), Toast.LENGTH_LONG);
                        errorToast.show();
                    }
                    // LiveData notification happens automatically via StationManager
                })
                .exceptionally(throwable -> {
                    Log.e("LOAD", "Load failed - Exception: " + throwable.getMessage());
                    Toast exceptionToast = Toast.makeText(context, context.getResources().getString(R.string.notify_load_playlist_nok, displayName, ""), Toast.LENGTH_LONG);
                    exceptionToast.show();
                    // LiveData notification happens automatically via StationManager
                    return null;
                });
    }

}

