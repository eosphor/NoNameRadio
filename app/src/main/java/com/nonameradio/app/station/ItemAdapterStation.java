package com.nonameradio.app.station;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.nonameradio.app.core.event.EventBus;
import com.nonameradio.app.core.event.MetaUpdateEvent;
import com.nonameradio.app.core.event.RadioStationChangedEvent;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.*;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.IconicsSize;
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial;
import com.mikepenz.iconics.view.IconicsImageButton;

import com.nonameradio.app.*;
import com.nonameradio.app.interfaces.IAdapterRefreshable;
import com.nonameradio.app.players.PlayStationTask;
import com.nonameradio.app.players.selector.PlayerType;
import com.nonameradio.app.utils.RecyclerItemMoveAndSwipeHelper;
import com.nonameradio.app.service.PlayerService;
import com.nonameradio.app.service.MediaSessionUtil;
import com.nonameradio.app.utils.ImageLoader;
import com.nonameradio.app.utils.RecyclerItemSwipeHelper;
import com.nonameradio.app.utils.SwipeableViewHolder;
import com.nonameradio.app.views.TagsView;

public class ItemAdapterStation
        extends RecyclerView.Adapter<ItemAdapterStation.StationViewHolder>
        implements RecyclerItemMoveAndSwipeHelper.MoveAndSwipeCallback<ItemAdapterStation.StationViewHolder> {

    public interface StationActionsListener {
        void onStationClick(DataRadioStation station, int pos);

        void onStationMoved(int from, int to);

        void onStationSwiped(DataRadioStation station);

        void onStationMoveFinished();
    }

    public interface FilterListener {
        void onSearchCompleted(StationsFilter.SearchStatus searchStatus);
    }

    private final String TAG = "AdapterStations";

    List<DataRadioStation> stationsList;
    List<DataRadioStation> filteredStationsList = new ArrayList<>();

    // Map for fast UUID to position lookup
    private final Map<String, Integer> uuidToPositionMap = new HashMap<>();

    int resourceId;

    /**
     * Rebuilds the UUID to position mapping for fast lookups
     */
    private void rebuildPositionMap() {
        uuidToPositionMap.clear();
        if (filteredStationsList != null) {
            for (int i = 0; i < filteredStationsList.size(); i++) {
                DataRadioStation station = filteredStationsList.get(i);
                if (station != null && station.StationUuid != null) {
                    uuidToPositionMap.put(station.StationUuid, i);
                }
            }
        }
    }

    StationActionsListener stationActionsListener;
    private FilterListener filterListener;
    private boolean supportsStationRemoval = false;
    private StationsFilter.FilterType filterType = StationsFilter.FilterType.LOCAL;

    private boolean shouldLoadIcons;

    private IAdapterRefreshable refreshable;
    FragmentActivity activity;

    private int expandedPosition = -1;
    public int playingStationPosition = -1;

    Drawable stationImagePlaceholder;

    private final FavouriteManager favouriteManager;
    
    // EventBus listeners (keep references for unregister)
    private final EventBus.EventListener<MetaUpdateEvent> metaUpdateListener = event -> highlightCurrentStation();
    private final EventBus.EventListener<RadioStationChangedEvent> stationChangedListener = event -> {
        if (event.stationUuid != null) {
            notifyChangedByStationUuid(event.stationUuid);
        }
    };

    private StationsFilter filter;

    private final TagsView.TagSelectionCallback tagSelectionCallback = new TagsView.TagSelectionCallback() {
        @Override
        public void onTagSelected(String tag) {
            Intent i = new Intent(getContext(), ActivityMain.class);
            i.putExtra(ActivityMain.EXTRA_SEARCH_TAG, tag);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(i);
        }
    };

    class StationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, SwipeableViewHolder {
        View viewForeground;
        LinearLayout layoutMain;
        FrameLayout frameLayout;

        ImageView imageViewIcon;
        ImageView transparentImageView;
        ImageView starredStatusIcon;
        TextView textViewTitle;
        TextView textViewShortDescription;
        TextView textViewTags;
        ImageButton buttonMore;
        ImageButton buttonFavorite;

        private boolean iconClicked = false;

        View viewDetails;
        ViewStub stubDetails;
        IconicsImageButton buttonVisitWebsite;
        ImageButton buttonBookmark;
        ImageButton buttonShare;
        ImageView imageTrend;
        ImageButton buttonAddAlarm;
        TagsView viewTags;
        ImageButton buttonCreateShortcut;
        ImageButton buttonPlayInternalOrExternal;

        StationViewHolder(View itemView) {
            super(itemView);

            viewForeground = itemView.findViewById(R.id.station_foreground);
            layoutMain = itemView.findViewById(R.id.layoutMain);
            frameLayout = itemView.findViewById(R.id.frameLayout);

            imageViewIcon = itemView.findViewById(R.id.imageViewIcon);
            imageTrend = itemView.findViewById(R.id.trendStatusIcon);
            transparentImageView = itemView.findViewById(R.id.transparentCircle);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewShortDescription = itemView.findViewById(R.id.textViewShortDescription);
            textViewTags = itemView.findViewById(R.id.textViewTags);
            buttonMore = itemView.findViewById(R.id.buttonMore);
            buttonFavorite = itemView.findViewById(R.id.buttonFavorite);
            stubDetails = itemView.findViewById(R.id.stubDetails);

        }

        @Override
        public void onClick(View view) {
            Log.d("StationClick", "Card clicked for station: " + filteredStationsList.get(getBindingAdapterPosition()).Name + ", view: " + view.getClass().getSimpleName());
            if (stationActionsListener != null) {
                int pos = getBindingAdapterPosition();
                stationActionsListener.onStationClick(filteredStationsList.get(pos), pos);
            }
        }

        @Override
        public View getForegroundView() {
            return viewForeground;
        }
    }

    public ItemAdapterStation(FragmentActivity fragmentActivity, int resourceId, StationsFilter.FilterType filterType) {
        this.activity = fragmentActivity;
        this.resourceId = resourceId;
        this.filterType = filterType;

        stationImagePlaceholder = AppCompatResources.getDrawable(fragmentActivity, R.drawable.ic_photo_24dp);

        NoNameRadioApp app = (NoNameRadioApp) fragmentActivity.getApplication();
        favouriteManager = app.getFavouriteManager();
        // Register EventBus listeners for meta updates and station changes
        EventBus.register(MetaUpdateEvent.class, metaUpdateListener);
        EventBus.register(RadioStationChangedEvent.class, stationChangedListener);
    }

    public void setStationActionsListener(StationActionsListener stationActionsListener) {
        this.stationActionsListener = stationActionsListener;
    }

    public void setFilterListener(FilterListener filterListener) {
        this.filterListener = filterListener;
    }

    public void enableItemRemoval(RecyclerView recyclerView) {
        if (!supportsStationRemoval) {
            supportsStationRemoval = true;

            RecyclerItemSwipeHelper<StationViewHolder> swipeHelper = new RecyclerItemSwipeHelper<>(getContext(), 0, ItemTouchHelper.LEFT + ItemTouchHelper.RIGHT, this);
            new ItemTouchHelper(swipeHelper).attachToRecyclerView(recyclerView);
        }
    }

    public void enableItemMoveAndRemoval(RecyclerView recyclerView) {
        if (!supportsStationRemoval) {
            supportsStationRemoval = true;

            RecyclerItemMoveAndSwipeHelper<StationViewHolder> swipeAndMoveHelper = new RecyclerItemMoveAndSwipeHelper<>(getContext(), ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, this);
            new ItemTouchHelper(swipeAndMoveHelper).attachToRecyclerView(recyclerView);
        }
    }

    public void updateList(FragmentStarred refreshableList, List<DataRadioStation> stationsList) {
        this.refreshable = refreshableList;
        this.stationsList = stationsList;
        this.filteredStationsList = stationsList;

        notifyStationsChanged();
    }

    private void notifyStationsChanged() {
        android.util.Log.d("ItemAdapterStation", "notifyStationsChanged: stations count=" + (filteredStationsList != null ? filteredStationsList.size() : 0));
        expandedPosition = -1;
        playingStationPosition = -1;

        // Rebuild the position map for fast lookups
        rebuildPositionMap();

        shouldLoadIcons = Utils.shouldLoadIcons(getContext());

        highlightCurrentStation();

        // Use a more gentle update approach for large lists
        if (filteredStationsList != null && filteredStationsList.size() > 1000) {
            // For large lists, post the update to avoid blocking the UI thread
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                notifyDataSetChanged();
            });
        } else {
            notifyDataSetChanged();
        }
    }

    @Override
    public StationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(resourceId, parent, false);

        return new StationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final StationViewHolder holder, int position) {
        final DataRadioStation station = filteredStationsList.get(position);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
        boolean useCircularIcons = Utils.useCircularIcons(getContext());
        if (station.DeletedOnServer){
            // set to red
            holder.itemView.setBackgroundColor(0xFFFF0000);
        }else if (!station.Working){
            // set to yellow
            holder.itemView.setBackgroundColor(0xFFFFFF00);
        }else{
            // set to transparent
            holder.itemView.setBackgroundColor(0x00000000);
        }

        // Set click listener on the entire card (excluding buttons) for playback
        holder.itemView.setOnClickListener(holder);

        if (!shouldLoadIcons) {
            holder.imageViewIcon.setVisibility(View.GONE);
        } else {
            if (station.hasIcon()) {
                setupIcon(useCircularIcons, holder.imageViewIcon, holder.transparentImageView);
                MediaSessionUtil.getStationIcon(holder.imageViewIcon, station.IconUrl, stationImagePlaceholder);
            } else {
                holder.imageViewIcon.setImageDrawable(stationImagePlaceholder);
            }

            // Make sure imageViewIcon is not clickable
            holder.imageViewIcon.setClickable(false);
            holder.imageViewIcon.setFocusable(false);

            if (prefs.getBoolean("compact_style", false))
                setupCompactStyle(holder);

            // Set up favorite button
            final boolean isInFavorites = favouriteManager.has(station.StationUuid);
            holder.buttonFavorite.setContentDescription(getContext().getApplicationContext().getString(isInFavorites ? R.string.detail_unstar : R.string.detail_star));
            holder.buttonFavorite.setImageResource(isInFavorites ? R.drawable.ic_heart_24dp : R.drawable.ic_heart_border_24dp);
            holder.buttonFavorite.setClickable(true);
            holder.buttonFavorite.setFocusable(true);

            // Enable favorite button click to toggle favourites without triggering card click
            holder.buttonFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("StationClick", "Favorite button clicked for station: " + station.Name);

                    if (favouriteManager.has(station.StationUuid)) {
                        StationActions.removeFromFavourites(getContext(), view, station);
                    } else {
                        StationActions.markAsFavourite(getContext(), station);
                    }

                    int position = holder.getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        notifyItemChanged(position);
                    }
                }
            });

        }


        final boolean isExpanded = holder.getBindingAdapterPosition() == expandedPosition;
        holder.textViewTags.setVisibility(isExpanded ? View.GONE : View.VISIBLE);

        holder.buttonMore.setImageResource(isExpanded ? R.drawable.ic_expand_less_black_24dp : R.drawable.ic_expand_more_black_24dp);
        holder.buttonMore.setContentDescription(getContext().getApplicationContext().getString(isExpanded ? R.string.image_button_less : R.string.image_button_more));


        holder.buttonMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("StationClick", "More button clicked for station: " + filteredStationsList.get(holder.getBindingAdapterPosition()).Name);
                // Notify prev item change
                if (expandedPosition != -1) {
                    notifyItemChanged(expandedPosition);
                }

                int position = holder.getBindingAdapterPosition();
                expandedPosition = isExpanded ? -1 : position;

                // Notify current item changed
                if (expandedPosition != -1) {
                    notifyItemChanged(expandedPosition);
                }
            }
        });

        TypedValue tv = new TypedValue();
        if (playingStationPosition == holder.getBindingAdapterPosition()) {
            getContext().getTheme().resolveAttribute(R.attr.colorAccentMy, tv, true);
            holder.textViewTitle.setTextColor(tv.data);
            holder.textViewTitle.setTypeface(null, Typeface.BOLD);
                    
            // Add background highlight for Android TV
            if (Utils.isRunningOnTV(getContext())) {
                holder.itemView.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.selected_state));
            }
        } else {
            getContext().getTheme().resolveAttribute(R.attr.boxBackgroundColor, tv, true);
            holder.textViewTitle.setTypeface(holder.textViewShortDescription.getTypeface());
            getContext().getTheme().resolveAttribute(R.attr.iconsInItemBackgroundColor, tv, true);
            holder.textViewTitle.setTextColor(tv.data);
            
            // Reset background for Android TV
            if (Utils.isRunningOnTV(getContext())) {
                getContext().getTheme().resolveAttribute(R.attr.selectableItemBackground, tv, true);
                holder.itemView.setBackgroundResource(tv.resourceId);
            }
        }

        holder.textViewTitle.setText(station.Name);
        holder.textViewShortDescription.setText(station.getShortDetails(getContext()));
        holder.textViewTags.setText(station.TagsAll.replace(",", ", "));


        if (prefs.getBoolean("click_trend_icon_visible", true)) {
            if (station.ClickTrend < 0) {
                holder.imageTrend.setImageResource(R.drawable.ic_trending_down_black_24dp);
                holder.imageTrend.setContentDescription(getContext().getString(R.string.icon_click_trend_decreasing));
            } else if (station.ClickTrend > 0) {
                holder.imageTrend.setImageResource(R.drawable.ic_trending_up_black_24dp);
                holder.imageTrend.setContentDescription(getContext().getString(R.string.icon_click_trend_increasing));
            } else {
                holder.imageTrend.setImageResource(R.drawable.ic_trending_flat_black_24dp);
                holder.imageTrend.setContentDescription(getContext().getString(R.string.icon_click_trend_stable));
            }
        } else {
            holder.imageTrend.setVisibility(View.GONE);
        }

        Drawable flag = CountryFlagsLoader.getInstance().getFlag(activity, station.CountryCode);

        if (flag != null) {
            float k = flag.getMinimumWidth() / (float) flag.getMinimumHeight();
            float viewHeight = holder.textViewShortDescription.getTextSize();
            flag.setBounds(0, 0, (int) (k * viewHeight), (int) viewHeight);
        }

        holder.textViewShortDescription.setCompoundDrawablesRelative(flag, null, null, null);

        if (isExpanded) {
            holder.viewDetails = holder.stubDetails == null ? holder.viewDetails : holder.stubDetails.inflate();
            holder.stubDetails = null;
            holder.viewTags = holder.viewDetails.findViewById(R.id.viewTags);
            holder.buttonVisitWebsite = holder.viewDetails.findViewById(R.id.buttonVisitWebsite);
            holder.buttonShare = holder.viewDetails.findViewById(R.id.buttonShare);
            holder.buttonBookmark = holder.viewDetails.findViewById(R.id.buttonBookmark);
            holder.buttonAddAlarm = holder.viewDetails.findViewById(R.id.buttonAddAlarm);
            holder.buttonCreateShortcut = holder.viewDetails.findViewById(R.id.buttonCreateShortcut);
            holder.buttonPlayInternalOrExternal = holder.viewDetails.findViewById(R.id.buttonPlayInNoNameRadio);

            holder.buttonVisitWebsite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    StationActions.openStationHomeUrl(activity, station);
                }
            });

            holder.buttonShare.setOnClickListener(view -> StationActions.share(activity, station));

            if (favouriteManager.has(station.StationUuid)) {
                // favorite stations should only be removed in the favorites view
                holder.buttonBookmark.setVisibility(View.GONE);
            } else {
                holder.buttonBookmark.setOnClickListener(view -> {
                    StationActions.markAsFavourite(getContext(), station);
                    int position1 = holder.getBindingAdapterPosition();
                    notifyItemChanged(position1);
                });
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
                    && getContext().getApplicationContext().getSystemService(ShortcutManager.class).isRequestPinShortcutSupported()) {
                holder.buttonCreateShortcut.setVisibility(View.VISIBLE);
                holder.buttonCreateShortcut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        station.prepareShortcut(getContext(), new CreatePinShortcutListener());
                    }
                });
            } else {
                holder.buttonCreateShortcut.setVisibility(View.INVISIBLE);
            }

            holder.buttonAddAlarm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    StationActions.setAsAlarm(activity, station);
                }
            });

            // External player functionality removed. Hide the button in expanded card.
            holder.buttonPlayInternalOrExternal.setVisibility(View.GONE);
            String[] tags = station.TagsAll.split(",");
            holder.viewTags.setTags(Arrays.asList(tags));
            holder.viewTags.setTagSelectionCallback(tagSelectionCallback);
        }
        if (holder.viewDetails != null)
            holder.viewDetails.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onViewRecycled(StationViewHolder holder) {
        super.onViewRecycled(holder);

        if (holder.imageViewIcon != null) {
            ImageLoader.cancelRequest(holder.imageViewIcon.getContext(), holder.imageViewIcon);
            holder.imageViewIcon.setImageDrawable(stationImagePlaceholder);
        }
    }

    @TargetApi(26)
    public class CreatePinShortcutListener implements DataRadioStation.ShortcutReadyListener {
        @Override
        public void onShortcutReadyListener(ShortcutInfo shortcut) {
            ShortcutManager shortcutManager = getContext().getApplicationContext().getSystemService(ShortcutManager.class);
            if (shortcutManager.isRequestPinShortcutSupported()) {
                shortcutManager.requestPinShortcut(shortcut, null);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (filteredStationsList != null) {
            return filteredStationsList.size();
        }
        return 0;
    }

    @Override
    public void onSwiped(StationViewHolder viewHolder, int direction) {
        stationActionsListener.onStationSwiped(filteredStationsList.get(viewHolder.getBindingAdapterPosition()));
    }

    @Override
    public void onDragged(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, double dX, double dY) {

    }

    @Override
    public void onMoved(StationViewHolder viewHolder, int from, int to) {
        stationActionsListener.onStationMoved(from, to);
        notifyItemMoved(from, to);
    }

    @Override
    public void onMoveEnded(StationViewHolder viewHolder) {
        stationActionsListener.onStationMoveFinished();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        EventBus.unregister(MetaUpdateEvent.class, metaUpdateListener);
        EventBus.unregister(RadioStationChangedEvent.class, stationChangedListener);
    }

    public StationsFilter getFilter() {
        if (filter == null) {
            filter = new StationsFilter(getContext(), filterType, new StationsFilter.DataProvider() {
                @Override
                public List<DataRadioStation> getOriginalStationList() {
                    return stationsList;
                }

                @Override
                public void notifyFilteredStationsChanged(StationsFilter.SearchStatus status, List<DataRadioStation> filteredStations) {
                    filteredStationsList = filteredStations;

                    notifyStationsChanged();

                    if (filterListener != null) {
                        filterListener.onSearchCompleted(status);
                    }
                }
            });
        }

        return filter;
    }

    Context getContext() {
        return activity;
    }


    void setupIcon(boolean useCircularIcons, ImageView imageView, ImageView transparentImageView) {
        if (useCircularIcons) {
            transparentImageView.setVisibility(View.VISIBLE);
            imageView.getLayoutParams().height = imageView.getLayoutParams().height = imageView.getLayoutParams().width;
            imageView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.black));
        }
    }

    private void setupCompactStyle(final StationViewHolder holder) {
        holder.layoutMain.setMinimumHeight((int) getContext().getResources().getDimension(R.dimen.compact_style_item_minimum_height));
        holder.frameLayout.getLayoutParams().width = (int) getContext().getResources().getDimension(R.dimen.compact_style_icon_container_width);
        holder.imageViewIcon.getLayoutParams().width = (int) getContext().getResources().getDimension(R.dimen.compact_style_icon_width);

        holder.textViewShortDescription.setVisibility(View.GONE);
        if (holder.transparentImageView.getVisibility() == View.VISIBLE) {
            holder.transparentImageView.getLayoutParams().height = (int) getContext().getResources().getDimension(R.dimen.compact_style_icon_height);
            holder.transparentImageView.getLayoutParams().width = (int) getContext().getResources().getDimension(R.dimen.compact_style_icon_width);
            holder.imageViewIcon.getLayoutParams().height = (int) getContext().getResources().getDimension(R.dimen.compact_style_icon_height);
        }
    }

    private void highlightCurrentStation() {
        if (filteredStationsList == null) return;

        int oldPlayingStationPosition = playingStationPosition;

        String currentStationUuid = MediaSessionUtil.getStationId();
        if (currentStationUuid == null || currentStationUuid.isEmpty()) {
            playingStationPosition = -1;
        } else {
            for (int i = 0; i < filteredStationsList.size(); i++) {
                if (filteredStationsList.get(i).StationUuid.equals(currentStationUuid)) {
                    playingStationPosition = i;
                    break;
                }
            }
        }
        
        if (playingStationPosition != oldPlayingStationPosition) {
            android.util.Log.d("ItemAdapterStation", "highlightCurrentStation: old=" + oldPlayingStationPosition + ", new=" + playingStationPosition);
            if (oldPlayingStationPosition > -1)
                notifyItemChanged(oldPlayingStationPosition);
            if (playingStationPosition > -1)
                notifyItemChanged(playingStationPosition);
        }
    }

    private void notifyChangedByStationUuid(String uuid) {
        // Use the position map for O(1) lookup instead of O(n) iteration
        Integer position = uuidToPositionMap.get(uuid);
        if (position != null) {
            notifyItemChanged(position);
        }
    }
}
