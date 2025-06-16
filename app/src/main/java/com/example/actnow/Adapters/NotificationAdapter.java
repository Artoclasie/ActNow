package com.example.actnow.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.actnow.Models.NotificationModel;
import com.example.actnow.R;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final Context context;
    private final List<NotificationModel> notifications;
    private final OnNotificationListener listener;

    public interface OnNotificationListener {
        void onNotificationClick(NotificationModel notification);
        void onNotificationLongClick(NotificationModel notification, int position);
        void onDeleteClick(NotificationModel notification, int position);
    }

    public NotificationAdapter(Context context, List<NotificationModel> notifications, OnNotificationListener listener) {
        this.context = context;
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationModel notification = notifications.get(position);
        holder.bind(notification);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onNotificationLongClick(notification, position);
                return true;
            }
            return false;
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(notification, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void showDeleteButton(int position) {
        notifications.get(position).setShowDeleteButton(true);
        notifyItemChanged(position);
    }

    public void hideDeleteButton(int position) {
        notifications.get(position).setShowDeleteButton(false);
        notifyItemChanged(position);
    }

    public void removeNotification(int position) {
        notifications.remove(position);
        notifyItemRemoved(position);
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        MaterialButton btnDelete; // Изменил тип на MaterialButton

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvMessage = itemView.findViewById(R.id.tv_notification_message);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
            btnDelete = itemView.findViewById(R.id.btn_delete); // Теперь ожидается MaterialButton
        }

        public void bind(NotificationModel notification) {
            tvTitle.setText(notification.getTitle());
            tvMessage.setText(notification.getMessage());

            // Форматирование даты и времени в формате "HH:mm dd.MM.yyyy"
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault());
            String time = sdf.format(new Date(notification.getCreatedAt().getSeconds() * 1000));
            tvTime.setText(time);

            // Показываем/скрываем кнопку удаления
            btnDelete.setVisibility(notification.isShowDeleteButton() ? View.VISIBLE : View.GONE);
        }
    }
}