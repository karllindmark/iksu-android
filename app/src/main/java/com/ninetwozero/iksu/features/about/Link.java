package com.ninetwozero.iksu.features.about;

import android.support.annotation.DrawableRes;

public class Link {
    private static final int NO_IMAGE = -1;

    private @DrawableRes int icon;
    private String title;
    private String subtitle;
    private String url;

    public Link(final String title, final String subtitle, final String url) {
        this.title = title;
        this.subtitle = subtitle;
        this.url = url;
        this.icon = NO_IMAGE;
    }

    public Link(final int icon, final String title, final String subtitle, final String url) {
        this(title, subtitle, url);
        this.icon = icon;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public boolean hasIcon() {
        return icon != NO_IMAGE;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
