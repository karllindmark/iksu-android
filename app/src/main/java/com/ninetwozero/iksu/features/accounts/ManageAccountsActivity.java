package com.ninetwozero.iksu.features.accounts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.common.ui.BaseSecondaryActivity;
import com.ninetwozero.iksu.common.ui.QuickAndDirtyItemAnimator;
import com.ninetwozero.iksu.models.ApiSession;
import com.ninetwozero.iksu.models.UserAccount;
import com.ninetwozero.iksu.utils.Constants;

import butterknife.BindView;
import io.realm.OrderedRealmCollection;

import static com.ninetwozero.iksu.app.IksuApp.getContext;

public class ManageAccountsActivity extends BaseSecondaryActivity {
    public static final int REQUEST = 2001;
    public static final String HAS_CHANGES = "hasChanges";

    @BindView(R.id.recyclerview)
    protected RecyclerView recyclerView;
    @BindView(R.id.state_empty_view)
    protected View uiEmptyView;

    private AccountListAdapter adapter;
    private boolean hasChanges = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupAdapter();
        setupRecyclerView();
        setupToolbar(R.string.label_manage_accounts);
        refreshAccountList();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_manage_accounts;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST && resultCode == Activity.RESULT_OK) {
            refreshAccountList();
        }
    }

    @Override
    public void finish() {
        if (getCallingActivity() != null) {
            setResult(Activity.RESULT_OK, new Intent().putExtra(HAS_CHANGES, hasChanges));
        }
        super.finish();
    }

    @Override
    protected void setUiState(int state) {
        if (state == STATE_EMPTY) {
            recyclerView.setVisibility(View.GONE);
            uiEmptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            uiEmptyView.setVisibility(View.GONE);
        }
    }

    private void setupAdapter() {
        adapter = new AccountListAdapter(
                getContext(),
                new AccountListCallbacks() {
                    @Override
                    public void onAccountClick(UserAccount account) {
                    }

                    @Override
                    public void onAccountLongClick(final UserAccount account) {
                        final String username = account.getUsername();
                        new MaterialDialog.Builder(ManageAccountsActivity.this)
                            .title(getString(R.string.label_sign_out_title, username))
                            .negativeText(R.string.label_cancel)
                            .positiveText(R.string.label_sign_out)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    realm.beginTransaction();
                                    realm.where(ApiSession.class).equalTo(Constants.ID, account.getSessionId()).findAll().deleteAllFromRealm();
                                    realm.where(UserAccount.class).equalTo(Constants.USERNAME, account.getUsername()).findAll().deleteAllFromRealm();
                                    realm.commitTransaction();

                                    if (username.equals(IksuApp.getActiveUsername())) {
                                        sharedPreferences.edit().putString(Constants.CURRENT_USER, null).apply();
                                        IksuApp.setActiveUser(null);
                                        hasChanges = true;
                                    }

                                    refreshAccountList();
                                }
                            }).build().show();
                    }

                    @Override
                    public void onToggleAccountState(UserAccount account, boolean disabled) {
                        if (!disabled && account.isDisabled()) {
                            final Intent intent = new Intent(getApplicationContext(), LoginActivity.class).putExtra(LoginActivity.USERNAME, account.getUsername());
                            startActivityForResult(intent, REQUEST);
                            hasChanges = true;
                        }
                    }
                },
                null,
                false
        );
    }

    private OrderedRealmCollection<UserAccount> getDataFromDatabase() {
        return realm.where(UserAccount.class).findAllSorted(Constants.USERNAME);
    }

    private void setupRecyclerView() {
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setItemAnimator(new QuickAndDirtyItemAnimator());

        uiEmptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivityForResult(intent, REQUEST);
            }
        });
    }

    private void refreshAccountList() {
        final OrderedRealmCollection<UserAccount> newAccounts = getDataFromDatabase();
        adapter.updateData(newAccounts);
        setUiState(newAccounts.size() == 0 ? STATE_EMPTY : STATE_NORMAL);
    }
}
