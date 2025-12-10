package com.example.smartexpense;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AddTransactionActivity extends AppCompatActivity {

    private ImageView btnBack;
    private MaterialCardView cardAddIncome;
    private MaterialCardView cardAddExpense;
    private FloatingActionButton fabAdd;
    private RecyclerView rvLatestEntries;

    private ImageView iconIncome, iconExpense;
    private TextView txtIncome, txtExpense;

    // Bottom navigation views
    private LinearLayout navHome, navTransactions, navStatistics, navProfile;
    private ImageView navHomeIcon, navTransactionsIcon, navStatisticsIcon, navProfileIcon;
    private TextView navHomeText, navTransactionsText, navStatisticsText, navProfileText;

    private boolean isIncomeSelected = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        initViews();
        setupToolbar();
        setupClickListeners();
        setupBottomNavigation();
        setupBackPress();
        updateTabSelection();
        loadLatestEntries();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        cardAddIncome = findViewById(R.id.cardAddIncome);
        cardAddExpense = findViewById(R.id.cardAddExpense);
        fabAdd = findViewById(R.id.fabAdd);
        rvLatestEntries = findViewById(R.id.rvLatestEntries);

        iconIncome = findViewById(R.id.iconIncome);
        iconExpense = findViewById(R.id.iconExpense);
        txtIncome = findViewById(R.id.txtIncome);
        txtExpense = findViewById(R.id.txtExpense);

        // Bottom navigation
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

        rvLatestEntries.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Add");
        }
    }

    private void setupBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        cardAddIncome.setOnClickListener(v -> {
            if (!isIncomeSelected) {
                isIncomeSelected = true;
                updateTabSelection();
                loadLatestEntries();
            }
        });

        cardAddExpense.setOnClickListener(v -> {
            if (isIncomeSelected) {
                isIncomeSelected = false;
                updateTabSelection();
                loadLatestEntries();
            }
        });

        fabAdd.setOnClickListener(v -> {
            Intent intent;
            if (isIncomeSelected) {
                intent = new Intent(AddTransactionActivity.this, AddIncomeActivity.class);
            } else {
                intent = new Intent(AddTransactionActivity.this, AddExpenseActivity.class);
            }
            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        // Không highlight bất kỳ tab nào vì đây là trang Add riêng biệt
        updateBottomNavSelection(-1);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(AddTransactionActivity.this, MainActivity.class));
            finish();
        });

        navTransactions.setOnClickListener(v -> {
            startActivity(new Intent(AddTransactionActivity.this, CalendarActivity.class));
            finish();
        });

        navStatistics.setOnClickListener(v -> {
            startActivity(new Intent(AddTransactionActivity.this, StatisticsActivity.class));
            finish();
        });

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(AddTransactionActivity.this, ProfileActivity.class));
            finish();
        });
    }

    private void updateBottomNavSelection(int selectedTab) {
        int primaryColor = getResources().getColor(R.color.primary, getTheme());
        int secondaryColor = getResources().getColor(R.color.text_secondary, getTheme());

        // Reset all tabs
        navHomeIcon.setColorFilter(secondaryColor);
        navHomeText.setTextColor(secondaryColor);

        navTransactionsIcon.setColorFilter(secondaryColor);
        navTransactionsText.setTextColor(secondaryColor);

        navStatisticsIcon.setColorFilter(secondaryColor);
        navStatisticsText.setTextColor(secondaryColor);

        navProfileIcon.setColorFilter(secondaryColor);
        navProfileText.setTextColor(secondaryColor);

        // Highlight selected tab
        switch (selectedTab) {
            case 0: // Home
                navHomeIcon.setColorFilter(primaryColor);
                navHomeText.setTextColor(primaryColor);
                break;
            case 1: // Transactions
                navTransactionsIcon.setColorFilter(primaryColor);
                navTransactionsText.setTextColor(primaryColor);
                break;
            case 2: // Statistics
                navStatisticsIcon.setColorFilter(primaryColor);
                navStatisticsText.setTextColor(primaryColor);
                break;
            case 3: // Profile
                navProfileIcon.setColorFilter(primaryColor);
                navProfileText.setTextColor(primaryColor);
                break;
        }
    }

    private void updateTabSelection() {
        int primaryColor = getResources().getColor(R.color.primary, getTheme());
        int whiteColor = getResources().getColor(R.color.white, getTheme());
        int textPrimaryColor = getResources().getColor(R.color.text_primary, getTheme());

        if (isIncomeSelected) {
            cardAddIncome.setCardBackgroundColor(primaryColor);
            cardAddExpense.setCardBackgroundColor(whiteColor);

            iconIncome.setColorFilter(whiteColor);
            txtIncome.setTextColor(whiteColor);

            iconExpense.setColorFilter(textPrimaryColor);
            txtExpense.setTextColor(textPrimaryColor);
        } else {
            cardAddIncome.setCardBackgroundColor(whiteColor);
            cardAddExpense.setCardBackgroundColor(primaryColor);

            iconIncome.setColorFilter(textPrimaryColor);
            txtIncome.setTextColor(textPrimaryColor);

            iconExpense.setColorFilter(whiteColor);
            txtExpense.setTextColor(whiteColor);
        }
    }

    private void loadLatestEntries() {
        // TODO: Implement database query
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLatestEntries();
        updateBottomNavSelection(-1); // No tab selected for Add screen
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        return true;
    }
}
