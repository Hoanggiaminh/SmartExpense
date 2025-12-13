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

public class CategoryFilterAdapter extends RecyclerView.Adapter<CategoryFilterAdapter.ViewHolder> {
    private List<Category> categories;
    private Context context;
    private String selectedCategoryId;
    private OnCategorySelectedListener listener;

    public interface OnCategorySelectedListener {
        void onCategorySelected(Category category);
    }

    public CategoryFilterAdapter(Context context, List<Category> categories) {
        this.context = context;
        this.categories = categories;
        this.selectedCategoryId = "all"; // Default to "Tất cả"
    }

    public void setOnCategorySelectedListener(OnCategorySelectedListener listener) {
        this.listener = listener;
    }

    public void setSelectedCategoryId(String categoryId) {
        this.selectedCategoryId = categoryId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_filter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);

        holder.tvCategoryName.setText(category.getName());

        // Set category icon
        int iconRes = getCategoryIconResource(category.getIcon());
        holder.ivCategoryIcon.setImageResource(iconRes);

        // Show/hide selected indicator
        boolean isSelected = category.getId().equals(selectedCategoryId);
        holder.ivSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategorySelected(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    private int getCategoryIconResource(String iconName) {
        if (iconName == null) return R.drawable.ic_other;

        switch (iconName) {
            case "ic_all": return R.drawable.ic_all;
            case "ic_salary": return R.drawable.ic_salary;
            case "ic_bonus": return R.drawable.ic_bonus;
            case "ic_investment": return R.drawable.ic_investment;
            case "ic_food": return R.drawable.ic_food;
            case "ic_transport": return R.drawable.ic_transport;
            case "ic_shopping": return R.drawable.ic_shopping;
            case "ic_entertainment": return R.drawable.ic_entertainment;
            case "ic_health": return R.drawable.ic_health;
            case "ic_education": return R.drawable.ic_education;
            case "ic_bills": return R.drawable.ic_bills;
            case "ic_other":
            default:
                return R.drawable.ic_other;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon;
        TextView tvCategoryName;
        ImageView ivSelected;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            ivSelected = itemView.findViewById(R.id.iv_selected);
        }
    }
}
