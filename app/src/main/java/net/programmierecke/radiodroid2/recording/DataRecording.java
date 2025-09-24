package net.programmierecke.radiodroid2.recording;

import java.util.Date;
import android.net.Uri;

public class DataRecording {
    public String Name = "";
    public Date Time;
    public Uri ContentUri;
    public String AbsolutePath;
    public long SizeBytes;
    public boolean InProgress;
}
