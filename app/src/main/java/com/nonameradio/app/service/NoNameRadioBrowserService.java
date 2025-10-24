package com.nonameradio.app.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import com.nonameradio.app.core.event.EventBus;
import com.nonameradio.app.core.event.PlayStationByIdEvent;

import com.nonameradio.app.IPlayerService;
import com.nonameradio.app.NoNameRadioApp;
import com.nonameradio.app.station.DataRadioStation;
import com.nonameradio.app.utils.GetRealLinkAndPlayTask;

import java.util.List;

public class NoNameRadioBrowserService extends MediaBrowserServiceCompat {
    private NoNameRadioBrowser noNameRadioBrowser;
    private ServiceConnection playerServiceConnection;
    private IPlayerService playerService;
    private GetRealLinkAndPlayTask playTask;

    // EventBus listener for playing station by ID (keep reference for unregister)
    private final EventBus.EventListener<PlayStationByIdEvent> playStationListener = event -> {
        if (event.stationId != null) {
            DataRadioStation station = noNameRadioBrowser.getStationById(event.stationId);

            if (station != null) {
                if (playTask != null) {
                    playTask.cancel(false);
                }

                playTask = new GetRealLinkAndPlayTask(this, station, playerService);
                playTask.executeAsync();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        noNameRadioBrowser = new NoNameRadioBrowser((NoNameRadioApp) getApplication());

        Intent anIntent = new Intent(this, PlayerService.class);
        anIntent.putExtra(PlayerService.PLAYER_SERVICE_NO_NOTIFICATION_EXTRA, true);
        startService(anIntent);

        playerServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                playerService = IPlayerService.Stub.asInterface(iBinder);
                try {
                    NoNameRadioBrowserService.this.setSessionToken(playerService.getMediaSessionToken());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                playerService = null;
            }
        };

        bindService(anIntent, playerServiceConnection, Context.BIND_AUTO_CREATE);

        // Register EventBus listener for playing station by ID (Android Auto)
        EventBus.register(PlayStationByIdEvent.class, playStationListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister EventBus listener (fixes memory leak from original code)
        EventBus.unregister(PlayStationByIdEvent.class, playStationListener);
        
        unbindService(playerServiceConnection);
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return noNameRadioBrowser.onGetRoot(clientPackageName, clientUid, rootHints);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        noNameRadioBrowser.onLoadChildren(parentId, result);
    }
}
