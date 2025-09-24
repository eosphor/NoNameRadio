package com.nonameradio.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nonameradio.app.alarm.RadioAlarmManager;

public class BootReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        NoNameRadioApp app = (NoNameRadioApp)context.getApplicationContext();
        app.getAlarmManager().resetAllAlarms();
    }
}
