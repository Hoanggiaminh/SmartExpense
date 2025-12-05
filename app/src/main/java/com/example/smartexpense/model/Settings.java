package com.example.smartexpense.model;

import com.google.firebase.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class Settings {
    private String theme;        // light | dark | system
    private String language;     // vi | en
    private String currency;     // VND | USD | EUR
    private boolean notifications;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Settings() {}

    public Settings(String theme, String language, String currency, boolean notifications) {
        this.theme = theme;
        this.language = language;
        this.currency = currency;
        this.notifications = notifications;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    // Getters
    public String getTheme() { return theme; }

    public String getLanguage() { return language; }

    public String getCurrency() { return currency; }

    public boolean isNotifications() { return notifications; }

    public Timestamp getCreatedAt() { return createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }

    // Setters
    public void setTheme(String theme) { this.theme = theme; }

    public void setLanguage(String language) { this.language = language; }

    public void setCurrency(String currency) { this.currency = currency; }

    public void setNotifications(boolean notifications) { this.notifications = notifications; }

    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // Convert to Map
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("theme", theme);
        map.put("language", language);
        map.put("currency", currency);
        map.put("notifications", notifications);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
}
