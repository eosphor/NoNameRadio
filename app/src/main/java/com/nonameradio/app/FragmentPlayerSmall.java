package com.nonameradio.app;

import android.app.Activity;
import android.app.Application;
import android.content.*;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.nonameradio.app.core.event.EventBus;
import com.nonameradio.app.core.event.PlayerStateChangeEvent;
import com.nonameradio.app.core.event.MetaUpdateEvent;
import com.nonameradio.app.core.event.PlayerServiceBoundEvent;

import com.nonameradio.app.history.TrackHistoryRepository;
import com.nonameradio.app.players.mpd.MPDClient;
import com.nonameradio.app.service.PauseReason;
import com.nonameradio.app.service.PlayerService;
import com.nonameradio.app.service.MediaSessionUtil;
import com.nonameradio.app.station.DataRadioStation;
import com.nonameradio.app.station.StationActions;
import com.nonameradio.app.station.live.StreamLiveInfo;

public class FragmentPlayerSmall extends Fragment {
    private TrackHistoryRepository trackHistoryRepository;

    public enum Role {
        HEADER,
        PLAYER
    }

    public interface Callback {
        void onToggle();
    }

    private MPDClient mpdClient;

    private Callback callback;
    
    // EventBus listeners (keep references for unregister)
    private final EventBus.EventListener<PlayerStateChangeEvent> playerStateListener = event -> fullUpdate();
    private final EventBus.EventListener<MetaUpdateEvent> metaUpdateListener = event -> fullUpdate();
    private final EventBus.EventListener<PlayerServiceBoundEvent> serviceBoundListener = event -> tryPlayAtStart();

    private Role role = Role.PLAYER;

    private TextView textViewStationName;
    private TextView textViewLiveInfo;

    private TextView textViewLiveInfoBig;

    private ImageView imageViewIcon;

    private ImageButton buttonPlay;
    private ImageButton buttonMore;

    private boolean firstPlayAttempted = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NoNameRadioApp app = (NoNameRadioApp) requireActivity().getApplication();
        mpdClient = app.getMpdClient();
        trackHistoryRepository = app.getTrackHistoryRepository();

        // Register activity lifecycle callbacks
        app.registerActivityLifecycleCallbacks(new LifecycleCallbacks());

        // EventBus listeners are registered in onResume()
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.layout_player_small, container, false);

        textViewStationName = view.findViewById(R.id.textViewStationName);
        textViewLiveInfo = view.findViewById(R.id.textViewLiveInfo);
        textViewLiveInfoBig = view.findViewById(R.id.textViewLiveInfoBig);
        imageViewIcon = view.findViewById(R.id.playerRadioImage);

        buttonPlay = view.findViewById(R.id.buttonPlay);
        buttonMore = view.findViewById(R.id.buttonMore);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup click listeners after views are created
        buttonPlay.setOnClickListener(v -> {
            // Double-check current state to prevent double playback
            boolean isCurrentlyPlaying = MediaSessionUtil.isPlaying();
            Log.d("FragmentPlayerSmall", "Play button clicked - isPlaying: " + isCurrentlyPlaying);
            
            if (isCurrentlyPlaying) {
                if (MediaSessionUtil.isRecording()) {
                    MediaSessionUtil.stopRecording();
                }

                MediaSessionUtil.pause(PauseReason.USER);
                fullUpdate(); // Immediate UI update
            } else {
                playLastFromHistory();
                fullUpdate(); // Immediate UI update
            }
        });

        buttonMore.setOnClickListener(v2 -> {
            DataRadioStation station = Utils.getCurrentOrLastStation(requireContext());
            if (station == null) {
                return;
            }

            NoNameRadioApp app = (NoNameRadioApp) requireActivity().getApplication();
            FavouriteManager favouriteManager = app.getFavouriteManager();
            boolean isInFavorites = favouriteManager.has(station.StationUuid);

            showPlayerMenu(station, isInFavorites);
        });

        view.setOnClickListener(v3 -> {
            if (callback != null) {
                callback.onToggle();
            }
        });

        tryPlayAtStart();
        fullUpdate();
        setupStationIcon();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register EventBus listeners for player updates
        EventBus.register(PlayerStateChangeEvent.class, playerStateListener);
        EventBus.register(MetaUpdateEvent.class, metaUpdateListener);
        EventBus.register(PlayerServiceBoundEvent.class, serviceBoundListener);

        fullUpdate();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Unregister EventBus listeners
        EventBus.unregister(PlayerStateChangeEvent.class, playerStateListener);
        EventBus.unregister(MetaUpdateEvent.class, metaUpdateListener);
        EventBus.unregister(PlayerServiceBoundEvent.class, serviceBoundListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setRole(Role role) {
        this.role = role;
        fullUpdate();
    }

    private void playLastFromHistory() {
        NoNameRadioApp app = (NoNameRadioApp) requireActivity().getApplication();
        DataRadioStation station = MediaSessionUtil.getCurrentStation();

        if (station == null) {
            HistoryManager historyManager = app.getHistoryManager();
            station = historyManager.getFirst();
        }

        if (station != null && !MediaSessionUtil.isPlaying()) {
            Utils.showPlaySelection(app, station, getActivity().getSupportFragmentManager());
        }
    }

    private void tryPlayAtStart() {
        boolean play = false;
	boolean auto_off = false;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext().getApplicationContext());
        if (!firstPlayAttempted && MediaSessionUtil.isServiceBound()) {
            firstPlayAttempted = true;

            if (!MediaSessionUtil.isPlaying()) {
                play = sharedPreferences.getBoolean("auto_play_on_startup", false);
            }
        }

        if (play) {
            auto_off = sharedPreferences.getBoolean("auto_off_on_startup", false);
            if (auto_off) {
                int timeout;
                try {
                    timeout = Integer.parseInt(sharedPreferences.getString("auto_off_timeout", "10"));
                } catch(Exception e) {
                    timeout=10;
                }
                MediaSessionUtil.addTimer(timeout * 60);
            }
            playLastFromHistory();
        }
    }

    private void setupStationIcon() {
        boolean useCircularIcons = PreferenceManager.getDefaultSharedPreferences(requireContext().getApplicationContext()).getBoolean("circular_icons", false);
        if (useCircularIcons) {
            imageViewIcon.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.black));
        }

        ImageView transparentCircle = requireView().findViewById(R.id.transparentCircle);
        transparentCircle.setVisibility(useCircularIcons ? View.VISIBLE : View.GONE);
    }

    private void fullUpdate() {
        DataRadioStation station = Utils.getCurrentOrLastStation(requireContext());
        boolean isCurrentlyPlaying = MediaSessionUtil.isPlaying();
        
        // Show play button if not playing, pause button if playing
        if (isCurrentlyPlaying) {
            buttonPlay.setImageResource(R.drawable.ic_pause_circle);
            buttonPlay.setContentDescription(getResources().getString(R.string.detail_pause));
        } else {
            buttonPlay.setImageResource(R.drawable.ic_play_circle);
            buttonPlay.setContentDescription(getResources().getString(R.string.detail_play));
        }

        final String stationName = station != null ? station.Name : "";
        textViewStationName.setText(stationName);

        StreamLiveInfo liveInfo = MediaSessionUtil.getMetadataLive();
        String streamTitle = liveInfo != null ? liveInfo.getTitle() : null;
        if (!TextUtils.isEmpty(streamTitle)) {
            textViewLiveInfo.setVisibility(View.VISIBLE);
            textViewLiveInfo.setText(streamTitle);
            textViewStationName.setGravity(Gravity.BOTTOM);
        } else {
            textViewLiveInfo.setVisibility(View.GONE);
            textViewStationName.setGravity(Gravity.CENTER_VERTICAL);
        }

        textViewLiveInfoBig.setText(stationName);

        if (!Utils.shouldLoadIcons(getContext())) {
            imageViewIcon.setVisibility(View.GONE);
        } else if (station != null && station.hasIcon()) {
            imageViewIcon.setVisibility(View.VISIBLE);
            MediaSessionUtil.getStationIcon(imageViewIcon, station.IconUrl);
        } else {
            imageViewIcon.setVisibility(View.VISIBLE);
            imageViewIcon.setImageResource(R.drawable.ic_launcher);
        }

        if (role == Role.PLAYER) {
            buttonPlay.setVisibility(View.VISIBLE);
            buttonMore.setVisibility(View.GONE);

            textViewStationName.setVisibility(View.VISIBLE);
            textViewLiveInfoBig.setVisibility(View.GONE);
        } else if (role == Role.HEADER) {
            buttonPlay.setVisibility(View.GONE);
            buttonMore.setVisibility(View.VISIBLE);

            textViewLiveInfo.setVisibility(View.GONE);
            textViewStationName.setVisibility(View.GONE);
            textViewLiveInfoBig.setVisibility(View.VISIBLE);
        }
    }

    private void showPlayerMenu(@NonNull final DataRadioStation currentStation, final boolean stationIsInFavourites) {
        final PopupMenu dropDownMenu = new PopupMenu(getContext(), buttonMore);
        dropDownMenu.getMenuInflater().inflate(R.menu.menu_player, dropDownMenu.getMenu());
        dropDownMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.action_homepage: {
                    StationActions.showWebLinks(requireActivity(), currentStation);
                    break;
                }
                case R.id.action_share: {
                    StationActions.share(requireContext(), currentStation);
                    break;
                }
                case R.id.action_set_alarm: {
                    StationActions.setAsAlarm(requireActivity(), currentStation);
                    break;
                }
                case R.id.action_delete_stream_history: {
                    trackHistoryRepository.deleteHistory();
                    break;
                }
            }

            return true;
        });

        dropDownMenu.show();
    }

    class LifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            Context ctx = getContext();
            if (ctx == null) {
                return;
            }

            tryPlayAtStart();
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }
}
