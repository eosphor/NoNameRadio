package com.nonameradio.app;
import com.nonameradio.app.core.event.EventBus;
import com.nonameradio.app.core.event.HideLoadingEvent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nonameradio.app.station.ItemAdapterStation;
import com.nonameradio.app.station.DataRadioStation;
import com.nonameradio.app.station.ItemAdapterIconOnlyStation;
import com.nonameradio.app.interfaces.IAdapterRefreshable;
import com.nonameradio.app.station.StationActions;
import com.nonameradio.app.station.StationsFilter;
import com.nonameradio.app.service.MediaSessionUtil;
import com.nonameradio.app.service.PauseReason;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import okhttp3.OkHttpClient;

public class FragmentStarred extends Fragment implements IAdapterRefreshable {
    private static final String TAG = "FragmentStarred";

    private RecyclerView rvStations;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isDownloading = false;

    private FavouriteManager favouriteManager;

    void onStationClick(DataRadioStation theStation) {
        // Check if this station is currently playing
        DataRadioStation currentStation = MediaSessionUtil.getCurrentStation();

        if (currentStation != null && currentStation.StationUuid.equals(theStation.StationUuid) && MediaSessionUtil.isPlaying()) {
            // Same station is playing - pause it
            MediaSessionUtil.pause(PauseReason.USER);
        } else {
            // Different station or nothing playing - start this station
            Utils.play((NoNameRadioApp) getActivity().getApplication(), theStation);
        }
    }

    public void RefreshListGui() {
        if (BuildConfig.DEBUG) Log.d(TAG, "refreshing the stations list.");

        ItemAdapterStation adapter = (ItemAdapterStation) rvStations.getAdapter();

        if (BuildConfig.DEBUG) Log.d(TAG, "stations count:" + favouriteManager.getListStations().size());

        adapter.updateList(this, favouriteManager.getListStations());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        NoNameRadioApp app = (NoNameRadioApp) requireActivity().getApplication();
        favouriteManager = app.getFavouriteManager();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stations, container, false);
        rvStations = view.findViewById(R.id.recyclerViewStations);

        // Always use ItemAdapterStation for unified card appearance across all pages
        ItemAdapterStation adapter;
        adapter = new ItemAdapterStation(getActivity(), R.layout.list_item_station, StationsFilter.FilterType.LOCAL);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(RecyclerView.VERTICAL);

        rvStations.setAdapter(adapter);
        rvStations.setLayoutManager(llm);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvStations.getContext(),
                llm.getOrientation());
        rvStations.addItemDecoration(dividerItemDecoration);
        adapter.enableItemMoveAndRemoval(rvStations);

        adapter.setStationActionsListener(new ItemAdapterStation.StationActionsListener() {
            @Override
            public void onStationClick(DataRadioStation station, int pos) {
                FragmentStarred.this.onStationClick(station);
            }

            @Override
            public void onStationSwiped(final DataRadioStation station) {
                StationActions.removeFromFavourites(requireContext(), getView(), station);
            }

            @Override
            public void onStationMoved(int from, int to) {
                favouriteManager.moveWithoutNotify(from, to);
            }

            @Override
            public void onStationMoveFinished() {
                // We don't want to update RecyclerView during its layout process
                requireView().post(() -> {
                    favouriteManager.Save();
                    favouriteManager.notifyObservers();
                });
            }
        });

        swipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "onRefresh called from SwipeRefreshLayout");
                            }
                            RefreshDownloadList();
                        }
                    }
            );
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe favourites changes using LiveData - automatic lifecycle management
        favouriteManager.getStationsLiveData().observe(getViewLifecycleOwner(), stations -> {
            RefreshListGui();
        });

        // Initial load
        RefreshListGui();
    }

    void RefreshDownloadList(){
        NoNameRadioApp app = (NoNameRadioApp) getActivity().getApplication();
        final OkHttpClient httpClient = app.getHttpClient();
        ArrayList<String> listUUids = new ArrayList<String>();
        for (DataRadioStation station : favouriteManager.getListStations()){
            listUUids.add(station.StationUuid);
        }
        Log.d(TAG, "Search for items: "+listUUids.size());

        if (isDownloading) return;
        isDownloading = true;

        com.nonameradio.app.core.utils.AsyncExecutor.executeIOTask(
            () -> Utils.getStationsByUuid(httpClient, getActivity(), listUUids),
            result -> {
                isDownloading = false;
                DownloadFinished();
                if(getContext() != null)
                    EventBus.post(HideLoadingEvent.INSTANCE);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Download relativeUrl finished");
                }
                if (result != null) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Download relativeUrl OK");
                    }
                    Log.d(TAG, "Found items: "+result.size());
                    SyncList(result);
                    RefreshListGui();
                } else {
                    try {
                        Toast toast = Toast.makeText(getContext(), getResources().getText(R.string.error_list_update), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    catch(Exception e){
                        Log.e("ERR",e.toString());
                    }
                }
            },
            throwable -> {
                isDownloading = false;
                DownloadFinished();
                if(getContext() != null)
                    EventBus.post(HideLoadingEvent.INSTANCE);
                Log.e(TAG, "Error downloading stations", throwable);
                try {
                    Toast toast = Toast.makeText(getContext(), getResources().getText(R.string.error_list_update), Toast.LENGTH_SHORT);
                    toast.show();
                }
                catch(Exception e){
                    Log.e("ERR",e.toString());
                }
            }
        );
    }

    private void SyncList(List<DataRadioStation> list_new) {
        ArrayList<String> to_remove = new ArrayList<String>();
        for (DataRadioStation station_current: favouriteManager.getListStations()){
            boolean found = false;
            for (DataRadioStation station_new: list_new){
                if (station_new.StationUuid.equals(station_current.StationUuid)) {
                    found = true;
                    break;
                }
            }
            if (!found){
                Log.d(TAG,"Remove station: " + station_current.StationUuid + " - " + station_current.Name);
                to_remove.add(station_current.StationUuid);
                station_current.DeletedOnServer = true;
            }
        }
        Log.d(TAG,"replace items");
        favouriteManager.replaceList(list_new);
        Log.d(TAG,"fin save");

        if (to_remove.size() > 0) {
            Toast toast = Toast.makeText(getContext(), getResources().getString(R.string.notify_sync_list_deleted_entries, to_remove.size(), favouriteManager.size()), Toast.LENGTH_LONG);
            toast.show();
        }
    }

    protected void DownloadFinished() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rvStations.setAdapter(null);
        // LiveData observer is automatically unregistered via getViewLifecycleOwner()
    }

    /**
     * Get the RecyclerView for TV remote navigation
     * @return the RecyclerView containing the stations list
     */
    public RecyclerView getRecyclerView() {
        return rvStations;
    }
}