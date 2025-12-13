package com.example.smartexpense;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartexpense.services.FirebaseService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends BaseActivity {

    private TextView tvMonthYear;
    private TextView tvMonthlyIncome;
    private TextView tvMonthlyExpense;
    private TextView tvMonthlyBalance;
    private ImageView btnPreviousMonth;
    private ImageView btnNextMonth;
    private RecyclerView rvCalendar;
    private FloatingActionButton fabAddTransaction;

    private CalendarDayAdapter calendarAdapter;
    private Calendar currentMonth;
    private FirebaseService firebaseService;
    private Map<String, Double> dayAmounts = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        firebaseService = FirebaseService.getInstance();

        // Initialize current month
        currentMonth = Calendar.getInstance();

        initViews();
        setupCalendarRecyclerView();
        updateMonthYearDisplay();
        loadMonthTransactions();
        setupClickListeners();

        // Initialize bottom navigation
        initBottomNavigation();

        // Get selected tab from intent or default to 1 (Calendar)
        int selectedTab = getIntent().getIntExtra("selectedTab", 1);
        updateTabState(selectedTab);
    }

    private void initViews() {
        tvMonthYear = findViewById(R.id.tvMonthYear);
        tvMonthlyIncome = findViewById(R.id.tvMonthlyIncome);
        tvMonthlyExpense = findViewById(R.id.tvMonthlyExpense);
        tvMonthlyBalance = findViewById(R.id.tvMonthlyBalance);
        btnPreviousMonth = findViewById(R.id.btnPreviousMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        rvCalendar = findViewById(R.id.rvCalendar);
        fabAddTransaction = findViewById(R.id.fab_add_transaction);
    }

    private void setupCalendarRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 7);
        rvCalendar.setLayoutManager(layoutManager);

        List<CalendarDay> days = generateCalendarDays(currentMonth);
        calendarAdapter = new CalendarDayAdapter(days, this::onDaySelected);
        rvCalendar.setAdapter(calendarAdapter);
    }

    private List<CalendarDay> generateCalendarDays(Calendar month) {
        List<CalendarDay> days = new ArrayList<>();

        Calendar calendar = (Calendar) month.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Add previous month days to fill first week
        Calendar prevMonth = (Calendar) calendar.clone();
        prevMonth.add(Calendar.MONTH, -1);
        int daysInPrevMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = firstDayOfWeek - 1; i > 0; i--) {
            Calendar day = (Calendar) prevMonth.clone();
            day.set(Calendar.DAY_OF_MONTH, daysInPrevMonth - i + 1);
            days.add(new CalendarDay(day, true));
        }

        // Add current month days
        for (int i = 1; i <= daysInMonth; i++) {
            Calendar day = (Calendar) calendar.clone();
            day.set(Calendar.DAY_OF_MONTH, i);
            days.add(new CalendarDay(day, false));
        }

        // Add next month days to complete the grid (35 or 42 days total)
        int totalDays = days.size();
        int remainingDays = (totalDays <= 35) ? (35 - totalDays) : (42 - totalDays);

        Calendar nextMonth = (Calendar) calendar.clone();
        nextMonth.add(Calendar.MONTH, 1);

        for (int i = 1; i <= remainingDays; i++) {
            Calendar day = (Calendar) nextMonth.clone();
            day.set(Calendar.DAY_OF_MONTH, i);
            days.add(new CalendarDay(day, true));
        }

        return days;
    }

    private void updateMonthYearDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("'Tháng' MMMM yyyy", new Locale("vi", "VN"));
        tvMonthYear.setText(sdf.format(currentMonth.getTime()));
    }

    private void loadMonthTransactions() {
        // Get start and end of month
        Calendar startOfMonth = (Calendar) currentMonth.clone();
        startOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        startOfMonth.set(Calendar.HOUR_OF_DAY, 0);
        startOfMonth.set(Calendar.MINUTE, 0);
        startOfMonth.set(Calendar.SECOND, 0);

        Calendar endOfMonth = (Calendar) currentMonth.clone();
        endOfMonth.set(Calendar.DAY_OF_MONTH, currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
        endOfMonth.set(Calendar.HOUR_OF_DAY, 23);
        endOfMonth.set(Calendar.MINUTE, 59);
        endOfMonth.set(Calendar.SECOND, 59);

        Timestamp startTimestamp = new Timestamp(startOfMonth.getTime());
        Timestamp endTimestamp = new Timestamp(endOfMonth.getTime());

        firebaseService.getTransactionsBetweenDates(startTimestamp, endTimestamp)
                .addOnSuccessListener(querySnapshot -> {
                    // Sử dụng mảng để có thể thay đổi giá trị trong lambda
                    final double[] totals = new double[2]; // [0] = income, [1] = expense
                    dayAmounts.clear();

                    querySnapshot.forEach(document -> {
                        String type = document.getString("type");
                        Double amount = document.getDouble("amount");
                        Timestamp date = document.getTimestamp("date");

                        if (amount != null && date != null) {
                            // Calculate totals
                            if ("income".equals(type)) {
                                totals[0] += amount;
                            } else if ("expense".equals(type)) {
                                totals[1] += amount;
                            }

                            // Track daily amounts
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date.toDate());
                            String dateKey = String.format(Locale.US, "%04d-%02d-%02d",
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH) + 1,
                                    cal.get(Calendar.DAY_OF_MONTH));

                            double dailyBalance = dayAmounts.getOrDefault(dateKey, 0.0);
                            if ("income".equals(type)) {
                                dailyBalance += amount;
                            } else {
                                dailyBalance -= amount;
                            }
                            dayAmounts.put(dateKey, dailyBalance);
                        }
                    });

                    // Update summary display
                    double totalIncome = totals[0];
                    double totalExpense = totals[1];
                    tvMonthlyIncome.setText(formatCurrency(totalIncome));
                    tvMonthlyExpense.setText(formatCurrency(totalExpense));
                    tvMonthlyBalance.setText(formatCurrency(totalIncome - totalExpense));

                    // Update calendar with amounts
                    calendarAdapter.setDayAmounts(dayAmounts);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải giao dịch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupClickListeners() {
        btnPreviousMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            updateMonthYearDisplay();
            List<CalendarDay> days = generateCalendarDays(currentMonth);
            calendarAdapter.updateDays(days);
            loadMonthTransactions();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            updateMonthYearDisplay();
            List<CalendarDay> days = generateCalendarDays(currentMonth);
            calendarAdapter.updateDays(days);
            loadMonthTransactions();
        });

        fabAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, AddTransactionActivity.class);
            startActivity(intent);
        });
    }

    private void onDaySelected(Calendar selectedDate) {
        // Navigate to DayDetailActivity
        Intent intent = new Intent(CalendarActivity.this, DayDetailActivity.class);
        intent.putExtra("selected_date", selectedDate.getTimeInMillis());
        startActivity(intent);
    }

    private String formatCurrency(double amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        DecimalFormat formatter = new DecimalFormat("$ #,###", symbols);
        if (amount == 0) {
            return "$ 0";
        }
        return formatter.format(amount);
    }
}
