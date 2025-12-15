package com.example.smartexpense.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartexpense.R;
import com.example.smartexpense.model.TransactionItem;
import com.example.smartexpense.utils.CurrencyUtils;

import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private List<TransactionItem> transactions;
    private Context context;
    private OnTransactionClickListener clickListener;

    public interface OnTransactionClickListener {
        void onTransactionClick(TransactionItem transaction);
    }

    public TransactionAdapter(Context context, List<TransactionItem> transactions) {
        this.context = context;
        this.transactions = transactions;
    }

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionItem item = transactions.get(position);

        // Hiển thị date header nếu cần
        if (item.isShowDateHeader()) {
            holder.tvDateHeader.setVisibility(View.VISIBLE);
            holder.tvDateHeader.setText(item.getDateHeaderText());
        } else {
            holder.tvDateHeader.setVisibility(View.GONE);
        }

        // Set transaction data
        holder.tvTransactionTitle.setText(item.getTitle());
        holder.tvCategoryName.setText(item.getCategoryName());

        // Set category icon
        int iconRes = getCategoryIconResource(item.getCategoryIcon());
        holder.ivCategoryIcon.setImageResource(iconRes);

        // Format and set amount with proper color
        String formattedAmount;
        int amountColor;

        if ("income".equals(item.getType())) {
            formattedAmount = "+ " + CurrencyUtils.formatCurrency(item.getAmount());
            amountColor = ContextCompat.getColor(context, R.color.income);
        } else {
            formattedAmount = "- " + CurrencyUtils.formatCurrency(item.getAmount());
            amountColor = ContextCompat.getColor(context, R.color.expense);
        }

        holder.tvAmount.setText(formattedAmount);
        holder.tvAmount.setTextColor(amountColor);

        // Set click listener on the transaction row (allow clicks even when date header is visible)
        holder.layoutTransactionItem.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onTransactionClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    private int getCategoryIconResource(String iconName) {
        if (iconName == null) return R.drawable.ic_other;

        switch (iconName) {
            case "ic_salary": return R.drawable.ic_salary;
            case "ic_bonus": return R.drawable.ic_bonus;
            case "ic_investment": return R.drawable.ic_investment;
            case "ic_food": return R.drawable.ic_food;
            case "ic_shopping": return R.drawable.ic_shopping;
            case "ic_bills": return R.drawable.ic_bills;
            case "ic_health": return R.drawable.ic_health;
            case "ic_entertainment": return R.drawable.ic_entertainment;
            case "ic_education": return R.drawable.ic_education;
            case "ic_transport": return R.drawable.ic_transport;
            default: return R.drawable.ic_other;
        }
    }

    public void updateTransactions(List<TransactionItem> newTransactions) {
        this.transactions = newTransactions;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateHeader, tvTransactionTitle, tvCategoryName, tvAmount;
        ImageView ivCategoryIcon;
        View layoutTransactionItem;

        ViewHolder(View itemView) {
            super(itemView);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
            tvTransactionTitle = itemView.findViewById(R.id.tvTransactionTitle);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            layoutTransactionItem = itemView.findViewById(R.id.layout_transaction_item);
        }
    }
}
