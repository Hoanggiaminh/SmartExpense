package com.example.smartexpense.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartexpense.R;
import com.example.smartexpense.model.CategoryStat;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CategoryStatAdapter extends RecyclerView.Adapter<CategoryStatAdapter.ViewHolder> {

    private Context context;
    private List<CategoryStat> categoryStats;

    public CategoryStatAdapter(Context context, List<CategoryStat> categoryStats) {
        this.context = context;
        this.categoryStats = categoryStats;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_stat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryStat stat = categoryStats.get(position);

        holder.tvCategoryName.setText(stat.getCategoryName());

        // Format amount
        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        String formattedAmount = numberFormat.format(stat.getAmount()) + "đ";
        holder.tvAmount.setText(formattedAmount);

        // Set percentage
        holder.tvPercentage.setText(String.format("%.0f%%", stat.getPercentage()));
        holder.progressBar.setProgress((int) stat.getPercentage());

        // Set progress bar color based on type
        if ("income".equals(stat.getType())) {
            holder.progressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_category_income));
            holder.tvAmount.setTextColor(context.getResources().getColor(R.color.income));
        } else {
            holder.progressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_category_expense));
            holder.tvAmount.setTextColor(context.getResources().getColor(R.color.expense));
        }

        // Set icon based on category name
        int iconRes = getIconResource(stat.getIcon());
        holder.ivCategoryIcon.setImageResource(iconRes);
    }

    @Override
    public int getItemCount() {
        return categoryStats.size();
    }

    private int getIconResource(String iconName) {
        if (iconName == null || iconName.isEmpty()) {
            return R.drawable.ic_general_icon;
        }

        int resourceId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
        return resourceId != 0 ? resourceId : R.drawable.ic_general_icon;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon;
        TextView tvCategoryName;
        TextView tvAmount;
        TextView tvPercentage;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}

