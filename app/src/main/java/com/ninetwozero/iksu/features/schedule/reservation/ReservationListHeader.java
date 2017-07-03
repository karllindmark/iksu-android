package com.ninetwozero.iksu.features.schedule.reservation;

class ReservationListHeader implements ReservationListItem{
    private String title;

    public ReservationListHeader(final String title) {
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
