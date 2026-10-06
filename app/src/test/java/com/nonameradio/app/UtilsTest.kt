package com.nonameradio.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class UtilsTest {
    @Test
    fun testUrlIndicatesHlsStream() {
        assertTrue(Utils.urlIndicatesHlsStream("http://www.example.org/playlist.m3u8"))
        assertTrue(Utils.urlIndicatesHlsStream("http://www.example.org/playlist.m3u8 # cool"))
        assertTrue(Utils.urlIndicatesHlsStream("http://www.example.org/playlist.m3u8\n"))
        assertTrue(Utils.urlIndicatesHlsStream("https://stream.revma.ihrhls.com/zc5260/hls.m3u8?streamid=5260"))
        assertTrue(Utils.urlIndicatesHlsStream("http://www.example.org/playlist.m3u8?bitrate=256&z=43"))
        assertTrue(Utils.urlIndicatesHlsStream("http://www.example.org/playlist.m3u8#0100"))
        assertTrue(Utils.urlIndicatesHlsStream("http://www.example.org/playlist.m3u8#START"))
        assertTrue(Utils.urlIndicatesHlsStream("http://www.example.org/m3u8playlist.m3u8"))

        assertFalse(Utils.urlIndicatesHlsStream("http://www.example.org/no.m3u8.m3u"))
        assertFalse(Utils.urlIndicatesHlsStream("http://www.example.org/playlist.m3u85"))
        assertFalse(Utils.urlIndicatesHlsStream("http://www.example.org/playlist.m3united"))
    }
}
