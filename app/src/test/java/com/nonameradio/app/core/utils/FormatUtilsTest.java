package com.nonameradio.app.core.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class FormatUtilsTest {

    @Test
    public void testFormatStationName_withValidName() {
        String result = FormatUtils.formatStationName("Test Station");
        assertEquals("Test Station", result);
    }

    @Test
    public void testFormatStationName_withNullName() {
        String result = FormatUtils.formatStationName(null);
        assertEquals("Unknown Station", result);
    }

    @Test
    public void testFormatStationName_withEmptyName() {
        String result = FormatUtils.formatStationName("");
        assertEquals("Unknown Station", result);
    }

    @Test
    public void testFormatStationName_withWhitespace() {
        String result = FormatUtils.formatStationName("  Test Station  ");
        assertEquals("Test Station", result);
    }

    @Test
    public void testFormatFileSize() {
        assertEquals("0 B", FormatUtils.formatFileSize(0));
        assertEquals("1.0 KB", FormatUtils.formatFileSize(1024));
        assertEquals("1.0 MB", FormatUtils.formatFileSize(1024 * 1024));
        assertEquals("1.0 GB", FormatUtils.formatFileSize(1024 * 1024 * 1024));
    }

    @Test
    public void testFormatDuration() {
        assertEquals("00:00", FormatUtils.formatDuration(0));
        assertEquals("00:30", FormatUtils.formatDuration(30000));
        assertEquals("01:30", FormatUtils.formatDuration(90000));
        assertEquals("01:30:30", FormatUtils.formatDuration(5430000));
    }

    @Test
    public void testFormatStringWithNamedArgs() {
        String template = "Station: %name%, Bitrate: %bitrate% kbps";
        String result = FormatUtils.formatStringWithNamedArgs(template,
            java.util.Map.of("name", "Test FM", "bitrate", "128"));
        assertEquals("Station: Test FM, Bitrate: 128 kbps", result);
    }

    @Test
    public void testFormatStringWithNamedArgs_missingArgs() {
        String template = "Station: %name%, Bitrate: %bitrate% kbps";
        String result = FormatUtils.formatStringWithNamedArgs(template,
            java.util.Map.of("name", "Test FM"));
        assertEquals("Station: Test FM, Bitrate: %bitrate% kbps", result);
    }
}

