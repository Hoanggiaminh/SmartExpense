package com.example.smartexpense;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView btnPrevMonth, btnNextMonth;
    private TextView tvMonthYear;
    private RecyclerView rvCalendar;
    private EditText etExpenseTitle, etAmount;
    private ChipGroup chipGroupCategories;
    private MaterialButton btnAddExpense;

    private Calendar currentMonth;
    private Calendar selectedDate;
    private CalendarDayAdapter calendarAdapter;
    private String selectedCategory = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        initViews();
        setupToolbar();
        setupCalendar();
        setupCategories();
        setupClickListeners();
        setupBackPress();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        tvMonthYear = findViewById(R.id.tvMonthYear);
        rvCalendar = findViewById(R.id.rvCalendar);
        etExpenseTitle = findViewById(R.id.etExpenseTitle);
        etAmount = findViewById(R.id.etAmount);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        btnAddExpense = findViewById(R.id.btnAddExpense);

        currentMonth = Calendar.getInstance();
        selectedDate = Calendar.getInstance();
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Thêm Chi Tiêu");
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

    private void setupCalendar() {
        rvCalendar.setLayoutManager(new GridLayoutManager(this, 7));
        calendarAdapter = new CalendarDayAdapter(generateDays(), this::onDateSelected);
        rvCalendar.setAdapter(calendarAdapter);

        updateMonthYearText();

        btnPrevMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            updateCalendar();
        });
    }

    private void updateCalendar() {
        calendarAdapter.updateDays(generateDays());
        updateMonthYearText();
    }

    private void updateMonthYearText() {
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM - yyyy", Locale.ENGLISH);
        tvMonthYear.setText(formatter.format(currentMonth.getTime()));
    }

    private List<CalendarDay> generateDays() {
        List<CalendarDay> days = new ArrayList<>();

        Calendar firstOfMonth = (Calendar) currentMonth.clone();
        firstOfMonth.set(Calendar.DAY_OF_MONTH, 1);

        int dayOfWeek = firstOfMonth.get(Calendar.DAY_OF_WEEK) - 1;

        Calendar prevMonthDate = (Calendar) firstOfMonth.clone();
        prevMonthDate.add(Calendar.DAY_OF_MONTH, -dayOfWeek);
        for (int i = 0; i < dayOfWeek; i++) {
            Calendar day = (Calendar) prevMonthDate.clone();
            day.add(Calendar.DAY_OF_MONTH, i);
            days.add(new CalendarDay(day, true));
        }

        int daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= daysInMonth; i++) {
            Calendar day = (Calendar) currentMonth.clone();
            day.set(Calendar.DAY_OF_MONTH, i);
            days.add(new CalendarDay(day, false));
        }

        int remainingDays = 35 - days.size();
        Calendar nextMonthDate = (Calendar) currentMonth.clone();
        nextMonthDate.add(Calendar.MONTH, 1);
        nextMonthDate.set(Calendar.DAY_OF_MONTH, 1);
        for (int i = 0; i < remainingDays; i++) {
            Calendar day = (Calendar) nextMonthDate.clone();
            day.add(Calendar.DAY_OF_MONTH, i);
            days.add(new CalendarDay(day, true));
        }

        return days;
    }

    private void onDateSelected(Calendar date) {
        selectedDate = date;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    private void setupCategories() {
        Chip chipHealth = findViewById(R.id.chipHealth);
        if (chipHealth != null) {
            chipHealth.setText("Sức khỏe");
        }

        Chip chipGrocery = findViewById(R.id.chipGrocery);
        if (chipGrocery != null) {
            chipGrocery.setText("Mua sắm");
        }

        addCategoryChip("Giao thông");
        addCategoryChip("Ăn uống");
        addCategoryChip("Giải trí");
        addCategoryChip("Hóa đơn");

        chipGroupCategories.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = findViewById(checkedId);
            if (chip != null && checkedId != R.id.chipAddCategory) {
                selectedCategory = chip.getText().toString();
            }
        });

        Chip chipAddCategory = findViewById(R.id.chipAddCategory);
        if (chipAddCategory != null) {
            chipAddCategory.setOnClickListener(v -> {
                Toast.makeText(this, "Chức năng thêm danh mục đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void addCategoryChip(String categoryName) {
        Chip chip = new Chip(this);
        chip.setText(categoryName);
        chip.setCheckable(true);
        chip.setChipBackgroundColorResource(android.R.color.white);
        chip.setChipStrokeWidth(1);
        chip.setChipStrokeColorResource(R.color.chip_stroke);
        chipGroupCategories.addView(chip, chipGroupCategories.getChildCount() - 1);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        btnAddExpense.setOnClickListener(v -> {
            String title = etExpenseTitle.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tiêu đề chi tiêu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedCategory.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String dateStr = sdf.format(selectedDate.getTime());

                Toast.makeText(this, "Đã thêm chi tiêu thành công", Toast.LENGTH_SHORT).show();
                finish();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });
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
