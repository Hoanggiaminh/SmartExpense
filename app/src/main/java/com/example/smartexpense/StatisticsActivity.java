package com.example.smartexpense;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartexpense.adapters.CategoryStatAdapter;
import com.example.smartexpense.model.Category;
import com.example.smartexpense.model.CategoryStat;
import com.example.smartexpense.model.Transaction;
import com.example.smartexpense.services.FirebaseService;
import com.example.smartexpense.utils.CurrencyUtils;
import com.google.firebase.Timestamp;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsActivity extends BaseActivity {

    private TextView tvBalance, btnColumnChart, btnPieChart, tvDateRange;
    private ImageView btnPrevWeek, btnNextWeek;
    private LinearLayout chartContainer;
    private LinearLayout categoriesContainer;

    private FirebaseService firebaseService;

    private Calendar currentWeekStart;
    private List<Transaction> weekTransactions;
    private List<CategoryStat> categoryStats;
    private Map<String, Category> categoryMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistics);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Service
        firebaseService = FirebaseService.getInstance();

        // Initialize views
        initViews();

        // Initialize week to current week
        currentWeekStart = Calendar.getInstance();
        currentWeekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        currentWeekStart.set(Calendar.HOUR_OF_DAY, 0);
        currentWeekStart.set(Calendar.MINUTE, 0);
        currentWeekStart.set(Calendar.SECOND, 0);
        currentWeekStart.set(Calendar.MILLISECOND, 0);

        // Initialize data structures
        categoryStats = new ArrayList<>();
        categoryMap = new HashMap<>();

        // Setup click listeners
        setupClickListeners();

        // Load categories first, then load week data
        loadCategories();

        // Initialize bottom navigation
        initBottomNavigation();

        // Get selected tab from intent or default to 2 (Statistics)
        int selectedTab = getIntent().getIntExtra("selectedTab", 2);
        updateTabState(selectedTab);
    }

    private void initViews() {
        tvBalance = findViewById(R.id.tvBalance);
        btnColumnChart = findViewById(R.id.btnColumnChart);
        btnPieChart = findViewById(R.id.btnPieChart);
        tvDateRange = findViewById(R.id.tvDateRange);
        btnPrevWeek = findViewById(R.id.btnPrevWeek);
        btnNextWeek = findViewById(R.id.btnNextWeek);
        chartContainer = findViewById(R.id.chartContainer);
        categoriesContainer = findViewById(R.id.categoriesContainer);
    }

    private void setupClickListeners() {

        btnPrevWeek.setOnClickListener(v -> {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1);
            loadWeekData();
        });

        btnNextWeek.setOnClickListener(v -> {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1);
            loadWeekData();
        });

        btnColumnChart.setOnClickListener(v -> {
            // Already on column chart view
        });

        btnPieChart.setOnClickListener(v -> {
            Intent intent = new Intent(StatisticsActivity.this, PieChartStatisticsActivity.class);
            intent.putExtra("selectedTab", 2);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void loadCategories() {
        firebaseService.getAllUserCategories()
                .addOnSuccessListener(categories -> {
                    categoryMap.clear();
                    for (Category category : categories) {
                        categoryMap.put(category.getId(), category);
                    }
                    // Now load week data
                    loadWeekData();
                })
                .addOnFailureListener(e -> {
                    // Handle error, still try to load week data
                    loadWeekData();
                });
    }

    private void loadWeekData() {
        // Calculate week range
        Calendar weekEnd = (Calendar) currentWeekStart.clone();
        weekEnd.add(Calendar.DAY_OF_YEAR, 6);
        weekEnd.set(Calendar.HOUR_OF_DAY, 23);
        weekEnd.set(Calendar.MINUTE, 59);
        weekEnd.set(Calendar.SECOND, 59);

        // Update date range text
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));
        String dateRangeText = dateFormat.format(currentWeekStart.getTime()) + " - " + dateFormat.format(weekEnd.getTime());
        tvDateRange.setText(dateRangeText);

        // Query transactions for the week
        Timestamp startTimestamp = new Timestamp(currentWeekStart.getTime());
        Timestamp endTimestamp = new Timestamp(weekEnd.getTime());

        firebaseService.getTransactionsBetweenDates(startTimestamp, endTimestamp)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    weekTransactions = new ArrayList<>();
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        Transaction transaction = queryDocumentSnapshots.getDocuments().get(i).toObject(Transaction.class);
                        if (transaction != null) {
                            weekTransactions.add(transaction);
                        }
                    }

                    // Update UI with animation
                    updateUIWithAnimation();
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    weekTransactions = new ArrayList<>();
                    updateUIWithAnimation();
                });
    }

    private void updateBalance() {
        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction transaction : weekTransactions) {
            if ("income".equals(transaction.getType())) {
                totalIncome += transaction.getAmount();
            } else if ("expense".equals(transaction.getType())) {
                totalExpense += transaction.getAmount();
            }
        }

        double balance = totalIncome - totalExpense;

        String formattedBalance = CurrencyUtils.formatCurrency(balance);
        tvBalance.setText(formattedBalance);

        // Change color based on balance
        if (balance < 0) {
            tvBalance.setTextColor(ContextCompat.getColor(this, R.color.expense));
        } else {
            tvBalance.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        }
    }

    private void updateUIWithAnimation() {
        // Add fade out animation
        if (chartContainer != null) {
            chartContainer.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_out));
        }
        if (categoriesContainer != null) {
            categoriesContainer.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_out));
        }

        // Update data after a short delay
        chartContainer.postDelayed(() -> {
            updateBalance();
            updateChart();
            updateCategoryStats();

            // Add fade in animation
            if (chartContainer != null) {
                chartContainer.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in));
            }
            if (categoriesContainer != null) {
                categoriesContainer.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in));
            }
        }, 200);
    }

    private void updateChart() {
        chartContainer.removeAllViews();

        // Create data for each day of the week
        Map<String, Double> dailyIncome = new HashMap<>();
        Map<String, Double> dailyExpense = new HashMap<>();

        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        // Initialize all days with 0
        Calendar day = (Calendar) currentWeekStart.clone();
        for (int i = 0; i < 7; i++) {
            String dateKey = dayFormat.format(day.getTime());
            dailyIncome.put(dateKey, 0.0);
            dailyExpense.put(dateKey, 0.0);
            day.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Aggregate transactions by day
        for (Transaction transaction : weekTransactions) {
            String dateKey = dayFormat.format(transaction.getDate().toDate());
            if ("income".equals(transaction.getType())) {
                dailyIncome.put(dateKey, dailyIncome.getOrDefault(dateKey, 0.0) + transaction.getAmount());
            } else if ("expense".equals(transaction.getType())) {
                dailyExpense.put(dateKey, dailyExpense.getOrDefault(dateKey, 0.0) + transaction.getAmount());
            }
        }

        // Find max value for scaling
        double maxValue = 1000000; // Default minimum
        for (Double value : dailyIncome.values()) {
            maxValue = Math.max(maxValue, value);
        }
        for (Double value : dailyExpense.values()) {
            maxValue = Math.max(maxValue, value);
        }

        // Create bar chart for each day
        day = (Calendar) currentWeekStart.clone();
        SimpleDateFormat labelFormat = new SimpleDateFormat("dd/MM", new Locale("vi", "VN"));

        for (int i = 0; i < 7; i++) {
            String dateKey = dayFormat.format(day.getTime());
            double income = dailyIncome.get(dateKey);
            double expense = dailyExpense.get(dateKey);

            // Get day of week label
            String dayLabel = getDayOfWeekLabel(day.get(Calendar.DAY_OF_WEEK));
            String dateLabel = labelFormat.format(day.getTime());

            // Create bar group
            LinearLayout barGroup = createBarGroup(income, expense, maxValue, dayLabel, dateLabel);
            chartContainer.addView(barGroup);

            day.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private LinearLayout createBarGroup(double income, double expense, double maxValue, String dayLabel, String dateLabel) {
        LinearLayout barGroup = new LinearLayout(this);
        barGroup.setOrientation(LinearLayout.VERTICAL);
        barGroup.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(50),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
        barGroup.setLayoutParams(params);

        // Bar container
        LinearLayout barContainer = new LinearLayout(this);
        barContainer.setOrientation(LinearLayout.HORIZONTAL);
        barContainer.setGravity(Gravity.BOTTOM);
        LinearLayout.LayoutParams barContainerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(160)
        );
        barContainer.setLayoutParams(barContainerParams);

        // Income bar
        View incomeBar = new View(this);
        int incomeHeight = (int) ((income / maxValue) * dpToPx(150));
        LinearLayout.LayoutParams incomeParams = new LinearLayout.LayoutParams(
                0,
                Math.max(incomeHeight, dpToPx(2)),
                1
        );
        incomeParams.setMargins(0, 0, dpToPx(2), 0);
        incomeBar.setLayoutParams(incomeParams);
        incomeBar.setBackgroundColor(ContextCompat.getColor(this, R.color.income));

        // Expense bar
        View expenseBar = new View(this);
        int expenseHeight = (int) ((expense / maxValue) * dpToPx(150));
        LinearLayout.LayoutParams expenseParams = new LinearLayout.LayoutParams(
                0,
                Math.max(expenseHeight, dpToPx(2)),
                1
        );
        expenseParams.setMargins(dpToPx(2), 0, 0, 0);
        expenseBar.setLayoutParams(expenseParams);
        expenseBar.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));

        barContainer.addView(incomeBar);
        barContainer.addView(expenseBar);

        // Day label
        TextView tvDay = new TextView(this);
        tvDay.setText(dayLabel);
        tvDay.setTextSize(12);
        tvDay.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        tvDay.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams dayParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dayParams.setMargins(0, dpToPx(4), 0, 0);
        tvDay.setLayoutParams(dayParams);

        // Date label
        TextView tvDate = new TextView(this);
        tvDate.setText(dateLabel);
        tvDate.setTextSize(10);
        tvDate.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tvDate.setGravity(Gravity.CENTER);
        tvDate.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        barGroup.addView(barContainer);
        barGroup.addView(tvDay);
        barGroup.addView(tvDate);

        return barGroup;
    }

    private String getDayOfWeekLabel(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY: return "T2";
            case Calendar.TUESDAY: return "T3";
            case Calendar.WEDNESDAY: return "T4";
            case Calendar.THURSDAY: return "T5";
            case Calendar.FRIDAY: return "T6";
            case Calendar.SATURDAY: return "T7";
            case Calendar.SUNDAY: return "CN";
            default: return "";
        }
    }

    private void updateCategoryStats() {
        categoryStats.clear();

        Log.d("StatisticsActivity", "=== Starting updateCategoryStats ===");
        Log.d("StatisticsActivity", "Total transactions: " + weekTransactions.size());
        Log.d("StatisticsActivity", "Total categories in map: " + categoryMap.size());

        // Track income and expense separately for each category
        // Key format: "categoryId_type" (e.g., "cat123_income" or "cat123_expense")
        Map<String, Double> categoryTypeAmounts = new HashMap<>();
        double totalIncome = 0;
        double totalExpense = 0;

        // Calculate totals for each category by transaction type
        for (Transaction transaction : weekTransactions) {
            String categoryId = transaction.getCategoryId();
            double amount = transaction.getAmount();
            String transactionType = transaction.getType();

            Log.d("StatisticsActivity", "Transaction: categoryId=" + categoryId +
                  ", amount=" + amount + ", type=" + transactionType);

            // Create unique key for category + transaction type combination
            String key = categoryId + "_" + transactionType;

            Double currentAmount = categoryTypeAmounts.get(key);
            if (currentAmount == null) currentAmount = 0.0;
            categoryTypeAmounts.put(key, currentAmount + amount);

            // Track totals by type
            if ("income".equals(transactionType)) {
                totalIncome += amount;
            } else if ("expense".equals(transactionType)) {
                totalExpense += amount;
            }
        }

        Log.d("StatisticsActivity", "Total Income: " + totalIncome);
        Log.d("StatisticsActivity", "Total Expense: " + totalExpense);
        Log.d("StatisticsActivity", "Category-Type combinations: " + categoryTypeAmounts.size());

        // Create CategoryStat objects for all category-type combinations
        for (Map.Entry<String, Double> entry : categoryTypeAmounts.entrySet()) {
            String key = entry.getKey();
            double amount = entry.getValue();

            Log.d("StatisticsActivity", "Processing key: " + key + ", amount: " + amount);

            // Parse the key to get categoryId and type
            String[] parts = key.split("_");
            if (parts.length != 2) {
                Log.w("StatisticsActivity", "Invalid key format: " + key);
                continue;
            }

            String categoryId = parts[0];
            String transactionType = parts[1];

            Category category = categoryMap.get(categoryId);
            if (category == null) {
                Log.w("StatisticsActivity", "Category not found for id: " + categoryId);
                continue;
            }

            String categoryName = category.getName();
            String categoryIcon = category.getIcon();

            // Calculate percentage based on transaction type
            double percentage = 0;
            if ("income".equals(transactionType)) {
                percentage = totalIncome > 0 ? (amount / totalIncome) * 100 : 0;
            } else if ("expense".equals(transactionType)) {
                percentage = totalExpense > 0 ? (amount / totalExpense) * 100 : 0;
            }

            Log.d("StatisticsActivity", "Creating stat: " + categoryName +
                  " (" + transactionType + ") - " + amount + " - " + percentage + "%");

            CategoryStat stat = new CategoryStat(
                    categoryId,
                    categoryName,
                    categoryIcon,
                    transactionType,  // Use transaction type, not category type
                    amount,
                    percentage
            );
            categoryStats.add(stat);
        }

        Log.d("StatisticsActivity", "Total CategoryStats created: " + categoryStats.size());

        // Sort: income first (by amount desc), then expense (by amount desc)
        categoryStats.sort((a, b) -> {
            // First sort by type (income before expense)
            if (!a.getType().equals(b.getType())) {
                return "income".equals(a.getType()) ? -1 : 1;
            }
            // Then sort by amount descending within same type
            return Double.compare(b.getAmount(), a.getAmount());
        });

        // Clear container and add all category stat views
        categoriesContainer.removeAllViews();

        for (CategoryStat stat : categoryStats) {
            View itemView = getLayoutInflater().inflate(R.layout.item_category_stat, categoriesContainer, false);

            ImageView ivIcon = itemView.findViewById(R.id.ivCategoryIcon);
            TextView tvName = itemView.findViewById(R.id.tvCategoryName);
            TextView tvAmount = itemView.findViewById(R.id.tvAmount);
            TextView tvPercentage = itemView.findViewById(R.id.tvPercentage);
            android.widget.ProgressBar progressBar = itemView.findViewById(R.id.progressBar);

            // Set data
            tvName.setText(stat.getCategoryName());
            tvAmount.setText(CurrencyUtils.formatCurrency(stat.getAmount()));
            tvPercentage.setText(String.format("%.0f%%", stat.getPercentage()));
            progressBar.setProgress((int) stat.getPercentage());

            // Set icon
            int iconRes = getIconResource(stat.getIcon());
            ivIcon.setImageResource(iconRes);

            // Set color based on type
            if ("income".equals(stat.getType())) {
                progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_category_income));
                tvAmount.setTextColor(getResources().getColor(R.color.income));
            } else {
                progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_category_expense));
                tvAmount.setTextColor(getResources().getColor(R.color.expense));
            }

            categoriesContainer.addView(itemView);

            Log.d("StatisticsActivity", "Added view for: " + stat.getCategoryName());
        }

        Log.d("StatisticsActivity", "Total views added: " + categoriesContainer.getChildCount());
    }

    private int getIconResource(String iconName) {
        if (iconName == null || iconName.isEmpty()) {
            return R.drawable.ic_general_icon;
        }

        int resourceId = getResources().getIdentifier(iconName, "drawable", getPackageName());
        return resourceId != 0 ? resourceId : R.drawable.ic_general_icon;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
