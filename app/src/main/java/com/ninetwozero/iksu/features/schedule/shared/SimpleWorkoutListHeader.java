package com.ninetwozero.iksu.features.schedule.shared;

public class SimpleWorkoutListHeader implements WorkoutListItem {
    private String title;

    public SimpleWorkoutListHeader(final String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int getItemType() {
        return HEADER;
    }
}
