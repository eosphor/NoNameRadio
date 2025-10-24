package com.nonameradio.app.core.domain;

import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class ErrorHandlerTest {

    private Context context;
    private ErrorHandler errorHandler;

    @Before
    public void setup() {
        context = RuntimeEnvironment.getApplication();
        errorHandler = new ErrorHandler(context);
    }

    @Test
    public void testHandleNetworkError() {
        Exception exception = new RuntimeException("Network error");
        // Note: In real implementation, this would show a toast
        // For testing, we just verify no exceptions are thrown
        errorHandler.handleNetworkError(exception);
    }

    @Test
    public void testHandlePlaybackError() {
        Exception exception = new RuntimeException("Playback error");
        errorHandler.handlePlaybackError(exception);
    }

    @Test
    public void testHandleRecordingError() {
        Exception exception = new RuntimeException("Recording error");
        errorHandler.handleRecordingError(exception);
    }

    @Test
    public void testHandleGenericError() {
        Exception exception = new RuntimeException("Generic error");
        errorHandler.handleGenericError(exception);
    }

    @Test
    public void testHandlePermissionDenied() {
        errorHandler.handlePermissionDenied("android.permission.RECORD_AUDIO");
    }

    @Test
    public void testHandleApiError() {
        errorHandler.handleApiError(404, "Not found");
    }
}

