package com.nonameradio.app.config;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.nonameradio.app.NoNameRadioApp;

import java.io.InputStream;

import okhttp3.OkHttpClient;

/**
 * Glide configuration module для оптимизации загрузки изображений станций.
 * 
 * Оптимизации:
 * - Настройка кастомного OkHttpClient с таймаутами
 * - Оптимизация использования памяти (RGB_565 формат)
 * - Настройка кэша и пулов потоков
 */
@GlideModule
public class NoNameRadioGlideModule extends AppGlideModule {
    private static final String TAG = "GlideModule";

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        // Использовать RGB_565 вместо ARGB_8888 для экономии памяти (50%)
        builder.setDefaultRequestOptions(
            new RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565)
                .timeout(5000) // 5 секунд таймаут для всех запросов
        );

        // Установить уровень логирования
        builder.setLogLevel(Log.ERROR);
        
        Log.d(TAG, "Glide module configured with optimizations");
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        // Получить оптимизированный HTTP клиент из приложения
        NoNameRadioApp app = (NoNameRadioApp) context.getApplicationContext();
        OkHttpClient okHttpClient = app.getHttpClientForGlide();
        
        // Зарегистрировать OkHttp integration для Glide
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(okHttpClient));
        
        Log.d(TAG, "Registered OkHttp integration for Glide");
    }

    @Override
    public boolean isManifestParsingEnabled() {
        // Отключаем парсинг манифеста для ускорения инициализации
        return false;
    }
}

