package com.example.smartexpense;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartexpense.adapters.IconAdapter;
import com.example.smartexpense.model.Category;
import com.example.smartexpense.model.Transaction;
import com.example.smartexpense.services.FirebaseService;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

    // Category components
    private MaterialButton btnAddCategory;
    private Spinner spinnerCategories;
    private List<Category> incomeCategories;
    private Category selectedCategory;
    private ArrayAdapter<String> categorySpinnerAdapter;

    // Firebase components
    private FirebaseService firebaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_income);

        initViews();
        setupFirebase();
        setupToolbar();
        setupCalendar();
        setupCategories();
        setupClickListeners();
        setupBackPress();
        loadIncomeCategories();
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

        btnAddCategory = findViewById(R.id.btnAddCategory);
        spinnerCategories = findViewById(R.id.spinnerCategories);
    }

    private void setupFirebase() {
        firebaseService = FirebaseService.getInstance();
        incomeCategories = new ArrayList<>();
    }

    private void setupCategories() {
        // Setup spinner adapter
        if (spinnerCategories != null) {
            List<String> categoryNames = new ArrayList<>();
            categorySpinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryNames);
            categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategories.setAdapter(categorySpinnerAdapter);

            // Listener để cập nhật selectedCategory khi user chọn
            spinnerCategories.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    if (position >= 0 && position < incomeCategories.size()) {
                        selectedCategory = incomeCategories.get(position);
                    }
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    // Do nothing
                }
            });
        }
    }

    private void loadIncomeCategories() {
        firebaseService.getCategoriesByType("income")
                .addOnSuccessListener(categories -> {
                    incomeCategories.clear();
                    incomeCategories.addAll(categories);

                    if (incomeCategories.isEmpty()) {
                        // Tạo danh mục mặc định nếu chưa có
                        firebaseService.createDefaultCategories();
                        // Reload sau khi tạo
                        new android.os.Handler().postDelayed(this::loadIncomeCategories, 2000);
                    } else {
                        // Set default selected category
                        if (selectedCategory == null && !incomeCategories.isEmpty()) {
                            selectedCategory = incomeCategories.get(0);
                        }

                        // Update spinner
                        updateCategorySpinner();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải danh mục: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Set default category nếu lỗi
                    selectedCategory = new Category(null, "Lương", "ic_salary", "income");
                });
    }

    private void updateCategorySpinner() {
        if (categorySpinnerAdapter != null && spinnerCategories != null) {
            List<String> categoryNames = new ArrayList<>();
            for (Category category : incomeCategories) {
                categoryNames.add(category.getName());
            }

            categorySpinnerAdapter.clear();
            categorySpinnerAdapter.addAll(categoryNames);
            categorySpinnerAdapter.notifyDataSetChanged();

            // Set default selection
            if (!incomeCategories.isEmpty()) {
                spinnerCategories.setSelection(0);
                selectedCategory = incomeCategories.get(0);
            }
        }
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Thêm Thu Nhập");
        }
    }

    private void setupCalendar() {
        currentMonth = Calendar.getInstance();
        selectedDate = Calendar.getInstance();

        // Check if a date was passed from CalendarActivity
        long selectedDateMillis = getIntent().getLongExtra("selected_date", -1);
        if (selectedDateMillis != -1) {
            selectedDate.setTimeInMillis(selectedDateMillis);
            currentMonth.setTimeInMillis(selectedDateMillis);
        }

        adapter = new CalendarDayAdapter(new ArrayList<>(), date -> {
            selectedDate = (Calendar) date.clone();
            // Don't call updateCalendar() here to avoid resetting selection
            // The adapter handles visual updates internally
        });
        rvCalendar.setLayoutManager(new GridLayoutManager(this, 7));
        rvCalendar.setAdapter(adapter);

        updateCalendar();
        // Set initial selection to selected date (today or from calendar)
        adapter.setSelectedDate(selectedDate);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Nút thêm danh mục
        if (btnAddCategory != null) {
            btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
        }

        btnAddIncome.setOnClickListener(v -> addIncomeTransaction());

        btnPrevMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            updateCalendar();
        });
    }

    private void showAddCategoryDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_category);

        // Set dialog window attributes for proper sizing
        if (dialog.getWindow() != null) {
            android.view.WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85); // 85% of screen width
            params.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(params);
        }

        EditText etCategoryName = dialog.findViewById(R.id.etCategoryName);
        TextView btnCancel = dialog.findViewById(R.id.btnCancel);
        TextView btnCreate = dialog.findViewById(R.id.btnCreate);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String categoryName = etCategoryName.getText().toString().trim();
            if (TextUtils.isEmpty(categoryName)) {
                Toast.makeText(this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            // Use default icon for all custom categories
            createNewIncomeCategory(categoryName, "ic_other");
            dialog.dismiss();
        });

        dialog.show();
    }

    private List<IconAdapter.IconItem> getAvailableIncomeIcons() {
        return Arrays.asList(
            new IconAdapter.IconItem("ic_salary", R.drawable.ic_salary),
            new IconAdapter.IconItem("ic_bonus", R.drawable.ic_bonus),
            new IconAdapter.IconItem("ic_investment", R.drawable.ic_investment),
            new IconAdapter.IconItem("ic_other", R.drawable.ic_other)
        );
    }

    private void createNewIncomeCategory(String name, String iconName) {
        Category newCategory = new Category(null, name, iconName, "income");

        firebaseService.addUserCategory(newCategory)
                .addOnSuccessListener(documentReference -> {
                    newCategory.setId(documentReference.getId());
                    incomeCategories.add(newCategory);

                    // Update spinner và set selection cho category mới
                    updateCategorySpinner();
                    int newPosition = incomeCategories.size() - 1;
                    if (spinnerCategories != null) {
                        spinnerCategories.setSelection(newPosition);
                    }
                    selectedCategory = newCategory;

                    Toast.makeText(this, "Tạo danh mục thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tạo danh mục: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addIncomeTransaction() {
        String title = etIncomeTitle.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(amountStr)) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCategory == null) {
            Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create transaction
        String userId = firebaseService.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        Transaction transaction = new Transaction(
                null, // ID will be auto-generated
                selectedCategory.getId(),
                title,
                amount,
                "income",
                new Timestamp(selectedDate.getTime())
        );

        // Save to Firebase
        btnAddIncome.setEnabled(false);
        firebaseService.addTransaction(transaction)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Thêm thu nhập thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi thêm thu nhập: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnAddIncome.setEnabled(true);
                });
    }

    private void updateCalendar() {
        List<CalendarDay> days = generateCalendarDays(currentMonth);
        android.util.Log.d("Calendar", "Generated " + days.size() + " calendar days");
        adapter.updateDays(days);

        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM - yyyy", Locale.getDefault());
        tvMonthYear.setText(monthFormat.format(currentMonth.getTime()));

        // Set selection after updating calendar
        if (selectedDate != null) {
            adapter.setSelectedDate(selectedDate);
        }
    }

    private List<CalendarDay> generateCalendarDays(Calendar month) {
        List<CalendarDay> days = new ArrayList<>();
        Calendar calendar = (Calendar) month.clone();

        // Set to first day of month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        // Add empty days for previous month
        Calendar prevMonth = (Calendar) calendar.clone();
        prevMonth.add(Calendar.MONTH, -1);
        int daysInPrevMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = firstDayOfWeek - 1; i >= 0; i--) {
            Calendar emptyDay = (Calendar) prevMonth.clone();
            emptyDay.set(Calendar.DAY_OF_MONTH, daysInPrevMonth - i);
            days.add(new CalendarDay(emptyDay, true));
        }

        // Add days of current month
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int day = 1; day <= daysInMonth; day++) {
            Calendar currentDay = (Calendar) calendar.clone();
            currentDay.set(Calendar.DAY_OF_MONTH, day);
            days.add(new CalendarDay(currentDay, false));
        }

        android.util.Log.d("Calendar", "Month: " + month.get(Calendar.MONTH) +
                          ", Year: " + month.get(Calendar.YEAR) +
                          ", Days: " + days.size() +
                          ", First day of week: " + firstDayOfWeek);

        return days;
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void setupBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }
}
