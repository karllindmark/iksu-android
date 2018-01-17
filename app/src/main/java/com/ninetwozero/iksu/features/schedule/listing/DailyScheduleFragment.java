package com.ninetwozero.iksu.features.schedule.listing;

import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.common.ui.BaseListFragment;
import com.ninetwozero.iksu.common.ui.DefaultMaterialHorizontalDivider;
import com.ninetwozero.iksu.features.schedule.detail.WorkoutDetailActivity;
import com.ninetwozero.iksu.features.schedule.detail.WorkoutDetailFragment;
import com.ninetwozero.iksu.features.schedule.filter.ScheduleFilterActivity;
import com.ninetwozero.iksu.features.schedule.filter.ScheduleFilterItem;
import com.ninetwozero.iksu.models.Workout;
import com.ninetwozero.iksu.network.IksuWorkoutService;
import com.ninetwozero.iksu.utils.Constants;
import com.ninetwozero.iksu.utils.WorkoutServiceHelper;

import io.realm.OrderedRealmCollection;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public class DailyScheduleFragment extends BaseListFragment<Workout, WorkoutListAdapter> {
    public static final String USE_FILTER_SETTINGS = "useFilterSettings";
    public static final String SHOW_LOADING = "showLoading";
    public static final String DATE = "date";

    private static final int REQUEST_CODE_FILTER = 4001;

    private String date = null;

    private boolean shouldOnlyLoadWorkoutsRelevantToAccount;
    private boolean shouldUseFilterSettings = true;
    private boolean hasAppliedDataFilters;

    private final WorkoutBroadcastReceiver workoutReceiver = new WorkoutBroadcastReceiver();

    public static DailyScheduleFragment newInstance(String date, boolean shouldOnlyLoadWorkoutsRelevantToAccount, boolean shouldUseFilterSettings) {
        final Bundle arguments = new Bundle();
        arguments.putString(DATE, date);
        arguments.putBoolean(IksuWorkoutService.ONLY_RELEVANT_TO_LOGIN, shouldOnlyLoadWorkoutsRelevantToAccount);
        arguments.putBoolean(USE_FILTER_SETTINGS, shouldUseFilterSettings);

        final DailyScheduleFragment fragment = new DailyScheduleFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (sharedPreferences.contains(IksuApp.getLatestRefreshKey())) {
            setUiState(adapter.getItemCount() == 0 ? STATE_LOADING : STATE_NORMAL, hasAppliedDataFilters ? R.string.msg_no_workouts_with_filter : R.string.msg_no_workouts);

            // Pre-secroll the list to the first "reservable" class
            for (int i = 0, max = adapter.getItemCount(); i < max; i++) {
                if (adapter.getItem(i).isOpenForReservations()) {
                    recyclerView.scrollToPosition(i);
                    break;
                }
            }
        } else {
            setUiState(STATE_LOADING, R.string.msg_loading_workouts);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(workoutReceiver, new IntentFilter(IksuWorkoutService.ACTION));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_daily_schedule, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_filter) {
            startActivityForResult(new Intent(getContext(), ScheduleFilterActivity.class).putExtra("", ""), REQUEST_CODE_FILTER);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(workoutReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FILTER) {
            final Fragment fragment = getParentFragment();
            if (fragment == null) {
                reconfigureListDataSource();
            } else {
                if (fragment instanceof WeeklyScheduleFragment) {
                    ((WeeklyScheduleFragment) fragment).reconfigureAllLists();
                }
            }
        }
    }

    @Override
    protected void bootstrap(final Bundle state) {
        final Bundle arguments = getArguments();
        if (arguments != null) {
            this.shouldOnlyLoadWorkoutsRelevantToAccount = arguments.getBoolean(IksuWorkoutService.ONLY_RELEVANT_TO_LOGIN, false);
            this.shouldUseFilterSettings = arguments.getBoolean(USE_FILTER_SETTINGS, false);
            this.date = arguments.getString(DATE, "");
        }
    }

    @Override
    protected void createAdapter() {
        final OrderedRealmCollection<Workout> workouts = loadWorkoutsFromDatabase(shouldOnlyLoadWorkoutsRelevantToAccount, shouldUseFilterSettings);
        if (workouts.isEmpty()) {
            setUiState(STATE_LOADING);
        }

        adapter = new WorkoutListAdapter(getContext(), new ListHandler(), workouts,true);
    }

    @Override
    protected RecyclerView.ItemDecoration createListItemDivider() {
        return new DefaultMaterialHorizontalDivider(getContext(), DividerItemDecoration.VERTICAL);
    }

    @Override
    public void reloadList() {
        getActivity().startService(IksuWorkoutService.newIntent(getActivity(), IksuWorkoutService.ACTION, shouldOnlyLoadWorkoutsRelevantToAccount));
    }

    @Override
    public void reconfigureListDataSource() {
        adapter.updateData(loadWorkoutsFromDatabase(shouldOnlyLoadWorkoutsRelevantToAccount, shouldUseFilterSettings));
    }

    private OrderedRealmCollection<Workout> loadWorkoutsFromDatabase(final boolean fromConnectedAccount, final boolean useFilters) {
        final String connectedAccount = fromConnectedAccount ? IksuApp.getActiveUsername() : null;
        RealmQuery<Workout> query = realm.where(Workout.class)
                .equalTo(Constants.CONNECTED_ACCOUNT, connectedAccount)
                .beginsWith(Constants.START_DATE_STRING, date);

        if (useFilters) {
            final RealmResults<ScheduleFilterItem> activeFilters = realm.where(ScheduleFilterItem.class)
                    .equalTo(Constants.CONNECTED_ACCOUNT, connectedAccount)
                    .findAllSorted(Constants.TYPE, Sort.ASCENDING);

            if (activeFilters.size() > 0) {
                hasAppliedDataFilters = true;

                query.beginGroup();

                int lastFilterType = -1;
                for (int i = 0, nestedIndex = 0, max = activeFilters.size(); i < max; i++, nestedIndex++) {
                    if (lastFilterType == activeFilters.get(i).getType()) {
                        if (nestedIndex > 0) {
                            query.or();
                        }
                    } else {
                        nestedIndex = 0;
                        if (i > 0) {
                            query.endGroup();
                            query.beginGroup();
                        }
                    }

                    switch (activeFilters.get(i).getType()) {
                        case ScheduleFilterItem.ROW_FILTER_TYPE:
                            query.equalTo(Constants.TYPE, activeFilters.get(i).getId());
                            break;
                        case ScheduleFilterItem.ROW_FILTER_LOCATION:
                            query.equalTo("facilityId", activeFilters.get(i).getExtra());
                            break;
                        case ScheduleFilterItem.ROW_FILTER_INSTRUCTOR:
                            query.equalTo("instructorKey", activeFilters.get(i).getId()).or().equalTo("coInstructorKey", activeFilters.get(i).getId());
                            break;
                        case ScheduleFilterItem.ROW_FILTER_TIME_OF_DAY:
                            query.beginsWith(Constants.START_DATE_STRING, activeFilters.get(i).getExtra());
                            break;
                    }

                    lastFilterType = activeFilters.get(i).getType();
                }
                query.endGroup();
            } else {
                hasAppliedDataFilters = false;
            }
        }
        return query.findAllSorted(Constants.START_DATE_STRING, Sort.ASCENDING);
    }

    public class ListHandler implements WorkoutListCallbacks {
        public void onWorkoutClick(final View view, final Workout workout) {
            final Intent intent = new Intent(getContext(), WorkoutDetailActivity.class);
            intent.putExtra(WorkoutDetailFragment.WORKOUT_ID, workout.getId());
            intent.putExtra(WorkoutDetailFragment.WORKOUT_TITLE, workout.getTitle());

            if (!workout.isOpenForReservations() && workout.getReservationId() == 0) {
                startActivity(intent);
                return;
            }

            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
        }

        @Override
        public void onItemCountChanged(int count) {
            setUiState(
                count == 0 ? STATE_EMPTY : STATE_NORMAL,
                hasAppliedDataFilters ? R.string.msg_no_workouts_with_filter : R.string.msg_no_workouts
            );
        }
    }

    private class WorkoutBroadcastReceiver extends BroadcastReceiver {
        private WorkoutBroadcastReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(IksuWorkoutService.ACTION)) {
                if (intent.hasExtra(IksuWorkoutService.LOADING)) {
                    setUiState(STATE_LOADING, 0, adapter.getItemCount() == 0);
                } else {
                    final int count = intent.getIntExtra(IksuWorkoutService.STATUS, WorkoutServiceHelper.RESULT_ERROR);
                    if (count == -1) {
                        Snackbar.make(getView(), R.string.msg_general_error, Snackbar.LENGTH_SHORT).show();
                    }
                    setUiState(adapter.getItemCount() == 0 ? STATE_EMPTY : STATE_NORMAL, hasAppliedDataFilters ? R.string.msg_no_workouts_with_filter : R.string.msg_no_workouts);
                }
            }
        }
    }
}
