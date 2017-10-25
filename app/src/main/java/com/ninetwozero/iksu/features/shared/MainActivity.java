package com.ninetwozero.iksu.features.shared;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.common.ui.BaseActivity;
import com.ninetwozero.iksu.features.about.AboutActivity;
import com.ninetwozero.iksu.features.accounts.LoginActivity;
import com.ninetwozero.iksu.features.accounts.ManageAccountsActivity;
import com.ninetwozero.iksu.features.schedule.listing.WeeklyScheduleFragment;
import com.ninetwozero.iksu.features.schedule.reservation.ReservationTabFragment;
import com.ninetwozero.iksu.models.ApiSession;
import com.ninetwozero.iksu.models.UserAccount;
import com.ninetwozero.iksu.network.IksuLoginService;
import com.ninetwozero.iksu.network.IksuWorkoutService;
import com.ninetwozero.iksu.utils.ApiHelper;
import com.ninetwozero.iksu.utils.Constants;
import com.ninetwozero.iksu.utils.DensityUtils;

import java.util.UUID;

import butterknife.BindView;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class MainActivity extends BaseActivity {
    private static final String PREF_USER_LEARNED_DRAWER = "user_has_opened_the_drawer";

    @BindView(R.id.drawer_layout)
    protected DrawerLayout drawerLayout;
    @BindView(R.id.navigation_view)
    protected NavigationView navigationView;

    private ActionBarDrawerToggle drawerToggle;
    private View navHeaderView;
    private ImageView navHeaderAvatarView;
    private TextView navHeaderNameView;
    private TextView navHeaderUsernameView;
    private View navHeaderDropdownIndicator;

    private boolean userLearnedDrawer;
    private boolean isShowingAccountSwitcher = false;

    private RealmResults<UserAccount> userAccounts; // Used to keep GC away
    private final RealmChangeListener<RealmResults<UserAccount>> sessionDbListener = new RealmChangeListener<RealmResults<UserAccount>>() {
        @Override
        public void onChange(RealmResults<UserAccount> accounts) {
            final Menu menu = navigationView.getMenu();
            menu.removeGroup(R.id.account_list);

            int i = 1001;
            for (UserAccount account : accounts) {
                if (!account.isSelected()) {
                    menu.add(R.id.account_list, i, Menu.NONE, account.getUsername()).setIcon(R.drawable.ic_account_circle_black_24dp);
                    i++;
                }
            }
            toggleAccountSwitcher(false);
            updateAccountBox();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findAllViews();
        setupNavigationDrawer();

        if (savedInstanceState == null) {
            goToFeature(getDefaultNavigationViewIdToCheck(), userLearnedDrawer);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onResume() {
        super.onResume();

        navigationView.getMenu().findItem(R.id.menu_reservations).setVisible(IksuApp.hasSelectedAccount());
        userAccounts = realm.where(UserAccount.class).findAllSortedAsync(Constants.USERNAME);
        userAccounts.addChangeListener(sessionDbListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (userAccounts != null) {
            userAccounts.removeChangeListener(sessionDbListener);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IksuLoginService.LOGIN_REQUESTED || (requestCode == ManageAccountsActivity.REQUEST) && data.getBooleanExtra(ManageAccountsActivity.HAS_CHANGES, false)) {
                updateAccountBox();
                goToFeature(R.id.menu_all_classes, true);
            }
        }
    }

    private void setupNavigationDrawer() {
        userLearnedDrawer = sharedPreferences.getBoolean(PREF_USER_LEARNED_DRAWER, false);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
                if (item.getGroupId() == R.id.account_list) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            changeUserAccount(String.valueOf(item.getTitle()));
                        }
                    }, 300);
                    return false;
                }

                if (item.getItemId() == R.id.kill_session && IksuApp.hasEnabledDeveloperMode()) {
                    Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(final Realm realm) {
                            final UserAccount tempAccount = realm.where(UserAccount.class).equalTo("username", IksuApp.getActiveUsername()).findFirst();
                            tempAccount.getSession().deleteFromRealm();

                            final ApiSession fakeSession = realm.createObject(ApiSession.class, UUID.randomUUID().toString());
                            tempAccount.setSession(fakeSession);
                            realm.insertOrUpdate(tempAccount);
                        }
                    });
                    return false;
                }
                return goToFeature(item.getItemId(), true);
            }
        });
        navigationView.getMenu().findItem(R.id.kill_session).setVisible(IksuApp.hasSelectedAccount() && IksuApp.hasEnabledDeveloperMode());

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.dummy_name, R.string.dummy_email) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                toggleAccountSwitcher(false);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                if (!userLearnedDrawer) {
                    userLearnedDrawer = true;
                    sharedPreferences.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }
                invalidateOptionsMenu();
            }
        };

        updateAccountBox();
        navigationView.getMenu().findItem(getDefaultNavigationViewIdToCheck()).setChecked(true);

        // Set the drawer toggle as the DrawerListener
        drawerLayout.addDrawerListener(drawerToggle);
        if (!userLearnedDrawer) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private int getDefaultNavigationViewIdToCheck() {
        return R.id.menu_all_classes;
    }

    private void changeUserAccount(final String username) {
        realm.executeTransactionAsync(
            new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    final UserAccount previousSelection = realm.where(UserAccount.class).equalTo("selected", true).findFirst();
                    if (previousSelection != null) {
                        previousSelection.setSelected(false);
                        realm.insertOrUpdate(previousSelection);
                    }

                    final UserAccount newSelection = realm.where(UserAccount.class).equalTo(Constants.USERNAME, username).findFirst();
                    if (newSelection != null) {
                        newSelection.setSelected(true);
                        realm.insertOrUpdate(newSelection);
                    }
                }
            },
            new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    sharedPreferences.edit().putString(Constants.CURRENT_USER, username).apply();
                    IksuApp.setActiveUser(realm.copyFromRealm(realm.where(UserAccount.class).equalTo(Constants.USERNAME, username).findFirst()));
                    updateAccountBox();
                    goToFeature(navigationView.getMenu().findItem(R.id.menu_reservations).isChecked() ? R.id.menu_reservations : R.id.menu_all_classes, true);
                }
            });
    }

    // FIXME: Can we ButterKnife these in some way?
    private void findAllViews() {
        navHeaderView = navigationView.getHeaderView(0);
        navHeaderAvatarView = (ImageView) navHeaderView.findViewById(R.id.avatar);
        navHeaderNameView = (TextView) navHeaderView.findViewById(R.id.name);
        navHeaderUsernameView = (TextView) navHeaderView.findViewById(R.id.username);
        navHeaderDropdownIndicator = navHeaderView.findViewById(R.id.indicator);
    }

    private void updateAccountBox() {
        if (IksuApp.hasSelectedAccount()) {
            int size = DensityUtils.toPixels(getResources().getDimension(R.dimen.navigation_drawer_main_avatar_size));
            Glide.with(this)
                .load(ApiHelper.buildGravatarUrl(IksuApp.getActiveAccount().getUsername(), size))
                .fallback(R.drawable.ic_account_circle_black_24dp)
                .error(R.drawable.ic_account_circle_black_24dp)
                .into(navHeaderAvatarView);
            navHeaderNameView.setText(IksuApp.getActiveAccount().getName());
            navHeaderUsernameView.setText(IksuApp.getActiveAccount().getUsername());
            navHeaderDropdownIndicator.setVisibility(View.VISIBLE);

            navHeaderView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isShowingAccountSwitcher = !isShowingAccountSwitcher;
                    toggleAccountSwitcher(isShowingAccountSwitcher);
                }
            });
        } else {
            navHeaderAvatarView.setImageResource(R.drawable.ic_fitness_center_black_24dp);
            navHeaderAvatarView.getDrawable().setTint(ContextCompat.getColor(this, R.color.colorAccent));

            navHeaderNameView.setText(R.string.label_not_signed_in);
            navHeaderUsernameView.setText(R.string.label_tap_to_sign_in);
            navHeaderDropdownIndicator.setVisibility(View.GONE);
            navHeaderView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(getApplicationContext(), LoginActivity.class), IksuLoginService.LOGIN_REQUESTED);
                }
            });
        }
    }

    private boolean goToFeature(final int menuItemId, boolean closeDrawer) {
        final Bundle data = new Bundle();

        Fragment fragment = null;
        Intent intent = null;
        int requestId = 0;

        switch (menuItemId) {
            case R.id.menu_reservations:
                fragment = new ReservationTabFragment();
                break;

            case R.id.menu_all_classes:
                fragment = new WeeklyScheduleFragment();
                data.putBoolean(IksuWorkoutService.ONLY_RELEVANT_TO_LOGIN, IksuApp.hasSelectedAccount());
                break;

            case R.id.menu_about:
                intent = new Intent(getApplicationContext(), AboutActivity.class);
                requestId = AboutActivity.REQUEST;
                break;

            case R.id.add_account:
                intent = new Intent(getApplicationContext(), LoginActivity.class);
                requestId = IksuLoginService.LOGIN_REQUESTED;
                toggleAccountSwitcher(false);
                break;

            case R.id.manage_accounts:
                intent = new Intent(getApplicationContext(), ManageAccountsActivity.class);
                requestId = ManageAccountsActivity.REQUEST;
                toggleAccountSwitcher(false);
                break;

            default:
                Toast.makeText(getApplicationContext(), getResources().getResourceName(menuItemId), Toast.LENGTH_SHORT).show();
        }

        if (fragment != null) {
            fragment.setArguments(data);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment).commit();
            drawerLayout.closeDrawer(GravityCompat.START, closeDrawer);
            return true;
        }

        if (intent != null) {
            startActivityForResult(intent, requestId);
        }
        return false;
    }

    private void toggleAccountSwitcher(final boolean show) {
        final Menu menu = navigationView.getMenu();
        menu.setGroupVisible(R.id.default_primary, !show);
        menu.setGroupVisible(R.id.default_secondary, !show);
        menu.setGroupVisible(R.id.account_list, show);
        menu.setGroupVisible(R.id.acccount_actions, show);

        if (!IksuApp.hasSelectedAccount()) {
            menu.findItem(R.id.menu_reservations).setVisible(false);
        }

        if (!IksuApp.hasEnabledDeveloperMode()) {
            menu.findItem(R.id.kill_session).setVisible(false);
        }
        navHeaderDropdownIndicator.animate().rotation(show ? -180f : 0f);
    }

}
