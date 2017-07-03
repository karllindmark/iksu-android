package com.ninetwozero.iksu.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ApiSession extends RealmObject {
    @PrimaryKey
    private String id;
    private long validFrom;

    public ApiSession() {}

    public ApiSession(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public long getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(long validFrom) {
        this.validFrom = validFrom;
    }
}
