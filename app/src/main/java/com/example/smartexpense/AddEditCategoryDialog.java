package com.example.smartexpense;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartexpense.model.Category;
import com.example.smartexpense.services.FirebaseService;

public class AddEditCategoryDialog extends Dialog {

    private EditText etCategoryName;
    private TextView btnCancel, btnUpdate;
    private Category category; // Null for add, filled for edit
    private OnCategorySaveListener listener;
    private FirebaseService firebaseService;

    public interface OnCategorySaveListener {
        void onSave(Category category);
    }

    // Constructor for editing existing category
    public AddEditCategoryDialog(Context context, Category category, OnCategorySaveListener listener) {
        super(context);
        this.category = category;
        this.listener = listener;
        this.firebaseService = FirebaseService.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_edit_category);

        initViews();
        setupClickListeners();

        // Pre-fill data if editing
        if (category != null) {
            etCategoryName.setText(category.getName());
        }
    }

    private void initViews() {
        etCategoryName = findViewById(R.id.etCategoryName);
        btnCancel = findViewById(R.id.btnCancel);
        btnUpdate = findViewById(R.id.btnUpdate);
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> dismiss());

        btnUpdate.setOnClickListener(v -> {
            String categoryName = etCategoryName.getText().toString().trim();

            if (TextUtils.isEmpty(categoryName)) {
                Toast.makeText(getContext(), "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update category name
            category.setName(categoryName);

            // Update in Firebase
            firebaseService.updateCategory(category.getId(), category)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Cập nhật danh mục thành công!", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onSave(category);
                    }
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi cập nhật danh mục: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });
    }
}
