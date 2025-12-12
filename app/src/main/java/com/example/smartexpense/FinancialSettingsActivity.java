package com.example.smartexpense;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class FinancialSettingsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private MaterialCardView btnCurrencySettings, btnCategoryManagement, btnBudgetCycle;
    private TextView tvCurrentCurrency, tvBudgetCycle;
    private String selectedCurrency = "VND";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financial_settings);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnCurrencySettings = findViewById(R.id.btnCurrencySettings);
        btnCategoryManagement = findViewById(R.id.btnCategoryManagement);
        btnBudgetCycle = findViewById(R.id.btnBudgetCycle);
        tvCurrentCurrency = findViewById(R.id.tvCurrentCurrency);
        tvBudgetCycle = findViewById(R.id.tvBudgetCycle);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnCurrencySettings.setOnClickListener(v -> showCurrencyDialog());

        btnCategoryManagement.setOnClickListener(v -> {
            Intent intent = new Intent(FinancialSettingsActivity.this, CategoryManagementActivity.class);
            startActivity(intent);
        });

        btnBudgetCycle.setOnClickListener(v -> showBudgetCycleDialog());
    }

    private void showCurrencyDialog() {
        String[] currencies = {"VND - Việt Nam Đồng", "USD - US Dollar"};
        int checkedItem = selectedCurrency.equals("VND") ? 0 : 1;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn đơn vị tiền tệ")
                .setSingleChoiceItems(currencies, checkedItem, (dialog, which) -> {
                    selectedCurrency = which == 0 ? "VND" : "USD";
                    tvCurrentCurrency.setText(currencies[which]);
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showBudgetCycleDialog() {
        String[] cycles = {
                "Ngày 1 hàng tháng",
                "Ngày 15 hàng tháng",
                "Ngày cuối tháng",
                "Tùy chỉnh"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn chu kỳ ngân sách")
                .setItems(cycles, (dialog, which) -> {
                    tvBudgetCycle.setText(cycles[which]);
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}

