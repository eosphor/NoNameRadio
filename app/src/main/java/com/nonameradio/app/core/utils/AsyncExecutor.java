package com.nonameradio.app.core.utils;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Modern asynchronous execution utilities to replace AsyncTask.
 * Provides thread pools for different types of operations.
 *
 * @author NoNameRadio Team
 * @version 1.0.0
 * @since 0.86.903
 */
public class AsyncExecutor {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int MAX_IO_THREADS = Math.max(2, CPU_COUNT);

    // IO-bound operations (network, disk)
    private static final ExecutorService IO_EXECUTOR =
        Executors.newFixedThreadPool(MAX_IO_THREADS, r -> {
            Thread t = new Thread(r, "NoNameRadio-IO");
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        });

    // CPU-bound operations (parsing, computation)
    private static final ExecutorService COMPUTATION_EXECUTOR =
        Executors.newFixedThreadPool(CPU_COUNT, r -> {
            Thread t = new Thread(r, "NoNameRadio-Computation");
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        });

    // Scheduled operations
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR =
        Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "NoNameRadio-Scheduled");
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        });

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    /**
     * Execute IO-bound task asynchronously
     */
    public static <T> CompletableFuture<T> submitIOTask(@NonNull Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new RuntimeException("IO task failed", e);
            }
        }, IO_EXECUTOR);
    }

    /**
     * Execute IO-bound task asynchronously with callback
     */
    public static <T> void executeIOTask(@NonNull Callable<T> task,
                                       @Nullable OnResultCallback<T> onSuccess,
                                       @Nullable OnErrorCallback onError) {
        submitIOTask(task)
            .thenAccept(result -> {
                if (onSuccess != null) {
                    MAIN_HANDLER.post(() -> onSuccess.onResult(result));
                }
            })
            .exceptionally(throwable -> {
                if (onError != null) {
                    MAIN_HANDLER.post(() -> onError.onError(throwable));
                }
                return null;
            });
    }

    /**
     * Execute computation-bound task asynchronously
     */
    public static <T> CompletableFuture<T> submitComputationTask(@NonNull Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new RuntimeException("Computation task failed", e);
            }
        }, COMPUTATION_EXECUTOR);
    }

    /**
     * Execute computation-bound task asynchronously with callback
     */
    public static <T> void executeComputationTask(@NonNull Callable<T> task,
                                                @Nullable OnResultCallback<T> onSuccess,
                                                @Nullable OnErrorCallback onError) {
        submitComputationTask(task)
            .thenAccept(result -> {
                if (onSuccess != null) {
                    MAIN_HANDLER.post(() -> onSuccess.onResult(result));
                }
            })
            .exceptionally(throwable -> {
                if (onError != null) {
                    MAIN_HANDLER.post(() -> onError.onError(throwable));
                }
                return null;
            });
    }

    /**
     * Schedule task to run after delay
     */
    public static void scheduleTask(@NonNull Runnable task, long delayMillis) {
        SCHEDULED_EXECUTOR.schedule(task, delayMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Schedule task to run periodically
     */
    public static void schedulePeriodicTask(@NonNull Runnable task,
                                          long initialDelay,
                                          long period) {
        SCHEDULED_EXECUTOR.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    /**
     * Run task on main thread
     */
    public static void runOnMainThread(@NonNull Runnable task) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            task.run();
        } else {
            MAIN_HANDLER.post(task);
        }
    }

    /**
     * Run task on main thread with delay
     */
    public static void runOnMainThreadDelayed(@NonNull Runnable task, long delayMillis) {
        MAIN_HANDLER.postDelayed(task, delayMillis);
    }

    /**
     * Shutdown all executors (call on app termination)
     */
    public static void shutdown() {
        IO_EXECUTOR.shutdown();
        COMPUTATION_EXECUTOR.shutdown();
        SCHEDULED_EXECUTOR.shutdown();

        try {
            if (!IO_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                IO_EXECUTOR.shutdownNow();
            }
            if (!COMPUTATION_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                COMPUTATION_EXECUTOR.shutdownNow();
            }
            if (!SCHEDULED_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                SCHEDULED_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            IO_EXECUTOR.shutdownNow();
            COMPUTATION_EXECUTOR.shutdownNow();
            SCHEDULED_EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Get executor statistics for monitoring
     */
    public static ExecutorStats getStats() {
        return new ExecutorStats(
            ((java.util.concurrent.ThreadPoolExecutor) IO_EXECUTOR).getActiveCount(),
            ((java.util.concurrent.ThreadPoolExecutor) COMPUTATION_EXECUTOR).getActiveCount(),
            ((java.util.concurrent.ThreadPoolExecutor) IO_EXECUTOR).getQueue().size(),
            ((java.util.concurrent.ThreadPoolExecutor) COMPUTATION_EXECUTOR).getQueue().size()
        );
    }

    /**
     * Callback interface for successful results
     */
    public interface OnResultCallback<T> {
        void onResult(T result);
    }

    /**
     * Callback interface for errors
     */
    public interface OnErrorCallback {
        void onError(Throwable throwable);
    }

    /**
     * Executor statistics
     */
    public static class ExecutorStats {
        public final int activeIOThreads;
        public final int activeComputationThreads;
        public final int queuedIOTasks;
        public final int queuedComputationTasks;

        public ExecutorStats(int activeIOThreads, int activeComputationThreads,
                           int queuedIOTasks, int queuedComputationTasks) {
            this.activeIOThreads = activeIOThreads;
            this.activeComputationThreads = activeComputationThreads;
            this.queuedIOTasks = queuedIOTasks;
            this.queuedComputationTasks = queuedComputationTasks;
        }

        @Override
        public String toString() {
            return "ExecutorStats{" +
                    "activeIOThreads=" + activeIOThreads +
                    ", activeComputationThreads=" + activeComputationThreads +
                    ", queuedIOTasks=" + queuedIOTasks +
                    ", queuedComputationTasks=" + queuedComputationTasks +
                    '}';
        }
    }
}
