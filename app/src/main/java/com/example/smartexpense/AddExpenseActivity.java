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

public class AddExpenseActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView btnPrevMonth, btnNextMonth;
    private TextView tvMonthYear;
    private RecyclerView rvCalendar;
    private EditText etExpenseTitle, etAmount;
    private MaterialButton btnAddExpense;
    private MaterialButton btnAddCategory;

    // Category components
    private Spinner spinnerCategories;
    private List<Category> expenseCategories;
    private Category selectedCategoryObj;
    private ArrayAdapter<String> categorySpinnerAdapter;

    private Calendar currentMonth;
    private Calendar selectedDate;
    private CalendarDayAdapter calendarAdapter;

    // Firebase components
    private FirebaseService firebaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        initViews();
        setupFirebase();
        setupToolbar();
        setupCalendar();
        setupCategories();
        setupClickListeners();
        setupBackPress();
        loadExpenseCategories();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        tvMonthYear = findViewById(R.id.tvMonthYear);
        rvCalendar = findViewById(R.id.rvCalendar);
        etExpenseTitle = findViewById(R.id.etExpenseTitle);
        etAmount = findViewById(R.id.etAmount);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        btnAddCategory = findViewById(R.id.btnAddCategory);

        spinnerCategories = findViewById(R.id.spinnerCategories);
    }

    private void setupFirebase() {
        firebaseService = FirebaseService.getInstance();
        expenseCategories = new ArrayList<>();
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
                    if (position >= 0 && position < expenseCategories.size()) {
                        selectedCategoryObj = expenseCategories.get(position);
                    }
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    // Do nothing
                }
            });
        }
    }

    private void loadExpenseCategories() {
        firebaseService.getCategoriesByType("expense")
                .addOnSuccessListener(categories -> {
                    expenseCategories.clear();
                    expenseCategories.addAll(categories);

                    if (expenseCategories.isEmpty()) {
                        // Tạo danh mục mặc định nếu chưa có
                        firebaseService.createDefaultCategories();
                        // Reload sau khi tạo
                        new android.os.Handler().postDelayed(this::loadExpenseCategories, 2000);
                    } else {
                        updateCategorySpinner();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải danh mục: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Set default category nếu lỗi
                    selectedCategoryObj = new Category(null, "Ăn uống", "ic_food", "expense");
                });
    }

    private void updateCategorySpinner() {
        if (categorySpinnerAdapter != null && spinnerCategories != null) {
            List<String> categoryNames = new ArrayList<>();
            for (Category category : expenseCategories) {
                categoryNames.add(category.getName());
            }

            categorySpinnerAdapter.clear();
            categorySpinnerAdapter.addAll(categoryNames);
            categorySpinnerAdapter.notifyDataSetChanged();

            // Set default selection
            if (!expenseCategories.isEmpty()) {
                spinnerCategories.setSelection(0);
                selectedCategoryObj = expenseCategories.get(0);
            }
        }
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Thêm Chi Tiêu");
        }
    }

    private void setupCalendar() {
        currentMonth = Calendar.getInstance();
        selectedDate = Calendar.getInstance();

        calendarAdapter = new CalendarDayAdapter(new ArrayList<>(), date -> {
            selectedDate = (Calendar) date.clone();
            updateCalendar();
        });
        rvCalendar.setLayoutManager(new GridLayoutManager(this, 7));
        rvCalendar.setAdapter(calendarAdapter);

        updateCalendar();
        // Set initial selection to today
        calendarAdapter.setSelectedDate(selectedDate);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Nút thêm danh mục
        if (btnAddCategory != null) {
            btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
        }

        btnAddExpense.setOnClickListener(v -> addExpenseTransaction());

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
            createNewExpenseCategory(categoryName, "ic_other");
            dialog.dismiss();
        });

        dialog.show();
    }

    private List<IconAdapter.IconItem> getAvailableExpenseIcons() {
        return Arrays.asList(
                new IconAdapter.IconItem("ic_food", R.drawable.ic_food),
                new IconAdapter.IconItem("ic_transport", R.drawable.ic_transport),
                new IconAdapter.IconItem("ic_shopping", R.drawable.ic_shopping),
                new IconAdapter.IconItem("ic_entertainment", R.drawable.ic_entertainment),
                new IconAdapter.IconItem("ic_health", R.drawable.ic_health),
                new IconAdapter.IconItem("ic_education", R.drawable.ic_education),
                new IconAdapter.IconItem("ic_bills", R.drawable.ic_bills),
                new IconAdapter.IconItem("ic_other", R.drawable.ic_other)
        );
    }

    private void createNewExpenseCategory(String name, String iconName) {
        Category newCategory = new Category(null, name, iconName, "expense");

        firebaseService.addUserCategory(newCategory)
                .addOnSuccessListener(documentReference -> {
                    newCategory.setId(documentReference.getId());
                    expenseCategories.add(newCategory);

                    // Update spinner và set selection cho category mới
                    updateCategorySpinner();
                    int newPosition = expenseCategories.size() - 1;
                    if (spinnerCategories != null) {
                        spinnerCategories.setSelection(newPosition);
                    }
                    selectedCategoryObj = newCategory;

                    Toast.makeText(this, "Tạo danh mục thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tạo danh mục: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addExpenseTransaction() {
        String title = etExpenseTitle.getText().toString().trim();
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

        if (TextUtils.isEmpty(selectedCategoryObj.getName())) {
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
                selectedCategoryObj.getId(),
                title,
                amount,
                "expense",
                new Timestamp(selectedDate.getTime())
        );

        // Save to Firebase
        btnAddExpense.setEnabled(false);
        firebaseService.addTransaction(transaction)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Thêm chi tiêu thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi thêm chi tiêu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnAddExpense.setEnabled(true);
                });
    }

    private void updateCalendar() {
        List<CalendarDay> days = generateCalendarDays(currentMonth);
        android.util.Log.d("Calendar", "Generated " + days.size() + " calendar days for expense activity");
        calendarAdapter.updateDays(days);

        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM - yyyy", Locale.getDefault());
        tvMonthYear.setText(monthFormat.format(currentMonth.getTime()));
        
        // Set selection after updating calendar
        if (selectedDate != null) {
            calendarAdapter.setSelectedDate(selectedDate);
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

        android.util.Log.d("Calendar", "Expense Calendar - Month: " + month.get(Calendar.MONTH) + 
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
