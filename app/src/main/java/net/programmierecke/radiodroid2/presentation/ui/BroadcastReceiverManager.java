package net.programmierecke.radiodroid2.presentation.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.util.ArrayList;
import java.util.List;

public class BroadcastReceiverManager {
    private final Context context;
    private final List<BroadcastReceiver> receivers = new ArrayList<>();

    public BroadcastReceiverManager(Context context) {
        this.context = context;
    }

    public void registerReceiver(BroadcastReceiver receiver, String action) {
        IntentFilter filter = new IntentFilter(action);
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);
        receivers.add(receiver);
    }

    public void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);
        receivers.add(receiver);
    }

    public void unregisterAll() {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        for (BroadcastReceiver receiver : receivers) {
            try {
                lbm.unregisterReceiver(receiver);
            } catch (Exception e) {
                // Receiver might already be unregistered
            }
        }
        receivers.clear();
    }

    public void unregisterReceiver(BroadcastReceiver receiver) {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        try {
            lbm.unregisterReceiver(receiver);
            receivers.remove(receiver);
        } catch (Exception e) {
            // Receiver might already be unregistered
        }
    }
}

