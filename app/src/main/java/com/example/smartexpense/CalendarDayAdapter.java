package com.example.smartexpense;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Calendar;
import java.util.List;

public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.ViewHolder> {
    private List<CalendarDay> days;
    private OnDateSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnDateSelectedListener {
        void onDateSelected(Calendar date);
    }

    public CalendarDayAdapter(List<CalendarDay> days, OnDateSelectedListener listener) {
        this.days = days;
        this.listener = listener;
    }

    public void updateDays(List<CalendarDay> newDays) {
        this.days = newDays;
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarDay day = days.get(position);
        holder.bind(day, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayNumber;
        TextView tvDayName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            tvDayName = itemView.findViewById(R.id.tvDayName);

            // Ẩn tên ngày
            tvDayName.setVisibility(View.GONE);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    CalendarDay day = days.get(position);
                    if (!day.isOtherMonth()) {
                        selectedPosition = position;
                        notifyDataSetChanged();
                        if (listener != null) {
                            listener.onDateSelected(day.getDate());
                        }
                    }
                }
            });
        }

        void bind(CalendarDay day, boolean isSelected) {
            Calendar date = day.getDate();
            tvDayNumber.setText(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));

            Calendar today = Calendar.getInstance();
            boolean isToday = date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    date.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);

            if (day.isOtherMonth()) {
                tvDayNumber.setTextColor(Color.parseColor("#CCCCCC"));
                tvDayNumber.setBackgroundResource(R.drawable.bg_calendar_day);
            } else if (isSelected) {
                tvDayNumber.setTextColor(Color.WHITE);
                tvDayNumber.setBackgroundResource(R.drawable.bg_calendar_day_selected);
            } else if (isToday) {
                tvDayNumber.setTextColor(Color.parseColor("#3B82F6"));
                tvDayNumber.setBackgroundResource(R.drawable.bg_calendar_day);
            } else {
                tvDayNumber.setTextColor(Color.parseColor("#333333"));
                tvDayNumber.setBackgroundResource(R.drawable.bg_calendar_day);
            }
        }
    }
}
