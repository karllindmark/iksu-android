package com.ninetwozero.iksu.features.debug;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ninetwozero.iksu.BuildConfig;
import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.common.ui.BaseSecondaryActivity;
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

        if (item.getItemId() == R.id.menu_reset_api_token) {
            sharedPreferences.edit().remove(Constants.API_TOKEN).remove(Constants.API_TOKEN_EXPIRATION).apply();
            recyclerView.swapAdapter(createAdapter(), true);
        }
        return super.onOptionsItemSelected(item);
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
}
