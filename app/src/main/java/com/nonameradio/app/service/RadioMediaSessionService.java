package com.nonameradio.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import com.nonameradio.app.NoNameRadioApp;
import com.nonameradio.app.station.DataRadioStation;

import java.util.List;

/**
 * MediaSessionService for NoNameRadio using Media3 Session API
 * Provides system integration for media controls, notifications, and lock screen
 */
public class RadioMediaSessionService extends MediaBrowserServiceCompat {
    
    private static final String TAG = "RadioMediaSessionService";
    
    private MediaSessionCompat mediaSession;
    private RadioSessionCallback sessionCallback;
    private MediaSessionManager sessionManager;
    private BroadcastReceiver stateUpdateReceiver;
    
    // Static reference to get MediaSession token
    private static RadioMediaSessionService instance;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Set static instance
        instance = this;
        
        // Initialize MediaSession
        mediaSession = new MediaSessionCompat(this, TAG);
        setSessionToken(mediaSession.getSessionToken());
        
        // Set up session callback
        sessionCallback = new RadioSessionCallback(this);
        mediaSession.setCallback(sessionCallback);
        
        // Enable media session features
        mediaSession.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        );
        
        // Initialize MediaSessionManager
        NoNameRadioApp app = (NoNameRadioApp) getApplication();
        sessionManager = new MediaSessionManager(this, app.getRadioPlayer());
        sessionManager.setMediaSessionService(this);
        
        // Set session active
        mediaSession.setActive(true);
        
        // Register BroadcastReceiver for state updates from PlayerService
        stateUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.nonameradio.app.UPDATE_MEDIASESSION_STATE".equals(intent.getAction())) {
                    int state = intent.getIntExtra("state", 0);
                    long position = intent.getLongExtra("position", -1);
                    float speed = intent.getFloatExtra("speed", 0.0f);
                    
                    Log.d(TAG, "Received state update from PlayerService: " + state);
                    updatePlaybackState(state, position, speed);
                }
            }
        };
        
        IntentFilter filter = new IntentFilter("com.nonameradio.app.UPDATE_MEDIASESSION_STATE");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stateUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(stateUpdateReceiver, filter);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // Clear static instance
        instance = null;
        
        // Unregister BroadcastReceiver
        if (stateUpdateReceiver != null) {
            unregisterReceiver(stateUpdateReceiver);
        }
        
        if (sessionManager != null) {
            sessionManager.destroy();
        }
        
        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
        }
    }
    
    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        // Allow all clients to connect
        return new BrowserRoot("root", null);
    }
    
    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        // For now, return empty list - can be extended for media library browsing
        result.sendResult(null);
    }
    
    /**
     * Get the MediaSessionManager
     */
    public MediaSessionManager getSessionManager() {
        return sessionManager;
    }
    
    /**
     * Get the current media session
     */
    public MediaSessionCompat getMediaSession() {
        return mediaSession;
    }
    
    /**
     * Update session metadata with current station
     */
    public void updateSessionMetadata(DataRadioStation station) {
        if (mediaSession != null && station != null) {
            mediaSession.setMetadata(station.toMediaMetadataCompat());
        }
    }
    
    /**
     * Update playback state
     */
    public void updatePlaybackState(int state, long position, float speed) {
        if (mediaSession != null) {
            mediaSession.setPlaybackState(
                new android.support.v4.media.session.PlaybackStateCompat.Builder()
                    .setActions(
                        android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY |
                        android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE |
                        android.support.v4.media.session.PlaybackStateCompat.ACTION_STOP |
                        android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                        android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    )
                    .setState(state, position, speed)
                    .build()
            );
        }
    }
    
    /**
     * Get MediaSession token for notifications
     */
    public static MediaSessionCompat.Token getMediaSessionToken() {
        if (instance != null && instance.mediaSession != null) {
            return instance.mediaSession.getSessionToken();
        }
        return null;
    }
}