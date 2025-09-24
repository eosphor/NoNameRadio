package com.nonameradio.app.station;
import com.nonameradio.app.core.event.HideLoadingEvent;
import com.nonameradio.app.core.event.EventBus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;

import com.nonameradio.app.ActivityMain;
import com.nonameradio.app.BuildConfig;
import com.nonameradio.app.FragmentBase;
import com.nonameradio.app.R;
import com.nonameradio.app.NoNameRadioApp;
import com.nonameradio.app.Utils;
import com.nonameradio.app.interfaces.IFragmentSearchable;
import com.nonameradio.app.service.MediaSessionUtil;
import com.nonameradio.app.service.PauseReason;
import com.nonameradio.app.utils.CustomFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

public class FragmentStations extends FragmentBase implements IFragmentSearchable {
    private static final String TAG = "FragmentStations";

    public static final String KEY_SEARCH_ENABLED = "SEARCH_ENABLED";

    private RecyclerView rvStations;
    private ViewGroup layoutError;
    private MaterialButton btnRetry;
    private SwipeRefreshLayout swipeRefreshLayout;

    private SharedPreferences sharedPref;

    private boolean searchEnabled = false;

    private StationsFilter stationsFilter;
    private StationsFilter.SearchStyle lastSearchStyle = StationsFilter.SearchStyle.ByName;
    private String lastQuery = "";
    private List<DataRadioStation> stationsList;

    void onStationClick(DataRadioStation theStation, int pos) {
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

    @Override
    protected void RefreshListGui() {
        if (rvStations == null || !hasUrl()) {
            return;
        }

        if (BuildConfig.DEBUG) Log.d(TAG, "refreshing the stations list.");

        Context ctx = getContext();
        if (sharedPref == null) {
            sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        }

        boolean show_broken = sharedPref.getBoolean("show_broken", false);

        ArrayList<DataRadioStation> filteredStationsList = new ArrayList<>();
        String jsonResult = getUrlResult();
        if (BuildConfig.DEBUG) Log.d(TAG, "JSON result length: " + (jsonResult != null ? jsonResult.length() : "null"));
        List<DataRadioStation> radioStations = DataRadioStation.DecodeJson(jsonResult);
        stationsList.clear();
        stationsList.addAll(radioStations);

        if (BuildConfig.DEBUG) Log.d(TAG, "station count:" + radioStations.size());

        for (DataRadioStation station : radioStations) {
            if (show_broken || station.Working) {
                filteredStationsList.add(station);
            }
        }

        ItemAdapterStation adapter = (ItemAdapterStation) rvStations.getAdapter();
        if (adapter != null) {
            adapter.updateList(null, filteredStationsList);
            if (searchEnabled) {
                stationsFilter.filter("");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("STATIONS","onCreateView()");
        stationsList = new LinkedList<>();
        Bundle bundle = getArguments();
        if (bundle != null) {
            searchEnabled = bundle.getBoolean(KEY_SEARCH_ENABLED, false);
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stations_remote, container, false);
        rvStations = view.findViewById(R.id.recyclerViewStations);
        layoutError = view.findViewById(R.id.layoutError);
        btnRetry = view.findViewById(R.id.btnRefresh);

        ItemAdapterStation adapter = new ItemAdapterStation(getActivity(), R.layout.list_item_station, StationsFilter.FilterType.GLOBAL);
        adapter.setStationActionsListener(new ItemAdapterStation.StationActionsListener() {
            @Override
            public void onStationClick(DataRadioStation station, int pos) {
                FragmentStations.this.onStationClick(station, pos);
            }

            @Override
            public void onStationSwiped(DataRadioStation station) {
            }

            @Override
            public void onStationMoved(int from, int to) {
            }

            @Override
            public void onStationMoveFinished() {
            }
        });

        if (searchEnabled) {
            stationsFilter = adapter.getFilter();

            stationsFilter.setDelayer(new CustomFilter.Delayer() {
                private int previousLength = 0;

                public long getPostingDelay(CharSequence constraint) {
                    if (constraint == null) {
                        return 0;
                    }

                    long delay = 0;
                    if (constraint.length() < previousLength) {
                        delay = 500;
                    }
                    previousLength = constraint.length();

                    return delay;
                }
            });

            adapter.setFilterListener(searchStatus -> {
                layoutError.setVisibility(searchStatus == StationsFilter.SearchStatus.ERROR ? View.VISIBLE : View.GONE);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(ActivityMain.ACTION_HIDE_LOADING));
                swipeRefreshLayout.setRefreshing(false);
            });

            btnRetry.setOnClickListener(v -> Search(lastSearchStyle, lastQuery));
        }

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        rvStations.setLayoutManager(llm);
        rvStations.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvStations.getContext(),
                llm.getOrientation());
        rvStations.addItemDecoration(dividerItemDecoration);

        swipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(
                () -> {
                    if (hasUrl()) {
                        DownloadUrl(true, false);
                    } else if (searchEnabled) {
                        // force refresh
                        stationsFilter.clearList();
                        Search(lastSearchStyle, lastQuery);
                    }
                }
        );

        RefreshListGui();

        if (lastQuery != null && stationsFilter != null){
            Log.d("STATIONS", "do queued search for: "+lastQuery + " style="+lastSearchStyle);
            stationsFilter.clearList();
            Search(lastSearchStyle, lastQuery);
        }

        // Auto-focus search field on Android TV devices will be handled in onResume()

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        
        // Debug logging to understand execution flow
        Log.d("STATIONS", "onResume: searchEnabled=" + searchEnabled + ", isTV=" + Utils.isRunningOnTV(getContext()) + ", context=" + (getContext() != null));
        
        // Auto-focus search field on Android TV devices
        if (searchEnabled && Utils.isRunningOnTV(getContext())) {
            Log.d("STATIONS", "onResume: TV detected, will attempt to auto-focus search after delay");
            // Use a longer delay to ensure the activity and search view are fully ready
            com.nonameradio.app.core.utils.UiHandler.postDelayed(() -> {
                Log.d("STATIONS", "Delayed runnable executing for TV auto-focus, activity=" + getActivity() + ", isAdded=" + isAdded());
                if (getActivity() instanceof ActivityMain && isAdded()) {
                    Log.d("STATIONS", "Calling focusSearchView on ActivityMain");
                    ((ActivityMain) getActivity()).focusSearchView();
                } else {
                    Log.d("STATIONS", "Cannot call focusSearchView - activity not ready or fragment not added");
                }
            }, 500); // 500ms delay
        } else {
            Log.d("STATIONS", "NOT attempting TV auto-focus: searchEnabled=" + searchEnabled + ", isTV=" + Utils.isRunningOnTV(getContext()));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rvStations.setAdapter(null);
    }

    @Override
    public void Search(StationsFilter.SearchStyle searchStyle, String query) {
        Log.d("STATIONS", "query = "+query + " searchStyle="+searchStyle);
        lastQuery = query;
        lastSearchStyle = searchStyle;

        if (rvStations != null && searchEnabled) {
            Log.d("STATIONS", "query a = "+query);
            if (!TextUtils.isEmpty(query)) {
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(ActivityMain.ACTION_SHOW_LOADING));
            }

            stationsFilter.setSearchStyle(searchStyle);
            stationsFilter.filter(query);
        }else{
            Log.d("STATIONS", "query b = "+query + " " + searchEnabled + " ");
        }
    }

    @Override
    public void clearSearch() {
        Log.d("STATIONS", "clearSearch");
        lastQuery = null;
        lastSearchStyle = StationsFilter.SearchStyle.ByName;

        if (rvStations != null && searchEnabled) {
            stationsFilter.setSearchStyle(StationsFilter.SearchStyle.ByName);
            stationsFilter.filter("");
        }
    }

    @Override
    protected void DownloadFinished() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Get the RecyclerView for TV remote navigation
     * @return the RecyclerView containing the stations list
     */
    public RecyclerView getRecyclerView() {
        return rvStations;
    }
}
