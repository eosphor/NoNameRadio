package com.eosphor.nonameradio.players.mediaplayer;

import com.eosphor.nonameradio.station.live.ShoutcastInfo;
import com.eosphor.nonameradio.station.live.StreamLiveInfo;

interface StreamProxyListener {
    void onFoundShoutcastStream(ShoutcastInfo bitrate, boolean isHls);
    void onFoundLiveStreamInfo(StreamLiveInfo liveInfo);
    void onStreamCreated(String proxyConnection);
    void onStreamStopped();
    void onBytesRead(byte[] buffer, int offset, int length);
}
