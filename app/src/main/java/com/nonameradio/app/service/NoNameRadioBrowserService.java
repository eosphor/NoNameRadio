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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media.MediaBrowserServiceCompat;

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


    private final BroadcastReceiver playStationFromIdReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MediaSessionCallback.BROADCAST_PLAY_STATION_BY_ID.equals(action)) {
                String stationId = intent.getStringExtra(MediaSessionCallback.EXTRA_STATION_ID);

                DataRadioStation station = noNameRadioBrowser.getStationById(stationId);

                if (station != null) {
                    if (playTask != null) {
                        playTask.cancel(false);
                    }

                    playTask = new GetRealLinkAndPlayTask(context, station, playerService);
                    playTask.execute();
                }
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

        IntentFilter filter = new IntentFilter();
        filter.addAction(MediaSessionCallback.BROADCAST_PLAY_STATION_BY_ID);

        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.registerReceiver(playStationFromIdReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

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
