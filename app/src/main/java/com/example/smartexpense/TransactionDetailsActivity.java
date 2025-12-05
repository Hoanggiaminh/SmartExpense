package com.example.smartexpense;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.DatePicker;
import androidx.appcompat.app.AlertDialog;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class TransactionDetailsActivity extends AppCompatActivity {

    ImageView btnBack;
    LinearLayout layoutTransactionType, layoutCategory, layoutDate;
    TextView tvTransactionType, tvCategory, tvDate;

    // Data arrays
    private String[] transactionTypes = {"Thu nhập", "Chi tiêu"};
    private String[] categories = {"Lương thưởng", "Đầu tư", "Kinh doanh", "Ăn uống", "Di chuyển", "Mua sắm", "Giải trí", "Y tế", "Giáo dục", "Khác"};

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

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        layoutTransactionType = findViewById(R.id.layout_transaction_type);
        layoutCategory = findViewById(R.id.layout_category);
        layoutDate = findViewById(R.id.layout_date);
        tvTransactionType = findViewById(R.id.tv_transaction_type);
        tvCategory = findViewById(R.id.tv_category);
        tvDate = findViewById(R.id.tv_date);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Transaction Type Dropdown
        layoutTransactionType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTransactionTypeDialog();
            }
        });

        // Category Dropdown
        layoutCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategoryDialog();
            }
        });

        // Date Picker
        layoutDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }

    private void showTransactionTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn loại giao dịch");
        builder.setItems(transactionTypes, (dialog, which) -> {
            tvTransactionType.setText(transactionTypes[which]);
        });
        builder.show();
    }

    private void showCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn danh mục");
        builder.setItems(categories, (dialog, which) -> {
            tvCategory.setText(categories[which]);
        });
        builder.show();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);
                        tvDate.setText(selectedDate);
                    }
                },
                year, month, day
        );
        datePickerDialog.show();
    }
}