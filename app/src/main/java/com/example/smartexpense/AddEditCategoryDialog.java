package com.example.smartexpense;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartexpense.adapters.IconAdapter;
import com.example.smartexpense.model.Category;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddEditCategoryDialog extends Dialog {

    private TextView tvDialogTitle;
    private TextInputEditText etCategoryName;
    private RadioButton rbIncome, rbExpense;
    private RecyclerView rvIcons;
    private MaterialButton btnCancel, btnSave;

    private Category category;
    private OnCategorySaveListener listener;
    private IconAdapter iconAdapter;
    private String selectedIcon = "ic_other";

    // Available icons
    private String[] availableIcons = {
            "ic_food", "ic_transport", "ic_shopping", "ic_entertainment",
            "ic_health", "ic_education", "ic_bills", "ic_salary",
            "ic_bonus", "ic_investment", "ic_wallet", "ic_other"
    };

    public interface OnCategorySaveListener {
        void onSave(Category category);
    }

    public AddEditCategoryDialog(@NonNull Context context, Category category, OnCategorySaveListener listener) {
        super(context);
        this.category = category;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_edit_category);

        initViews();
        setupIconGrid();
        populateFields();
        setupClickListeners();
    }

    private void initViews() {
        tvDialogTitle = findViewById(R.id.tvDialogTitle);
        etCategoryName = findViewById(R.id.etCategoryName);
        rbIncome = findViewById(R.id.rbIncome);
        rbExpense = findViewById(R.id.rbExpense);
        rvIcons = findViewById(R.id.rvIcons);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupIconGrid() {
        iconAdapter = new IconAdapter(getContext(), availableIcons, selectedIcon);
        iconAdapter.setOnIconSelectedListener(icon -> {
            selectedIcon = icon;
        });

        rvIcons.setLayoutManager(new GridLayoutManager(getContext(), 6));
        rvIcons.setAdapter(iconAdapter);
    }

    private void populateFields() {
        if (category != null) {
            // Edit mode
            tvDialogTitle.setText("Edit category");
            etCategoryName.setText(category.getName());
            selectedIcon = category.getIcon();

            if ("income".equals(category.getType())) {
                rbIncome.setChecked(true);
            } else {
                rbExpense.setChecked(true);
            }

            iconAdapter.setSelectedIcon(selectedIcon);
        } else {
            // Add mode
            tvDialogTitle.setText("Add new category");
            rbExpense.setChecked(true);
        }
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etCategoryName.getText().toString().trim();

            if (name.isEmpty()) {
                etCategoryName.setError("Vui lòng nhập tên danh mục");
                return;
            }

            String type = rbIncome.isChecked() ? "income" : "expense";

            if (category != null) {
                // Update existing category
                category.setName(name);
                category.setIcon(selectedIcon);
                category.setType(type);
            } else {
                // Create new category
                category = new Category(null, name, selectedIcon, type);
            }

            if (listener != null) {
                listener.onSave(category);
            }

            dismiss();
        });
    }
}

