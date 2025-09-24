package com.nonameradio.app.recording;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.nonameradio.app.BuildConfig;
import com.nonameradio.app.R;
import com.nonameradio.app.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class RecordingsAdapter extends RecyclerView.Adapter<RecordingsAdapter.RecordingItemViewHolder> {
    final static String TAG = "RecordingsAdapter";

    private RecordingsManager recordingsManager;

    public RecordingsAdapter(Context context, RecordingsManager recordingsManager) {
        this.context = context;
        this.recordingsManager = recordingsManager;
        this.recordings = new ArrayList<>();
        this.deleteClickListener = null;
    }

    class RecordingItemViewHolder extends RecyclerView.ViewHolder {
        final ViewGroup viewRoot;
        final TextView textViewTitle;
        final TextView textViewTime;
        final ImageButton buttonDelete;

        private RecordingItemViewHolder(View itemView) {
            super(itemView);

            viewRoot = itemView.findViewById(R.id.layoutRecordingInfo);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteRecording);
        }
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(DataRecording recording);
    }

    private final Context context;
    private List<DataRecording> recordings;
    private OnDeleteClickListener deleteClickListener;

    public RecordingsAdapter(@NonNull Context context) {
        this.context = context;
        this.recordings = Collections.emptyList();
    }

    @NonNull
    @Override
    public RecordingsAdapter.RecordingItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.list_item_recording, parent, false);
        return new RecordingItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordingItemViewHolder holder, int position) {
        final DataRecording recording = recordings.get(position);

        // Get metadata for enhanced display
        RecordingMetadata metadata = recordingsManager.getRecordingMetadata(recording.Name);

        // Set title - use metadata title if available, otherwise file name
        String title = recording.Name;
        if (metadata != null && metadata.getDisplayName() != null) {
            title = metadata.getDisplayName();
        }
        holder.textViewTitle.setText(title);

        // Set time/size info
        StringBuilder info = new StringBuilder();

        if (recording.InProgress) {
            info.append(context.getString(R.string.recording_in_progress_size,
                    Utils.getReadableBytes(recording.SizeBytes)));
        } else {
            // Add recording date
            if (metadata != null && metadata.getStartDate() != null) {
                info.append(DateFormat.getMediumDateFormat(context).format(metadata.getStartDate()));
                info.append(" ");
                info.append(DateFormat.getTimeFormat(context).format(metadata.getStartDate()));
            } else if (recording.Time != null) {
                info.append(DateFormat.getMediumDateFormat(context).format(recording.Time));
                info.append(" ");
                info.append(DateFormat.getTimeFormat(context).format(recording.Time));
            }

            // Add duration and size if metadata available
            if (metadata != null) {
                if (info.length() > 0) info.append(" • ");
                info.append(metadata.getDurationString());
                info.append(" • ");
                info.append(metadata.getFileSizeString());
            } else if (recording.SizeBytes > 0) {
                if (info.length() > 0) info.append(" • ");
                info.append(Utils.getReadableBytes(recording.SizeBytes));
            }
        }

        holder.textViewTime.setText(info.toString());

        holder.viewRoot.setOnClickListener(view -> openRecording(recording));
        holder.buttonDelete.setOnClickListener(view -> confirmDelete(recording));
    }

    public void setRecordings(List<DataRecording> recordings) {
        this.recordings = recordings;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return recordings != null ? recordings.size() : 0;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    private void openRecording(DataRecording dataRecording) {
        Uri fileUri = resolveRecordingUri(dataRecording);
        if (fileUri == null) {
            Toast.makeText(context, R.string.error_play_stream, Toast.LENGTH_SHORT).show();
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "play: " + fileUri);
        }

        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(fileUri, "audio/*")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        context.startActivity(intent);
    }

    private void confirmDelete(DataRecording dataRecording) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.recording_delete_title)
                .setMessage(context.getString(R.string.recording_delete_message, dataRecording.Name))
                .setPositiveButton(R.string.recording_delete_confirm, (dialog, which) -> {
                    if (deleteClickListener != null) {
                        deleteClickListener.onDeleteClick(dataRecording);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private Uri resolveRecordingUri(DataRecording dataRecording) {
        if (dataRecording.ContentUri != null) {
            return dataRecording.ContentUri;
        }

        if (dataRecording.AbsolutePath != null) {
            File file = new File(dataRecording.AbsolutePath);
            if (file.exists()) {
                return FileProvider.getUriForFile(context, "com.nonameradio.app.fileprovider", file);
            }
        }

        String path = RecordingsManager.getRecordDir() + "/" + dataRecording.Name;
        File file = new File(path);
        if (file.exists()) {
            return FileProvider.getUriForFile(context, "com.nonameradio.app.fileprovider", file);
        }

        return null;
    }

    private void grantUriPermissionsLegacy(Uri fileUri, Intent intent) {
        List<ResolveInfo> resInfoList =
                context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, fileUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }
}
