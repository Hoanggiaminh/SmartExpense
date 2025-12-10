package com.example.smartexpense.model;

import com.google.firebase.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class Transaction {
    private String id;
    private String categoryId;
    private String title;
    private double amount;
    private String type;         // income | expense
    private Timestamp date;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Transaction() {}

    public Transaction(String id, String categoryId, String title, double amount,
                       String type, Timestamp date) {
        this.id = id;
        this.categoryId = categoryId;
        this.title = title;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    // Getters
    public String getId() { return id; }

    public String getCategoryId() { return categoryId; }

    public String getTitle() { return title; }

    public double getAmount() { return amount; }

    public String getType() { return type; }

    public Timestamp getDate() { return date; }

    public Timestamp getCreatedAt() { return createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(String id) { this.id = id; }

    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public void setTitle(String title) { this.title = title; }

    public void setAmount(double amount) { this.amount = amount; }

    public void setType(String type) { this.type = type; }

    public void setDate(Timestamp date) { this.date = date; }

    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("categoryId", categoryId);
        map.put("title", title);
        map.put("amount", amount);
        map.put("type", type);
        map.put("date", date);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
}
