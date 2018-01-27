package com.ninetwozero.iksu.features.schedule.listing;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.common.ui.BaseListFragment;
import com.ninetwozero.iksu.features.schedule.detail.WorkoutDetailActivity;
import com.ninetwozero.iksu.features.schedule.detail.WorkoutDetailFragment;
import com.ninetwozero.iksu.features.schedule.shared.SimpleListItemDivider;
import com.ninetwozero.iksu.features.schedule.shared.SimpleWorkoutListHeader;
import com.ninetwozero.iksu.features.schedule.shared.SimpleWorkoutListItemAdapter;
import com.ninetwozero.iksu.features.schedule.shared.WorkoutListItem;
import com.ninetwozero.iksu.models.Workout;
import com.ninetwozero.iksu.network.IksuWorkoutService;
import com.ninetwozero.iksu.utils.Constants;
import com.ninetwozero.iksu.utils.DateUtils;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmResults;
import io.realm.Sort;

public class MonitoredWorkoutFragment extends BaseListFragment<WorkoutListItem, SimpleWorkoutListItemAdapter> {
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_monitored_workouts, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        setupToolbar(toolbar, getString(R.string.label_monitored_workouts), null);
    }

    @Override
    protected void reloadList() {
        getActivity().startService(
            IksuWorkoutService.newIntent(getActivity(), IksuWorkoutService.ACTION, true)
        );
    }

    @Override
    public int getDefaultEmptyTextResource() {
        return R.string.msg_no_monitored_workouts;
    }

    @Override
    protected RecyclerView.ItemDecoration createListItemDivider() {
        return new SimpleListItemDivider(getContext());
    }


    @Override
    protected void createAdapter() {
        adapter = new SimpleWorkoutListItemAdapter(
            getContext(),
            new WorkoutListCallbacks() {
                @Override
                public void onWorkoutClick(View view, Workout workout) {
                    startActivity(
                        new Intent(getContext(), WorkoutDetailActivity.class)
                            .putExtra(WorkoutDetailFragment.WORKOUT_ID, workout.getId())
                            .putExtra(WorkoutDetailFragment.WORKOUT_TITLE, workout.getTitle())
                    );
                }

                @Override
                public void onItemCountChanged(int count) {
                    setUiState(count > 0 ? STATE_NORMAL : STATE_EMPTY, R.string.msg_no_monitored_workouts);
                }
            },
            prepareListDataItems()
        );
    }

    @Override
    protected List<WorkoutListItem> prepareListDataItems() {
        final List<WorkoutListItem> listItems = new ArrayList<>();

        String date = "";
        final RealmResults<Workout> monitoredItems = loadMonitoredWorkoutsFromDatabase();
        for (Workout item : monitoredItems) {
            if (!date.equals(item.getStartDateString().substring(0, 10))) {
                date = item.getStartDateString().substring(0, 10);
                listItems.add(new SimpleWorkoutListHeader(DateUtils.getWeekday(getContext(), DateUtils.countDaysBetween(LocalDate.now(), LocalDate.parse(date)))));
            }
            listItems.add(item);
        }
        return listItems;
    }

    private RealmResults<Workout> loadMonitoredWorkoutsFromDatabase() {
        return realm.where(Workout.class)
            .equalTo(Constants.CONNECTED_ACCOUNT, IksuApp.getActiveUsername())
            .equalTo(Constants.MONITORING, true)
            .findAllSorted(Constants.START_DATE, Sort.ASCENDING);
    }
}