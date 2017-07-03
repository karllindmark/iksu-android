package com.ninetwozero.iksu.common.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.animation.DecelerateInterpolator;

public class QuickAndDirtyItemAnimator extends DefaultItemAnimator {
    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        runEnterAnimation(holder);
        return false;
    }

    @Override
    public boolean canReuseUpdatedViewHolder(@Nullable RecyclerView.ViewHolder viewHolder) {
        return true;
    }

    private void runEnterAnimation(final RecyclerView.ViewHolder holder) {
        holder.itemView.setTranslationX(-holder.itemView.getMeasuredWidth());
        holder.itemView.animate()
            .translationX(0)
            .setInterpolator(new DecelerateInterpolator(3.f))
            .setDuration(700)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    dispatchAddFinished(holder);
                }
            })
            .start();
    }
}
