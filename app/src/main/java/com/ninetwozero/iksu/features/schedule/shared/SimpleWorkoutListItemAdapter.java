package com.ninetwozero.iksu.features.schedule.shared;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ninetwozero.iksu.BR;
import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.features.schedule.listing.WorkoutListCallbacks;
import com.ninetwozero.iksu.models.Workout;

import java.util.List;

public class SimpleWorkoutListItemAdapter extends RecyclerView.Adapter<SimpleWorkoutListItemAdapter.ViewHolder> {
    private final Context context;
    private List<WorkoutListItem> items;
    private LayoutInflater layoutInflater;
    private final WorkoutListCallbacks listCallbacks;
    private final WorkoutUiHelper workoutUiHelper = new WorkoutUiHelper();

    public SimpleWorkoutListItemAdapter(final Context context, final WorkoutListCallbacks callbacks, final List<WorkoutListItem> items) {
        this.context = context;
        this.items = items;
        this.listCallbacks = callbacks;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getItemType();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(DataBindingUtil.inflate(layoutInflater, viewType == WorkoutListItem.HEADER ? R.layout.list_section_row: R.layout.list_item_schedule_overview, parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public WorkoutListItem getItem(int position) {
        return items.get(position);
    }

    public void setItems(List<WorkoutListItem> items) {
        this.items = items;
        notifyDataSetChanged();
        listCallbacks.onItemCountChanged(items.size());
    }

    public void removeItemAt(int position) {
        items.remove(position);
        notifyItemRemoved(position);
        listCallbacks.onItemCountChanged(items.size());
    }

    public void removeItemRange(int startingPosition, int count) {
        for (int i = (startingPosition + count - 1); i >= startingPosition; i--) {
            items.remove(i);
        }
        notifyItemRangeRemoved(startingPosition, count);
        listCallbacks.onItemCountChanged(items.size());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
            private ViewDataBinding binding;

            public ViewHolder(ViewDataBinding binding, int type) {
                super(binding.getRoot());
                this.binding = binding;

                if (listCallbacks != null && type == WorkoutListItem.ITEM) {
                    this.binding.setVariable(com.ninetwozero.iksu.BR.handler, listCallbacks);
                }
            }

            public void bind(WorkoutListItem workoutListItem) {
                if (workoutListItem.getItemType() == WorkoutListItem.HEADER) {
                    bindHeader((SimpleWorkoutListHeader) workoutListItem);
                } else {
                    bindItemRow((Workout) workoutListItem);
                }
                binding.executePendingBindings();
            }

            private void bindHeader(SimpleWorkoutListHeader header) {
                binding.setVariable(BR.title, header.getTitle());
            }

            private void bindItemRow(Workout workout) {
                binding.setVariable(com.ninetwozero.iksu.BR.actionStringRes, workoutUiHelper.getActionTextForWorkout(context, workout));
                binding.setVariable(com.ninetwozero.iksu.BR.statusTint, ContextCompat.getColor(context, workoutUiHelper.getColorForStatusBadge(workout)));
                binding.setVariable(BR.workout, workout);
            }
        }
    }