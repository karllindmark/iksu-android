package com.ninetwozero.iksu.features.schedule.filter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ninetwozero.iksu.BR;
import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.features.schedule.shared.WorkoutUiHelper;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ScheduleFilterAdapter extends RecyclerView.Adapter<ScheduleFilterAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter{
    private final LayoutInflater layoutInflater;
    private final FilterListCallbacks callbacks;
    private final List<ScheduleFilterItem> items;
    private final List<String> itemTitles = new ArrayList<>();

    private final WorkoutUiHelper workoutUiHelper = new WorkoutUiHelper();

    public ScheduleFilterAdapter(Context context, @Nullable List<ScheduleFilterItem> items, FilterListCallbacks callbacks) {
        this.callbacks = callbacks;
        this.layoutInflater = LayoutInflater.from(context);
        this.items = items;

        if (items != null) {
            for (ScheduleFilterItem filter : items) {
                if (filter.getType() == ScheduleFilterItem.ROW_FILTER_INSTRUCTOR) {
                    this.itemTitles.add(filter.getExtra());
                } else {
                    this.itemTitles.add(context.getString(workoutUiHelper.getTitleForFilter(filter.getId(), filter.getType())));
                }
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(DataBindingUtil.inflate(layoutInflater, R.layout.list_item_schedule_filter, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public List<ScheduleFilterItem> getItems() {
        return items;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        if (Character.isDigit(itemTitles.get(position).charAt(0))) {
            return "#";
        }
        return itemTitles.get(position).substring(0, 1);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ViewDataBinding binding;
        private final Context context;

        ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.context = binding.getRoot().getContext();

            this.binding.setVariable(BR.helper, workoutUiHelper);
            this.binding.setVariable(BR.handler, callbacks);
        }

        public void bind(final int position) {
            binding.setVariable(BR.filter, items.get(position));

            if (items.get(position).getType() == ScheduleFilterItem.ROW_FILTER_INSTRUCTOR) {
                binding.setVariable(BR.title, items.get(position).getExtra());
            } else {
                binding.setVariable(BR.title, context.getString(workoutUiHelper.getTitleForFilter(items.get(position).getId(), items.get(position).getType())));
            }
            this.binding.executePendingBindings();
        }
    }
}
