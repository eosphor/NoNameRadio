package net.programmierecke.radiodroid2.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import net.programmierecke.radiodroid2.R;
import net.programmierecke.radiodroid2.Utils;

/**
 * Utility class for loading images using Glide
 * Replaces the deprecated Picasso implementation
 */
public class ImageLoader {

    /**
     * Interface for bitmap loading callbacks
     */
    public interface BitmapTarget {
        void onBitmapLoaded(Bitmap bitmap);
        void onBitmapFailed(Drawable errorDrawable);
        void onPrepareLoad(Drawable placeHolderDrawable);
    }

    /**
     * Load image into ImageView with standard options
     */
    public static void loadImage(Context context, String url, ImageView imageView) {
        Drawable placeholder = AppCompatResources.getDrawable(context, R.drawable.ic_launcher);
        RequestOptions options = createRequestOptions(context, placeholder, new CenterCrop());

        Glide.with(context)
                .load(url)
                .apply(options)
                .into(imageView);
    }

    /**
     * Load image into ImageView bypassing cache (for forced refresh)
     */
    public static void loadImageFresh(Context context, String url, ImageView imageView) {
        Drawable placeholder = AppCompatResources.getDrawable(context, R.drawable.ic_launcher);
        RequestOptions options = createRequestOptions(context, placeholder, new CenterCrop())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true);

        Glide.with(context)
                .load(url)
                .apply(options)
                .into(imageView);
    }

    /**
     * Load image into ImageView with transformations for radio station icons
     */
    public static void loadStationIcon(Context context, String url, ImageView imageView) {
        Drawable placeholder = AppCompatResources.getDrawable(context, R.drawable.ic_photo_24dp);
        loadStationIcon(context, url, imageView, placeholder);
    }

    public static void loadStationIcon(Context context, String url, ImageView imageView, @Nullable Drawable placeholder) {
        Drawable safePlaceholder = placeholder != null ? placeholder : AppCompatResources.getDrawable(context, R.drawable.ic_photo_24dp);
        RequestOptions options = createStationIconOptions(context, safePlaceholder);

        Glide.with(context)
                .load(url)
                .apply(options)
                .into(imageView);
    }

    /**
     * Load image with specific size and transformations for media browser
     */
    public static void loadStationIconForBrowser(Context context, String url, int size, boolean useCircular, BitmapTarget target) {
        Drawable placeholder = AppCompatResources.getDrawable(context, R.drawable.ic_photo_24dp);
        Transformation<Bitmap> transformation = useCircular
                ? new MultiTransformation<>(new CenterCrop(), new CircleCrop())
                : new MultiTransformation<>(new CenterCrop(), new RoundedCorners(dpToPx(context, 12)));

        RequestOptions options = createRequestOptions(context, placeholder, transformation)
                .override(size, size);

        Glide.with(context)
                .asBitmap()
                .load(url)
                .apply(options)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        target.onBitmapLoaded(resource);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        target.onBitmapFailed(errorDrawable);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        target.onPrepareLoad(placeholder);
                    }
                });
    }

    /**
     * Cancel image loading for a specific ImageView
     */
    public static void cancelRequest(Context context, ImageView imageView) {
        Glide.with(context).clear(imageView);
    }

    /**
     * Cancel image loading for a specific target
     */
    public static void cancelRequest(Context context, CustomTarget<?> target) {
        Glide.with(context).clear(target);
    }

    /**
     * Load station icon with offline-first strategy and fallback
     * Mimics Picasso's NetworkPolicy.OFFLINE behavior
     */
    public static void loadStationIconOfflineFirst(Context context, String url, ImageView imageView, Drawable placeholder) {
        final float px = android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP, 70, 
                context.getResources().getDisplayMetrics());

        RequestOptions baseOptions = createStationIconOptions(context, placeholder);

        RequestOptions offlineOptions = baseOptions.clone()
                .override((int) px, (int) px)  // Force square aspect ratio
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .onlyRetrieveFromCache(true);

        RequestOptions fallbackOptions = baseOptions.clone()
                .override((int) px, (int) px)  // Force square aspect ratio
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        // Create a fallback request using Glide's error() method
        com.bumptech.glide.RequestBuilder<Drawable> fallbackRequest = Glide.with(context)
                .load(url)
                .apply(fallbackOptions);

        // First try to load from cache only, with network fallback on error
        Glide.with(context)
                .load(url)
                .apply(offlineOptions)
                .error(fallbackRequest)
                .into(imageView);
}

    private static RequestOptions createStationIconOptions(Context context, Drawable placeholder) {
        if (Utils.useCircularIcons(context)) {
            return createRequestOptions(context, placeholder, new MultiTransformation<>(new CenterCrop(), new CircleCrop()));
        }

        return createRequestOptions(context, placeholder, new MultiTransformation<>(new CenterCrop(), new RoundedCorners(dpToPx(context, 12))));
    }

    private static RequestOptions createRequestOptions(Context context, Drawable placeholder, Transformation<Bitmap> transformation) {
        Drawable safePlaceholder = placeholder != null ? placeholder : AppCompatResources.getDrawable(context, R.drawable.ic_launcher);
        if (safePlaceholder == null) {
            safePlaceholder = ContextCompat.getDrawable(context, R.drawable.ic_launcher);
        }

        RequestOptions options = new RequestOptions()
                .placeholder(safePlaceholder)
                .error(safePlaceholder)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        if (transformation != null) {
            options = options.transform(transformation);
        }

        return options;
    }

    private static int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}
