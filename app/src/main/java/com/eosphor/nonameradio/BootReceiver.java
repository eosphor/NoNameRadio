package com.eosphor.nonameradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.eosphor.nonameradio.alarm.RadioAlarmManager;

public class BootReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        RadioDroidApp radioDroidApp = (RadioDroidApp)context.getApplicationContext();
        radioDroidApp.getAlarmManager().resetAllAlarms();
    }
}
