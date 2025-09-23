package net.programmierecke.radiodroid2.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.core.net.ConnectivityManagerCompat;

public class ConnectivityChecker {

    public enum ConnectionType {
        NOT_METERED,
        METERED
    }

    public interface ConnectivityCallback {
        void onConnectivityChanged(boolean connected, ConnectionType connectionType);
    }

    private ConnectivityManager connectivityManager;

    private ConnectivityManager.NetworkCallback networkCallback;

    private ConnectivityCallback connectivityCallback;

    private ConnectionType lastConnectionType;

    public static ConnectionType getCurrentConnectionType(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return ConnectivityManagerCompat.isActiveNetworkMetered(connectivityManager) ? ConnectionType.METERED : ConnectionType.NOT_METERED;
    }

    public void startListening(Context context, ConnectivityCallback connectivityCallback) {
        this.connectivityCallback = connectivityCallback;

        if (networkCallback != null) {
            return;
        }

        lastConnectionType = getCurrentConnectionType(context);

        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                boolean connected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                boolean metered = !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
                onConnectivityChanged(connected, metered ? ConnectionType.METERED : ConnectionType.NOT_METERED);
            }

            @Override
            public void onLost(Network network) {
                onConnectivityChanged(false, ConnectionType.METERED);
            }
        };
        connectivityManager.registerNetworkCallback(new NetworkRequest.Builder().build(), networkCallback);
    }

    public void stopListening(Context context) {
        this.connectivityCallback = null;

        if (networkCallback != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.unregisterNetworkCallback(networkCallback);
            networkCallback = null;
        }
    }

    private void onConnectivityChanged(boolean connected, ConnectionType connectionType) {
        if (lastConnectionType == connectionType) {
            return;
        } else {
            lastConnectionType = connectionType;
        }

        if (connectivityCallback != null) {
            connectivityCallback.onConnectivityChanged(connected, connectionType);
        }
    }
}
