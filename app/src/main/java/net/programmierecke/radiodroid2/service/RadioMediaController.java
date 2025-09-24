package net.programmierecke.radiodroid2.service;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.NonNull;

import net.programmierecke.radiodroid2.station.DataRadioStation;

/**
 * MediaController for NoNameRadio
 * Provides client-side interface to MediaSessionService
 * Handles connection to service and media control commands
 */
public class RadioMediaController {
    
    private static final String TAG = "RadioMediaController";
    
    private final Context context;
    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaController;
    private boolean isConnected = false;
    
    public RadioMediaController(Context context) {
        this.context = context;
    }
    
    /**
     * Connect to MediaSessionService
     */
    public void connect() {
        if (isConnected) {
            Log.w(TAG, "Already connected to MediaSessionService");
            return;
        }
        
        ComponentName serviceComponent = new ComponentName(context, RadioMediaSessionService.class);
        
        mediaBrowser = new MediaBrowserCompat(context, serviceComponent, connectionCallback, null);
        mediaBrowser.connect();
    }
    
    /**
     * Disconnect from MediaSessionService
     */
    public void disconnect() {
        if (mediaController != null) {
            mediaController.unregisterCallback(controllerCallback);
            mediaController = null;
        }
        
        if (mediaBrowser != null && isConnected) {
            mediaBrowser.disconnect();
            mediaBrowser = null;
        }
        
        isConnected = false;
    }
    
    /**
     * Play current station
     */
    public void play() {
        if (mediaController != null) {
            mediaController.getTransportControls().play();
        } else {
            Log.w(TAG, "MediaController not available");
        }
    }
    
    /**
     * Pause playback
     */
    public void pause() {
        if (mediaController != null) {
            mediaController.getTransportControls().pause();
        } else {
            Log.w(TAG, "MediaController not available");
        }
    }
    
    /**
     * Stop playback
     */
    public void stop() {
        if (mediaController != null) {
            mediaController.getTransportControls().stop();
        } else {
            Log.w(TAG, "MediaController not available");
        }
    }
    
    /**
     * Skip to next station
     */
    public void skipToNext() {
        if (mediaController != null) {
            mediaController.getTransportControls().skipToNext();
        } else {
            Log.w(TAG, "MediaController not available");
        }
    }
    
    /**
     * Skip to previous station
     */
    public void skipToPrevious() {
        if (mediaController != null) {
            mediaController.getTransportControls().skipToPrevious();
        } else {
            Log.w(TAG, "MediaController not available");
        }
    }
    
    /**
     * Send custom action to service
     */
    public void sendCustomAction(String action, android.os.Bundle extras) {
        if (mediaController != null) {
            mediaController.getTransportControls().sendCustomAction(action, extras);
        } else {
            Log.w(TAG, "MediaController not available");
        }
    }
    
    /**
     * Add station to favorites
     */
    public void addToFavorites(DataRadioStation station) {
        if (station != null) {
            android.os.Bundle extras = new android.os.Bundle();
            extras.putString("station_uuid", station.StationUuid);
            sendCustomAction("FAVORITE", extras);
        }
    }
    
    /**
     * Toggle recording
     */
    public void toggleRecording() {
        sendCustomAction("RECORD", null);
    }
    
    /**
     * Check if connected to service
     */
    public boolean isConnected() {
        return isConnected && mediaController != null;
    }
    
    /**
     * Get current playback state
     */
    public int getPlaybackState() {
        if (mediaController != null) {
            return mediaController.getPlaybackState().getState();
        }
        return android.support.v4.media.session.PlaybackStateCompat.STATE_NONE;
    }
    
    /**
     * Get current metadata
     */
    public android.support.v4.media.MediaMetadataCompat getMetadata() {
        if (mediaController != null) {
            return mediaController.getMetadata();
        }
        return null;
    }
    
    // Connection callback
    private final MediaBrowserCompat.ConnectionCallback connectionCallback = new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
            Log.d(TAG, "Connected to MediaSessionService");
            MediaSessionCompat.Token token = mediaBrowser.getSessionToken();
            mediaController = new MediaControllerCompat(context, token);
            mediaController.registerCallback(controllerCallback);
            isConnected = true;
        }
        
        @Override
        public void onConnectionFailed() {
            Log.e(TAG, "Failed to connect to MediaSessionService");
            isConnected = false;
        }
        
        @Override
        public void onConnectionSuspended() {
            Log.w(TAG, "Connection to MediaSessionService suspended");
            isConnected = false;
        }
    };
    
    // Controller callback
    private final MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull android.support.v4.media.session.PlaybackStateCompat state) {
            Log.d(TAG, "Playback state changed: " + state.getState());
        }
        
        @Override
        public void onMetadataChanged(android.support.v4.media.MediaMetadataCompat metadata) {
            Log.d(TAG, "Metadata changed");
        }
        
        @Override
        public void onSessionEvent(@NonNull String event, Bundle extras) {
            Log.d(TAG, "Session event: " + event);
        }
    };
}