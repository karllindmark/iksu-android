package com.ninetwozero.iksu.features.debug;

import android.app.Dialog;
import android.app.job.JobScheduler;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ninetwozero.iksu.BuildConfig;
import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.common.NotificationHelper;
import com.ninetwozero.iksu.common.ui.BaseSecondaryActivity;
import com.ninetwozero.iksu.features.schedule.shared.IksuCheckinService;
import com.ninetwozero.iksu.features.schedule.shared.WorkoutMonitorHelper;
import com.ninetwozero.iksu.models.Workout;
import com.ninetwozero.iksu.utils.Constants;
import com.ninetwozero.iksu.utils.DateUtils;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.Sort;
import timber.log.Timber;


public class DebugActivity extends BaseSecondaryActivity {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEEE");
    @BindView(R.id.list)
    protected RecyclerView recyclerView;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_debug;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupToolbar(R.string.label_debug);
        recyclerView.setAdapter(createAdapter());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_debug, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_reset_api_token:
                sharedPreferences.edit().remove(Constants.API_TOKEN).remove(Constants.API_TOKEN_EXPIRATION).apply();
                recyclerView.swapAdapter(createAdapter(), true);
                return true;
            case R.id.menu_test_checkin:
                startCheckinTestFlow();
                return true;
            case R.id.menu_test_notifications:
                startNotificationTestingFlow();
                return true;
            case R.id.menu_test_monitoring:
                startMonitorTestingFlow();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startCheckinTestFlow() {
        final DebugWorkoutAdapter debugWorkoutAdapter = new DebugWorkoutAdapter(this, loadFromDatabase());
        final Dialog dialog = new MaterialDialog.Builder(this)
            .tag(getString(R.string.label_debug_test_checkin))
            .title(R.string.label_debug_test_checkin)
            .adapter(debugWorkoutAdapter, new LinearLayoutManager(this))
            .build();
        debugWorkoutAdapter.setDialog(dialog);
        dialog.show();
    }

    private void startNotificationTestingFlow() {
        RealmResults<Workout> results = realm.where(Workout.class).findAllSorted(Constants.START_DATE, Sort.ASCENDING);
        int fromIndex = (int) Math.round(Math.random() * results.size());
        int toIndex = (int) Math.min(fromIndex + Math.round(Math.random() * results.size()), results.size());
        new NotificationHelper(getApplication()).notify(realm.copyFromRealm(results.subList(fromIndex, toIndex)));
    }

    // TODO: Let's actually extract this to one of those fancy test cases "soon"
    private void startMonitorTestingFlow() {
        final JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        final WorkoutMonitorHelper helper = new WorkoutMonitorHelper(getApplication());

        final List<Workout> workouts = realm.where(Workout.class)
            .greaterThan(Constants.START_DATE, System.currentTimeMillis())
            .findAllSorted(Constants.START_DATE, Sort.ASCENDING)
            .subList(0, 3);

        boolean singleStatus = startMonitoringOneWorkout(jobScheduler, helper, workouts.get(0));
        boolean multipleStatus = startMonitoringMultipleWorkouts(jobScheduler, helper, workouts);

        Snackbar.make(findViewById(android.R.id.content), "DONE (" + ((singleStatus && multipleStatus) ? "OK" : "NOK") + ")", Toast.LENGTH_SHORT).show();
    }

    private boolean startMonitoringOneWorkout(JobScheduler jobScheduler, WorkoutMonitorHelper helper, Workout workout) {
        int initialJobCount = jobScheduler.getAllPendingJobs().size();
        helper.schedule(workout);
        int jobCountAfterScheduling = jobScheduler.getAllPendingJobs().size();
        helper.unschedule(workout);
        int jobCountAfterUnscheduling = jobScheduler.getAllPendingJobs().size();

        Timber.i("Initial job count: %d", initialJobCount);
        Timber.i("Job count after scheduling workout: %d (expected %d)", jobCountAfterScheduling, initialJobCount + 2);
        Timber.i("Job count removing said workout: %d", jobCountAfterUnscheduling);

        return initialJobCount == jobCountAfterUnscheduling;
    }

    private boolean startMonitoringMultipleWorkouts(JobScheduler jobScheduler, WorkoutMonitorHelper helper, List<Workout> workouts) {
        int initialJobCount = jobScheduler.getAllPendingJobs().size();
        for (Workout workout : workouts) {
            helper.schedule(workout);
        }

        int jobCountAfterScheduling = jobScheduler.getAllPendingJobs().size();
        helper.unschedule(workouts);
        int jobCountAfterUnscheduling = jobScheduler.getAllPendingJobs().size();

        Timber.i("Initial job count: %d", initialJobCount);
        Timber.i("Job count after scheduling workout: %d (expected %d)", jobCountAfterScheduling, initialJobCount + 4);
        Timber.i("Job count removing said workout: %d", jobCountAfterUnscheduling);

        return initialJobCount == jobCountAfterUnscheduling;
    }

    private OrderedRealmCollection<Workout> loadFromDatabase() {
        try (Realm realm = Realm.getDefaultInstance()) {
            return realm.where(Workout.class)
                .equalTo(Constants.CONNECTED_ACCOUNT, IksuApp.getActiveUsername())
                .greaterThan(Constants.RESERVATION_ID, 0)
                .equalTo(Constants.CHECKED_IN, false)
                .greaterThan(Constants.START_DATE, System.currentTimeMillis())
                .findAllSorted(Constants.START_DATE, Sort.ASCENDING);
        }
    }

    private RecyclerView.Adapter createAdapter() {
        return new DebugAdapter(this, createItems());
    }


    private List<DebugItem> createItems() {
        long now = DateUtils.nowInMillis();
        long tokenExpiration = sharedPreferences.getLong(Constants.API_TOKEN_EXPIRATION, 0);

        List<DebugItem> items = new ArrayList<>();
        items.add(new DebugItem("API information", DebugItem.VIEW_TYPE_HEADING));
        if (sharedPreferences.contains(Constants.API_TOKEN)) {
            items.add(new DebugItem("<b>Token:</b>\n" + sharedPreferences.getString(Constants.API_TOKEN, "N/A")));
            items.add(new DebugItem("<b>Expires on:</b>\n" + printLongTimestamp(tokenExpiration)));
            items.add(new DebugItem("<b>Now:</b>\n" + printLongTimestamp(now)));
            items.add(new DebugItem("<b>Days left:</b>\n" + TimeUnit.MILLISECONDS.toDays(tokenExpiration - now)));
        } else {
            items.add(new DebugItem("No API credentials available</b>"));
        }

        if (IksuApp.hasSelectedAccount()) {
            items.add(new DebugItem("Current user", DebugItem.VIEW_TYPE_HEADING));
            items.add(new DebugItem("<b>Username:</b>\n" + IksuApp.getActiveUsername()));
            items.add(new DebugItem("<b>Session valid from:</b>\n" + printLongTimestamp(IksuApp.getActiveAccount().getSession().getValidFrom())));
        }

        items.add(new DebugItem("Other", DebugItem.VIEW_TYPE_HEADING));
        items.add(new DebugItem("<b>Internal version:</b>\n" + BuildConfig.VERSION_CODE));
        items.add(new DebugItem("<b>Build type:</b>\n" + BuildConfig.BUILD_TYPE));
        return items;
    }

    private String printLongTimestamp(long millis) {
        final LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.of("Europe/Stockholm"));
        return DateTimeFormatter.ISO_DATE_TIME.format(localDateTime) + " (" + millis + ")";
    }

    private class DebugAdapter extends RecyclerView.Adapter<ViewHolder> {
        private LayoutInflater layoutInflater;
        private List<DebugItem> items;

        public DebugAdapter(Context context, List<DebugItem> items) {
            this.layoutInflater = LayoutInflater.from(context);
            this.items = items;
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).type;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == DebugItem.VIEW_TYPE_HEADING) {
                return new ViewHolder(layoutInflater.inflate(R.layout.list_item_debug_heading, parent, false));
            }
            return new ViewHolder(layoutInflater.inflate(R.layout.list_item_debug_normal, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bindData(getItem(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private DebugItem getItem(int position) {
            return items.get(position);
        }
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text)
        protected TextView textView;
        
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindData(DebugItem data) {
            textView.setText(Html.fromHtml(data.text));
        }
    }

    private class DebugItem {
        public static final int VIEW_TYPE_NORMAL = 0;
        public static final int VIEW_TYPE_HEADING = 1;

        public final String text;
        public final int type;

        public DebugItem(String text) {
            this(text, VIEW_TYPE_NORMAL);
        }

        public DebugItem(String text, int type) {
            this.text = text;
            this.type = type;
        }
    }

    class DebugWorkoutAdapter extends RealmRecyclerViewAdapter<Workout, DebugWorkoutAdapter.ViewHolder> {
        private final LayoutInflater layoutInflater;
        private Dialog dialog;

        public DebugWorkoutAdapter(final Context context, @Nullable OrderedRealmCollection<Workout> data) {
            super(data, true);
            this.layoutInflater = LayoutInflater.from(context);
        }


        @Override
        public DebugWorkoutAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(layoutInflater.inflate(android.R.layout.two_line_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(DebugWorkoutAdapter.ViewHolder holder, int position) {
            holder.bind(getItem(position));
        }

        public void setDialog(Dialog dialog) {
            this.dialog = dialog;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(android.R.id.text1)
            TextView text1;
            @BindView(android.R.id.text2)
            TextView text2;
            public ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
                view.setClickable(true);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Workout workout = getItem(getAdapterPosition());

                        final Context context = v.getContext();
                        context.startService(IksuCheckinService.newInstance(context, workout.getPkId()));
                        Toast.makeText(context, "Trying to check-in " + workout.getTitle() + "...", Toast.LENGTH_SHORT).show();
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });
            }

            public void bind(Workout workout) {
                this.text1.setText(workout.getTitle());
                this.text2.setText(workout.getStartDateString());
            }
        }
    }
}
