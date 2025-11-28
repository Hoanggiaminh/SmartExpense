package com.example.smartexpense;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public abstract class BaseActivity extends AppCompatActivity {

    // Bottom Navigation Views
    LinearLayout navHome, navTransactions, navStatistics, navProfile;
    ImageView navHomeIcon, navTransactionsIcon, navStatisticsIcon, navProfileIcon;
    TextView navHomeText, navTransactionsText, navStatisticsText, navProfileText;

    protected int currentTab = 0; // 0: Home, 1: Calendar, 2: Statistics, 3: Profile

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void initBottomNavigation() {
        // Initialize bottom navigation views
        navHome = findViewById(R.id.nav_home);
        navTransactions = findViewById(R.id.nav_transactions);
        navStatistics = findViewById(R.id.nav_statistics);
        navProfile = findViewById(R.id.nav_profile);

        navHomeIcon = findViewById(R.id.nav_home_icon);
        navTransactionsIcon = findViewById(R.id.nav_transactions_icon);
        navStatisticsIcon = findViewById(R.id.nav_statistics_icon);
        navProfileIcon = findViewById(R.id.nav_profile_icon);

        navHomeText = findViewById(R.id.nav_home_text);
        navTransactionsText = findViewById(R.id.nav_transactions_text);
        navStatisticsText = findViewById(R.id.nav_statistics_text);
        navProfileText = findViewById(R.id.nav_profile_text);

        setupBottomNavigationListeners();
    }

    private void setupBottomNavigationListeners() {
        // Bottom Navigation Click Listeners
        navHome.setOnClickListener(v -> {
            if (currentTab != 0) {
                navigateToTab(MainActivity.class, 0);
            }
        });

        navTransactions.setOnClickListener(v -> {
            if (currentTab != 1) {
                navigateToTab(CalendarActivity.class, 1);
            }
        });

        navStatistics.setOnClickListener(v -> {
            if (currentTab != 2) {
                navigateToTab(StatisticsActivity.class, 2);
            }
        });

        navProfile.setOnClickListener(v -> {
            if (currentTab != 3) {
                navigateToTab(ProfileActivity.class, 3);
            }
        });
    }

    private void navigateToTab(Class<?> activityClass, int tabIndex) {
        Intent intent = new Intent(this, activityClass);
        intent.putExtra("selectedTab", tabIndex);
        startActivity(intent);
        overridePendingTransition(0, 0); // No animation for smooth tab switching
        finish();
    }

    protected void updateTabState(int selectedTab) {
        currentTab = selectedTab;

        // Reset all tabs to inactive state
        resetAllTabs();

        // Set selected tab to active state
        switch (selectedTab) {
            case 0: // Home
                setTabActive(navHomeIcon, navHomeText);
                break;
            case 1: // Calendar
                setTabActive(navTransactionsIcon, navTransactionsText);
                break;
            case 2: // Statistics
                setTabActive(navStatisticsIcon, navStatisticsText);
                break;
            case 3: // Profile
                setTabActive(navProfileIcon, navProfileText);
                break;
        }
    }

    private void resetAllTabs() {
        // Set all tabs to inactive state (secondary color)
        setTabInactive(navHomeIcon, navHomeText);
        setTabInactive(navTransactionsIcon, navTransactionsText);
        setTabInactive(navStatisticsIcon, navStatisticsText);
        setTabInactive(navProfileIcon, navProfileText);
    }

    private void setTabActive(ImageView icon, TextView text) {
        // Set active state (primary color)
        icon.setColorFilter(ContextCompat.getColor(this, R.color.primary));
        text.setTextColor(ContextCompat.getColor(this, R.color.primary));
    }

    private void setTabInactive(ImageView icon, TextView text) {
        // Set inactive state (secondary color)
        icon.setColorFilter(ContextCompat.getColor(this, R.color.text_secondary));
        text.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
    }
}
