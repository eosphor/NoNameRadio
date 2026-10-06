package com.nonameradio.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nonameradio.app.alarm.RadioAlarmManager;

public class BootReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        // The receiver is exported for the system broadcast, so ignore anything else
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }
        NoNameRadioApp app = (NoNameRadioApp)context.getApplicationContext();
        app.getAlarmManager().resetAllAlarms();
    }
}
