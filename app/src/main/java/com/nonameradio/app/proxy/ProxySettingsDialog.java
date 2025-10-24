package com.nonameradio.app.proxy;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.nonameradio.app.R;
import com.nonameradio.app.NoNameRadioApp;
import com.nonameradio.app.Utils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.nonameradio.app.Utils.parseIntWithDefault;

public class ProxySettingsDialog extends DialogFragment {

    final static private String TEST_ADDRESS = "http://radio-browser.info";

    private EditText editProxyHost;
    private EditText editProxyPort;
    private AppCompatSpinner spinnerProxyType;
    private EditText editLogin;
    private EditText editProxyPassword;
    private TextView textProxyTestResult;

    private ArrayAdapter<Proxy.Type> proxyTypeAdapter;

    private CompletableFuture<TestResult> proxyTestTask;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View layout = inflater.inflate(R.layout.dialog_proxy_settings, null);

        editProxyHost = layout.findViewById(R.id.edit_proxy_host);
        editProxyPort = layout.findViewById(R.id.edit_proxy_port);
        spinnerProxyType = layout.findViewById(R.id.spinner_proxy_type);
        editLogin = layout.findViewById(R.id.edit_proxy_login);
        editProxyPassword = layout.findViewById(R.id.edit_proxy_password);
        textProxyTestResult = layout.findViewById(R.id.text_test_proxy_result);

        proxyTypeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                new Proxy.Type[]{Proxy.Type.DIRECT, Proxy.Type.HTTP, Proxy.Type.SOCKS});

        spinnerProxyType.setAdapter(proxyTypeAdapter);

        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            ProxySettings proxySettings = ProxySettings.fromPreferences(sharedPref);

            if (proxySettings != null) {
                editProxyHost.setText(proxySettings.host);
                editProxyPort.setText(Integer.toString(proxySettings.port));
                editLogin.setText(proxySettings.login);
                editProxyPassword.setText(proxySettings.password);
                spinnerProxyType.setSelection(proxyTypeAdapter.getPosition(proxySettings.type));
            }
        }

        final Dialog dialog = builder.setView(layout)
                .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                        SharedPreferences.Editor editor = sharedPref.edit();

                        ProxySettings proxySettings = createProxySettings();
                        proxySettings.toPreferences(editor);
                        editor.apply();

                        NoNameRadioApp app = (NoNameRadioApp) getActivity().getApplication();
                        app.rebuildHttpClient();
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ProxySettingsDialog.this.getDialog().cancel();
                    }
                })
                .setNeutralButton(R.string.settings_proxy_action_test, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ProxySettings proxySettings = createProxySettings();
                        testProxy(proxySettings);
                    }
                });
            }
        });

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (proxyTestTask != null) {
            proxyTestTask.cancel(true);
        }
    }

    private static class TestResult {
        final boolean success;
        final String message;
        final boolean invalidInput;

        TestResult(boolean success, String message, boolean invalidInput) {
            this.success = success;
            this.message = message;
            this.invalidInput = invalidInput;
        }
    }

    private ProxySettings createProxySettings() {
        ProxySettings settings = new ProxySettings();

        settings.host = editProxyHost.getText().toString();
        settings.port = parseIntWithDefault(editProxyPort.getText().toString(), 0);
        settings.login = editLogin.getText().toString();
        settings.password = editProxyPassword.getText().toString();
        settings.type = proxyTypeAdapter.getItem(spinnerProxyType.getSelectedItemPosition());

        return settings;
    }

    private static class ConnectionTester {
        static CompletableFuture<TestResult> testConnection(@NonNull NoNameRadioApp app, 
                                                             @NonNull ProxySettings proxySettings) {
            final String connectionSuccessStr = app.getString(R.string.settings_proxy_working, TEST_ADDRESS);
            final String connectionFailedStr = app.getString(R.string.settings_proxy_not_working);
            final String connectionInvalidInputStr = app.getString(R.string.settings_proxy_invalid);

            return com.nonameradio.app.core.utils.AsyncExecutor.submitIOTask(() -> {
                // Build HTTP client with proxy
                OkHttpClient.Builder builder = app.newHttpClientWithoutProxy()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS);

                if (!Utils.setOkHttpProxy(builder, proxySettings)) {
                    // Invalid proxy settings
                    return new TestResult(false, connectionInvalidInputStr, true);
                }

                OkHttpClient okHttpClient = builder.build();

                // Execute test request
                Request.Builder requestBuilder = new Request.Builder().url(TEST_ADDRESS);
                Call call = okHttpClient.newCall(requestBuilder.build());

                try {
                    Response response = call.execute();
                    if (response.isSuccessful()) {
                        return new TestResult(true, connectionSuccessStr, false);
                    } else {
                        String errorMsg = String.format(connectionFailedStr, TEST_ADDRESS, response.message());
                        return new TestResult(false, errorMsg, false);
                    }
                } catch (IOException e) {
                    String errorMsg = String.format(connectionFailedStr, TEST_ADDRESS, e.getMessage());
                    return new TestResult(false, errorMsg, false);
                }
            });
        }
    }

    private void testProxy(@NonNull ProxySettings proxySettings) {
        if (proxyTestTask != null) {
            proxyTestTask.cancel(true);
        }

        // Clear previous result
        textProxyTestResult.setText("");

        NoNameRadioApp app = (NoNameRadioApp) getActivity().getApplication();
        
        proxyTestTask = ConnectionTester.testConnection(app, proxySettings);
        
        proxyTestTask.thenAccept(result -> {
            // Update UI on main thread
            com.nonameradio.app.core.utils.UiHandler.post(() -> {
                if (textProxyTestResult != null) {
                    textProxyTestResult.setText(result.message);
                }
            });
        }).exceptionally(throwable -> {
            // Handle error on main thread
            com.nonameradio.app.core.utils.UiHandler.post(() -> {
                if (textProxyTestResult != null) {
                    String errorMsg = String.format(app.getString(R.string.settings_proxy_not_working), 
                                                    TEST_ADDRESS, 
                                                    throwable.getMessage());
                    textProxyTestResult.setText(errorMsg);
                }
            });
            return null;
        });
    }
}
