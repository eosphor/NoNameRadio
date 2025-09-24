package com.nonameradio.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;

import com.nonameradio.app.service.MediaSessionUtil;
import androidx.preference.PreferenceManager;

public class BecomingNoisyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction()) && MediaSessionUtil.isPlaying()) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            if (sharedPref.getBoolean("pause_when_noisy", true)) {
                MediaSessionUtil.pause(PauseReason.BECAME_NOISY);
            }
        }
    }
}
