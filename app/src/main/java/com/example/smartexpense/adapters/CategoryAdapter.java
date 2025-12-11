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

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<Category> categories;
    private Context context;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(Context context, List<Category> categories) {
        this.context = context;
        this.categories = categories;
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateCategories(List<Category> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon;
        private TextView tvName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvName = itemView.findViewById(R.id.tvCategoryName);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onCategoryClick(categories.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Category category) {
            tvName.setText(category.getName());

            // Set icon based on iconName
            int iconResource = getIconResource(category.getIcon());
            if (iconResource != 0) {
                ivIcon.setImageResource(iconResource);
            }
        }

        private int getIconResource(String iconName) {
            switch (iconName) {
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
                case "ic_other": return R.drawable.ic_other;
                default: return R.drawable.ic_other;
            }
        }
    }
}
