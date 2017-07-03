package com.ninetwozero.iksu.models;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class UserAccount extends RealmObject {
    @PrimaryKey
    private String username;
    private String name;
    private ApiSession session;
    private boolean disabled;
    private String password;
    private boolean selected;

    public UserAccount() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ApiSession getSession() {
        return session;
    }

    public void setSession(ApiSession session) {
        this.session = session;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getSessionId() {
        return session == null ? null : session.getId();
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return "UserAccount{" +
            "username='" + username + '\'' +
            ", name='" + name + '\'' +
            ", session=" + session +
            ", disabled=" + disabled +
            ", password='" + password + '\'' +
            ", selected=" + selected +
            '}';
    }
}
