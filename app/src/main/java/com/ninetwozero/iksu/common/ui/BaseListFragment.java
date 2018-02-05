package com.ninetwozero.iksu.common.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ninetwozero.iksu.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public abstract class BaseListFragment<D, T extends RecyclerView.Adapter> extends BaseFragment {
    @BindView(R.id.swipeRefreshLayout)
    protected SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerview)
    protected RecyclerView recyclerView;
    @BindView(R.id.state_empty_view)
    protected View uiStateEmpty;
    @BindView(R.id.empty_text)
    protected TextView uiStateEmptyText;
    @BindView(R.id.state_loading_view)
    protected View uiStateLoading;
    @BindView(R.id.loading_text)
    protected TextView uiStateLoadingText;
    
    protected T adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bootstrap(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(getLayoutResource(), container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        createAdapter();
        setupRecyclerView();
        setupSwipeRefreshLayout();
    }

    @Override
    public void onDestroy() {
        recyclerView.setAdapter(null);
        super.onDestroy();
    }

    @Override
    protected void setUiState(int state) {
        if (state == STATE_NORMAL) {
            uiStateEmpty.setVisibility(View.GONE);
            uiStateLoading.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else if (state == STATE_EMPTY) {
            uiStateEmpty.setVisibility(View.VISIBLE);
            uiStateLoading.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        } else {
            uiStateEmpty.setVisibility(View.GONE);
            uiStateLoading.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    protected void setUiState(final int state, final int message) {
        setUiState(state, message, true);
    }

    protected void setUiState(final int state, final int message, boolean fullscreen) {
        if (state == STATE_EMPTY) {
            uiStateLoading.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);

            uiStateEmpty.setVisibility(View.VISIBLE);
            if (message != 0) {
                uiStateEmptyText.setText(message);
            }
        } else if (state == STATE_LOADING) {
            if (fullscreen) {
                swipeRefreshLayout.setRefreshing(false);
                recyclerView.setVisibility(View.GONE);
                uiStateEmpty.setVisibility(View.GONE);

                uiStateLoading.setVisibility(View.VISIBLE);
                if (message != 0) {
                    uiStateLoadingText.setText(message);
                }
            } else {
                uiStateLoading.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(true);
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);

            uiStateEmpty.setVisibility(View.GONE);
            uiStateLoading.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    protected void bootstrap(final Bundle state) {
        // NO-OP
    }

    private void setupRecyclerView() {
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        final RecyclerView.ItemDecoration listItemDivider = createListItemDivider();
        if (listItemDivider != null) {
            recyclerView.addItemDecoration(listItemDivider);
        }
        recyclerView.setItemAnimator(new QuickAndDirtyItemAnimator());

        setUiState(adapter.getItemCount() > 0 ? STATE_NORMAL : STATE_EMPTY, getDefaultEmptyTextResource());
    }

    protected RecyclerView.ItemDecoration createListItemDivider() {
        return new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
    }

    public int getDefaultEmptyTextResource() {
        return R.string.msg_no_workouts;
    }

    private void setupSwipeRefreshLayout() {
        if (isRefreshEnabled()) {
            swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryLight, R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccent);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    reloadList();
                }
            });
            swipeRefreshLayout.setEnabled(true);
        } else {
            swipeRefreshLayout.setEnabled(false);
        }
    }

    protected boolean isRefreshEnabled() {
        return true;
    }

    protected int getLayoutResource() {
        return R.layout.fragment_base_list;
    }

    protected void reconfigureListDataSource(boolean newState) {
        // NO-OP
    }

    protected abstract void createAdapter();
    protected List<D> prepareListDataItems() {
        return new ArrayList<>();
    }
    protected abstract void reloadList();
}
