package com.nonameradio.app.data.repository.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nonameradio.app.BuildConfig;
import com.nonameradio.app.core.architecture.IStationRepository;
import com.nonameradio.app.core.architecture.Result;
import com.nonameradio.app.station.DataRadioStation;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Repository implementation for station persistence using SharedPreferences.
 * Provides async operations for saving and loading station data.
 */
public class StationRepository implements IStationRepository {
    private static final String TAG = "StationRepository";

    private final Context context;
    private final String saveId;

    public StationRepository(Context context, String saveId) {
        this.context = context;
        this.saveId = saveId;
    }

    @Override
    public CompletableFuture<Result<Void>> save(List<DataRadioStation> stations) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JSONArray arr = new JSONArray();
                for (DataRadioStation station : stations) {
                    arr.put(station.toJson());
                }

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = sharedPref.edit();
                String jsonStr = arr.toString();

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Saving stations: " + jsonStr);
                }

                editor.putString(saveId, jsonStr);
                editor.apply();

                return Result.success(null);
            } catch (Exception e) {
                Log.e(TAG, "Failed to save stations", e);
                return Result.error(e);
            }
        });
    }

    @Override
    public CompletableFuture<Result<List<DataRadioStation>>> load() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                String jsonStr = sharedPref.getString(saveId, null);

                if (jsonStr == null) {
                    Log.w(TAG, "No stations to load");
                    return Result.success(new ArrayList<>());
                }

                List<DataRadioStation> stations = DataRadioStation.DecodeJson(jsonStr);
                return Result.success(stations);
            } catch (Exception e) {
                Log.e(TAG, "Failed to load stations", e);
                return Result.error(e);
            }
        });
    }

    @Override
    public String getSaveId() {
        return saveId;
    }
}