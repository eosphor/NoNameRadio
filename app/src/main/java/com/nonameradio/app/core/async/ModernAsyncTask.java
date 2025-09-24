package com.nonameradio.app.core.async;

import android.util.Log;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Modern replacement for deprecated AsyncTask.
 * Uses CompletableFuture and ExecutorService for better concurrency control.
 *
 * Provides fluent API for async operations with proper error handling.
 */
public class ModernAsyncTask {
    private static final String TAG = "ModernAsyncTask";
    private static final int DEFAULT_POOL_SIZE = 4;

    private final ExecutorService executor;

    public ModernAsyncTask() {
        this(DEFAULT_POOL_SIZE);
    }

    public ModernAsyncTask(int poolSize) {
        this.executor = Executors.newFixedThreadPool(poolSize);
        Log.d(TAG, "Created thread pool with size: " + poolSize);
    }

    /**
     * Execute a task asynchronously and return CompletableFuture
     * @param task The task to execute
     * @param <T> The result type
     * @return CompletableFuture with result
     */
    public <T> CompletableFuture<T> execute(Task<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Log.d(TAG, "Executing task: " + task.getClass().getSimpleName());
                return task.execute();
            } catch (Exception e) {
                Log.e(TAG, "Task execution failed", e);
                throw new RuntimeException(e);
            }
        }, executor);
    }

    /**
     * Execute task with success and error callbacks
     * @param task The task to execute
     * @param onSuccess Success callback
     * @param onError Error callback
     * @param <T> The result type
     */
    public <T> void execute(Task<T> task, Consumer<T> onSuccess, Consumer<Exception> onError) {
        execute(task)
            .thenAccept(result -> {
                try {
                    onSuccess.accept(result);
                } catch (Exception e) {
                    Log.e(TAG, "Success callback failed", e);
                }
            })
            .exceptionally(throwable -> {
                Exception error = throwable instanceof Exception
                    ? (Exception) throwable
                    : new RuntimeException(throwable);
                try {
                    onError.accept(error);
                } catch (Exception e) {
                    Log.e(TAG, "Error callback failed", e);
                }
                return null;
            });
    }

    /**
     * Execute task with success callback only (errors logged)
     * @param task The task to execute
     * @param onSuccess Success callback
     * @param <T> The result type
     */
    public <T> void execute(Task<T> task, Consumer<T> onSuccess) {
        execute(task, onSuccess, error -> Log.e(TAG, "Task failed", error));
    }

    /**
     * Shutdown the executor (call when done)
     */
    public void shutdown() {
        executor.shutdown();
        Log.d(TAG, "Executor shutdown");
    }

    /**
     * Task interface for async operations
     */
    public interface Task<T> {
        T execute() throws Exception;
    }

    /**
     * Runnable task (no result)
     */
    public interface RunnableTask extends Task<Void> {
        void run() throws Exception;

        @Override
        default Void execute() throws Exception {
            run();
            return null;
        }
    }
}
