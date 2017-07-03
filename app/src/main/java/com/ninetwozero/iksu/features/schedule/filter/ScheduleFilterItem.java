package com.ninetwozero.iksu.features.schedule.filter;

import io.realm.RealmObject;

public class ScheduleFilterItem extends RealmObject {
    public static final int ROW_HEADING = 0;
    public static final int ROW_FILTER_TYPE = 1;
    public static final int ROW_FILTER_TIME_OF_DAY = 2;
    public static final int ROW_FILTER_LOCATION = 3;
    public static final int ROW_FILTER_INSTRUCTOR = 4;

    private String id;
    private int type;
    private boolean enabled;
    private String connectedAccount;
    private String extra;

    public ScheduleFilterItem() {}

    public ScheduleFilterItem(final String id, final int type) {
        this.id = id;
        this.type = type;
        this.connectedAccount = null;
    }

    public ScheduleFilterItem(final String id, final int type, final boolean enabled, final String connectedAccount) {
        this.id = id;
        this.type = type;
        this.enabled = enabled;
        this.connectedAccount = connectedAccount;
    }

    public ScheduleFilterItem(final String id, final int type, final boolean enabled, final String connectedAccount, final String extra) {
        this(id, type, enabled, connectedAccount);
        this.extra = extra;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getConnectedAccount() {
        return connectedAccount;
    }

    public void setConnectedAccount(String connectedAccount) {
        this.connectedAccount = connectedAccount;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    @Override
    public String toString() {
        return "ScheduleFilterItem{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", enabled=" + enabled +
                ", connectedAccount='" + connectedAccount + '\'' +
                ", extra='" + extra + '\'' +
                '}';
    }
}
