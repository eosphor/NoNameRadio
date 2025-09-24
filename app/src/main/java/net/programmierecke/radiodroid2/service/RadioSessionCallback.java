package net.programmierecke.radiodroid2.service;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;

import net.programmierecke.radiodroid2.RadioDroidApp;
import net.programmierecke.radiodroid2.players.RadioPlayer;
import net.programmierecke.radiodroid2.players.PlayState;
import net.programmierecke.radiodroid2.station.DataRadioStation;
import net.programmierecke.radiodroid2.recording.RecordingsManager;

/**
 * Callback for MediaSession events
 * Handles media control commands from system UI, notifications, and external devices
 */
public class RadioSessionCallback extends MediaSessionCompat.Callback {
    
    private static final String TAG = "RadioSessionCallback";
    
    private final Context context;
    private final RadioMediaSessionService service;
    private RadioPlayer radioPlayer;
    
    public RadioSessionCallback(RadioMediaSessionService service) {
        this.service = service;
        this.context = service.getApplicationContext();
        this.radioPlayer = ((RadioDroidApp) context).getRadioPlayer();
    }
    
    @Override
    public void onPlay() {
        Log.d(TAG, "onPlay called");
        if (radioPlayer != null) {
            // Resume playback if paused, or play current station if stopped
            if (radioPlayer.getPlayState() == PlayState.Paused) {
                radioPlayer.pause(); // This will resume if paused
            } else {
                // Try to play current station if available
                DataRadioStation currentStation = radioPlayer.getCurrentStation();
                if (currentStation != null) {
                    radioPlayer.play(currentStation, false);
                } else {
                    Log.w(TAG, "No current station to play");
                }
            }
        }
    }
    
    @Override
    public void onPause() {
        Log.d(TAG, "onPause called");
        if (radioPlayer != null) {
            radioPlayer.pause();
        }
    }
    
    @Override
    public void onStop() {
        Log.d(TAG, "onStop called");
        if (radioPlayer != null) {
            radioPlayer.stop();
        }
    }
    
    @Override
    public void onSkipToNext() {
        Log.d(TAG, "onSkipToNext called");
        // Could implement next station logic here
        // For now, just log the action
    }
    
    @Override
    public void onSkipToPrevious() {
        Log.d(TAG, "onSkipToPrevious called");
        // Could implement previous station logic here
        // For now, just log the action
    }
    
    @Override
    public void onSeekTo(long pos) {
        Log.d(TAG, "onSeekTo called: " + pos);
        // Radio streams typically don't support seeking
        // But we can implement this for recorded content
    }
    
    @Override
    public void onSetRepeatMode(int repeatMode) {
        Log.d(TAG, "onSetRepeatMode called: " + repeatMode);
        // Could implement repeat mode for playlist functionality
    }
    
    @Override
    public void onSetShuffleMode(int shuffleMode) {
        Log.d(TAG, "onSetShuffleMode called: " + shuffleMode);
        // Could implement shuffle mode for playlist functionality
    }
    
    @Override
    public void onCustomAction(@NonNull String action, Bundle extras) {
        Log.d(TAG, "onCustomAction called: " + action);
        
        // Handle custom actions specific to radio app
        switch (action) {
            case "FAVORITE":
                handleFavoriteAction(extras);
                break;
            case "RECORD":
                handleRecordAction(extras);
                break;
            default:
                Log.w(TAG, "Unknown custom action: " + action);
        }
    }
    
    private void handleFavoriteAction(Bundle extras) {
        if (extras != null && extras.containsKey("station_uuid")) {
            String stationUuid = extras.getString("station_uuid");
            // Add to favorites logic
            Log.d(TAG, "Adding station to favorites: " + stationUuid);
        }
    }
    
    private void handleRecordAction(Bundle extras) {
        if (radioPlayer != null) {
            RadioDroidApp app = (RadioDroidApp) service.getApplication();
            RecordingsManager recordingsManager = app.getRecordingsManager();
            if (radioPlayer.isRecording()) {
                recordingsManager.stopRecording(radioPlayer);
            } else {
                recordingsManager.record(service, radioPlayer);
            }
        }
    }
    
    /**
     * Update the session callback with current radio player
     */
    public void setRadioPlayer(RadioPlayer player) {
        this.radioPlayer = player;
    }
}