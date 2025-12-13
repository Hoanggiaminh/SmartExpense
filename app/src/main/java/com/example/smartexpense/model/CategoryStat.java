package com.example.smartexpense.model;

public class CategoryStat {
    private String categoryId;
    private String categoryName;
    private String icon;
    private String type;
    private double amount;
    private double percentage;

    public CategoryStat() {}

    public CategoryStat(String categoryId, String categoryName, String icon, String type, double amount, double percentage) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.icon = icon;
        this.type = type;
        this.amount = amount;
        this.percentage = percentage;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}

