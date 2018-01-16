package com.ninetwozero.iksu.features.schedule.reservation;

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
import com.ninetwozero.iksu.features.schedule.shared.WorkoutUiHelper;
import com.ninetwozero.iksu.models.Workout;

import java.util.List;

public class ReservationListAdapter extends RecyclerView.Adapter<ReservationListAdapter.ViewHolder> {
    private final Context context;
    private List<ReservationListItem> items;
    private LayoutInflater layoutInflater;
    private final WorkoutListCallbacks listCallbacks;
    private final WorkoutUiHelper workoutUiHelper = new WorkoutUiHelper();

    public ReservationListAdapter(final Context context, final WorkoutListCallbacks callbacks, final List<ReservationListItem> items) {
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
        return new ViewHolder(DataBindingUtil.inflate(layoutInflater, viewType == ReservationListItem.HEADER ? R.layout.list_section_row: R.layout.list_item_reservation, parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<ReservationListItem> items) {
        this.items = items;
        notifyItemRangeChanged(0, items.size());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
            private ViewDataBinding binding;

            public ViewHolder(ViewDataBinding binding, int type) {
                super(binding.getRoot());
                this.binding = binding;

                if (listCallbacks != null && type == ReservationListItem.RESERVATION) {
                    this.binding.setVariable(com.ninetwozero.iksu.BR.handler, listCallbacks);
                }
            }

            public void bind(ReservationListItem reservationListItem) {
                if (reservationListItem.getItemType() == ReservationListItem.HEADER) {
                    bindHeader((ReservationListHeader) reservationListItem);
                } else {
                    bindReservationRow((Workout) reservationListItem);
                }
                binding.executePendingBindings();
            }

            private void bindHeader(ReservationListHeader header) {
                binding.setVariable(BR.title, header.getTitle());
            }

            private void bindReservationRow(Workout reservation) {
                binding.setVariable(com.ninetwozero.iksu.BR.actionStringRes, workoutUiHelper.getActionTextForWorkout(context, reservation));
                binding.setVariable(com.ninetwozero.iksu.BR.statusTint, ContextCompat.getColor(context, workoutUiHelper.getColorForStatusBadge(reservation)));
                binding.setVariable(BR.workout, reservation);
            }
        }
    }