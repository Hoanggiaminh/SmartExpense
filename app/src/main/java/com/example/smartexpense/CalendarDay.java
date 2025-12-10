package com.example.smartexpense;

import java.util.Calendar;

public class CalendarDay {
    private Calendar date;
    private boolean isOtherMonth;

    public CalendarDay(Calendar date, boolean isOtherMonth) {
        this.date = date;
        this.isOtherMonth = isOtherMonth;
    }

    public Calendar getDate() {
        return date;
    }

    public boolean isOtherMonth() {
        return isOtherMonth;
    }
}
