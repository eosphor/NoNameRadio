package com.nonameradio.app.history;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.nonameradio.app.R;

import java.text.DateFormat;
import java.util.Objects;

public class TrackHistoryInfoDialog extends BottomSheetDialogFragment {

    public static final String FRAGMENT_TAG = "tracks_history_info_dialog_fragment";

    private final TrackHistoryEntry historyEntry;

    public TrackHistoryInfoDialog(TrackHistoryEntry historyEntry) {
        this.historyEntry = historyEntry;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setRetainInstance(true);

        View view = inflater.inflate(R.layout.dialog_track_history_details, container, false);

        AppCompatImageView imageViewTrackArt = view.findViewById(R.id.imageViewTrackArt);
        TextView textViewDate = view.findViewById(R.id.textViewDate);
        TextView textViewDuration = view.findViewById(R.id.textViewDuration);
        AppCompatButton btnLyrics = view.findViewById(R.id.btnViewLyrics);
        AppCompatButton btnCopyInfo = view.findViewById(R.id.btnCopyTrackInfo);

        Resources resource = requireContext().getResources();
        final float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, resource.getDisplayMetrics());
        
        Drawable placeholder = AppCompatResources.getDrawable(getContext(), R.drawable.ic_photo_24dp);
        RequestOptions options = new RequestOptions()
                .placeholder(placeholder)
                .override((int) px, 0);
                
        Glide.with(this)
                .load(historyEntry.artUrl)
                .apply(options)
                .into(imageViewTrackArt);

        // Add icons for date and duration
        Drawable dateIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_query_builder_black_24dp);
        Drawable durationIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_hourglass_empty_black_24dp);

        if (dateIcon != null) {
            dateIcon.setBounds(0, 0, 24, 24);
            textViewDate.setCompoundDrawables(dateIcon, null, null, null);
            textViewDate.setCompoundDrawablePadding(8);
        }

        textViewDate.setText(DateFormat.getDateInstance().format(historyEntry.startTime));

        if (historyEntry.endTime.after(historyEntry.startTime)) {
            String elapsedTime = DateUtils.formatElapsedTime((historyEntry.endTime.getTime() - historyEntry.startTime.getTime()) / 1000);
            textViewDuration.setText(elapsedTime);

            if (durationIcon != null) {
                durationIcon.setBounds(0, 0, 24, 24);
                textViewDuration.setCompoundDrawables(durationIcon, null, null, null);
                textViewDuration.setCompoundDrawablePadding(8);
            }
        } else {
            textViewDuration.setText("");
        }

        btnLyrics.setOnClickListener(v -> {
            if (isQuickLyricInstalled()) {
                getContext().startActivity(new Intent("com.geecko.QuickLyric.getLyrics")
                        .putExtra("TAGS", new String[]{historyEntry.artist, historyEntry.track}));
            } else {
                new AlertDialog.Builder(getContext())
                        .setMessage(this.getString(R.string.alert_install_lyrics_app))
                        .setCancelable(true)
                        .setPositiveButton(this.getString(R.string.yes), (dialog, id) -> {
                            try {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.geecko.QuickLyric"));
                                getContext().startActivity(browserIntent);
                            } catch (ActivityNotFoundException ex) {
                                try {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.geecko.QuickLyric"));
                                    getContext().startActivity(browserIntent);
                                } catch (ActivityNotFoundException ex2) {
                                    Toast toast = Toast.makeText(getContext(), R.string.notify_open_link_failure, Toast.LENGTH_LONG);
                                    toast.show();
                                }
                            }
                        })
                        .setNegativeButton(this.getString(R.string.no), null)
                        .show();
            }
        });

        btnCopyInfo.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("Track info", String.format("%s %s", historyEntry.artist, historyEntry.track));
                clipboard.setPrimaryClip(clip);

                CharSequence toastText = getContext().getResources().getText(R.string.notify_track_info_copied);
                Toast.makeText(getContext().getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
            } else {
                //Log.e(TAG, "Clipboard is NULL!");
                // TODO: toast general error
            }
        });

        return view;
    }

    private boolean isQuickLyricInstalled() {
        PackageManager pm = requireContext().getPackageManager();
        try {
            return pm.getApplicationInfo("com.geecko.QuickLyric", 0).enabled;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }
}
