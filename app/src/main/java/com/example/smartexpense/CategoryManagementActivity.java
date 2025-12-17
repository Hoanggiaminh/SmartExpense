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
    private CategoryManageAdapter categoryAdapter;
    private List<Object> categoryItems; // Mix of headers (String) and categories (Category)
    private FirebaseService firebaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        firebaseService = FirebaseService.getInstance();
        categoryItems = new ArrayList<>();

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
        // Setup single RecyclerView with adapter supporting headers
        categoryAdapter = new CategoryManageAdapter(this, categoryItems);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setAdapter(categoryAdapter);

        // Set click listeners for edit/delete actions
        categoryAdapter.setOnCategoryActionListener(new CategoryManageAdapter.OnCategoryActionListener() {
            @Override
            public void onEditCategory(Category category) {
                showEditCategoryDialog(category);
            }

            @Override
            public void onDeleteCategory(Category category) {
                showDeleteCategoryDialog(category);
            }
        });
    }

    private void loadCategories() {
        // Load income categories
        firebaseService.getCategoriesByType("income")
                .addOnSuccessListener(incomeCategories -> {
                    // Load expense categories
                    firebaseService.getCategoriesByType("expense")
                            .addOnSuccessListener(expenseCategories -> {
                                // Combine all categories with headers
                                buildCategoryList(incomeCategories, expenseCategories);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Lỗi khi tải danh mục chi tiêu", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải danh mục thu nhập", Toast.LENGTH_SHORT).show();
                });
    }

    private void buildCategoryList(List<Category> incomeCategories, List<Category> expenseCategories) {
        categoryItems.clear();

        // Add Income section
        if (!incomeCategories.isEmpty()) {
            categoryItems.add("Thu nhập"); // Header
            categoryItems.addAll(incomeCategories); // Income categories
        }

        // Add Expense section
        if (!expenseCategories.isEmpty()) {
            categoryItems.add("Chi tiêu"); // Header
            categoryItems.addAll(expenseCategories); // Expense categories
        }

        // Update adapter
        categoryAdapter.updateItems(categoryItems);
    }

    private void showEditCategoryDialog(Category category) {
        AddEditCategoryDialog dialog = new AddEditCategoryDialog(this, category, new AddEditCategoryDialog.OnCategorySaveListener() {
            @Override
            public void onSave(Category editedCategory) {
                // Reload categories after edit
                loadCategories();
            }
        });
        dialog.show();
    }

    private void showDeleteCategoryDialog(Category category) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Xóa danh mục");
        builder.setMessage("Bạn có chắc chắn muốn xóa danh mục '" + category.getName() + "'?\n\nLưu ý: Tất cả giao dịch thuộc danh mục này sẽ được chuyển về danh mục 'Khác' tương ứng.");

        builder.setPositiveButton("Xóa", (dialog, which) -> {
            deleteCategory(category);
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void deleteCategory(Category category) {
        firebaseService.deleteCategoryAndMoveTransactions(category)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Xóa danh mục thành công!", Toast.LENGTH_LONG).show();
                    // Reload categories after deletion
                    loadCategories();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi xóa danh mục: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload categories when returning to this activity
        loadCategories();
    }
}
