package com.example.smartexpense.model;

import com.google.firebase.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class Category {
    private String id;
    private String name;
    private String icon;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Category() {
    }

    public Category(String id, String name, String icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    // Getters
    public String getId() { return id; }

    public String getName() { return name; }

    public String getIcon() { return icon; }

    public Timestamp getCreatedAt() { return createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(String id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    public void setIcon(String icon) { this.icon = icon; }

    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("icon", icon);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
}
