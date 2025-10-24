package com.nonameradio.app.core.utils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.os.Handler;
import android.os.Looper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unit tests for AsyncExecutor class.
 * Tests both IO and computation task execution, error handling, and main thread operations.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 28)
public class AsyncExecutorTest {

    @Mock
    private AsyncExecutor.OnResultCallback<String> mockOnSuccessString;

    @Mock
    private AsyncExecutor.OnResultCallback<Integer> mockOnSuccessInteger;

    @Mock
    private AsyncExecutor.OnErrorCallback mockOnError;

    private AutoCloseable mocks;

    @Before
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @After
    public void tearDown() throws Exception {
        mocks.close();
        // Don't shutdown AsyncExecutor in tests as it's shared between tests
        // AsyncExecutor.shutdown() will be called once after all tests
    }

    @Test
    public void testSubmitIOTask_returnsCorrectResult() throws Exception {
        // Given
        String expectedResult = "IO Task Result";

        // When
        CompletableFuture<String> future = AsyncExecutor.submitIOTask(() -> {
            // Simulate IO operation
            Thread.sleep(10);
            return expectedResult;
        });

        String actualResult = future.get(1, TimeUnit.SECONDS);

        // Then
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testSubmitIOTask_handlesException() throws Exception {
        // Given
        RuntimeException expectedException = new RuntimeException("IO Task Failed");

        // When
        CompletableFuture<String> future = AsyncExecutor.submitIOTask(() -> {
            throw expectedException;
        });

        // Then
        try {
            future.get(1, TimeUnit.SECONDS);
            fail("Expected exception was not thrown");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof RuntimeException);
            assertEquals("IO task failed", e.getCause().getMessage());
        }
    }

    @Test
    public void testExecuteIOTask_successCallback() throws Exception {
        // Given
        String expectedResult = "Success Result";
        AtomicReference<String> actualResult = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        // When
        AsyncExecutor.submitIOTask(
            () -> {
                Thread.sleep(10);
                return expectedResult;
            }
        ).thenAccept(result -> {
            actualResult.set(result);
            latch.countDown();
        });

        // Then
        assertTrue("Task should complete within timeout", latch.await(2, TimeUnit.SECONDS));
        assertEquals(expectedResult, actualResult.get());
    }

    @Test
    public void testExecuteIOTask_errorCallback() throws Exception {
        // Given
        RuntimeException expectedException = new RuntimeException("Task failed");
        AtomicReference<Throwable> actualException = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        // When
        AsyncExecutor.submitIOTask(
            () -> {
                throw expectedException;
            }
        ).exceptionally(throwable -> {
            actualException.set(throwable);
            latch.countDown();
            return null;
        });

        // Then
        assertTrue("Task should complete within timeout", latch.await(2, TimeUnit.SECONDS));
        assertNotNull(actualException.get());
        assertTrue(actualException.get().getCause() instanceof RuntimeException);
    }

    @Test
    public void testSubmitComputationTask_returnsCorrectResult() throws Exception {
        // Given
        Integer expectedResult = 55; // sum of 1+2+...+10

        // When
        CompletableFuture<Integer> future = AsyncExecutor.submitComputationTask(() -> {
            // Simulate CPU-intensive operation
            int result = 0;
            for (int i = 1; i <= 10; i++) {
                result += i;
                Thread.sleep(1); // Small delay to simulate work
            }
            return result;
        });

        Integer actualResult = future.get(2, TimeUnit.SECONDS);

        // Then
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testExecuteComputationTask_successCallback() throws Exception {
        // Given
        Integer expectedResult = 55; // sum of 1+2+...+10
        AtomicReference<Integer> actualResult = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        // When
        AsyncExecutor.submitComputationTask(
            () -> {
                int sum = 0;
                for (int i = 1; i <= 10; i++) {
                    sum += i;
                }
                return sum;
            }
        ).thenAccept(result -> {
            actualResult.set(result);
            latch.countDown();
        });

        // Then
        assertTrue("Computation task should complete within timeout", latch.await(2, TimeUnit.SECONDS));
        assertEquals(expectedResult, actualResult.get());
    }

    @Test
    public void testRunOnMainThread_executesOnMainThread() throws Exception {
        // Given
        AtomicBoolean executedOnMainThread = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        // When
        AsyncExecutor.runOnMainThread(() -> {
            executedOnMainThread.set(Looper.myLooper() == Looper.getMainLooper());
            latch.countDown();
        });

        // Force main thread execution
        ShadowLooper.runMainLooperToNextTask();

        // Then
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertTrue(executedOnMainThread.get());
    }

    @Test
    public void testRunOnMainThreadDelayed_executesAfterDelay() throws Exception {
        // Given
        AtomicBoolean executed = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        // When
        AsyncExecutor.runOnMainThreadDelayed(() -> {
            executed.set(true);
            latch.countDown();
        }, 100);

        // Wait for delay and execute
        Thread.sleep(150);
        ShadowLooper.runMainLooperToNextTask();

        // Then
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertTrue("Task should be executed on main thread", executed.get());
    }

    @Test
    public void testScheduleTask_executesAfterDelay() throws Exception {
        // Given
        AtomicBoolean executed = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        // When
        AsyncExecutor.scheduleTask(() -> {
            executed.set(true);
            latch.countDown();
        }, 50);

        // Wait and check
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertTrue(executed.get());
    }

    @Test
    public void testSchedulePeriodicTask_executesMultipleTimes() throws Exception {
        // Given
        AtomicInteger executionCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(3);

        // When
        AsyncExecutor.schedulePeriodicTask(() -> {
            executionCount.incrementAndGet();
            latch.countDown();
        }, 0, 50); // Start immediately, then every 50ms

        // Wait for 3 executions
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(3, executionCount.get());
    }

    @Test
    public void testGetStats_returnsValidStats() {
        // Given - execute some tasks first
        AsyncExecutor.submitIOTask(() -> "test");
        AsyncExecutor.submitComputationTask(() -> 42);

        // When
        AsyncExecutor.ExecutorStats stats = AsyncExecutor.getStats();

        // Then
        assertNotNull(stats);
        assertTrue(stats.activeIOThreads >= 0);
        assertTrue(stats.activeComputationThreads >= 0);
        assertTrue(stats.queuedIOTasks >= 0);
        assertTrue(stats.queuedComputationTasks >= 0);
    }

    // Test for shutdown is removed since AsyncExecutor is shared between tests
    // and shutdown affects other tests. Shutdown testing should be done
    // in integration tests or with proper test isolation.

    @Test
    public void testMultipleConcurrentTasks() throws Exception {
        // Given
        int taskCount = 10;
        CountDownLatch latch = new CountDownLatch(taskCount);
        AtomicInteger completedCount = new AtomicInteger(0);

        // When - execute multiple tasks concurrently
        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            AsyncExecutor.submitIOTask(() -> {
                Thread.sleep(5); // Small delay
                return "Task " + taskId;
            }).thenAccept(result -> {
                completedCount.incrementAndGet();
                latch.countDown();
            });
        }

        // Then
        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertEquals(taskCount, completedCount.get());
    }

    @Test
    public void testExceptionPropagation() throws Exception {
        // Given
        AtomicReference<Throwable> capturedException = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        // When
        AsyncExecutor.submitIOTask(
            () -> {
                throw new IllegalArgumentException("Test exception");
            }
        ).exceptionally(throwable -> {
            capturedException.set(throwable);
            latch.countDown();
            return null;
        });

        // Then
        assertTrue("Exception should be propagated within timeout", latch.await(2, TimeUnit.SECONDS));
        assertNotNull("Exception should be captured", capturedException.get());
        assertTrue("Should be RuntimeException", capturedException.get() instanceof RuntimeException);
    }
}
