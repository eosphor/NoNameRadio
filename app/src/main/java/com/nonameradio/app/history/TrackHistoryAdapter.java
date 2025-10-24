package com.nonameradio.app.history;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentActivity;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.nonameradio.app.R;
import com.nonameradio.app.Utils;
import com.nonameradio.app.service.MediaSessionUtil;
import com.nonameradio.app.utils.ImageLoader;

public class TrackHistoryAdapter extends PagingDataAdapter<TrackHistoryEntry, TrackHistoryAdapter.TrackHistoryItemViewHolder> {
    class TrackHistoryItemViewHolder extends RecyclerView.ViewHolder {
        final View rootview;

        final ImageView imageViewStationIcon;
        final TextView textViewTrackName;
        final TextView textViewTrackArtist;

        private TrackHistoryItemViewHolder(View itemView) {
            super(itemView);

            rootview = itemView;

            imageViewStationIcon = itemView.findViewById(R.id.imageViewStationIcon);
            textViewTrackName = itemView.findViewById(R.id.textViewTrackName);
            textViewTrackArtist = itemView.findViewById(R.id.textViewTrackArtist);
        }
    }

    private final Context context;
    private final FragmentActivity activity;
    private final LayoutInflater inflater;
    private boolean shouldLoadIcons;
    private final Drawable stationImagePlaceholder;

    public TrackHistoryAdapter(FragmentActivity activity) {
        super(DIFF_CALLBACK);
        this.activity = activity;
        this.context = activity;
        inflater = LayoutInflater.from(context);

        stationImagePlaceholder = AppCompatResources.getDrawable(context, R.drawable.ic_photo_24dp);
    }

    @NonNull
    @Override
    public TrackHistoryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.list_item_history_track_item, parent, false);
        return new TrackHistoryItemViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull final TrackHistoryItemViewHolder holder, int position) {
        final TrackHistoryEntry historyEntry = getItem(position);

        // null if a placeholder
        if (historyEntry == null) {
            return;
        }

        if (shouldLoadIcons) {
            if (!TextUtils.isEmpty(historyEntry.stationIconUrl)) {
                //setupIcon(useCircularIcons, holder.imageViewIcon, holder.transparentImageView);
                MediaSessionUtil.getStationIcon(holder.imageViewStationIcon, historyEntry.stationIconUrl, stationImagePlaceholder);
            } else {
                holder.imageViewStationIcon.setImageDrawable(stationImagePlaceholder);
            }
        } else {
            holder.imageViewStationIcon.setVisibility(View.GONE);
        }

        holder.textViewTrackName.setText(historyEntry.track);
        holder.textViewTrackArtist.setText(historyEntry.artist);

        holder.textViewTrackName.setSelected(true);
        holder.textViewTrackArtist.setSelected(true);

        holder.rootview.setOnClickListener(view -> showTrackInfoDialog(historyEntry));
    }

    @Override
    public void onViewRecycled(@NonNull TrackHistoryItemViewHolder holder) {
        super.onViewRecycled(holder);

        ImageLoader.cancelRequest(holder.imageViewStationIcon.getContext(), holder.imageViewStationIcon);
        holder.imageViewStationIcon.setImageDrawable(stationImagePlaceholder);
    }

    public void setShouldLoadIcons(boolean shouldLoadIcons) {
        this.shouldLoadIcons = shouldLoadIcons;
    }

    private void showTrackInfoDialog(final TrackHistoryEntry historyEntry) {
        TrackHistoryInfoDialog trackHistoryInfoDialog = new TrackHistoryInfoDialog(historyEntry);
        trackHistoryInfoDialog.show(activity.getSupportFragmentManager(), TrackHistoryInfoDialog.FRAGMENT_TAG);
    }

    private static final DiffUtil.ItemCallback<TrackHistoryEntry> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<TrackHistoryEntry>() {
                @Override
                public boolean areItemsTheSame(TrackHistoryEntry oldEntry, TrackHistoryEntry newEntry) {
                    return oldEntry.uid == newEntry.uid;
                }

                @Override
                public boolean areContentsTheSame(TrackHistoryEntry oldEntry,
                                                  @NonNull TrackHistoryEntry newEntry) {
                    return oldEntry.equals(newEntry);
                }
            };

}
