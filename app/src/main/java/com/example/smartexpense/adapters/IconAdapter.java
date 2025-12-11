package com.example.smartexpense.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartexpense.R;
import java.util.List;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder> {
    private List<IconItem> icons;
    private Context context;
    private int selectedPosition = 0;
    private OnIconSelectedListener listener;

    public interface OnIconSelectedListener {
        void onIconSelected(IconItem icon);
    }

    public IconAdapter(Context context, List<IconItem> icons) {
        this.context = context;
        this.icons = icons;
    }

    public void setOnIconSelectedListener(OnIconSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_icon, parent, false);
        return new IconViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
        IconItem icon = icons.get(position);
        holder.bind(icon, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return icons.size();
    }

    public IconItem getSelectedIcon() {
        return icons.get(selectedPosition);
    }

    class IconViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon;
        private View background;

        public IconViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            background = itemView.findViewById(R.id.iconBackground);

            itemView.setOnClickListener(v -> {
                int previousSelected = selectedPosition;
                selectedPosition = getAdapterPosition();

                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);

                if (listener != null) {
                    listener.onIconSelected(icons.get(selectedPosition));
                }
            });
        }

        public void bind(IconItem icon, boolean isSelected) {
            ivIcon.setImageResource(icon.getResourceId());

            if (isSelected) {
                background.setBackgroundResource(R.drawable.bg_blue_rounded);
                ivIcon.setColorFilter(context.getResources().getColor(R.color.white));
            } else {
                background.setBackgroundResource(0);
                ivIcon.setColorFilter(context.getResources().getColor(R.color.text_secondary));
            }
        }
    }

    public static class IconItem {
        private String name;
        private int resourceId;

        public IconItem(String name, int resourceId) {
            this.name = name;
            this.resourceId = resourceId;
        }

        public String getName() {
            return name;
        }

        public int getResourceId() {
            return resourceId;
        }
    }
}
