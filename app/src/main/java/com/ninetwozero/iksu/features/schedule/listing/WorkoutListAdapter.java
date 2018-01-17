package com.ninetwozero.iksu.features.schedule.listing;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ninetwozero.iksu.BR;
import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.features.schedule.shared.WorkoutUiHelper;
import com.ninetwozero.iksu.models.Workout;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class WorkoutListAdapter extends RealmRecyclerViewAdapter<Workout, WorkoutListAdapter.ViewHolder> {
    protected final LayoutInflater layoutInflater;
    private final WorkoutListCallbacks listCallbacks;
    private final Context context;
    private final WorkoutUiHelper workoutUiHelper = new WorkoutUiHelper();

    public WorkoutListAdapter(Context context, WorkoutListCallbacks listCallbacks, @Nullable OrderedRealmCollection<Workout> data, boolean autoUpdate) {
        super(data, autoUpdate);

        this.context = context;
        this.listCallbacks = listCallbacks;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewDataBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_schedule_overview, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    public void updateData(@Nullable OrderedRealmCollection<Workout> data) {
        super.updateData(data);
        notifyDataSetChanged();
        this.listCallbacks.onItemCountChanged(data.size());
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        final ViewDataBinding binding;

        public ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            if (listCallbacks != null) {
                this.binding.setVariable(BR.handler, listCallbacks);
            }
        }

        void bind(final Workout workout) {
            this.binding.setVariable(BR.workout, workout);
            this.binding.setVariable(BR.statusTint, ContextCompat.getColor(context, workoutUiHelper.getColorForStatusBadge(workout)));
            this.binding.setVariable(BR.inactive, System.currentTimeMillis() > workout.getStartDate());
            this.binding.setVariable(BR.actionStringRes, workoutUiHelper.getActionTextForWorkout(context, workout));
            this.binding.executePendingBindings();
        }
    }
}