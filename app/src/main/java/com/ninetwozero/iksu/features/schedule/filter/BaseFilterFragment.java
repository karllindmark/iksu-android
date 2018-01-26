package com.ninetwozero.iksu.features.schedule.filter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.common.ui.BaseListFragment;
import com.ninetwozero.iksu.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;


abstract public class BaseFilterFragment extends BaseListFragment<ScheduleFilterItem, ScheduleFilterAdapter> {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout.setEnabled(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.activity_schedule_filter, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_clear_filters) {
            List<ScheduleFilterItem> allFilters = adapter.getItems();
            List<String> filterIdsToRemove = new ArrayList<>();
            List<Integer> updatedAdapterIndices = new ArrayList<>();

            for (int i = 0, max = adapter.getItemCount(); i < max; i++) {
                if (allFilters.get(i).isEnabled()) {
                    filterIdsToRemove.add(allFilters.get(i).getId());
                    updatedAdapterIndices.add(i);
                }
                allFilters.get(i).setEnabled(false);
            }

            realm.beginTransaction();
            if (filterIdsToRemove.size() > 0) {
                realm.where(ScheduleFilterItem.class)
                        .equalTo(Constants.CONNECTED_ACCOUNT, IksuApp.getActiveUsername())
                        .in(Constants.ID, filterIdsToRemove.toArray(new String[]{}))
                        .findAll()
                        .deleteAllFromRealm();
            }
            realm.commitTransaction();

            if (updatedAdapterIndices.size() > 0) {
                for (int index : updatedAdapterIndices) {
                    adapter.notifyItemChanged(index);
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((ScheduleFilterActivity) getActivity()).setupToolbar(R.string.label_filter);
    }

    @Override
    protected void createAdapter() {
        adapter = new ScheduleFilterAdapter(getActivity(), prepareListDataItems(), getListSelectionCallback());
    }

    @Override
    protected RecyclerView.ItemDecoration createListItemDivider() {
        return null;
    }

    @Override
    protected void reloadList() {
        // NO-OP
    }

    protected FilterListCallbacks getListSelectionCallback() {
        return new FilterListCallbacks() {
            @Override
            public void onFilterClick(final View view, final ScheduleFilterItem filter) {
                final CheckBox checkBox = view.findViewById(R.id.checkbox);
                final boolean checked = !checkBox.isChecked();
                checkBox.setChecked(checked);
                filter.setEnabled(checked);

                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        if (checked) {
                            realm.insertOrUpdate(filter);
                        } else {
                            realm.where(ScheduleFilterItem.class)
                                .equalTo(Constants.CONNECTED_ACCOUNT, IksuApp.getActiveUsername())
                                .equalTo(Constants.ID, filter.getId())
                                .findAll()
                                .deleteAllFromRealm();
                        }
                    }
                });
            }
        };
    }

    protected Map<String, ScheduleFilterItem> getActiveFilterItems(final String account, final Integer... filterType) {
        final Map<String, ScheduleFilterItem> items = new HashMap<>();

        final RealmResults<ScheduleFilterItem> activeFilters = realm.where(ScheduleFilterItem.class).equalTo(Constants.CONNECTED_ACCOUNT, account).in("type", filterType).findAll();
        for (ScheduleFilterItem item : activeFilters) {
            items.put(item.getId(), item);
        }
        return items;
    }

    protected abstract List<ScheduleFilterItem> prepareListDataItems();
}

