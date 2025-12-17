package com.example.smartexpense.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartexpense.R;
import com.example.smartexpense.model.Category;

import java.util.List;

public class CategoryManageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CATEGORY = 1;

    private Context context;
    private List<Object> items; // Mix of String (headers) and Category objects
    private OnCategoryActionListener listener;

    public interface OnCategoryActionListener {
        void onEditCategory(Category category);
        void onDeleteCategory(Category category);
    }

    public CategoryManageAdapter(Context context, List<Object> items) {
        this.context = context;
        this.items = items;
    }

    public void setOnCategoryActionListener(OnCategoryActionListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_HEADER : TYPE_CATEGORY;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_category_section_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_category_manage, parent, false);
            return new CategoryViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            String title = (String) items.get(position);
            headerHolder.tvSectionTitle.setText(title);
        } else if (holder instanceof CategoryViewHolder) {
            CategoryViewHolder categoryHolder = (CategoryViewHolder) holder;
            Category category = (Category) items.get(position);

            categoryHolder.tvCategoryName.setText(category.getName());

            // Set category icon
            int iconResId = getCategoryIconResource(category.getIcon());
            categoryHolder.ivCategoryIcon.setImageResource(iconResId);

            // Check if category is custom (user-created) or default (system)
            boolean isCustomCategory = !isDefaultCategory(category.getName());

            if (isCustomCategory) {
                // Show edit and delete buttons for custom categories
                categoryHolder.ivEditCategory.setVisibility(View.VISIBLE);
                categoryHolder.ivDeleteCategory.setVisibility(View.VISIBLE);

                categoryHolder.ivEditCategory.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onEditCategory(category);
                    }
                });

                categoryHolder.ivDeleteCategory.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDeleteCategory(category);
                    }
                });
            } else {
                // Hide edit and delete buttons for default categories
                categoryHolder.ivEditCategory.setVisibility(View.GONE);
                categoryHolder.ivDeleteCategory.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItems(List<Object> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    private int getCategoryIconResource(String iconName) {
        if (iconName == null) return R.drawable.ic_other;

        // Try to get icon by name first
        int iconResId = context.getResources().getIdentifier(
                iconName, "drawable", context.getPackageName());

        if (iconResId != 0) {
            return iconResId;
        }

        // Fallback to predefined icons
        switch (iconName) {
            case "ic_salary":
                return R.drawable.ic_salary;
            case "ic_bonus":
                return R.drawable.ic_bonus;
            case "ic_investment":
                return R.drawable.ic_investment;
            case "ic_food":
                return R.drawable.ic_food;
            case "ic_shopping":
                return R.drawable.ic_shopping;
            case "ic_bills":
                return R.drawable.ic_bills;
            case "ic_health":
                return R.drawable.ic_health;
            case "ic_entertainment":
                return R.drawable.ic_entertainment;
            case "ic_education":
                return R.drawable.ic_education;
            case "ic_transport":
                return R.drawable.ic_transport;
            case "ic_other":
                return R.drawable.ic_other;
            default:
                return R.drawable.ic_other;
        }
    }

    private boolean isDefaultCategory(String categoryName) {
        // Define default categories that cannot be edited/deleted
        String[] defaultIncomeCategories = {"Lương", "Thưởng", "Đầu tư", "Khác (thu)"};
        String[] defaultExpenseCategories = {"Ăn uống", "Mua sắm", "Hóa đơn", "Y tế",
                "Giải trí", "Giáo dục", "Di chuyển", "Khác (chi)"};

        for (String defaultCategory : defaultIncomeCategories) {
            if (defaultCategory.equals(categoryName)) {
                return true;
            }
        }

        for (String defaultCategory : defaultExpenseCategories) {
            if (defaultCategory.equals(categoryName)) {
                return true;
            }
        }

        return false;
    }

    // Header ViewHolder
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvSectionTitle;

        HeaderViewHolder(View itemView) {
            super(itemView);
            tvSectionTitle = itemView.findViewById(R.id.tvSectionTitle);
        }
    }

    // Category ViewHolder
    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon;
        TextView tvCategoryName;
        ImageView ivEditCategory;
        ImageView ivDeleteCategory;

        CategoryViewHolder(View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            ivEditCategory = itemView.findViewById(R.id.ivEditCategory);
            ivDeleteCategory = itemView.findViewById(R.id.ivDeleteCategory);
        }
    }
}
