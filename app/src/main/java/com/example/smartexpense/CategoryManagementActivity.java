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
    private RecyclerView rvCategories;
    private CategoryManageAdapter adapter;
    private List<Category> categories;
    private FirebaseService firebaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        firebaseService = FirebaseService.getInstance();
        categories = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadCategories();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvCategories = findViewById(R.id.rvCategories);
    }

    private void setupRecyclerView() {
        adapter = new CategoryManageAdapter(this, categories);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setAdapter(adapter);
    }

    private void loadCategories() {
        // Load both income and expense categories
        firebaseService.getCategoriesByType("income")
                .addOnSuccessListener(incomeCategories -> {
                    categories.clear();
                    categories.addAll(incomeCategories);

                    firebaseService.getCategoriesByType("expense")
                            .addOnSuccessListener(expenseCategories -> {
                                categories.addAll(expenseCategories);
                                adapter.updateCategories(categories);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Lỗi khi tải danh mục chi tiêu", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải danh mục", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
}

