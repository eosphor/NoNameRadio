package com.nonameradio.app.recording;

import android.net.Uri;

import java.io.OutputStream;

public class RunningRecordingInfo {
    private Recordable recordable;
    private String title;
    private String fileName;
    private OutputStream outputStream;
    private long bytesWritten;
    private Uri mediaStoreUri;
    private DataRecording linkedDataRecording;

    public Recordable getRecordable() {
        return recordable;
    }

    protected void setRecordable(Recordable recordable) {
        this.recordable = recordable;
    }

    public DataRecording getLinkedDataRecording() {
        return linkedDataRecording;
    }

    protected void setLinkedDataRecording(DataRecording linkedDataRecording) {
        this.linkedDataRecording = linkedDataRecording;
    }

    void updateLinkedRecordingSize() {
        if (linkedDataRecording != null) {
            linkedDataRecording.SizeBytes = bytesWritten;
        }
    }

    void updateLinkedRecordingFinished() {
        if (linkedDataRecording != null) {
            linkedDataRecording.InProgress = false;
        }
    }

    public String getTitle() {
        return title;
    }

    protected void setTitle(String title) {
        this.title = title;
    }

    public String getFileName() {
        return fileName;
    }

    protected void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    protected void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public long getBytesWritten() {
        return bytesWritten;
    }

    protected void setBytesWritten(long bytesWritten) {
        this.bytesWritten = bytesWritten;
        updateLinkedRecordingSize();
    }

    public Uri getMediaStoreUri() {
        return mediaStoreUri;
    }

    protected void setMediaStoreUri(Uri mediaStoreUri) {
        this.mediaStoreUri = mediaStoreUri;
        if (linkedDataRecording != null) {
            linkedDataRecording.ContentUri = mediaStoreUri;
        }
    }
}
