package com.example.smartexpense;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartexpense.utils.CurrencyUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.ViewHolder> {
    private List<CalendarDay> days;
    private final Context context;
    private final OnDateSelectedListener listener;
    private int selectedPosition = -1;
    private Map<String, Double> dayAmounts = new HashMap<>();

    public interface OnDateSelectedListener {
        void onDateSelected(Calendar date);
    }

    public CalendarDayAdapter(List<CalendarDay> days, OnDateSelectedListener listener) {
        this.days = days;
        this.listener = listener;
        this.context = null; // Will be set in onCreateViewHolder
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarDay day = days.get(position);

        // Set day text
        holder.tvDay.setText(String.valueOf(day.getDayOfMonth()));

        // Reset styles first
        holder.tvDay.setBackgroundResource(0); // Clear background
        holder.tvDay.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
        holder.tvAmount.setVisibility(View.GONE);

        // Handle previous month days (grayed out)
        if (day.isFromPreviousMonth()) {
            holder.tvDay.setTextColor(Color.parseColor("#CCCCCC"));
            holder.itemView.setEnabled(false);
            holder.itemView.setClickable(false);
        } 
        // Handle future dates (grayed out and disabled)
        else if (day.isFutureDate()) {
            holder.tvDay.setTextColor(Color.parseColor("#CCCCCC"));
            holder.itemView.setEnabled(false);
            holder.itemView.setClickable(false);
        }
        else {
            holder.itemView.setEnabled(true);
            holder.itemView.setClickable(true);
            
            // Show amount if available
            String dateKey = getDateKey(day.getDate());
            if (dayAmounts.containsKey(dateKey)) {
                Double amount = dayAmounts.get(dateKey);
                if (amount != null && amount != 0) {
                    holder.tvAmount.setVisibility(View.VISIBLE);
                    holder.tvAmount.setText(CurrencyUtils.formatNumber(Math.abs(amount)));

                    // Set color based on positive/negative
                    if (amount > 0) {
                        holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.income));
                    } else {
                        holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.expense));
                    }
                }
            }

            // Highlight today with border
            if (day.isToday()) {
                try {
                    holder.tvDay.setBackgroundResource(R.drawable.bg_calendar_today);
                    holder.tvDay.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
                } catch (Exception e) {
                    // Fallback if drawable not found
                    holder.tvDay.setTextColor(Color.BLUE);
                }
            }
            
            // Highlight selected day with solid background
            if (position == selectedPosition) {
                try {
                    holder.tvDay.setBackgroundResource(R.drawable.bg_calendar_selected);
                    holder.tvDay.setTextColor(Color.WHITE);
                } catch (Exception e) {
                    // Fallback if drawable not found
                    holder.tvDay.setBackgroundColor(Color.BLUE);
                    holder.tvDay.setTextColor(Color.WHITE);
                }
            }
        }
        
        // Set click listener - only allow clicking on valid dates (not previous month, not future)
        holder.itemView.setOnClickListener(v -> {
            if (!day.isFromPreviousMonth() && !day.isFutureDate()) {
                int previousSelected = selectedPosition;
                selectedPosition = holder.getBindingAdapterPosition();
                
                // Notify changes for visual update
                if (previousSelected != -1 && previousSelected != selectedPosition) {
                    notifyItemChanged(previousSelected);
                }
                if (selectedPosition != -1) {
                    notifyItemChanged(selectedPosition);
                }
                
                // Call listener
                if (listener != null) {
                    listener.onDateSelected(day.getDate());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public void updateDays(List<CalendarDay> newDays) {
        this.days = newDays;
        this.selectedPosition = -1; // Reset selection when month changes
        notifyDataSetChanged();
    }

    public void setDayAmounts(Map<String, Double> amounts) {
        this.dayAmounts = amounts;
        notifyDataSetChanged();
    }

    public void setSelectedDate(Calendar selectedDate) {
        selectedPosition = -1;
        for (int i = 0; i < days.size(); i++) {
            CalendarDay day = days.get(i);
            if (!day.isFromPreviousMonth() && !day.isFutureDate() && isSameDay(day.getDate(), selectedDate)) {
                selectedPosition = i;
                break;
            }
        }
        notifyDataSetChanged();
    }

    private String getDateKey(Calendar date) {
        return String.format(Locale.US, "%04d-%02d-%02d",
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH));
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;
        TextView tvAmount;
        LinearLayout dayContainer;

        ViewHolder(View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            dayContainer = itemView.findViewById(R.id.dayContainer);
        }
    }
}
