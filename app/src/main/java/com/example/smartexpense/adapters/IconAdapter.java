package com.example.smartexpense.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartexpense.R;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.ViewHolder> {

    private Context context;
    private String[] icons;
    private String selectedIcon;
    private OnIconSelectedListener listener;

    public interface OnIconSelectedListener {
        void onIconSelected(String icon);
    }

    public IconAdapter(Context context, String[] icons, String selectedIcon) {
        this.context = context;
        this.icons = icons;
        this.selectedIcon = selectedIcon;
    }

    public void setOnIconSelectedListener(OnIconSelectedListener listener) {
        this.listener = listener;
    }

    public void setSelectedIcon(String icon) {
        this.selectedIcon = icon;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_icon, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String icon = icons[position];

        // Set icon
        int iconResId = context.getResources().getIdentifier(icon, "drawable", context.getPackageName());
        if (iconResId != 0) {
            holder.ivIcon.setImageResource(iconResId);
        }

        // Highlight selected icon
        if (icon.equals(selectedIcon)) {
            holder.itemView.setBackgroundResource(R.drawable.bg_calendar_selected);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_calendar_day);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            selectedIcon = icon;
            notifyDataSetChanged();

            if (listener != null) {
                listener.onIconSelected(icon);
            }
        });
    }

    @Override
    public int getItemCount() {
        return icons.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;

        ViewHolder(View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }

    /**
     * Simple data class to hold icon information (name and drawable resource ID)
     */
    public static class IconItem {
        public final String name;
        public final int drawableResId;

        public IconItem(String name, int drawableResId) {
            this.name = name;
            this.drawableResId = drawableResId;
        }
    }
}

