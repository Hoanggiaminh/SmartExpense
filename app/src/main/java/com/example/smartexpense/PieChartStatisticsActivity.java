package com.example.smartexpense;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartexpense.model.Category;
import com.example.smartexpense.model.CategoryStat;
import com.example.smartexpense.model.Transaction;
import com.example.smartexpense.services.FirebaseService;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.firebase.Timestamp;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PieChartStatisticsActivity extends BaseActivity {

    private static final String TAG = "PieChartStats";

    private TextView tvMonthYear, tvBalance;
    private TextView btnIncome, btnExpense;
    private ImageButton btnPreviousMonth, btnNextMonth;
    private TextView btnColumnChart, btnPieChart;
    private LinearLayout categoriesContainer;
    private PieChart pieChart;

    private FirebaseService firebaseService;
    private Calendar currentMonth;
    private List<Transaction> monthTransactions;
    private List<CategoryStat> categoryStats;
    private Map<String, Category> categoryMap;
    private String currentType = "income"; // "income" or "expense"

    private NumberFormat currencyFormat;

    // Màu sắc cho biểu đồ tròn
    private static final int[] INCOME_COLORS = {
            Color.rgb(52, 199, 89),    // Xanh lá chính
            Color.rgb(48, 209, 88),    // Xanh lá sáng
            Color.rgb(50, 215, 75),    // Xanh lá nhạt
            Color.rgb(52, 183, 241),   // Xanh dương
            Color.rgb(94, 92, 230),    // Tím
            Color.rgb(255, 159, 10),   // Cam
    };

    private static final int[] EXPENSE_COLORS = {
            Color.rgb(255, 59, 48),    // Đỏ chính
            Color.rgb(255, 69, 58),    // Đỏ sáng
            Color.rgb(13, 153, 255),   // Xanh dương
            Color.rgb(48, 209, 88),    // Xanh lá
            Color.rgb(175, 82, 222),   // Tím
            Color.rgb(255, 159, 10),   // Cam
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pie_chart_statistics);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        firebaseService = FirebaseService.getInstance();
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        // Initialize calendar to current month
        currentMonth = Calendar.getInstance();
        currentMonth.set(Calendar.DAY_OF_MONTH, 1);
        currentMonth.set(Calendar.HOUR_OF_DAY, 0);
        currentMonth.set(Calendar.MINUTE, 0);
        currentMonth.set(Calendar.SECOND, 0);
        currentMonth.set(Calendar.MILLISECOND, 0);

        // Initialize data structures
        monthTransactions = new ArrayList<>();
        categoryStats = new ArrayList<>();
        categoryMap = new HashMap<>();

        // Initialize views
        initViews();

        // Setup click listeners
        setupClickListeners();

        // Load categories and data
        loadCategories();

        // Initialize bottom navigation
        initBottomNavigation();
        int selectedTab = getIntent().getIntExtra("selectedTab", 2);
        updateTabState(selectedTab);
    }

    private void initViews() {
        tvMonthYear = findViewById(R.id.tvMonthYear);
        tvBalance = findViewById(R.id.tvBalance);
        btnPreviousMonth = findViewById(R.id.btnPreviousMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        btnIncome = findViewById(R.id.btnIncome);
        btnExpense = findViewById(R.id.btnExpense);
        btnColumnChart = findViewById(R.id.btnColumnChart);
        btnPieChart = findViewById(R.id.btnPieChart);
        categoriesContainer = findViewById(R.id.categoriesContainer);
        pieChart = findViewById(R.id.pieChart);

        setupPieChart();
    }

    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);

        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);

        pieChart.setDrawCenterText(false);
        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        pieChart.getLegend().setEnabled(false);
    }

    private void setupClickListeners() {

        btnPreviousMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            updateMonthYearDisplay();
            loadMonthData();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            updateMonthYearDisplay();
            loadMonthData();
        });

        btnIncome.setOnClickListener(v -> {
            if (!currentType.equals("income")) {
                currentType = "income";
                updateTypeToggle();
                updateUI();
            }
        });

        btnExpense.setOnClickListener(v -> {
            if (!currentType.equals("expense")) {
                currentType = "expense";
                updateTypeToggle();
                updateUI();
            }
        });

        btnColumnChart.setOnClickListener(v -> finish());
    }

    private void updateTypeToggle() {
        if ("income".equals(currentType)) {
            btnIncome.setBackgroundResource(R.drawable.bg_tab_selected);
            btnIncome.setTextColor(ContextCompat.getColor(this, R.color.primary));
            btnExpense.setBackgroundResource(0);
            btnExpense.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        } else {
            btnExpense.setBackgroundResource(R.drawable.bg_tab_selected);
            btnExpense.setTextColor(ContextCompat.getColor(this, R.color.primary));
            btnIncome.setBackgroundResource(0);
            btnIncome.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }

    private void updateMonthYearDisplay() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", new Locale("vi", "VN"));
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", new Locale("vi", "VN"));

        String monthName = monthFormat.format(currentMonth.getTime());
        String capitalizedMonth = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
        String year = yearFormat.format(currentMonth.getTime());

        tvMonthYear.setText("Tháng " + capitalizedMonth + "\n" + year);
    }

    private void loadCategories() {
        firebaseService.getAllUserCategories()
                .addOnSuccessListener(categories -> {
                    categoryMap.clear();
                    for (Category category : categories) {
                        categoryMap.put(category.getId(), category);
                    }
                    updateMonthYearDisplay();
                    loadMonthData();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading categories", e);
                    updateMonthYearDisplay();
                    loadMonthData();
                });
    }

    private void loadMonthData() {
        // Calculate month range
        Calendar monthStart = (Calendar) currentMonth.clone();
        Calendar monthEnd = (Calendar) currentMonth.clone();
        monthEnd.add(Calendar.MONTH, 1);
        monthEnd.add(Calendar.SECOND, -1);

        Timestamp startTimestamp = new Timestamp(monthStart.getTime());
        Timestamp endTimestamp = new Timestamp(monthEnd.getTime());

        firebaseService.getTransactionsBetweenDates(startTimestamp, endTimestamp)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    monthTransactions.clear();
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        Transaction transaction = queryDocumentSnapshots.getDocuments().get(i).toObject(Transaction.class);
                        if (transaction != null) {
                            monthTransactions.add(transaction);
                        }
                    }
                    Log.d(TAG, "Loaded " + monthTransactions.size() + " transactions");
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading transactions", e);
                    monthTransactions.clear();
                    updateUI();
                });
    }

    private void updateUI() {
        calculateCategoryStats();
        updateBalance();
        updatePieChart();
        displayCategories();
    }

    private void calculateCategoryStats() {
        categoryStats.clear();

        Map<String, Double> categoryAmounts = new HashMap<>();
        double total = 0;

        // Filter transactions by current type
        for (Transaction transaction : monthTransactions) {
            if (transaction.getType().equals(currentType)) {
                String categoryId = transaction.getCategoryId();
                double amount = transaction.getAmount();

                categoryAmounts.put(categoryId, categoryAmounts.getOrDefault(categoryId, 0.0) + amount);
                total += amount;
            }
        }

        // Create CategoryStat objects
        for (Map.Entry<String, Double> entry : categoryAmounts.entrySet()) {
            String categoryId = entry.getKey();
            double amount = entry.getValue();

            Category category = categoryMap.get(categoryId);
            if (category == null) continue;

            double percentage = total > 0 ? (amount / total) * 100 : 0;

            CategoryStat stat = new CategoryStat(
                    categoryId,
                    category.getName(),
                    category.getIcon(),
                    currentType,
                    amount,
                    percentage
            );
            categoryStats.add(stat);
        }

        // Sort by amount descending
        categoryStats.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));

        Log.d(TAG, "Created " + categoryStats.size() + " category stats for type: " + currentType);
    }

    private void updateBalance() {
        double total = 0;
        for (CategoryStat stat : categoryStats) {
            total += stat.getAmount();
        }
        tvBalance.setText(currencyFormat.format(total));

        if ("income".equals(currentType)) {
            tvBalance.setTextColor(ContextCompat.getColor(this, R.color.income));
        } else {
            tvBalance.setTextColor(ContextCompat.getColor(this, R.color.expense));
        }
    }

    private void updatePieChart() {
        if (categoryStats.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("Không có dữ liệu");
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        for (CategoryStat stat : categoryStats) {
            entries.add(new PieEntry((float) stat.getAmount(), stat.getCategoryName()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // Set colors based on type
        if ("income".equals(currentType)) {
            dataSet.setColors(INCOME_COLORS);
        } else {
            dataSet.setColors(EXPENSE_COLORS);
        }

        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));

        pieChart.setData(data);
        pieChart.invalidate();
        pieChart.animateY(1000);
    }

    private void displayCategories() {
        categoriesContainer.removeAllViews();

        for (int i = 0; i < categoryStats.size(); i++) {
            CategoryStat stat = categoryStats.get(i);
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_category_stat, categoriesContainer, false);

            ImageView ivIcon = itemView.findViewById(R.id.ivCategoryIcon);
            TextView tvName = itemView.findViewById(R.id.tvCategoryName);
            TextView tvAmount = itemView.findViewById(R.id.tvAmount);
            TextView tvPercentage = itemView.findViewById(R.id.tvPercentage);
            ProgressBar progressBar = itemView.findViewById(R.id.progressBar);

            // Set data
            tvName.setText(stat.getCategoryName());
            tvAmount.setText(currencyFormat.format(stat.getAmount()) + "đ");
            tvPercentage.setText(String.format("%.0f%%", stat.getPercentage()));
            progressBar.setProgress((int) stat.getPercentage());

            // Set icon
            int iconRes = getIconResource(stat.getIcon());
            ivIcon.setImageResource(iconRes);

            // Set color based on type
            int colorIndex = i % ("income".equals(currentType) ? INCOME_COLORS.length : EXPENSE_COLORS.length);
            int color = "income".equals(currentType) ? INCOME_COLORS[colorIndex] : EXPENSE_COLORS[colorIndex];

            progressBar.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
            tvAmount.setTextColor(color);

            categoriesContainer.addView(itemView);
        }

        Log.d(TAG, "Displayed " + categoriesContainer.getChildCount() + " categories");
    }

    private int getIconResource(String iconName) {
        if (iconName == null || iconName.isEmpty()) {
            return R.drawable.ic_general_icon;
        }
        int resourceId = getResources().getIdentifier(iconName, "drawable", getPackageName());
        return resourceId != 0 ? resourceId : R.drawable.ic_general_icon;
    }
}

