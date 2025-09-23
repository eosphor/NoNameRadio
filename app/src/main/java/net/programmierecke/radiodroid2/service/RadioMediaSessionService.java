package net.programmierecke.radiodroid2.service;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import net.programmierecke.radiodroid2.RadioDroidApp;
import net.programmierecke.radiodroid2.station.DataRadioStation;

import java.util.List;

/**
 * MediaSessionService for NoNameRadio using Media3 Session API
 * Provides system integration for media controls, notifications, and lock screen
 */
public class RadioMediaSessionService extends MediaBrowserServiceCompat {
    
    private static final String TAG = "RadioMediaSessionService";
    
    private MediaSessionCompat mediaSession;
    private RadioSessionCallback sessionCallback;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
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
        
        // Set session active
        mediaSession.setActive(true);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
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
}