package com.example.smartexpense.model;

import com.google.firebase.Timestamp;

public class TransactionItem {
    private String id;
    private String title;
    private String categoryName;
    private String categoryIcon;
    private double amount;
    private String type; // "income" hoặc "expense"
    private Timestamp date;
    private boolean showDateHeader;
    private String dateHeaderText;

    public TransactionItem() {}

    public TransactionItem(String id, String title, String categoryName, String categoryIcon,
                          double amount, String type, Timestamp date) {
        this.id = id;
        this.title = title;
        this.categoryName = categoryName;
        this.categoryIcon = categoryIcon;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.showDateHeader = false;
        this.dateHeaderText = "";
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getCategoryIcon() { return categoryIcon; }
    public void setCategoryIcon(String categoryIcon) { this.categoryIcon = categoryIcon; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }

    public boolean isShowDateHeader() { return showDateHeader; }
    public void setShowDateHeader(boolean showDateHeader) { this.showDateHeader = showDateHeader; }

    public String getDateHeaderText() { return dateHeaderText; }
    public void setDateHeaderText(String dateHeaderText) { this.dateHeaderText = dateHeaderText; }
}
