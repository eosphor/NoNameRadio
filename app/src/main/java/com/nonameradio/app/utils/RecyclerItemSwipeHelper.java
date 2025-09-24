package com.nonameradio.app.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.mikepenz.iconics.IconicsColor;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.IconicsSize;
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;

import com.nonameradio.app.R;
import com.nonameradio.app.Utils;

public class RecyclerItemSwipeHelper<ViewHolderType extends SwipeableViewHolder> extends ItemTouchHelper.SimpleCallback {


    public interface SwipeCallback<ViewHolderType> {
        void onSwiped(ViewHolderType viewHolder, int direction);
    }

    private final SwipeCallback<ViewHolderType> swipeListener;
    private final boolean swipeToDeleteIsEnabled;
    private IconicsDrawable icon;
    private final ColorDrawable background;

    public RecyclerItemSwipeHelper(Context context, int dragDirs, int swipeDirs, SwipeCallback<ViewHolderType> swipeListener) {
        super(dragDirs, swipeDirs);
        this.swipeListener = swipeListener;
        swipeToDeleteIsEnabled = ((swipeDirs & ItemTouchHelper.LEFT) > 0) || ((swipeDirs & ItemTouchHelper.RIGHT) > 0);
        background = new ColorDrawable(Utils.themeAttributeToColor(R.attr.swipeDeleteBackgroundColor, context, Color.RED));
        if (swipeToDeleteIsEnabled) {
            icon = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_delete_sweep);
            icon.setSizeXPx((int)(48 * context.getResources().getDisplayMetrics().density));
            icon.setSizeYPx((int)(48 * context.getResources().getDisplayMetrics().density));
            android.content.res.ColorStateList colorList = android.content.res.ColorStateList.valueOf(Utils.themeAttributeToColor(R.attr.swipeDeleteIconColor, context, Color.WHITE));
            icon.setColorList(colorList);
        }
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (viewHolder != null) {
            final View foregroundView = ((SwipeableViewHolder) viewHolder).getForegroundView();
            getDefaultUIUtil().onSelected(foregroundView);
        }
    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
        final View foregroundView = ((SwipeableViewHolder) viewHolder).getForegroundView();
        getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
                actionState, isCurrentlyActive);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final View foregroundView = ((SwipeableViewHolder) viewHolder).getForegroundView();
        getDefaultUIUtil().clearView(foregroundView);
    }

    private void drawSwipeToDeleteBackground(Canvas c, View itemView, float dX, float dY) {
        int backgroundCornerOffset = 20;
        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + icon.getIntrinsicHeight();

        if (dX > 0) { // Swiping to the right
            int iconRight = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
            int iconLeft = itemView.getLeft() + iconMargin;

            int magicConstraint = (itemView.getLeft() + ((int) dX) < iconRight + iconMargin) ? (int)dX - icon.getIntrinsicWidth() - ( iconMargin * 2 ) : 0;
            iconLeft += magicConstraint;
            iconRight += magicConstraint;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getLeft(), itemView.getTop(),
                    itemView.getLeft() + ((int) dX),
                    itemView.getBottom());
        } else if (dX < 0) { // Swiping to the left
            int iconRight = itemView.getRight()- iconMargin;
            int iconLeft = itemView.getRight()- iconMargin - icon.getIntrinsicWidth();

            int magicConstraint = (itemView.getRight() + ((int) dX) > iconLeft - iconMargin) ? icon.getIntrinsicWidth() + ( iconMargin * 2 ) + (int)dX : 0;
            iconLeft += magicConstraint;
            iconRight += magicConstraint;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getRight(), itemView.getTop(),
                    itemView.getRight() + ((int) dX),
                    itemView.getBottom());
        } else { // view is unSwiped
            icon.setBounds(0, 0, 0, 0);
            background.setBounds(0, 0, 0, 0);
        }

        background.draw(c);
        icon.draw(c);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        final View foregroundView = ((SwipeableViewHolder) viewHolder).getForegroundView();

        if (swipeToDeleteIsEnabled) {
            drawSwipeToDeleteBackground(c, viewHolder.itemView, dX, dY);
        }

        getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
                actionState, isCurrentlyActive);

    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        @SuppressWarnings("unchecked")
        ViewHolderType viewHolderType = (ViewHolderType) viewHolder;
        swipeListener.onSwiped(viewHolderType, direction);
    }

    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        // Effectively disable flinging because it's too easy to accidentally perform it.
        return 1;
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        // Since flinging is disabled we reduce swipe threshold.
        return 0.35f;
    }
}
