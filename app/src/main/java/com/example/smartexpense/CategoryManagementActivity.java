package com.example.smartexpense;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartexpense.adapters.CategoryManageAdapter;
import com.example.smartexpense.model.Category;
import com.example.smartexpense.services.FirebaseService;

import java.util.ArrayList;
import java.util.List;

public class CategoryManagementActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvExpenseCategories;
    private RecyclerView rvIncomeCategories;
    private CategoryManageAdapter expenseAdapter;
    private CategoryManageAdapter incomeAdapter;
    private List<Category> expenseCategories;
    private List<Category> incomeCategories;
    private FirebaseService firebaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        firebaseService = FirebaseService.getInstance();
        expenseCategories = new ArrayList<>();
        incomeCategories = new ArrayList<>();

        initViews();
        setupRecyclerViews();
        loadCategories();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvExpenseCategories = findViewById(R.id.rvExpenseCategories);
        rvIncomeCategories = findViewById(R.id.rvIncomeCategories);
    }

    private void setupRecyclerViews() {
        // Setup Expense RecyclerView
        expenseAdapter = new CategoryManageAdapter(this, expenseCategories);
        rvExpenseCategories.setLayoutManager(new LinearLayoutManager(this));
        rvExpenseCategories.setAdapter(expenseAdapter);
        rvExpenseCategories.setNestedScrollingEnabled(false);

        // Setup Income RecyclerView
        incomeAdapter = new CategoryManageAdapter(this, incomeCategories);
        rvIncomeCategories.setLayoutManager(new LinearLayoutManager(this));
        rvIncomeCategories.setAdapter(incomeAdapter);
        rvIncomeCategories.setNestedScrollingEnabled(false);
    }

    private void loadCategories() {
        android.util.Log.d("CategoryManagement", "Starting to load categories...");

        // Load expense categories
        firebaseService.getCategoriesByType("expense")
                .addOnSuccessListener(categories -> {
                    android.util.Log.d("CategoryManagement", "SUCCESS: Loaded " + categories.size() + " expense categories");

                    if (categories.isEmpty()) {
                        android.util.Log.w("CategoryManagement", "No expense categories found!");
                    }

                    expenseCategories.clear();
                    expenseCategories.addAll(categories);

                    // Log each category
                    for (Category cat : categories) {
                        android.util.Log.d("CategoryManagement", "Expense category: " + cat.getName() + " (ID: " + cat.getId() + ")");
                    }

                    // Notify adapter on UI thread
                    runOnUiThread(() -> {
                        expenseAdapter.notifyDataSetChanged();
                        android.util.Log.d("CategoryManagement", "Expense adapter notified with " + expenseCategories.size() + " items");
                    });
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("CategoryManagement", "FAILED: Error loading expense categories", e);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Lỗi khi tải danh mục chi tiêu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                });

        // Load income categories
        firebaseService.getCategoriesByType("income")
                .addOnSuccessListener(categories -> {
                    android.util.Log.d("CategoryManagement", "SUCCESS: Loaded " + categories.size() + " income categories");

                    if (categories.isEmpty()) {
                        android.util.Log.w("CategoryManagement", "No income categories found!");
                    }

                    incomeCategories.clear();
                    incomeCategories.addAll(categories);

                    // Log each category
                    for (Category cat : categories) {
                        android.util.Log.d("CategoryManagement", "Income category: " + cat.getName() + " (ID: " + cat.getId() + ")");
                    }

                    // Notify adapter on UI thread
                    runOnUiThread(() -> {
                        incomeAdapter.notifyDataSetChanged();
                        android.util.Log.d("CategoryManagement", "Income adapter notified with " + incomeCategories.size() + " items");
                    });
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("CategoryManagement", "FAILED: Error loading income categories", e);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Lỗi khi tải danh mục thu nhập: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
}

