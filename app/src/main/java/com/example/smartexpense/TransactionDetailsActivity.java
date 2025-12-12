package com.example.smartexpense;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.DatePicker;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.smartexpense.model.Category;
import com.example.smartexpense.services.FirebaseService;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionDetailsActivity extends AppCompatActivity {

    ImageView btnBack, ivCategoryIcon;
    LinearLayout layoutTransactionType, layoutCategory, layoutDate;
    TextView tvTransactionType, tvCategory, tvDate;
    TextView tvAmount, tvBadge;
    EditText etTitle, etAmount;
    MaterialButton btnUpdate, btnDelete;

    // Firebase
    private FirebaseService firebaseService;
    private List<Category> currentCategories = new ArrayList<>();
    private Category selectedCategory;

    // Transaction data
    private String transactionId;
    private String transactionTitle;
    private double transactionAmount;
    private String transactionType;
    private String transactionCategory;
    private String transactionCategoryId;
    private long transactionDateSeconds;

    private NumberFormat currencyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaction_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseService = FirebaseService.getInstance();
        currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        initViews();
        loadTransactionData();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        ivCategoryIcon = findViewById(R.id.iv_category_icon);
        layoutTransactionType = findViewById(R.id.layout_transaction_type);
        layoutCategory = findViewById(R.id.layout_category);
        layoutDate = findViewById(R.id.layout_date);
        tvTransactionType = findViewById(R.id.tv_transaction_type);
        tvCategory = findViewById(R.id.tv_category);
        tvDate = findViewById(R.id.tv_date);
        tvAmount = findViewById(R.id.tv_amount);
        tvBadge = findViewById(R.id.tv_badge);
        etTitle = findViewById(R.id.et_title);
        etAmount = findViewById(R.id.et_amount);
        btnUpdate = findViewById(R.id.btn_update);
        btnDelete = findViewById(R.id.btn_delete);
    }

    private void loadTransactionData() {
        // Get data from Intent
        Intent intent = getIntent();
        if (intent != null) {
            transactionId = intent.getStringExtra("transaction_id");
            transactionTitle = intent.getStringExtra("transaction_title");
            transactionAmount = intent.getDoubleExtra("transaction_amount", 0.0);
            transactionType = intent.getStringExtra("transaction_type");
            transactionCategory = intent.getStringExtra("transaction_category");
            transactionDateSeconds = intent.getLongExtra("transaction_date", 0);

            // Load transaction details from Firebase để lấy category ID
            loadTransactionFromFirebase();
        }
    }

    private void loadTransactionFromFirebase() {
        if (transactionId == null) return;
        
        firebaseService.getTransactionById(transactionId)
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Lấy category ID từ Firebase
                    transactionCategoryId = documentSnapshot.getString("categoryId");
                    
                    // Load categories và hiển thị dữ liệu
                    loadCategoriesAndDisplay();
                } else {
                    // Nếu không tìm thấy transaction, vẫn hiển thị dữ liệu cơ bản
                    displayBasicTransactionInfo();
                    loadCategories();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi khi tải chi tiết giao dịch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                displayBasicTransactionInfo();
                loadCategories();
            });
    }

    private void loadCategoriesAndDisplay() {
        if (transactionType == null) return;
        
        firebaseService.getCategoriesByType(transactionType)
            .addOnSuccessListener(categories -> {
                currentCategories = categories;
                
                // Tìm category hiện tại dựa trên ID
                selectedCategory = null;
                for (Category category : categories) {
                    if (category.getId().equals(transactionCategoryId)) {
                        selectedCategory = category;
                        break;
                    }
                }
                
                // Hiển thị thông tin transaction
                displayTransactionInfo();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi khi tải danh mục: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                displayBasicTransactionInfo();
            });
    }

    private void loadCategories() {
        if (transactionType == null) return;
        
        firebaseService.getCategoriesByType(transactionType)
            .addOnSuccessListener(categories -> {
                currentCategories = categories;
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi khi tải danh mục: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void displayTransactionInfo() {
        // Set title in EditText
        if (etTitle != null && transactionTitle != null) {
            etTitle.setText(transactionTitle);
        }

        // Set amount in EditText (số nguyên cho việc edit)
        if (etAmount != null) {
            etAmount.setText(String.valueOf((long) transactionAmount));
        }

        // Hiển thị amount với format VNĐ
        displayFormattedAmount();

        // Set transaction type và badge
        displayTransactionTypeAndBadge();

        // Set category icon và name
        if (selectedCategory != null) {
            tvCategory.setText(selectedCategory.getName());
            
            // Set category icon
            int iconRes = getCategoryIconResource(selectedCategory.getIcon());
            ivCategoryIcon.setImageResource(iconRes);
        } else {
            tvCategory.setText(transactionCategory != null ? transactionCategory : "Chưa chọn danh mục");
            ivCategoryIcon.setImageResource(R.drawable.ic_other);
        }

        // Set date
        if (transactionDateSeconds > 0) {
            Date date = new Date(transactionDateSeconds * 1000);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvDate.setText(dateFormat.format(date));
        }
    }

    private void displayBasicTransactionInfo() {
        // Set title in EditText
        if (etTitle != null && transactionTitle != null) {
            etTitle.setText(transactionTitle);
        }

        // Set amount in EditText
        if (etAmount != null) {
            etAmount.setText(String.valueOf((long) transactionAmount));
        }

        // Hiển thị amount với format VNĐ
        displayFormattedAmount();

        // Set transaction type và badge
        displayTransactionTypeAndBadge();

        // Set category
        if (transactionCategory != null) {
            tvCategory.setText(transactionCategory);
        }

        // Set default icon
        ivCategoryIcon.setImageResource(R.drawable.ic_other);

        // Set date
        if (transactionDateSeconds > 0) {
            Date date = new Date(transactionDateSeconds * 1000);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvDate.setText(dateFormat.format(date));
        }
    }

    private void displayFormattedAmount() {
        if (tvAmount == null) return;
        
        String formattedAmount;
        int amountColor;
        int badgeBackground;

        if ("income".equals(transactionType)) {
            formattedAmount = "+ " + currencyFormat.format(transactionAmount) + " VNĐ";
            amountColor = ContextCompat.getColor(this, R.color.income);
        } else {
            formattedAmount = "- " + currencyFormat.format(transactionAmount) + " VNĐ";
            amountColor = ContextCompat.getColor(this, R.color.expense);
        }

        tvAmount.setText(formattedAmount);
        tvAmount.setTextColor(amountColor);
    }

    private void displayTransactionTypeAndBadge() {
        if (transactionType != null) {
            if ("income".equals(transactionType)) {
                tvTransactionType.setText("Thu nhập");
                tvBadge.setText("Thu nhập");
                tvBadge.setTextColor(ContextCompat.getColor(this, R.color.income));
                tvBadge.setBackgroundResource(R.drawable.bg_income_badge);
            } else {
                tvTransactionType.setText("Chi tiêu");
                tvBadge.setText("Chi tiêu");
                tvBadge.setTextColor(ContextCompat.getColor(this, R.color.expense));
                tvBadge.setBackgroundResource(R.drawable.bg_expense_badge);
            }
        }
    }

    private int getCategoryIconResource(String iconName) {
        if (iconName == null) return R.drawable.ic_other;

        switch (iconName) {
            case "ic_salary": return R.drawable.ic_salary;
            case "ic_bonus": return R.drawable.ic_bonus;
            case "ic_investment": return R.drawable.ic_investment;
            case "ic_food": return R.drawable.ic_food;
            case "ic_shopping": return R.drawable.ic_shopping;
            case "ic_bills": return R.drawable.ic_bills;
            case "ic_health": return R.drawable.ic_health;
            case "ic_entertainment": return R.drawable.ic_entertainment;
            case "ic_education": return R.drawable.ic_education;
            case "ic_transport": return R.drawable.ic_transport;
            default: return R.drawable.ic_other;
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Transaction Type Dropdown
        layoutTransactionType.setOnClickListener(v -> showTransactionTypeDialog());

        // Category Dropdown
        layoutCategory.setOnClickListener(v -> showCategoryDialog());

        // Date Picker
        layoutDate.setOnClickListener(v -> showDatePickerDialog());

        // Update button
        btnUpdate.setOnClickListener(v -> updateTransaction());

        // Delete button
        btnDelete.setOnClickListener(v -> deleteTransaction());
    }

    private void showTransactionTypeDialog() {
        String[] transactionTypes = {"Thu nhập", "Chi tiêu"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn loại giao dịch");
        builder.setItems(transactionTypes, (dialog, which) -> {
            String oldType = transactionType;
            transactionType = which == 0 ? "income" : "expense";
            
            tvTransactionType.setText(transactionTypes[which]);
            
            // Cập nhật badge và màu sắc
            displayTransactionTypeAndBadge();
            displayFormattedAmount();
            
            // Nếu thay đổi type, load lại categories
            if (!transactionType.equals(oldType)) {
                selectedCategory = null;
                tvCategory.setText("Chọn danh mục");
                ivCategoryIcon.setImageResource(R.drawable.ic_other);
                loadCategories();
            }
        });
        builder.show();
    }

    private void showCategoryDialog() {
        if (currentCategories.isEmpty()) {
            Toast.makeText(this, "Đang tải danh mục...", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] categoryNames = new String[currentCategories.size()];
        for (int i = 0; i < currentCategories.size(); i++) {
            categoryNames[i] = currentCategories.get(i).getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn danh mục");
        builder.setItems(categoryNames, (dialog, which) -> {
            selectedCategory = currentCategories.get(which);
            tvCategory.setText(selectedCategory.getName());
            
            // Cập nhật icon
            int iconRes = getCategoryIconResource(selectedCategory.getIcon());
            ivCategoryIcon.setImageResource(iconRes);
        });
        builder.show();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        if (transactionDateSeconds > 0) {
            calendar.setTimeInMillis(transactionDateSeconds * 1000);
        }
        
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    tvDate.setText(selectedDate);
                    
                    // Update the timestamp
                    Calendar cal = Calendar.getInstance();
                    cal.set(selectedYear, selectedMonth, selectedDay);
                    transactionDateSeconds = cal.getTimeInMillis() / 1000;
                },
                year, month, day
        );
        
        // Don't allow future dates
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void updateTransaction() {
        // Get updated values
        String newTitle = etTitle.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        
        if (newTitle.isEmpty()) {
            etTitle.setError("Vui lòng nhập tiêu đề");
            return;
        }
        
        if (amountStr.isEmpty()) {
            etAmount.setError("Vui lòng nhập số tiền");
            return;
        }
        
        if (selectedCategory == null) {
            Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            double newAmount = Double.parseDouble(amountStr);
            if (newAmount <= 0) {
                etAmount.setError("Số tiền phải lớn hơn 0");
                return;
            }
            
            // Cập nhật transaction trong Firebase
            Timestamp timestamp = new Timestamp(transactionDateSeconds, 0);
            
            firebaseService.updateTransaction(transactionId, newTitle, newAmount, transactionType, 
                    selectedCategory.getId(), timestamp)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật giao dịch thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            
        } catch (NumberFormatException e) {
            etAmount.setError("Số tiền không hợp lệ");
        }
    }

    private void deleteTransaction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xóa giao dịch");
        builder.setMessage("Bạn có chắc chắn muốn xóa giao dịch này không?");
        builder.setPositiveButton("Xóa", (dialog, which) -> {
            firebaseService.deleteTransaction(transactionId)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Xóa giao dịch thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
}
