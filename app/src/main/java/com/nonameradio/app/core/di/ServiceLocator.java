package com.nonameradio.app.core.di;

import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple Service Locator pattern for Dependency Injection.
 * Thread-safe and lazy initialization of services.
 *
 * This replaces manual dependency management and provides centralized
 * service registration and resolution.
 */
public class ServiceLocator {
    private static final String TAG = "ServiceLocator";
    private static volatile ServiceLocator instance;

    private final Map<Class<?>, Provider<?>> providers = new ConcurrentHashMap<>();

    private ServiceLocator() {
        registerServices();
    }

    /**
     * Get singleton instance
     */
    public static ServiceLocator getInstance() {
        if (instance == null) {
            synchronized (ServiceLocator.class) {
                if (instance == null) {
                    instance = new ServiceLocator();
                }
            }
        }
        return instance;
    }

    /**
     * Get service by class type
     * @param serviceClass The service interface/class
     * @param <T> The service type
     * @return The service instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> serviceClass) {
        return getInstance().getService(serviceClass);
    }

    /**
     * Register a service provider
     * @param serviceClass The service interface/class
     * @param provider The provider function
     * @param <T> The service type
     */
    public static <T> void register(Class<T> serviceClass, Provider<T> provider) {
        getInstance().providers.put(serviceClass, provider);
        Log.d(TAG, "Registered service: " + serviceClass.getSimpleName());
    }

    /**
     * Internal service resolution
     */
    @SuppressWarnings("unchecked")
    private <T> T getService(Class<T> serviceClass) {
        Provider<T> provider = (Provider<T>) providers.get(serviceClass);
        if (provider == null) {
            throw new IllegalArgumentException("No provider registered for: " + serviceClass.getSimpleName());
        }

        try {
            T service = provider.get();
            Log.d(TAG, "Resolved service: " + serviceClass.getSimpleName());
            return service;
        } catch (Exception e) {
            Log.e(TAG, "Failed to create service: " + serviceClass.getSimpleName(), e);
            throw new RuntimeException("Failed to create service: " + serviceClass.getSimpleName(), e);
        }
    }

    /**
     * Register all application services
     */
    private void registerServices() {
        Log.d(TAG, "Registering application services...");

        // Register existing services from NoNameRadioApp
        // This will be expanded as we refactor more components

        // For now, register placeholder services
        // These will be replaced with actual implementations during refactoring

        Log.d(TAG, "Service registration completed");
    }

    /**
     * Service provider interface
     */
    public interface Provider<T> {
        T get();
    }
}
