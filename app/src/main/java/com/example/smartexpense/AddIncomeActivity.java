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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddIncomeActivity extends AppCompatActivity {
    private RecyclerView rvCalendar;
    private TextView tvMonthYear;
    private ImageView btnPrevMonth, btnNextMonth, btnBack;
    private EditText etIncomeTitle, etAmount;
    private MaterialButton btnAddIncome;
    private CalendarDayAdapter adapter;
    private Calendar currentMonth;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_income);

        initViews();
        setupToolbar();
        setupCalendar();
        setupClickListeners();
        setupBackPress();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvCalendar = findViewById(R.id.rvCalendar);
        tvMonthYear = findViewById(R.id.tvMonthYear);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        etIncomeTitle = findViewById(R.id.etIncomeTitle);
        etAmount = findViewById(R.id.etAmount);
        btnAddIncome = findViewById(R.id.btnAddIncome);

        currentMonth = Calendar.getInstance();
        selectedDate = Calendar.getInstance();
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Thêm Thu Nhập");
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
        adapter = new CalendarDayAdapter(generateDays(), this::onDateSelected);
        rvCalendar.setAdapter(adapter);

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

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        btnAddIncome.setOnClickListener(v -> {
            String title = etIncomeTitle.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tiêu đề thu nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String dateStr = sdf.format(selectedDate.getTime());

                Toast.makeText(this, "Đã thêm thu nhập thành công", Toast.LENGTH_SHORT).show();
                finish();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCalendar() {
        adapter.updateDays(generateDays());
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
