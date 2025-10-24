package com.nonameradio.app.service;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.NonNull;

import com.nonameradio.app.station.DataRadioStation;

/**
 * MediaController for NoNameRadio
 * Provides client-side interface to MediaSessionService
 * Handles connection to service and media control commands
 */
public class RadioMediaController {
    
    /**
     * Interface for listening to playback state changes
     */
    public interface PlaybackStateListener {
        void onPlaybackStateChanged(int state);
        void onMetadataChanged(android.support.v4.media.MediaMetadataCompat metadata);
    }
    
    private static final String TAG = "RadioMediaController";
    
    private final Context context;
    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaController;
    private boolean isConnected = false;
    private PlaybackStateListener playbackStateListener;
    
    public RadioMediaController(Context context) {
        this.context = context;
    }
    
    /**
     * Connect to PlayerService MediaSession
     */
    public void connect() {
        if (isConnected) {
            Log.w(TAG, "Already connected to PlayerService");
            return;
        }
        
        // Connect to PlayerService directly to get its MediaSession
        // We use PlayerServiceUtil to bind to the service
        android.content.ServiceConnection serviceConnection = new android.content.ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, android.os.IBinder service) {
                try {
                    com.nonameradio.app.IPlayerService playerService = 
                        com.nonameradio.app.IPlayerService.Stub.asInterface(service);
                    
                    MediaSessionCompat.Token token = playerService.getMediaSessionToken();
                    if (token != null) {
                        mediaController = new MediaControllerCompat(context, token);
                        mediaController.registerCallback(controllerCallback);
                        isConnected = true;
                        Log.d(TAG, "Connected to PlayerService MediaSession");
                    } else {
                        Log.w(TAG, "PlayerService MediaSession token is null");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error connecting to PlayerService", e);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isConnected = false;
                mediaController = null;
                Log.d(TAG, "Disconnected from PlayerService");
            }
        };

        // Bind to PlayerService
        android.content.Intent intent = new android.content.Intent(context, PlayerService.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
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
     * Set playback state listener
     */
    public void setPlaybackStateListener(PlaybackStateListener listener) {
        this.playbackStateListener = listener;
    }
    
    /**
     * Remove playback state listener
     */
    public void removePlaybackStateListener() {
        this.playbackStateListener = null;
    }
    
    /**
     * Get current playback state
     */
    public int getPlaybackState() {
        if (mediaController != null) {
            android.support.v4.media.session.PlaybackStateCompat state = mediaController.getPlaybackState();
            if (state != null) {
                return state.getState();
            }
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
            if (playbackStateListener != null) {
                playbackStateListener.onPlaybackStateChanged(state.getState());
            }
        }
        
        @Override
        public void onMetadataChanged(android.support.v4.media.MediaMetadataCompat metadata) {
            Log.d(TAG, "Metadata changed");
            if (playbackStateListener != null) {
                playbackStateListener.onMetadataChanged(metadata);
            }
        }
        
        @Override
        public void onSessionEvent(@NonNull String event, Bundle extras) {
            Log.d(TAG, "Session event: " + event);
        }
    };
}