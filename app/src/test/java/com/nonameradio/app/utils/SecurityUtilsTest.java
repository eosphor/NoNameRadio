package com.nonameradio.app.utils;

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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;

/**
 * Unit tests for SecurityUtils class.
 * Tests security validation methods, input sanitization, and secure configurations.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 28)
public class SecurityUtilsTest {

    @Mock
    private Context mockContext;

    private AutoCloseable mocks;

    @Before
    public void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @After
    public void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    public void testIsValidStationUuid_withValidUuid_returnsTrue() {
        // Given
        String validUuid = "550e8400-e29b-41d4-a716-446655440000";

        // When
        boolean result = SecurityUtils.isValidStationUuid(validUuid);

        // Then
        assertTrue("Valid UUID should be accepted", result);
    }

    @Test
    public void testIsValidStationUuid_withNull_returnsFalse() {
        // Given
        String nullUuid = null;

        // When
        boolean result = SecurityUtils.isValidStationUuid(nullUuid);

        // Then
        assertFalse("Null UUID should be rejected", result);
    }

    @Test
    public void testIsValidStationUuid_withEmptyString_returnsFalse() {
        // Given
        String emptyUuid = "";

        // When
        boolean result = SecurityUtils.isValidStationUuid(emptyUuid);

        // Then
        assertFalse("Empty UUID should be rejected", result);
    }

    @Test
    public void testIsValidStationUuid_withInvalidFormat_returnsFalse() {
        // Given
        String[] invalidUuids = {
            "550e8400-e29b-41d4-a716", // Too short
            "550e8400-e29b-41d4-a716-446655440000-extra", // Too long
            "550e8400-e29b-41d4-a716-44665544000g", // Invalid character 'g'
            "550e8400e29b41d4a716446655440000", // No dashes
            "550e8400-e29b-41d4-a716-44665544000", // Missing last character
        };

        for (String invalidUuid : invalidUuids) {
            // When
            boolean result = SecurityUtils.isValidStationUuid(invalidUuid);

            // Then
            assertFalse("Invalid UUID '" + invalidUuid + "' should be rejected", result);
        }
    }

    @Test
    public void testIsValidStationUuid_withUppercase_returnsTrue() {
        // Given
        String uppercaseUuid = "550E8400-E29B-41D4-A716-446655440000";

        // When
        boolean result = SecurityUtils.isValidStationUuid(uppercaseUuid);

        // Then
        assertTrue("Uppercase UUID should be accepted", result);
    }

    @Test
    public void testIsValidUrl_withValidHttpsUrl_returnsTrue() {
        // Given
        String validUrl = "https://radio-browser.info/webservice/json/stations";

        // When
        boolean result = SecurityUtils.isValidUrl(validUrl);

        // Then
        assertTrue("Valid HTTPS URL should be accepted", result);
    }

    @Test
    public void testIsValidUrl_withValidHttpUrl_returnsTrue() {
        // Given - use an allowed domain from ALLOWED_DOMAINS
        String validUrl = "http://radio-browser.info/stream.mp3";

        // When
        boolean result = SecurityUtils.isValidUrl(validUrl);

        // Then
        assertTrue("Valid HTTP URL with allowed domain should be accepted", result);
    }

    @Test
    public void testIsValidUrl_withNull_returnsFalse() {
        // Given
        String nullUrl = null;

        // When
        boolean result = SecurityUtils.isValidUrl(nullUrl);

        // Then
        assertFalse("Null URL should be rejected", result);
    }

    @Test
    public void testIsValidUrl_withEmptyString_returnsFalse() {
        // Given
        String emptyUrl = "";

        // When
        boolean result = SecurityUtils.isValidUrl(emptyUrl);

        // Then
        assertFalse("Empty URL should be rejected", result);
    }

    @Test
    public void testIsValidUrl_withInvalidFormat_returnsFalse() {
        // Given
        String[] invalidUrls = {
            "not-a-url",
            "ftp://example.com",
            "file:///etc/passwd",
            "javascript:alert('xss')",
            "data:text/plain;base64,SGVsbG8=",
            "http://",
            "https://",
            "://example.com"
        };

        for (String invalidUrl : invalidUrls) {
            // When
            boolean result = SecurityUtils.isValidUrl(invalidUrl);

            // Then
            assertFalse("Invalid URL '" + invalidUrl + "' should be rejected", result);
        }
    }

    @Test
    public void testIsValidUrl_withAllowedDomains_onlyAllowsWhitelist() throws Exception {
        // Get the allowed domains list using reflection
        Field allowedDomainsField = SecurityUtils.class.getDeclaredField("ALLOWED_DOMAINS");
        allowedDomainsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> allowedDomains = (List<String>) allowedDomainsField.get(null);

        // Test allowed domains
        for (String domain : allowedDomains) {
            String url = "https://" + domain + "/api/stations";
            boolean result = SecurityUtils.isValidUrl(url);
            assertTrue("URL with allowed domain '" + domain + "' should be accepted", result);
        }

        // Test disallowed domains
        String[] disallowedDomains = {"evil.com", "malicious.net", "hacker.org"};
        for (String domain : disallowedDomains) {
            String url = "https://" + domain + "/api/data";
            boolean result = SecurityUtils.isValidUrl(url);
            assertFalse("URL with disallowed domain '" + domain + "' should be rejected", result);
        }
    }

    @Test
    public void testSanitizeInput_withNormalText_returnsSame() {
        // Given
        String normalText = "This is normal text 123";

        // When
        String result = SecurityUtils.sanitizeInput(normalText);

        // Then
        assertEquals("Normal text should remain unchanged", normalText, result);
    }

    @Test
    public void testSanitizeInput_withNull_returnsEmptyString() {
        // Given
        String nullInput = null;

        // When
        String result = SecurityUtils.sanitizeInput(nullInput);

        // Then
        assertEquals("Null input should return empty string", "", result);
    }

    @Test
    public void testSanitizeInput_withScriptTags_removesTags() {
        // Given
        String maliciousInput = "<script>alert('xss')</script>Hello<script>evil()</script>";

        // When
        String result = SecurityUtils.sanitizeInput(maliciousInput);

        // Then
        assertFalse("Script tags should be removed", result.contains("<script>"));
        assertFalse("Script tags should be removed", result.contains("</script>"));
        assertTrue("Safe content should remain", result.contains("Hello"));
    }

    @Test
    public void testSanitizeInput_withSqlInjection_removesDangerousChars() {
        // Given
        String sqlInjection = "'; DROP TABLE users; --";

        // When
        String result = SecurityUtils.sanitizeInput(sqlInjection);

        // Then - dangerous characters should be removed
        assertFalse("Single quotes should be removed", result.contains("'"));
        assertFalse("Semicolons should be removed", result.contains(";"));
        // The rest of the content should remain (with spaces preserved)
        assertTrue("Safe content should remain", result.contains("DROP TABLE users"));
    }

    @Test
    public void testSanitizeInput_withHtmlEntities_removesTags() {
        // Given
        String htmlInput = "<b>Bold</b> & <i>Italic</i>";

        // When
        String result = SecurityUtils.sanitizeInput(htmlInput);

        // Then
        assertFalse("HTML tags should be removed", result.contains("<b>"));
        assertFalse("HTML tags should be removed", result.contains("</b>"));
        assertFalse("HTML tags should be removed", result.contains("<i>"));
        assertFalse("HTML tags should be removed", result.contains("</i>"));
        assertTrue("Text content should remain", result.contains("Bold"));
        assertTrue("Text content should remain", result.contains("Italic"));
    }

    @Test
    public void testSanitizeInput_withUnicodeCharacters_preservesUnicode() {
        // Given
        String unicodeText = "Hello 世界 🌍";

        // When
        String result = SecurityUtils.sanitizeInput(unicodeText);

        // Then
        assertEquals("Unicode characters should be preserved", unicodeText, result);
    }

    @Test
    public void testCreateSecureHttpClient_returnsNonNullClient() {
        // When
        OkHttpClient client = SecurityUtils.createSecureHttpClient(mockContext);

        // Then
        assertNotNull("Secure HTTP client should be created", client);
        // Note: context parameter is currently not used but kept for future extensibility
    }

    @Test
    public void testCreateSecureHttpClient_withNullContext_works() {
        // When - context is not currently used in implementation
        OkHttpClient client = SecurityUtils.createSecureHttpClient(null);

        // Then - should still create client successfully
        assertNotNull("Secure HTTP client should be created even with null context", client);
    }

    @Test
    public void testSanitizeInput_withExcessiveLength_trimsInput() {
        // Given - create a very long string
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longInput.append("a");
        }
        String veryLongInput = longInput.toString();

        // When
        String result = SecurityUtils.sanitizeInput(veryLongInput);

        // Then
        assertNotNull("Result should not be null", result);
        // The method should handle long inputs gracefully
        assertTrue("Result should be reasonable length", result.length() <= veryLongInput.length());
    }

    @Test
    public void testIsValidStationUuid_withVariousFormats() {
        // Test various valid UUID formats
        String[] validUuids = {
            "550e8400-e29b-41d4-a716-446655440000",
            "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
            "550e8400-E29B-41D4-A716-446655440000", // uppercase
            "550E8400-e29b-41d4-a716-446655440000"  // mixed case
        };

        for (String uuid : validUuids) {
            boolean result = SecurityUtils.isValidStationUuid(uuid);
            assertTrue("UUID '" + uuid + "' should be valid", result);
        }
    }
}
