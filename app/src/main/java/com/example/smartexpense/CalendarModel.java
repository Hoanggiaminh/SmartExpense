package com.example.smartexpense;

public class CalendarModel {
    private String dayName;   // VD: "Mo"
    private String dayNumber; // VD: "01"
    private long fullDate;    // Timestamp để xử lý logic sau này
    private boolean isSelected; // Trạng thái chọn

    public CalendarModel(String dayName, String dayNumber, long fullDate, boolean isSelected) {
        this.dayName = dayName;
        this.dayNumber = dayNumber;
        this.fullDate = fullDate;
        this.isSelected = isSelected;
    }

    public String getDayName() {
        return dayName;
    }

    public String getDayNumber() {
        return dayNumber;
    }

    public long getFullDate() {
        return fullDate;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}