package com.nonameradio.app.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.RemoteException;

import com.nonameradio.app.IPlayerService;
import com.nonameradio.app.NoNameRadioApp;
import com.nonameradio.app.Utils;
import com.nonameradio.app.station.DataRadioStation;

import java.lang.ref.WeakReference;

import okhttp3.OkHttpClient;

public class GetRealLinkAndPlayTask extends AsyncTask<Void, Void, String> {
    private final WeakReference<Context> contextRef;
    private final DataRadioStation station;
    private final WeakReference<IPlayerService> playerServiceRef;

    private final OkHttpClient httpClient;

    public GetRealLinkAndPlayTask(Context context, DataRadioStation station, IPlayerService playerService) {
        this.contextRef = new WeakReference<>(context);
        this.station = station;
        this.playerServiceRef = new WeakReference<>(playerService);

        NoNameRadioApp app = (NoNameRadioApp) context.getApplicationContext();
        httpClient = app.getHttpClient();
    }

    @Override
    protected String doInBackground(Void... params) {
        Context context = contextRef.get();
        if (context != null) {
            return Utils.getRealStationLink(httpClient, context.getApplicationContext(), station.StationUuid);
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        IPlayerService playerService = playerServiceRef.get();
        if (result != null && playerService != null && !isCancelled()) {
            try {
                station.playableUrl = result;
                playerService.SetStation(station);
                playerService.Play(false);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        super.onPostExecute(result);
    }
}
