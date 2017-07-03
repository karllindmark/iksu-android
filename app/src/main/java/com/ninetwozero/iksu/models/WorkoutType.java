package com.ninetwozero.iksu.models;

import com.squareup.moshi.Json;

public class WorkoutType {
    @Json(name = "ClassCode") private String id;
    @Json(name = "Description") private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
