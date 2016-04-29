package com.app_labs.fiveta.events;

/**
 * Gets the hour and minute from the picker
 */
public class GetTimePickedEvent {

    private int mHourOfDay;
    private int mMinute;

    public GetTimePickedEvent(int hourOfDay, int minute) {
        mHourOfDay = hourOfDay;
        mMinute = minute;
    }

    public int getHourOfDay() {
        return mHourOfDay;
    }

    public void setHourOfDay(int hourOfDay) {
        mHourOfDay = hourOfDay;
    }

    public int getMinute() {
        return mMinute;
    }

    public void setMinute(int minute) {
        mMinute = minute;
    }
}
