package com.nonameradio.app.presentation.ui;

import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.nonameradio.app.FragmentPlayerFull;
import com.nonameradio.app.FavouriteManager;
import com.nonameradio.app.NoNameRadioApp;
import com.nonameradio.app.R;
import com.nonameradio.app.Utils;
import com.nonameradio.app.core.domain.interfaces.IPlayerService;
import com.nonameradio.app.core.utils.UiUtils;
import com.nonameradio.app.service.MediaSessionUtil;
import com.nonameradio.app.station.DataRadioStation;
import com.nonameradio.app.station.live.ShoutcastInfo;
import com.nonameradio.app.station.live.StreamLiveInfo;

/**
 * Controller for managing player UI updates in FragmentPlayerFull
 */
public class PlayerUIController {
    private final FragmentPlayerFull fragment;
    private final IPlayerService playerService;

    public PlayerUIController(FragmentPlayerFull fragment, IPlayerService playerService) {
        this.fragment = fragment;
        this.playerService = playerService;
    }

    /**
     * Update play/pause button state
     */
    public void updatePlayPauseButton() {
        boolean isPlaying = playerService.isPlaying();

        if (fragment.btnPlay != null) {
            if (isPlaying) {
                fragment.btnPlay.setImageResource(R.drawable.ic_pause_circle);
                fragment.btnPlay.setContentDescription(fragment.getResources().getString(R.string.detail_pause));
            } else {
                fragment.btnPlay.setImageResource(R.drawable.ic_play_circle);
                fragment.btnPlay.setContentDescription(fragment.getResources().getString(R.string.detail_play));
            }
        }
    }

    /**
     * Update recording button state
     */
    public void updateRecordingButton() {
        boolean isPlaying = playerService.isPlaying();
        boolean isRecording = playerService.isRecording();

        if (fragment.btnRecord != null) {
            fragment.btnRecord.setEnabled(isPlaying);

            if (isRecording) {
                fragment.btnRecord.setImageResource(R.drawable.ic_stop_recording);
                fragment.btnRecord.setContentDescription(fragment.getResources().getString(R.string.detail_stop));
            } else {
                fragment.btnRecord.setImageResource(R.drawable.ic_start_recording);
                fragment.btnRecord.setContentDescription(fragment.getResources().getString(R.string.image_button_record));
            }
        }
    }

    /**
     * Update recording UI elements (icon, text, visibility)
     */
    public void updateRecordingUI() {
        updateRecordingButton();

        if (fragment.groupRecordings != null && fragment.imgRecordingIcon != null) {
            boolean isRecording = playerService.isRecording();

            if (isRecording) {
                fragment.groupRecordings.setVisibility(View.VISIBLE);

                // Load animation if not already loaded
                if (fragment.recordingAnimation == null) {
                    fragment.recordingAnimation = AnimationUtils.loadAnimation(fragment.getContext(), R.anim.blink_recording);
                }
                fragment.imgRecordingIcon.startAnimation(fragment.recordingAnimation);

                // Update recording size and name would need access to recording info
                // This could be enhanced by adding recording info methods to IPlayerService
                if (fragment.textViewRecordingSize != null) {
                    fragment.textViewRecordingSize.setText("Recording...");
                }

                if (fragment.textViewRecordingName != null) {
                    DataRadioStation station = playerService.getCurrentStation();
                    if (station != null) {
                        fragment.textViewRecordingName.setText(station.Name);
                    }
                }
            } else {
                fragment.groupRecordings.setVisibility(View.GONE);
                if (fragment.imgRecordingIcon.getAnimation() != null) {
                    fragment.imgRecordingIcon.clearAnimation();
                }
            }
        }
    }

    /**
     * Update station information display
     */
    public void updateStationInfo(DataRadioStation station) {
        if (station == null || fragment.textViewGeneralInfo == null) return;

        StreamLiveInfo liveInfo = MediaSessionUtil.getMetadataLive();
        String streamTitle = liveInfo != null ? liveInfo.getTitle() : null;

        if (!TextUtils.isEmpty(streamTitle)) {
            fragment.textViewGeneralInfo.setText(streamTitle);
        } else {
            fragment.textViewGeneralInfo.setText(station.Name);
        }

        // Station info is handled by FragmentPlayerFull.fullUpdate() method
    }

    /**
     * Update playback progress and timing information
     */
    public void updatePlaybackInfo() {
        if (fragment.textViewTimePlayed != null) {
            // Calculate playback time since start
            long deltaSeconds = 0;
            if (MediaSessionUtil.isPlaying()) {
                final long now = System.currentTimeMillis();
                final long startTime = MediaSessionUtil.getLastPlayStartTime();
                deltaSeconds = startTime > 0 ? ((now - startTime) / 1000) : 0;
                deltaSeconds = Math.max(deltaSeconds, 0);
            }

            fragment.textViewTimePlayed.setText(
                android.text.format.DateUtils.formatElapsedTime(deltaSeconds));
        }

        if (fragment.textViewNetworkUsageInfo != null) {
            long transferredBytes = MediaSessionUtil.getTransferredBytes();
            String networkInfo = Utils.getReadableBytes(transferredBytes);

            if (MediaSessionUtil.getShoutcastInfo() != null &&
                MediaSessionUtil.getShoutcastInfo().bitrate > 0) {
                networkInfo += " (" + MediaSessionUtil.getShoutcastInfo().bitrate + " kbps)";
            }

            fragment.textViewNetworkUsageInfo.setText(networkInfo);
        }

        if (fragment.textViewTimeCached != null) {
            long bufferedSeconds = MediaSessionUtil.getBufferedSeconds();
            fragment.textViewTimeCached.setText(
                android.text.format.DateUtils.formatElapsedTime(bufferedSeconds));
        }
    }

    /**
     * Update favorite button state
     */
    public void updateFavoriteButton() {
        if (fragment.btnFavourite == null) return;

        DataRadioStation station = playerService.getCurrentStation();
        if (station == null) return;

        // Get FavouriteManager from the application
        NoNameRadioApp app = (NoNameRadioApp) fragment.requireActivity().getApplication();
        FavouriteManager favouriteManager = app.getFavouriteManager();

        boolean isFavorite = favouriteManager.has(station.StationUuid);

        if (isFavorite) {
            fragment.btnFavourite.setImageResource(R.drawable.ic_heart_24dp);
            fragment.btnFavourite.setContentDescription(fragment.getString(R.string.detail_unstar));
        } else {
            fragment.btnFavourite.setImageResource(R.drawable.ic_heart_border_24dp);
            fragment.btnFavourite.setContentDescription(fragment.getString(R.string.detail_star));
        }
    }

    /**
     * Update all playback buttons at once
     */
    public void updateAllButtons() {
        updatePlayPauseButton();
        updateRecordingButton();
        updateFavoriteButton();
    }

    /**
     * Show error message to user
     */
    public void showError(String message) {
        UiUtils.showToast(fragment.getContext(), message);
    }

    /**
     * Show/hide loading indicator
     */
    public void showLoading(boolean show) {
        if (fragment.progressBarLoading != null) {
            fragment.progressBarLoading.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE);
        }
    }

    /**
     * Perform full UI update
     */
    public void performFullUpdate() {
        updateAllButtons();
        updateRecordingUI();
        updatePlaybackInfo();

        DataRadioStation station = playerService.getCurrentStation();
        updateStationInfo(station);
        updateFavoriteButton();
    }

    /**
     * Build station information string
     */
    private String buildStationInfo(DataRadioStation station) {
        StringBuilder info = new StringBuilder();

        // Add country
        if (station.Country != null && !station.Country.isEmpty()) {
            info.append(station.Country);
        }

        // Add bitrate (prefer Shoutcast info if available)
        ShoutcastInfo shoutcastInfo = MediaSessionUtil.getShoutcastInfo();
        int bitrate = 0;
        if (shoutcastInfo != null && shoutcastInfo.bitrate > 0) {
            bitrate = shoutcastInfo.bitrate;
        } else if (station.Bitrate > 0) {
            bitrate = station.Bitrate;
        }

        if (bitrate > 0) {
            if (info.length() > 0) info.append(" • ");
            info.append(bitrate).append(" kbps");
        }

        // Add sample rate and channels from Shoutcast
        if (shoutcastInfo != null) {
            if (shoutcastInfo.sampleRate > 0) {
                if (info.length() > 0) info.append(" • ");
                info.append(shoutcastInfo.sampleRate).append(" Hz");
            }

            if (shoutcastInfo.channels > 0) {
                if (shoutcastInfo.sampleRate > 0) {
                    info.append(" • ").append(shoutcastInfo.channels).append(" ch");
                } else if (info.length() > 0) {
                    info.append(" • ").append(shoutcastInfo.channels).append(" ch");
                } else {
                    info.append(shoutcastInfo.channels).append(" ch");
                }
            }

            // Add genre from Shoutcast
            if (shoutcastInfo.audioGenre != null && !shoutcastInfo.audioGenre.isEmpty()) {
                if (info.length() > 0) info.append(" • ");
                info.append(shoutcastInfo.audioGenre);
            }
        }

        // Add language
        if (station.Language != null && !station.Language.isEmpty()) {
            if (info.length() > 0) info.append(" • ");
            info.append(station.Language);
        }

        return info.toString();
    }
}