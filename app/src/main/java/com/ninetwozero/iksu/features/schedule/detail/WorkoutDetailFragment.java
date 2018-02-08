package com.ninetwozero.iksu.features.schedule.detail;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ninetwozero.iksu.BR;
import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.common.ui.BaseFragment;
import com.ninetwozero.iksu.features.accounts.LoginActivity;
import com.ninetwozero.iksu.features.schedule.listing.WorkoutListAdapter;
import com.ninetwozero.iksu.features.schedule.listing.WorkoutListCallbacks;
import com.ninetwozero.iksu.features.schedule.shared.IksuCheckinService;
import com.ninetwozero.iksu.features.schedule.shared.WorkoutMonitorHelper;
import com.ninetwozero.iksu.features.schedule.shared.WorkoutUiHelper;
import com.ninetwozero.iksu.models.Workout;
import com.ninetwozero.iksu.network.IksuLocationService;
import com.ninetwozero.iksu.network.IksuLoginService;
import com.ninetwozero.iksu.network.IksuReservationService;
import com.ninetwozero.iksu.network.IksuWorkoutService;
import com.ninetwozero.iksu.utils.ApiHelper;
import com.ninetwozero.iksu.utils.Constants;
import com.ninetwozero.iksu.utils.DateUtils;

import butterknife.BindView;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.Sort;
import pub.devrel.easypermissions.EasyPermissions;

import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.EXTRA_TEXT;
import static com.ninetwozero.iksu.network.IksuReservationService.ACTION_CANCEL;
import static com.ninetwozero.iksu.network.IksuReservationService.ACTION_CREATE;

public class WorkoutDetailFragment extends BaseFragment {
    public static final String WORKOUT_ID = "workoutId";
    public static final String WORKOUT_TITLE = "workoutTitle";

    private static final int REQUEST_NESTED = 1001;
    private static final int ACCESS_FINE_LOCATION_REQUEST = 1002;

    @BindView(R.id.toolbar)
    protected Toolbar toolbar;
    @BindView(R.id.nested_container)
    protected NestedScrollView scrollingContainer;
    @BindView(R.id.swipeRefreshLayout)
    protected SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.upcoming_classes)
    protected RecyclerView upcomingClassesList;
    @BindView(R.id.upcoming_classes_empty)
    protected View upcomingClassesEmptyView;

    private ViewDataBinding viewbinding;
    private WorkoutListAdapter upcomingClassesListAdapter;

    private Workout workout;
    private String workoutId;
    private String workoutTitle;

    private final ScheduleDetailHandler scheduleHandler = new ScheduleDetailHandler();
    private final WorkoutUiHelper workoutUiHelper = new WorkoutUiHelper();

    private final ReservationReceiver reservationReceiver = new ReservationReceiver();
    private final WorkoutReceiver workoutReceiver = new WorkoutReceiver();
    private final LocationCheckBroadcastReceiver locationCheckBroadcastReceiver = new LocationCheckBroadcastReceiver();
    private final CheckInBroadcastReceiver checkInBroadcastReceiver = new CheckInBroadcastReceiver();
    private final WorkoutChangeListener workoutChangeListener = new WorkoutChangeListener();

    public static WorkoutDetailFragment newInstance(final String workoutId, final String workoutTitle) {
        final Bundle args = new Bundle();
        args.putString(WORKOUT_ID, workoutId);
        args.putString(WORKOUT_TITLE, workoutTitle);

        final WorkoutDetailFragment fragment = new WorkoutDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        workoutId = getArguments().getString(WORKOUT_ID, "");
        workoutTitle = getArguments().getString(WORKOUT_TITLE, "");
        workout = loadWorkoutFromDatabase();
        workoutTitle = workout.getTitle();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        this.viewbinding = DataBindingUtil.inflate(inflater, R.layout.fragment_workout_detail, container, false);

        viewbinding.setVariable(BR.workout, workout);
        viewbinding.setVariable(BR.handler, scheduleHandler);
        viewbinding.setVariable(BR.helper, workoutUiHelper);
        viewbinding.setVariable(BR.actionStringRes, workoutUiHelper.getActionTextForWorkout(getContext(), workout, false));
        viewbinding.setVariable(BR.showSecondaryAction, workoutUiHelper.shouldShowTheSecondayAction(workout));
        viewbinding.setVariable(BR.secondaryActionStringRes, workoutUiHelper.getSecondaryActionTextForWorkout(workout));
        viewbinding.setVariable(BR.statusTint, ContextCompat.getColor(getContext(), workoutUiHelper.getColorForStatusBadge(workout)));
        viewbinding.executePendingBindings();

        return viewbinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSimilarClassesView();
        setupSwipeRefreshLayout();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.common_share, menu);

        final Intent intent = createShareIntent(workout);
        ((ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share))).setShareIntent(intent);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupToolbar(toolbar, workoutTitle, DateUtils.getLiteralDateFromDateTime(workout.getStartDateString()), R.drawable.ic_arrow_back_black_24dp);
    }

    @Override
    public void onResume() {
        super.onResume();
        addBroadcastReceivers();
        workout.addChangeListener(workoutChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        removeBroadcastReceivers();
        workout.removeChangeListener(workoutChangeListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IksuLoginService.LOGIN_REQUESTED && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(LoginActivity.STATUS, false)) {
                onUserLoggedInToReserve();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void setupSimilarClassesView() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setAutoMeasureEnabled(true);

        upcomingClassesListAdapter = new WorkoutListAdapter(
                getContext(),
                new SimilarSessionsListCallbacks(),
                loadUpcomingWorkoutsFromDatabase(),
                true
        );
        upcomingClassesList.setLayoutManager(layoutManager);
        upcomingClassesList.setAdapter(upcomingClassesListAdapter);

        toggleUpcomingClassesView(upcomingClassesListAdapter.getItemCount() > 0);
    }

    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryLight, R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getActivity().startService(IksuWorkoutService.newIntent(getActivity(), IksuWorkoutService.ACTION, workoutId));
            }
        });
    }

    private Workout loadWorkoutFromDatabase() {
        return realm.where(Workout.class)
                .equalTo(Constants.ID, workoutId)
                .equalTo(Constants.CONNECTED_ACCOUNT, IksuApp.getActiveUsername())
                .findFirst();
    }

    private OrderedRealmCollection<Workout> loadUpcomingWorkoutsFromDatabase() {
        return realm.where(Workout.class)
                .beginsWith(Constants.START_DATE_STRING, workout.getStartDateString().substring(0, 10))
                .equalTo(Constants.TYPE, workout.getType())
                .equalTo(Constants.CONNECTED_ACCOUNT, IksuApp.getActiveUsername())
                .notEqualTo(Constants.ID, workoutId)
                .findAllSorted(Constants.START_DATE, Sort.ASCENDING);
    }

    private void toggleUpcomingClassesView(final boolean hasClasses) {
        if (hasClasses) {
            upcomingClassesList.setVisibility(View.VISIBLE);
            upcomingClassesEmptyView.setVisibility(View.GONE);
        } else {
            upcomingClassesList.setVisibility(View.GONE);
            upcomingClassesEmptyView.setVisibility(View.VISIBLE);
        }
    }

    private void addBroadcastReceivers() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CANCEL);
        intentFilter.addAction(ACTION_CREATE);

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(reservationReceiver, intentFilter);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(workoutReceiver, new IntentFilter(IksuWorkoutService.ACTION));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(locationCheckBroadcastReceiver, new IntentFilter(IksuLocationService.ACTION_VALIDATE));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(checkInBroadcastReceiver, new IntentFilter(IksuCheckinService.ACTION));
    }

    private void removeBroadcastReceivers() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(reservationReceiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(workoutReceiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(locationCheckBroadcastReceiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(checkInBroadcastReceiver);
    }

    private void onUserLoggedInToReserve() {
        final OrderedRealmCollection<Workout> upcomingClasses = loadUpcomingWorkoutsFromDatabase();
        toggleUpcomingClassesView(upcomingClasses.size() > 0);
        upcomingClassesListAdapter.updateData(upcomingClasses);

        workout = loadWorkoutFromDatabase();
        onWorkoutChangedCallback(workout);

        if (workout.isOpenForReservations() && workout.getBookedSlotCount() < workout.getTotalSlotCount()) {
            if (IksuApp.hasSelectedAccount()) {
                startBookingFlow(workout);
            }
        }
    }

    private void onWorkoutChangedCallback(final Workout updatedWorkout) {
        if (!updatedWorkout.isValid()) {
            return;
        }

        viewbinding.setVariable(BR.workout, updatedWorkout);
        viewbinding.setVariable(BR.actionStringRes, workoutUiHelper.getActionTextForWorkout(getContext(), updatedWorkout, false));
        viewbinding.setVariable(BR.showSecondaryAction, workoutUiHelper.shouldShowTheSecondayAction(updatedWorkout));
        viewbinding.setVariable(BR.secondaryActionStringRes, workoutUiHelper.getSecondaryActionTextForWorkout(updatedWorkout));
        viewbinding.setVariable(BR.statusTint, ContextCompat.getColor(getContext(), workoutUiHelper.getColorForStatusBadge(updatedWorkout)));
        viewbinding.executePendingBindings();
        swipeRefreshLayout.setRefreshing(false);

        if (updatedWorkout.isMonitoring() && updatedWorkout.hasReservation()){
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    updatedWorkout.setMonitoring(false);
                }
            });
            new WorkoutMonitorHelper(getActivity()).unschedule(updatedWorkout);
        }
    }

    public class ScheduleDetailHandler {
        public void onPrimaryActionClick(final View view, final Workout workout) {
            if (workout.getReservationId() != 0) {
                if (workout.hasCheckedIn()) {
                    Snackbar.make(getView(), R.string.msg_already_checkedin, Snackbar.LENGTH_LONG).show();
                } else {
                    showCancellationDialog(workout);
                }
            } else if (workout.isDropin()) {
                Snackbar.make(getView(), R.string.msg_unable_to_reserve_dropin, Snackbar.LENGTH_LONG).show();
            } else if (workout.isOpenForReservations() && workout.getBookedSlotCount() < workout.getTotalSlotCount()) {
                if (IksuApp.hasSelectedAccount()){
                    startBookingFlow(workout);
                } else {
                    promptToSignIn(workout);
                }
            } else if (!workout.isOpenForReservations() || workout.getBookedSlotCount() >= workout.getTotalSlotCount()) {
                Snackbar.make(getView(), R.string.msg_unable_to_reserve_full, Snackbar.LENGTH_LONG).show();
            }
        }

        public void onSecondaryActionClick(final View view, final Workout workout) {
            final String text = ((Button) view).getText().toString();
            if (text.equals(getString(R.string.label_check_in_q))) {
                startCheckInFlow(workout);
            } else {
                toggleMonitoring(workout);
            }
        }

        private void startCheckInFlow(final Workout workout) {
            final Activity activity = getActivity();
            if (EasyPermissions.hasPermissions(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                activity.startService(IksuLocationService.newIntent(activity, workout));
                return;
            }

            EasyPermissions.requestPermissions(
                activity,
                activity.getString(R.string.msg_location_permission_rationale),
                ACCESS_FINE_LOCATION_REQUEST,
                Manifest.permission.ACCESS_FINE_LOCATION
            );
        }

        private void toggleMonitoring(final Workout workout) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    workout.setMonitoring(!workout.isMonitoring());
                    if (workout.isMonitoring()) {
                        scheduleMonitoring();
                    } else {
                        unscheduleMonitoring();
                    }
                    Snackbar.make(
                        getView(),
                        (workout.isMonitoring() ? "Start monitoring " : "Stop monitoring " ) + workout.getTitle(),
                        Toast.LENGTH_SHORT
                    ).show();
                }
            });

        }

        private void scheduleMonitoring() {
            new WorkoutMonitorHelper(getActivity()).schedule(workout);
        }

        private void unscheduleMonitoring() {
            new WorkoutMonitorHelper(getActivity()).unschedule(workout);
        }

        private void promptToSignIn(final Workout workout) {
            new MaterialDialog.Builder(getContext())
                    .title(R.string.msg_sign_in_dialog_title)
                    .content(R.string.msg_sign_in_dialog_content)
                    .negativeText(android.R.string.no)
                    .positiveText(android.R.string.yes)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            final Intent intent = new Intent(getContext(), LoginActivity.class).putExtra(Constants.WORKOUT_ID, workout.getId());
                            startActivityForResult(intent, IksuLoginService.LOGIN_REQUESTED);
                        }
                    })
                    .show();
        }

        private void showCancellationDialog(final Workout workout) {
            new MaterialDialog.Builder(getContext())
                    .title(R.string.msg_cancel_reservation_dialog_title)
                    .negativeText(android.R.string.no)
                    .positiveText(R.string.label_cancel_reservation)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            getContext().startService(IksuReservationService.newIntent(getContext(), ACTION_CANCEL, workout.getId()));
                        }
                    })
                    .show();
        }
    }

    private void startBookingFlow(final Workout workout) {
        new MaterialDialog.Builder(getContext())
                .title(R.string.msg_reserve_dialog_title)
                .content(R.string.msg_reserve_dialog_content)
                .negativeText(android.R.string.cancel)
                .positiveText(R.string.label_reserve)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        getContext().startService(IksuReservationService.newIntent(getContext(), ACTION_CREATE, workout.getId()));
                    }
                })
                .show();
    }

    private String createIksuUrl(Workout workout) {
        return "https://bokning.iksu.se/index.php?func=rd&id=" + workout.getId() + "&location=" + workout.getFacilityId();
    }

    private Intent createShareIntent(Workout workout) {
        return new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(EXTRA_TEXT, createIksuUrl(workout));
    }

    private class SimilarSessionsListCallbacks implements WorkoutListCallbacks {
        public void onWorkoutClick(final View view, final Workout workout) {
            final Intent intent = new Intent(getContext(), WorkoutDetailActivity.class);
            intent.putExtra(WorkoutDetailFragment.WORKOUT_ID, workout.getId());
            intent.putExtra(WorkoutDetailFragment.WORKOUT_TITLE, workout.getTitle());

            startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity()).toBundle());
        }

        @Override
        public void onItemCountChanged(int count) {
            // NO-OP
        }
    }

    private class WorkoutChangeListener implements RealmChangeListener<Workout> {
        @Override
        public void onChange(Workout updatedWorkout) {
            onWorkoutChangedCallback(updatedWorkout);
        }
    }

    class WorkoutReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final View view = getView();
            if (view == null) {
                return;
            }

            if (IksuWorkoutService.ACTION.equals(intent.getAction())) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    class ReservationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final View view = getView();
            if (view == null) {
                return;
            }

            final int status = intent.getIntExtra(IksuReservationService.STATUS, 0);

            final String action = intent.getAction();
            if (action.equals(IksuReservationService.ACTION_CREATE)) {
                if (status == IksuReservationService.RESULT_OK) {
                    Snackbar.make(view, R.string.msg_reservation_completed, Snackbar.LENGTH_LONG).show();
                } else if (status == IksuReservationService.RESULT_RESERVATION_ERROR) {
                    Snackbar.make(view, R.string.msg_reservation_error, Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(view, R.string.msg_general_error, Snackbar.LENGTH_LONG).show();
                }
            } else if (action.equals(IksuReservationService.ACTION_CANCEL)) {
                if (status == IksuReservationService.RESULT_OK) {
                    Snackbar.make(view, R.string.msg_cancel_reservation_ok, Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(view, R.string.msg_cancel_reservation_dialog_error_1, Snackbar.LENGTH_INDEFINITE).setAction(R.string.msg_cancel_reservation_dialog_error_2, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(ACTION_VIEW).setData(Uri.parse(Constants.IKSU_BOOKING_PORTAL_URL)));
                        }
                    }).setActionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccentLight)).show();
                }
            }
        }
    }

    private class LocationCheckBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (IksuLocationService.ACTION_VALIDATE.equals(intent.getAction())) {
                final int status = intent.getIntExtra(IksuLocationService.STATUS, IksuLocationService.STATUS_NONE);
                if (status == IksuLocationService.STATUS_VALID) {
                    getActivity().startService(IksuCheckinService.newInstance(context, workout.getPkId()));
                } else if (status == IksuLocationService.STATUS_INVALID) {
                    Snackbar.make(
                        getView(),
                        context.getString(R.string.msg_checkin_failed, workout.getFacility()),
                        Snackbar.LENGTH_LONG
                    ).show();
                } else {
                    Snackbar.make(getView(), R.string.msg_error_no_gps, Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

    private class CheckInBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (IksuCheckinService.ACTION.equals(intent.getAction())) {
                final int status = intent.getIntExtra(IksuCheckinService.STATUS, IksuCheckinService.STATUS_CHECKIN_FAILED);
                if (status == IksuCheckinService.STATUS_CHECKIN_OK) {
                    Snackbar.make(getView(), R.string.msg_checkin_ok, Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(
                        getView(),
                        ApiHelper.getStringResourceForErrorType(intent.getStringExtra(Constants.ERROR_KEY)),
                        Snackbar.LENGTH_LONG
                    ).show();
                }

            }
        }
    }
}
