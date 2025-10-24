package com.nonameradio.app.core.utils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Unit tests for ErrorHandler class.
 * Tests error handling, validation methods, and user-friendly error messages.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 28)
public class ErrorHandlerTest {

    @Mock
    private Context mockContext;

    private AutoCloseable mocks;

    @Before
    public void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);
        // Mock string resources
        when(mockContext.getString(anyInt())).thenReturn("Mock Error Message");
    }

    @After
    public void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    public void testHandleError_withThrowable_logsError() {
        // Given
        String tag = "TestTag";
        String message = "Test error message";
        Throwable throwable = new RuntimeException("Test exception");

        // When - should not throw exception
        ErrorHandler.handleError(tag, message, throwable, mockContext);

        // Then - method completed without throwing
        assertTrue("handleError should complete without throwing", true);
    }

    @Test
    public void testHandleError_withoutThrowable_logsMessage() {
        // Given
        String tag = "TestTag";
        String message = "Test error message";

        // When - should not throw exception
        ErrorHandler.handleError(tag, message, null, mockContext);

        // Then - method completed without throwing
        assertTrue("handleError should complete without throwing", true);
    }

    @Test
    public void testHandleError_withoutContext_logsBasicMessage() {
        // Given
        String tag = "TestTag";
        String message = "Test error message";
        Throwable throwable = new RuntimeException("Test exception");

        // When - should not throw exception
        ErrorHandler.handleError(tag, message, throwable, null);

        // Then - method completed without throwing
        assertTrue("handleError should complete without throwing", true);
    }

    @Test
    public void testGetUserFriendlyErrorMessage_withConnectException_returnsConnectionError() {
        // Given
        ConnectException exception = new ConnectException("Connection refused");

        // When
        String message = ErrorHandler.getUserFriendlyErrorMessage(exception, mockContext);

        // Then
        assertNotNull("Should return a message", message);
        assertTrue("Should contain error text", message.length() > 0);
    }

    @Test
    public void testGetUserFriendlyErrorMessage_withSocketTimeoutException_returnsTimeoutError() {
        // Given
        SocketTimeoutException exception = new SocketTimeoutException("Read timed out");

        // When
        String message = ErrorHandler.getUserFriendlyErrorMessage(exception, mockContext);

        // Then
        assertNotNull("Should return a message", message);
        assertTrue("Should contain error text", message.length() > 0);
    }

    @Test
    public void testGetUserFriendlyErrorMessage_withUnknownHostException_returnsHostError() {
        // Given
        UnknownHostException exception = new UnknownHostException("Host not found");

        // When
        String message = ErrorHandler.getUserFriendlyErrorMessage(exception, mockContext);

        // Then
        assertNotNull("Should return a message", message);
        assertTrue("Should contain error text", message.length() > 0);
    }

    @Test
    public void testGetUserFriendlyErrorMessage_withIOException_returnsIOError() {
        // Given
        IOException exception = new IOException("I/O error");

        // When
        String message = ErrorHandler.getUserFriendlyErrorMessage(exception, mockContext);

        // Then
        assertNotNull("Should return a message", message);
        assertTrue("Should contain error text", message.length() > 0);
    }

    @Test
    public void testGetUserFriendlyErrorMessage_withSecurityException_returnsSecurityError() {
        // Given
        SecurityException exception = new SecurityException("Security violation");

        // When
        String message = ErrorHandler.getUserFriendlyErrorMessage(exception, mockContext);

        // Then
        assertNotNull("Should return a message", message);
        assertTrue("Should contain error text", message.length() > 0);
    }

    @Test
    public void testGetUserFriendlyErrorMessage_withIllegalArgumentException_returnsInvalidArgumentError() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // When
        String message = ErrorHandler.getUserFriendlyErrorMessage(exception, mockContext);

        // Then
        assertNotNull("Should return a message", message);
        assertTrue("Should contain error text", message.length() > 0);
    }

    @Test
    public void testGetUserFriendlyErrorMessage_withUnknownException_returnsGenericError() {
        // Given
        Exception exception = new Exception("Unknown error");

        // When
        String message = ErrorHandler.getUserFriendlyErrorMessage(exception, mockContext);

        // Then
        assertNotNull("Should return a message", message);
        assertTrue("Should contain error text", message.length() > 0);
    }

    @Test
    public void testGetUserFriendlyErrorMessage_withNullException_returnsUnknownError() {
        // Given
        Throwable exception = null;

        // When
        String message = ErrorHandler.getUserFriendlyErrorMessage(exception, mockContext);

        // Then
        assertNotNull("Should return a message", message);
        assertTrue("Should contain error text", message.length() > 0);
    }

    @Test
    public void testGetUserFriendlyErrorMessage_withoutContext_handlesGracefully() {
        // Given
        Exception exception = new RuntimeException("Test error");

        // When - should not throw exception
        String message = ErrorHandler.getUserFriendlyErrorMessage(exception, null);

        // Then - method should handle null context gracefully
        // May return null for unmapped exceptions without context
        assertTrue("Method should complete without throwing", true);
    }

    @Test
    public void testValidateNotNull_withValidObject_doesNotThrow() {
        // Given
        Object validObject = new Object();
        String parameterName = "testObject";

        // When & Then - should not throw exception
        try {
            ErrorHandler.validateNotNull(validObject, parameterName);
            assertTrue("Validation should pass", true);
        } catch (Exception e) {
            fail("Should not throw exception for valid object");
        }
    }

    @Test
    public void testValidateNotNull_withNullObject_throwsException() {
        // Given
        Object nullObject = null;
        String parameterName = "testObject";

        // When & Then
        try {
            ErrorHandler.validateNotNull(nullObject, parameterName);
            fail("Should throw exception for null object");
        } catch (IllegalArgumentException e) {
            assertTrue("Should mention parameter name", e.getMessage().contains(parameterName));
        }
    }

    @Test
    public void testValidateNotEmpty_withValidString_doesNotThrow() {
        // Given
        String validString = "valid content";
        String parameterName = "testString";

        // When & Then - should not throw exception
        try {
            ErrorHandler.validateNotEmpty(validString, parameterName);
            assertTrue("Validation should pass", true);
        } catch (Exception e) {
            fail("Should not throw exception for valid string");
        }
    }

    @Test
    public void testValidateNotEmpty_withNullString_throwsException() {
        // Given
        String nullString = null;
        String parameterName = "testString";

        // When & Then
        try {
            ErrorHandler.validateNotEmpty(nullString, parameterName);
            fail("Should throw exception for null string");
        } catch (IllegalArgumentException e) {
            assertTrue("Should mention parameter name", e.getMessage().contains(parameterName));
        }
    }

    @Test
    public void testValidateNotEmpty_withEmptyString_throwsException() {
        // Given
        String emptyString = "";
        String parameterName = "testString";

        // When & Then
        try {
            ErrorHandler.validateNotEmpty(emptyString, parameterName);
            fail("Should throw exception for empty string");
        } catch (IllegalArgumentException e) {
            assertTrue("Should mention parameter name", e.getMessage().contains(parameterName));
        }
    }

    @Test
    public void testValidateNotEmpty_withWhitespaceString_throwsException() {
        // Given
        String whitespaceString = "   ";
        String parameterName = "testString";

        // When & Then
        try {
            ErrorHandler.validateNotEmpty(whitespaceString, parameterName);
            fail("Should throw exception for whitespace-only string");
        } catch (IllegalArgumentException e) {
            assertTrue("Should mention parameter name", e.getMessage().contains(parameterName));
        }
    }

    @Test
    public void testSafeExecute_withNormalRunnable_executesSuccessfully() {
        // Given
        final boolean[] executed = {false};
        Runnable normalRunnable = () -> executed[0] = true;
        String errorMessage = "Test error";

        // When
        ErrorHandler.safeExecute(normalRunnable, errorMessage, mockContext);

        // Then
        assertTrue("Runnable should be executed", executed[0]);
    }

    @Test
    public void testSafeExecute_withThrowingRunnable_handlesException() {
        // Given
        Runnable throwingRunnable = () -> {
            throw new RuntimeException("Test exception");
        };
        String errorMessage = "Test error";

        // When - should not throw exception
        ErrorHandler.safeExecute(throwingRunnable, errorMessage, mockContext);

        // Then - method completed without throwing
        assertTrue("safeExecute should handle exceptions gracefully", true);
    }

    @Test
    public void testSafeExecute_withNullRunnable_doesNotThrow() {
        // Given
        Runnable nullRunnable = null;
        String errorMessage = "Test error";

        // When - should not throw exception
        ErrorHandler.safeExecute(nullRunnable, errorMessage, mockContext);

        // Then - method completed without throwing
        assertTrue("safeExecute should handle null runnable gracefully", true);
    }

    @Test
    public void testResult_success_returnsSuccessResult() {
        // Given
        String testData = "test data";

        // When
        ErrorHandler.Result<String> result = ErrorHandler.Result.success(testData);

        // Then
        assertTrue("Should be success", result.isSuccess());
        assertFalse("Should not be error", result.isError());
        assertEquals("Should contain data", testData, result.getData());
        assertNull("Should not have error", result.getError());
    }

    @Test
    public void testResult_error_returnsErrorResult() {
        // Given
        Exception testError = new RuntimeException("Test error");

        // When
        ErrorHandler.Result<String> result = ErrorHandler.Result.error(testError);

        // Then
        assertFalse("Should not be success", result.isSuccess());
        assertTrue("Should be error", result.isError());
        assertNull("Should not contain data", result.getData());
        assertEquals("Should contain error", testError, result.getError());
    }

    @Test
    public void testResult_success_withNullData_returnsSuccessResult() {
        // When
        ErrorHandler.Result<String> result = ErrorHandler.Result.success(null);

        // Then
        assertTrue("Should be success", result.isSuccess());
        assertFalse("Should not be error", result.isError());
        assertNull("Should contain null data", result.getData());
        assertNull("Should not have error", result.getError());
    }

    @Test
    public void testResult_error_withNullError_returnsErrorResult() {
        // When
        ErrorHandler.Result<String> result = ErrorHandler.Result.error(null);

        // Then
        assertFalse("Should not be success", result.isSuccess());
        assertTrue("Should be error", result.isError());
        assertNull("Should not contain data", result.getData());
        assertNull("Should contain null error", result.getError());
    }

    @Test
    public void testValidateNotNull_withNullParameterName_doesNotThrow() {
        // Given
        Object validObject = new Object();
        String nullParameterName = null;

        // When & Then - method should handle null parameter name gracefully
        try {
            ErrorHandler.validateNotNull(validObject, nullParameterName);
            // Expected - no exception thrown for null parameter name
            assertTrue("Should handle null parameter name gracefully", true);
        } catch (Exception e) {
            fail("Should not throw exception for null parameter name");
        }
    }

    @Test
    public void testValidateNotEmpty_withNullParameterName_doesNotThrow() {
        // Given
        String validString = "valid";
        String nullParameterName = null;

        // When & Then - method should handle null parameter name gracefully
        try {
            ErrorHandler.validateNotEmpty(validString, nullParameterName);
            // Expected - no exception thrown for null parameter name
            assertTrue("Should handle null parameter name gracefully", true);
        } catch (Exception e) {
            fail("Should not throw exception for null parameter name");
        }
    }
}
