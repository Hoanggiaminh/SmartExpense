package com.example.smartexpense;

import java.util.Calendar;

public class CalendarDay {
    private Calendar date;
    private boolean isSelected;
    private boolean isFromPreviousMonth;
    private boolean isToday;
    private boolean isFutureDate;

    public CalendarDay(Calendar date, boolean isFromPreviousMonth) {
        this.date = date;
        this.isFromPreviousMonth = isFromPreviousMonth;
        this.isSelected = false;

        // Check if this day is today
        Calendar today = Calendar.getInstance();
        this.isToday = isSameDay(date, today);

        // Check if this day is in the future
        this.isFutureDate = date.after(today) && !isSameDay(date, today);
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isFromPreviousMonth() {
        return isFromPreviousMonth;
    }

    public void setFromPreviousMonth(boolean fromPreviousMonth) {
        isFromPreviousMonth = fromPreviousMonth;
    }

    public boolean isToday() {
        return isToday;
    }

    public void setToday(boolean today) {
        isToday = today;
    }

    public boolean isFutureDate() {
        return isFutureDate;
    }

    public void setFutureDate(boolean futureDate) {
        isFutureDate = futureDate;
    }

    public int getDayOfMonth() {
        return date.get(Calendar.DAY_OF_MONTH);
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
