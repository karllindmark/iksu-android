package com.ninetwozero.iksu.features.schedule.reservation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.view.View;

import com.ninetwozero.iksu.utils.DensityUtils;

class ReservationListItemDivider extends ItemDecoration {
    private static final int[] ATTRS = new int[]{ android.R.attr.listDivider };
    private Drawable divider;
    private final Rect bounds = new Rect();

    public ReservationListItemDivider(Context context) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        divider = a.getDrawable(0);
        a.recycle();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(0, 0, 0, divider.getIntrinsicHeight());
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        if (parent.getLayoutManager() == null) {
            return;
        }

        canvas.save();
        final int left;
        final int right;
        if (parent.getClipToPadding()) {
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
            canvas.clipRect(left, parent.getPaddingTop(), right,
                    parent.getHeight() - parent.getPaddingBottom());
        } else {
            left = DensityUtils.toPixels(72);
            right = parent.getWidth();
        }

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            if (shouldDrawDivider(parent, child)) {
                parent.getDecoratedBoundsWithMargins(child, bounds);
                final int bottom = bounds.bottom + Math.round(ViewCompat.getTranslationY(child));
                final int top = bottom - divider.getIntrinsicHeight();
                divider.setBounds(left, top, right, bottom);
                divider.draw(canvas);
            }
        }
        canvas.restore();
    }

    private boolean shouldDrawDivider(final RecyclerView parent, View child) {
        final int current = parent.getChildAdapterPosition(child);
        return (
            parent.getAdapter().getItemViewType(current) == ReservationListItem.RESERVATION &&
            parent.getAdapter().getItemCount() > (current+1) &&
            parent.getAdapter().getItemViewType(current + 1) == ReservationListItem.RESERVATION
        );
    }
}
