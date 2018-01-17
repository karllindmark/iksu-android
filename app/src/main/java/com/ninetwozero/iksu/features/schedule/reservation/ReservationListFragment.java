package com.ninetwozero.iksu.features.schedule.reservation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.common.ui.BaseListFragment;
import com.ninetwozero.iksu.features.schedule.detail.WorkoutDetailActivity;
import com.ninetwozero.iksu.features.schedule.detail.WorkoutDetailFragment;
import com.ninetwozero.iksu.features.schedule.listing.WorkoutListCallbacks;
import com.ninetwozero.iksu.models.Workout;
import com.ninetwozero.iksu.network.IksuWorkoutService;
import com.ninetwozero.iksu.utils.Constants;
import com.ninetwozero.iksu.utils.DateUtils;
import com.ninetwozero.iksu.utils.WorkoutServiceHelper;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public class ReservationListFragment extends BaseListFragment<ReservationListItem, ReservationListAdapter> {
    private static final String ONLY_COMPLETED = "showOnlyCompleted";

    private boolean shouldOnlyShowCompleted;
    private WorkoutBroadcastReceiver workoutReceiver = new WorkoutBroadcastReceiver();

    public static ReservationListFragment newInstance(final boolean shouldOnlyShowCompleted) {
        final Bundle args = new Bundle();
        args.putBoolean(ONLY_COMPLETED, shouldOnlyShowCompleted);

        final ReservationListFragment fragment = new ReservationListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.shouldOnlyShowCompleted = getArguments().getBoolean(ONLY_COMPLETED, shouldOnlyShowCompleted);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(workoutReceiver, new IntentFilter(IksuWorkoutService.ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(workoutReceiver);
    }

    @Override
    protected void createAdapter() {
        final List<ReservationListItem> items = getReservationsAsPreparedList(shouldOnlyShowCompleted);
        adapter = new ReservationListAdapter(
            getContext(),
            new WorkoutListCallbacks() {
                @Override
                public void onWorkoutClick(View view, Workout workout) {
                    // TODO: Refresh when we come back from the detail view? Fake news if we don't remove "unreserved" workuouts
                    startActivity(
                        new Intent(getContext(), WorkoutDetailActivity.class)
                            .putExtra(WorkoutDetailFragment.WORKOUT_ID, workout.getId())
                            .putExtra(WorkoutDetailFragment.WORKOUT_TITLE, workout.getTitle())
                    );
                }

                @Override
                public void onItemCountChanged(int count) {
                    setUiState(count > 0 ? STATE_NORMAL : STATE_EMPTY, R.string.msg_no_reservations);
                }
            },
            items
        );
    }

    @Override
    protected RecyclerView.ItemDecoration createListItemDivider() {
        return new ReservationListItemDivider(getContext());
    }

    @Override
    protected void reloadList() {
        getActivity().startService(IksuWorkoutService.newIntent(getContext(), IksuWorkoutService.ACTION, true));
    }

    private List<ReservationListItem> getReservationsAsPreparedList(final boolean showCompleted) {
        final List<ReservationListItem> listItems = new ArrayList<>();

        final RealmQuery<Workout> query = realm.where(Workout.class)
            .equalTo(Constants.CONNECTED_ACCOUNT, IksuApp.getActiveUsername())
            .notEqualTo(Constants.RESERVATION_ID, 0);

        final long now = DateUtils.nowInMillis();
        if (showCompleted) {
            query.lessThan(Constants.END_DATE, now);
        } else {
            query.greaterThan(Constants.START_DATE, now);
        }

        String date = "";
        final RealmResults<Workout> reservations = query.findAllSorted(Constants.START_DATE_STRING, Sort.ASCENDING);
        for (Workout reservation : reservations) {
            if (!date.equals(reservation.getStartDateString().substring(0, 10))) {
                date = reservation.getStartDateString().substring(0, 10);
                listItems.add(new ReservationListHeader(DateUtils.getWeekday(getContext(), DateUtils.countDaysBetween(LocalDate.now(), LocalDate.parse(date)))));
            }
            listItems.add(reservation);
        }
        return listItems;
    }

    @Override
    public int getDefaultEmptyTextResource() {
        return R.string.msg_no_reservations;
    }

    private class WorkoutBroadcastReceiver extends BroadcastReceiver {
        private WorkoutBroadcastReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(IksuWorkoutService.ACTION)) {
                if (intent.hasExtra(IksuWorkoutService.STATUS)) {
                    final int count = intent.getIntExtra(IksuWorkoutService.STATUS, WorkoutServiceHelper.RESULT_ERROR);
                    if (count > 0) {
                        final List<ReservationListItem> reservations = getReservationsAsPreparedList(shouldOnlyShowCompleted);
                        adapter.setItems(reservations);
                    } else {
                        setUiState(STATE_EMPTY, R.string.msg_no_reservations);
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        }
    }
}
