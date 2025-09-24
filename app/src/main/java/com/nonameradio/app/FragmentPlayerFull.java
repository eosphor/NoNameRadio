package com.nonameradio.app;
import androidx.lifecycle.Observer;
import java.util.List;
import com.nonameradio.app.recording.DataRecording;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Iterator;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.paging.PagedList;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.nonameradio.app.utils.ImageLoader;

import com.nonameradio.app.history.TrackHistoryAdapter;
import com.nonameradio.app.history.TrackHistoryEntry;
import com.nonameradio.app.history.TrackHistoryRepository;
import com.nonameradio.app.history.TrackHistoryViewModel;
import com.nonameradio.app.recording.Recordable;
import com.nonameradio.app.recording.RecordingsAdapter;
import com.nonameradio.app.recording.RecordingsManager;
import com.nonameradio.app.recording.RunningRecordingInfo;
import com.nonameradio.app.service.PauseReason;
import com.nonameradio.app.service.PlayerService;
import com.nonameradio.app.service.MediaSessionUtil;
import com.nonameradio.app.station.DataRadioStation;
import com.nonameradio.app.station.StationActions;
import com.nonameradio.app.station.live.ShoutcastInfo;
import com.nonameradio.app.station.live.StreamLiveInfo;
import com.nonameradio.app.station.live.metadata.TrackMetadata;
import com.nonameradio.app.station.live.metadata.TrackMetadataCallback;
import com.nonameradio.app.station.live.metadata.TrackMetadataSearcher;
import com.nonameradio.app.utils.RefreshHandler;
import com.nonameradio.app.views.RecyclerAwareNestedScrollView;
import com.nonameradio.app.views.TagsView;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;

public class FragmentPlayerFull extends Fragment {
    private final String TAG = "FragmentPlayerFull";

    private final static int PERM_REQ_STORAGE_RECORD = 1001;

    /**
     * Fragment may be a part of another view which could be dragged/scrolled
     * and certain hacks may require the fragment to request them to stop
     * intercepting touch events to not end up confused.
     */
    public interface TouchInterceptListener {
        void requestDisallowInterceptTouchEvent(boolean disallow);
    }

    private TouchInterceptListener touchInterceptListener;

    private BroadcastReceiver updateUIReceiver;

    private boolean initialized = false;

    private final RefreshHandler refreshHandler = new RefreshHandler();
    private final TimedUpdateTask timedUpdateTask = new TimedUpdateTask(this);
    private static final int TIMED_UPDATE_INTERVAL = 1000; // 1 second

    private PlayerTrackMetadataCallback trackMetadataCallback;
    private TrackMetadataCallback.FailureType trackMetadataLastFailureType = null;
    private StreamLiveInfo lastLiveInfoForTrackMetadata = null;

    private RecordingsManager recordingsManager;
    private androidx.lifecycle.Observer<List<com.nonameradio.app.recording.DataRecording>> recordingsObserver;

    private com.nonameradio.app.presentation.ui.PlayerUIController playerUIController;

    private FavouriteManager favouriteManager;
    private final FavouritesObserver favouritesObserver = new FavouritesObserver();

    private TrackHistoryRepository trackHistoryRepository;
    private TrackHistoryAdapter trackHistoryAdapter;

    private RecordingsAdapter recordingsAdapter;

    private boolean storagePermissionsDenied = false;
    private boolean forceAlbumArtRefresh = false;

    private RecyclerAwareNestedScrollView scrollViewContent;

    private ViewPager pagerArtAndInfo;

    public TextView textViewGeneralInfo;
    public TextView textViewTimePlayed;
    public TextView textViewNetworkUsageInfo;
    public TextView textViewTimeCached;

    public Group groupRecordings;
    public ImageView imgRecordingIcon;
    public TextView textViewRecordingSize;
    public TextView textViewRecordingName;

    private ViewPager pagerHistoryAndRecordings;
    private HistoryAndRecordsPagerAdapter historyAndRecordsPagerAdapter;

    private TrackHistoryViewModel trackHistoryViewModel;

    public ImageButton btnPlay;
    public ImageButton btnPrev;
    public ImageButton btnNext;
    public ImageButton btnRecord;
    public ImageButton btnFavourite;
    public android.widget.ProgressBar progressBarLoading;

    public ArtAndInfoPagerAdapter artAndInfoPagerAdapter;
    public android.view.animation.Animation recordingAnimation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        NoNameRadioApp app = (NoNameRadioApp) requireActivity().getApplication();

        recordingsManager = app.getRecordingsManager();
        recordingsObserver = recordings -> updateRecordings();

        favouriteManager = app.getFavouriteManager();

        // Initialize PlayerUIController
        playerUIController = new com.nonameradio.app.presentation.ui.PlayerUIController(
            this, com.nonameradio.app.core.di.DependencyInjector.getPlayerService());

        trackHistoryAdapter = new TrackHistoryAdapter(requireActivity());
        trackHistoryAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            public void onItemRangeInserted(int positionStart, int itemCount) {
                final LinearLayoutManager lm = (LinearLayoutManager) historyAndRecordsPagerAdapter.recyclerViewSongHistory.getLayoutManager();
                if (lm.findFirstVisibleItemPosition() < 2) {
                    historyAndRecordsPagerAdapter.recyclerViewSongHistory.scrollToPosition(0);
                }
            }
        });

        trackHistoryRepository = app.getTrackHistoryRepository();

        updateUIReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
//                    case PlayerService.PLAYER_SERVICE_TIMER_UPDATE: {
//                        timerUpdate();
//                    }
                    case PlayerService.PLAYER_SERVICE_STATE_CHANGE: {

                    }
                    case PlayerService.PLAYER_SERVICE_META_UPDATE: {
                        fullUpdate();
                    }
                }
            }
        };

        View view = inflater.inflate(R.layout.layout_player_full, container, false);

        scrollViewContent = view.findViewById(R.id.scrollViewContent);

        pagerArtAndInfo = view.findViewById(R.id.pagerArtAndInfo);
        artAndInfoPagerAdapter = new ArtAndInfoPagerAdapter(requireContext(), pagerArtAndInfo);
        pagerArtAndInfo.setAdapter(artAndInfoPagerAdapter);

        /* A hack to make horizontal ViewPager play nice with vertical ScrollView
         * Credits to https://stackoverflow.com/a/16224484/1741638
         */
        pagerArtAndInfo.setOnTouchListener(new View.OnTouchListener() {
            private static final int DRAG_THRESHOLD = 30;
            private int downX;
            private int downY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = (int) event.getRawX();
                        downY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int distanceX = Math.abs((int) event.getRawX() - downX);
                        int distanceY = Math.abs((int) event.getRawY() - downY);

                        if (distanceX > distanceY && distanceX > DRAG_THRESHOLD) {
                            pagerArtAndInfo.getParent().requestDisallowInterceptTouchEvent(true);
                            scrollViewContent.getParent().requestDisallowInterceptTouchEvent(false);
                            if (touchInterceptListener != null) {
                                touchInterceptListener.requestDisallowInterceptTouchEvent(true);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        scrollViewContent.getParent().requestDisallowInterceptTouchEvent(false);
                        pagerArtAndInfo.getParent().requestDisallowInterceptTouchEvent(false);
                        if (touchInterceptListener != null) {
                            touchInterceptListener.requestDisallowInterceptTouchEvent(false);
                        }
                        break;
                }
                return false;
            }
        });

        textViewGeneralInfo = view.findViewById(R.id.textViewGeneralInfo);
        textViewTimePlayed = view.findViewById(R.id.textViewTimePlayed);
        textViewNetworkUsageInfo = view.findViewById(R.id.textViewNetworkUsageInfo);
        textViewTimeCached = view.findViewById(R.id.textViewTimeCached);

        groupRecordings = view.findViewById(R.id.group_recording_info);
        imgRecordingIcon = view.findViewById(R.id.imgRecordingIcon);
        textViewRecordingSize = view.findViewById(R.id.textViewRecordingSize);
        textViewRecordingName = view.findViewById(R.id.textViewRecordingName);

        pagerHistoryAndRecordings = view.findViewById(R.id.pagerHistoryAndRecordings);
        historyAndRecordsPagerAdapter = new HistoryAndRecordsPagerAdapter(requireContext(), pagerHistoryAndRecordings);
        pagerHistoryAndRecordings.setAdapter(historyAndRecordsPagerAdapter);

        btnPlay = view.findViewById(R.id.buttonPlay);
        btnPrev = view.findViewById(R.id.buttonPrev);
        btnNext = view.findViewById(R.id.buttonNext);
        btnRecord = view.findViewById(R.id.buttonRecord);
        btnFavourite = view.findViewById(R.id.buttonFavorite);
        progressBarLoading = view.findViewById(R.id.progressBarLoading);

        historyAndRecordsPagerAdapter.recyclerViewSongHistory.setAdapter(trackHistoryAdapter);

        LinearLayoutManager llmHistory = new LinearLayoutManager(getContext());
        llmHistory.setOrientation(RecyclerView.VERTICAL);
        historyAndRecordsPagerAdapter.recyclerViewSongHistory.setLayoutManager(llmHistory);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(historyAndRecordsPagerAdapter.recyclerViewSongHistory.getContext(), llmHistory.getOrientation());
        historyAndRecordsPagerAdapter.recyclerViewSongHistory.addItemDecoration(dividerItemDecoration);

        trackHistoryViewModel = new ViewModelProvider(this).get(TrackHistoryViewModel.class);
        trackHistoryViewModel.getAllHistoryPaged().observe(getViewLifecycleOwner(), new Observer<PagedList<TrackHistoryEntry>>() {
            @Override
            public void onChanged(@Nullable PagedList<TrackHistoryEntry> songHistoryEntries) {
                trackHistoryAdapter.submitList(songHistoryEntries);
            }
        });

        recordingsAdapter = new RecordingsAdapter(requireContext(), recordingsManager);
        recordingsAdapter.setOnDeleteClickListener(recording -> {
            recordingsManager.deleteRecording(recording);
            updateRecordings();
        });
        recordingsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            public void onItemRangeInserted(int positionStart, int itemCount) {
                final LinearLayoutManager lm = (LinearLayoutManager) historyAndRecordsPagerAdapter.recyclerViewRecordings.getLayoutManager();
                if (lm.findFirstVisibleItemPosition() < 2) {
                    historyAndRecordsPagerAdapter.recyclerViewRecordings.scrollToPosition(0);
                }
            }
        });

        historyAndRecordsPagerAdapter.recyclerViewRecordings.setAdapter(recordingsAdapter);

        LinearLayoutManager llmRecordings = new LinearLayoutManager(getContext());
        llmRecordings.setOrientation(RecyclerView.VERTICAL);
        historyAndRecordsPagerAdapter.recyclerViewRecordings.setLayoutManager(llmRecordings);

        historyAndRecordsPagerAdapter.recyclerViewRecordings.addItemDecoration(dividerItemDecoration);

        // The scrollable part of the player should have the height of its parent but
        // we only can do this at the runtime.
        ViewTreeObserver viewTreeObserver = pagerHistoryAndRecordings.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(() -> {
                ViewGroup.LayoutParams layoutParams = pagerHistoryAndRecordings.getLayoutParams();
                final int newHeight = scrollViewContent.getHeight();
                if (newHeight != layoutParams.height) {
                    layoutParams.height = newHeight;
                    pagerHistoryAndRecordings.setLayoutParams(layoutParams);
                }
            });
        }

        return view;
    }

    public void init() {
        if (!initialized) {
            fullUpdate();
        }
    }

    /**
     * Public method to refresh the view when it becomes visible
     * Call this when the expanded player view is shown
     */
    public void refreshOnVisible() {
        if (isVisible() && initialized) {
            // Force refresh of all content
            lastLiveInfoForTrackMetadata = null;
            trackMetadataLastFailureType = null;
            forceAlbumArtRefresh = true;
            
            // Clear current album art to show loading state
            if (artAndInfoPagerAdapter != null && artAndInfoPagerAdapter.imageViewArt != null) {
                artAndInfoPagerAdapter.imageViewArt.setImageDrawable(null);
            }
            
            fullUpdate();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MediaSessionUtil.isPlaying()) {
                    if (MediaSessionUtil.isRecording()) {
                        MediaSessionUtil.stopRecording();
                        updateRunningRecording();
                    }

                    MediaSessionUtil.pause(PauseReason.USER);
                } else {
                    playLastFromHistory();
                }

                // Update button state immediately and again after delay to catch service state change
                updatePlaybackButtons(MediaSessionUtil.isPlaying(), MediaSessionUtil.isRecording());
                com.nonameradio.app.core.utils.UiHandler.postDelayed(() -> {
                    updatePlaybackButtons(MediaSessionUtil.isPlaying(), MediaSessionUtil.isRecording());
                    // Also update all station info and album art in case station changed
                    fullUpdate();
                }, 100);
            }
        });

        btnPrev.setOnClickListener(view -> {
            MediaSessionUtil.skipToPrevious();
            // Force UI update after station change
            com.nonameradio.app.core.utils.UiHandler.postDelayed(this::fullUpdate, 100);
        });
        btnNext.setOnClickListener(view -> {
            MediaSessionUtil.skipToNext();
            // Force UI update after station change
            com.nonameradio.app.core.utils.UiHandler.postDelayed(this::fullUpdate, 100);
        });

        btnRecord.setOnClickListener(view -> {
            if (MediaSessionUtil.isPlaying()) {
                if (MediaSessionUtil.isRecording()) {
                    MediaSessionUtil.stopRecording();
                } else {
                    if (recordingsManager.hasRecordingPermission()) {
                        MediaSessionUtil.startRecording();
                    } else {
                        requestPermissions(recordingsManager.getRequiredPermissions(), PERM_REQ_STORAGE_RECORD);
                    }
                }

                // Update UI immediately and schedule another update after brief delay
                updatePlaybackButtons(MediaSessionUtil.isPlaying(), MediaSessionUtil.isRecording());
                updateRunningRecording();
                
                // Schedule another update after 500ms to catch async state changes
                com.nonameradio.app.core.utils.UiHandler.postDelayed(() -> {
                    updatePlaybackButtons(MediaSessionUtil.isPlaying(), MediaSessionUtil.isRecording());
                    updateRunningRecording();
                }, 500);

                pagerHistoryAndRecordings.setCurrentItem(1, true);
            }
        });

        btnFavourite.setOnClickListener(v -> {
            DataRadioStation station = Utils.getCurrentOrLastStation(requireContext());

            if (station == null) {
                return;
            }

            if (favouriteManager.has(station.StationUuid)) {
                StationActions.removeFromFavourites(requireContext(), null, station);
            } else {
                StationActions.markAsFavourite(requireContext(), station);
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (hidden) {
            stopUpdating();
        } else {
            startUpdating();
            com.nonameradio.app.core.utils.UiHandler.postDelayed(this::fullUpdate, 100);
        }

        if (touchInterceptListener != null) {
            touchInterceptListener.requestDisallowInterceptTouchEvent(false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Ensure we update when fragment starts
        if (isVisible() && initialized) {
            refreshOnVisible();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        startUpdating();
        com.nonameradio.app.core.utils.UiHandler.postDelayed(this::fullUpdate, 100);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopUpdating();
    }

    public void setTouchInterceptListener(TouchInterceptListener touchInterceptListener) {
        this.touchInterceptListener = touchInterceptListener;
    }

    private void startUpdating() {
        if (!isVisible()) {
            return;
        }

        // Force album art refresh when view becomes visible
        lastLiveInfoForTrackMetadata = null;
        trackMetadataLastFailureType = null;
        forceAlbumArtRefresh = true;
        
        // Clear the current album art to force reload
        if (artAndInfoPagerAdapter != null && artAndInfoPagerAdapter.imageViewArt != null) {
            artAndInfoPagerAdapter.imageViewArt.setImageDrawable(null);
        }

        fullUpdate();

        refreshHandler.executePeriodically(timedUpdateTask, TIMED_UPDATE_INTERVAL);

        IntentFilter filter = new IntentFilter();

        filter.addAction(PlayerService.PLAYER_SERVICE_TIMER_UPDATE);
        filter.addAction(PlayerService.PLAYER_SERVICE_STATE_CHANGE);
        filter.addAction(PlayerService.PLAYER_SERVICE_META_UPDATE);

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(updateUIReceiver, filter);

        recordingsManager.getSavedRecordingsLiveData().observe(getViewLifecycleOwner(), recordingsObserver);

        favouriteManager.addObserver(favouritesObserver);
    }

    private void stopUpdating() {
        if (getView() == null) {
            return;
        }

        refreshHandler.cancel();

        if (trackMetadataCallback != null) {
            trackMetadataCallback.cancel();
        }

        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(updateUIReceiver);

        // LiveData automatically manages lifecycle

        favouriteManager.deleteObserver(favouritesObserver);
    }

    public void resetScroll() {
        scrollViewContent.scrollTo(0, 0);
        historyAndRecordsPagerAdapter.recyclerViewSongHistory.scrollToPosition(0);
        historyAndRecordsPagerAdapter.recyclerViewRecordings.scrollToPosition(0);
    }

    public boolean isScrolled() {
        return scrollViewContent.getScrollY() > 0;
    }

    private void playLastFromHistory() {
        NoNameRadioApp app = (NoNameRadioApp) requireActivity().getApplication();
        DataRadioStation station = MediaSessionUtil.getCurrentStation();

        if (station == null) {
            HistoryManager historyManager = app.getHistoryManager();
            station = historyManager.getFirst();
        }

        if (station != null) {
            Utils.showPlaySelection(app, station, getActivity().getSupportFragmentManager());
        }
    }

    private void fullUpdate() {
        if (!isAdded() || getContext() == null) {
            return;
        }
        DataRadioStation station = Utils.getCurrentOrLastStation(requireContext());

        if (station != null) {
            final ShoutcastInfo shoutcastInfo = MediaSessionUtil.getShoutcastInfo();
            // Shoutcast info is now displayed via PlayerUIController.buildStationInfo()

            final StreamLiveInfo liveInfo = MediaSessionUtil.getMetadataLive();
            String streamTitle = liveInfo != null ? liveInfo.getTitle() : null;

            if (!TextUtils.isEmpty(streamTitle)) {
                textViewGeneralInfo.setText(streamTitle);
            } else {
                textViewGeneralInfo.setText(station.Name);
            }

            Drawable flag = CountryFlagsLoader.getInstance().getFlag(requireContext(), station.CountryCode);
            if (flag != null) {
                float k = flag.getMinimumWidth() / (float) flag.getMinimumHeight();
                float viewHeight = artAndInfoPagerAdapter.textViewStationDescription.getTextSize();
                flag.setBounds(0, 0, (int) (k * viewHeight), (int) viewHeight);
            }

            artAndInfoPagerAdapter.textViewStationDescription.setCompoundDrawablesRelative(flag, null, null, null);

            // Add votes/clicks/trend statistics
            String statsText = buildStationStatsText(station);
            artAndInfoPagerAdapter.textViewStationStats.setText(statsText);

            artAndInfoPagerAdapter.textViewStationDescription.setText(station.getLongDetails(requireContext()));

            String[] tags = station.TagsAll.split(",");
            artAndInfoPagerAdapter.viewTags.setTags(Arrays.asList(tags));
            //artAndInfoPagerAdapter.viewTags.setTagSelectionCallback(tagSelectionCallback);
        }

        updateAlbumArt();
        updateRecordings();
        updatePlaybackButtons(MediaSessionUtil.isPlaying(), MediaSessionUtil.isRecording());

        // Update UI via PlayerUIController
        if (playerUIController != null) {
            playerUIController.performFullUpdate();
        }

        timedUpdateTask.run();

        initialized = true;
    }

    private void updatePlaybackButtons(boolean playing, boolean recording) {
        updatePlayButton(playing);
        updateRecordButton(playing, recording);
    }

    private void updatePlayButton(boolean playing) {
        if (playing) {
            btnPlay.setImageResource(R.drawable.ic_pause_circle);
            btnPlay.setContentDescription(getResources().getString(R.string.detail_pause));
        } else {
            btnPlay.setImageResource(R.drawable.ic_play_circle);
            btnPlay.setContentDescription(getResources().getString(R.string.detail_play));
        }
    }

    private void updateRecordButton(boolean playing, boolean recording) {
        btnRecord.setEnabled(playing);

        if (recording) {
            btnRecord.setImageResource(R.drawable.ic_stop_recording);
            btnRecord.setContentDescription(getResources().getString(R.string.detail_stop));
        } else {
            btnRecord.setImageResource(R.drawable.ic_start_recording);

            if (!storagePermissionsDenied) {
                btnRecord.setContentDescription(getResources().getString(R.string.image_button_record));
            } else {
                btnRecord.setContentDescription(getResources().getString(R.string.image_button_record_request_permission));
            }
        }
    }

    private void updateRecordings() {
        recordingsAdapter.setRecordings(recordingsManager.getSavedRecordings());
        updateRunningRecording();
    }

    private void updateRunningRecording() {
        Map<Recordable, RunningRecordingInfo> runningRecordings = recordingsManager.getRunningRecordings();
        if (MediaSessionUtil.isRecording() && !runningRecordings.isEmpty()) {
            // Get first recording info safely
            Iterator<Map.Entry<Recordable, RunningRecordingInfo>> iterator = runningRecordings.entrySet().iterator();
            if (iterator.hasNext()) {
                RunningRecordingInfo recordingInfo = iterator.next().getValue();

                groupRecordings.setVisibility(View.VISIBLE);
                imgRecordingIcon.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.blink_recording));
                textViewRecordingSize.setText(Utils.getReadableBytes(recordingInfo.getBytesWritten()));
                textViewRecordingName.setText(recordingInfo.getFileName());
            } else {
                groupRecordings.setVisibility(View.GONE);
                imgRecordingIcon.clearAnimation();
            }
        } else {
            groupRecordings.setVisibility(View.GONE);
            imgRecordingIcon.clearAnimation();
        }
    }

    private void updateAlbumArt() {
        DataRadioStation station = MediaSessionUtil.getCurrentStation();
        if (station == null) {
            return;
        }

        final StreamLiveInfo liveInfo = MediaSessionUtil.getMetadataLive();

        if (liveInfo == null) {
            forceAlbumArtRefresh = false;

            if (station.hasIcon()) {
                ImageLoader.loadImage(requireContext(), station.IconUrl, artAndInfoPagerAdapter.imageViewArt);
            } else {
                artAndInfoPagerAdapter.imageViewArt.setImageResource(R.drawable.ic_launcher);
            }

            return;
        }

        if (lastLiveInfoForTrackMetadata != null && liveInfo != null &&
                TextUtils.equals(lastLiveInfoForTrackMetadata.getArtist(), liveInfo.getArtist()) &&
                TextUtils.equals(lastLiveInfoForTrackMetadata.getTrack(), liveInfo.getTrack()) &&
                !TrackMetadataCallback.FailureType.RECOVERABLE.equals(trackMetadataLastFailureType)) {
            return;
        }

        final NoNameRadioApp app = (NoNameRadioApp) requireActivity().getApplication();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(app);
        String LastFMApiKey = sharedPref.getString("last_fm_api_key", "");

        if (TextUtils.isEmpty(liveInfo.getArtist()) || TextUtils.isEmpty(liveInfo.getTrack()) ||
                LastFMApiKey.isEmpty()) {
            if (station.hasIcon()) {
                // Use fresh loading if forced refresh is needed
                if (forceAlbumArtRefresh) {
                    ImageLoader.loadImageFresh(requireContext(), station.IconUrl, artAndInfoPagerAdapter.imageViewArt);
                    forceAlbumArtRefresh = false;
                } else {
                    ImageLoader.loadImage(requireContext(), station.IconUrl, artAndInfoPagerAdapter.imageViewArt);
                }
            } else {
                artAndInfoPagerAdapter.imageViewArt.setImageResource(R.drawable.ic_launcher);
            }
            return;
        }

        trackMetadataLastFailureType = null;
        lastLiveInfoForTrackMetadata = liveInfo;

        if (trackMetadataCallback != null) {
            trackMetadataCallback.cancel();
        }

        TrackMetadataSearcher trackMetadataSearcher = app.getTrackMetadataSearcher();

        final WeakReference<FragmentPlayerFull> fragmentWeakReference = new WeakReference<>(this);
        trackHistoryRepository.getLastInsertedHistoryItem((trackHistoryEntry, dao) -> {
            if (trackHistoryEntry == null) {
                Log.e(TAG, "trackHistoryEntry is null in updateAlbumArt which should not happen.");
                return;
            }

            if (!TextUtils.isEmpty(trackHistoryEntry.artUrl)) {
                return;
            }

            FragmentPlayerFull fragment = fragmentWeakReference.get();
            if (fragment != null) {
                fragment.requireActivity().runOnUiThread(() -> {
                    if (fragment.isResumed()) {
                        fragment.trackMetadataCallback = new PlayerTrackMetadataCallback(fragmentWeakReference, trackHistoryEntry);
                        trackMetadataSearcher.fetchTrackMetadata(LastFMApiKey, liveInfo.getArtist(), liveInfo.getTrack(), fragment.trackMetadataCallback);
                    }
                });
            }
        });
    }

    private void updateFavouriteButton() {
        DataRadioStation station = Utils.getCurrentOrLastStation(requireContext());

        if (station != null && favouriteManager.has(station.StationUuid)) {
            btnFavourite.setImageResource(R.drawable.ic_heart_24dp);
            btnFavourite.setContentDescription(requireContext().getApplicationContext().getString(R.string.detail_unstar));
        } else {
            btnFavourite.setImageResource(R.drawable.ic_heart_border_24dp);
            btnFavourite.setContentDescription(requireContext().getApplicationContext().getString(R.string.detail_star));
        }
    }

    private class FavouritesObserver implements java.util.Observer {

        @Override
        public void update(Observable o, Object arg) {
            updateFavouriteButton();
        }
    }

    private static class PlayerTrackMetadataCallback implements TrackMetadataCallback {
        private boolean canceled = false;
        private final WeakReference<FragmentPlayerFull> fragmentWeakReference;
        private final TrackHistoryEntry trackHistoryEntry;

        private PlayerTrackMetadataCallback(@NonNull WeakReference<FragmentPlayerFull> fragmentWeakReference, TrackHistoryEntry trackHistoryEntry) {
            this.fragmentWeakReference = fragmentWeakReference;
            this.trackHistoryEntry = trackHistoryEntry;
        }

        public void cancel() {
            canceled = true;
        }

        @Override
        public void onFailure(@NonNull FailureType failureType) {
            FragmentPlayerFull fragment = fragmentWeakReference.get();
            if (fragment != null) {
                fragment.requireActivity().runOnUiThread(() -> {
                    if (canceled) {
                        return;
                    }

                    fragment.trackMetadataLastFailureType = failureType;

                    DataRadioStation station = Utils.getCurrentOrLastStation(fragment.requireContext());

                    if (station != null && station.hasIcon()) {
                        // Use fresh loading if forced refresh is needed
                        if (fragment.forceAlbumArtRefresh) {
                            ImageLoader.loadImageFresh(fragment.requireContext(), station.IconUrl, fragment.artAndInfoPagerAdapter.imageViewArt);
                            fragment.forceAlbumArtRefresh = false;
                        } else {
                            ImageLoader.loadImage(fragment.requireContext(), station.IconUrl, fragment.artAndInfoPagerAdapter.imageViewArt);
                        }
                    } else {
                        fragment.artAndInfoPagerAdapter.imageViewArt.setImageResource(R.drawable.ic_launcher);
                    }

                    fragment.trackMetadataCallback = null;
                });
            }
        }

        @Override
        public void onSuccess(@NonNull final TrackMetadata trackMetadata) {
            FragmentPlayerFull fragment = fragmentWeakReference.get();
            if (fragment != null) {
                fragment.requireActivity().runOnUiThread(() -> {
                    if (canceled) {
                        return;
                    }

                    final List<TrackMetadata.AlbumArt> albumArts = trackMetadata.getAlbumArts();
                    if (!albumArts.isEmpty()) {
                        final String albumArtUrl = albumArts.get(0).url;

                        if (!TextUtils.isEmpty(albumArtUrl)) {
                            // Use fresh loading if forced refresh is needed
                            if (fragment.forceAlbumArtRefresh) {
                                ImageLoader.loadImageFresh(fragment.requireContext(), albumArtUrl, fragment.artAndInfoPagerAdapter.imageViewArt);
                                fragment.forceAlbumArtRefresh = false;
                            } else {
                                ImageLoader.loadImage(fragment.requireContext(), albumArtUrl, fragment.artAndInfoPagerAdapter.imageViewArt);
                            }

                            if (!albumArtUrl.equals(trackHistoryEntry.stationIconUrl)) {
                                fragment.trackHistoryRepository.setTrackArtUrl(trackHistoryEntry.uid, albumArtUrl);
                            }

                            fragment.trackMetadataCallback = null;

                            return;
                        }
                    }

                    onFailure(FailureType.UNRECOVERABLE);
                });
            }
        }
    }

    private class ArtAndInfoPagerAdapter extends PagerAdapter {
        private final ViewGroup layoutAlbumArt;
        private final ViewGroup layoutStationInfo;

        private final String[] titles;

        ImageView imageViewArt;
        TextView textViewStationDescription;
        TextView textViewStationStats;
        TagsView viewTags;

        ArtAndInfoPagerAdapter(@NonNull Context context, @NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);

            layoutAlbumArt = (ViewGroup) inflater.inflate(R.layout.page_player_album_art, parent, false);
            layoutStationInfo = (ViewGroup) inflater.inflate(R.layout.page_player_station_info, parent, false);

            titles = new String[]{getResources().getString(R.string.tab_player_art), getResources().getString(R.string.tab_player_info)};

            imageViewArt = layoutAlbumArt.findViewById(R.id.imageViewArt);

            textViewStationDescription = layoutStationInfo.findViewById(R.id.textViewStationDescription);
            textViewStationStats = layoutStationInfo.findViewById(R.id.textViewStationStats);
            viewTags = layoutStationInfo.findViewById(R.id.viewTags);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup collection, int position) {
            if (position == 0) {
                collection.addView(layoutAlbumArt);
                return layoutAlbumArt;
            } else {
                collection.addView(layoutStationInfo);
                return layoutStationInfo;
            }
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object view) {
            container.removeView((View) view);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    private class HistoryAndRecordsPagerAdapter extends PagerAdapter {
        private final ViewGroup layoutSongHistory;
        private final ViewGroup layoutRecordings;

        private final String[] titles;

        RecyclerView recyclerViewSongHistory;
        RecyclerView recyclerViewRecordings;

        HistoryAndRecordsPagerAdapter(@NonNull Context context, @NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);

            layoutSongHistory = (ViewGroup) inflater.inflate(R.layout.page_player_history, parent, false);
            layoutRecordings = (ViewGroup) inflater.inflate(R.layout.page_player_recordings, parent, false);

            titles = new String[]{getResources().getString(R.string.tab_player_history), getResources().getString(R.string.tab_player_recordings)};

            recyclerViewSongHistory = layoutSongHistory.findViewById(R.id.recyclerViewSongHistory);
            recyclerViewRecordings = layoutRecordings.findViewById(R.id.recyclerViewRecordings);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup collection, int position) {
            if (position == 0) {
                collection.addView(layoutSongHistory);
                return layoutSongHistory;
            } else {
                collection.addView(layoutRecordings);
                return layoutRecordings;
            }
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object view) {
            container.removeView((View) view);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    private static class TimedUpdateTask extends RefreshHandler.ObjectBoundRunnable<FragmentPlayerFull> {
        TimedUpdateTask(FragmentPlayerFull obj) {
            super(obj);
        }

        @Override
        protected void run(FragmentPlayerFull fragmentPlayerFull) {
            final ShoutcastInfo shoutcastInfo = MediaSessionUtil.getShoutcastInfo();

            if (MediaSessionUtil.isPlaying()) {
                String networkUsageInfo = Utils.getReadableBytes(MediaSessionUtil.getTransferredBytes());
                if (shoutcastInfo != null && shoutcastInfo.bitrate > 0) {
                    networkUsageInfo += " (" + shoutcastInfo.bitrate + " kbps)";
                }

                fragmentPlayerFull.textViewNetworkUsageInfo.setText(networkUsageInfo);

                final long now = System.currentTimeMillis();
                final long startTime = MediaSessionUtil.getLastPlayStartTime();
                long deltaSeconds = startTime > 0 ? ((now - startTime) / 1000) : 0;
                deltaSeconds = Math.max(deltaSeconds, 0);
                fragmentPlayerFull.textViewTimePlayed.setText(DateUtils.formatElapsedTime(deltaSeconds));

                fragmentPlayerFull.textViewTimeCached.setText(DateUtils.formatElapsedTime(MediaSessionUtil.getBufferedSeconds()));
            }

            // Update recording UI regardless of playback state
            fragmentPlayerFull.updateRunningRecording();

            // Update play/pause button state to ensure it stays in sync
            fragmentPlayerFull.updatePlaybackButtons(MediaSessionUtil.isPlaying(), MediaSessionUtil.isRecording());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == PERM_REQ_STORAGE_RECORD) {
            if (recordingsManager.hasRecordingPermission()) {
                storagePermissionsDenied = false;
                MediaSessionUtil.startRecording();
            } else {
                storagePermissionsDenied = true;
                Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.error_record_needs_write), Toast.LENGTH_SHORT);
                toast.show();
            }

            updatePlaybackButtons(MediaSessionUtil.isPlaying(), MediaSessionUtil.isRecording());
            updateRecordings();
        }
    }

    /**
     * Build formatted statistics text for the station
     */
    private String buildStationStatsText(DataRadioStation station) {
        if (station == null) {
            return "";
        }

        StringBuilder stats = new StringBuilder();

        // Add votes
        if (station.Votes > 0) {
            stats.append("👥 ");
            stats.append(formatNumber(station.Votes));
            stats.append(" votes");
        }

        // Add clicks
        if (station.ClickCount > 0) {
            if (stats.length() > 0) {
                stats.append(" • ");
            }
            stats.append("👆 ");
            stats.append(formatNumber(station.ClickCount));
            stats.append(" clicks");
        }

        // Add trend
        if (station.ClickTrend != 0) {
            if (stats.length() > 0) {
                stats.append(" • ");
            }
            if (station.ClickTrend > 0) {
                stats.append("📈 +");
                stats.append(station.ClickTrend);
            } else if (station.ClickTrend < 0) {
                stats.append("📉 ");
                stats.append(station.ClickTrend);
            }
        }

        return stats.toString();
    }

    /**
     * Format large numbers with K/M suffixes
     */
    private String formatNumber(int number) {
        if (number >= 1000000) {
            return String.format(Locale.ROOT, "%.1fM", number / 1000000.0);
        } else if (number >= 1000) {
            return String.format(Locale.ROOT, "%.1fK", number / 1000.0);
        } else {
            return String.valueOf(number);
        }
    }
}
