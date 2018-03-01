package com.ninetwozero.iksu.common;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.ninetwozero.iksu.features.schedule.shared.WorkoutListItem;

// Based on examples in https://medium.com/@kitek/recyclerview-swipe-to-delete-easier-than-you-thought-cff67ff5e5f6
public class SimpleRecyclerViewSwipeCallback extends ItemTouchHelper.SimpleCallback {
    public SimpleRecyclerViewSwipeCallback() {
        super(0, ItemTouchHelper.LEFT |  ItemTouchHelper.RIGHT );
    }

    @Override
    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (viewHolder.getItemViewType() == WorkoutListItem.HEADER) {
            // Don't allow swiping headers - we remove them when needed
            return 0;
        }
        return super.getSwipeDirs(recyclerView, viewHolder);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        // NO-OP
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        // NO-OP
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        // TODO?
    }
}
