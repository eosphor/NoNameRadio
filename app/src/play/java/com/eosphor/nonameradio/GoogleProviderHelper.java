package com.eosphor.nonameradio;

import android.content.Context;
import com.google.android.gms.security.ProviderInstaller;

public class GoogleProviderHelper{
    public static void use(Context context) {
        try {
            ProviderInstaller.installIfNeeded(context);
        } catch (Exception e) {
            // Игнорируем ошибки установки провайдера
        }
    }
}