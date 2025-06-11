package com.example.actnow.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.actnow.Models.EventModel;
import com.example.actnow.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private static final String TAG = "EventAdapter";

    private Context context;
    private List<EventModel> events;
    private OnEventClickListener onEventClickListener;

    // Интерфейс для обработки нажатий
    public interface OnEventClickListener {
        void onEventClick(EventModel event);
    }

    public EventAdapter(Context context, List<EventModel> events, OnEventClickListener listener) {
        this.context = context;
        this.events = events;
        this.onEventClickListener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.eventcontent, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventModel event = events.get(position);

        // Логируем данные мероприятия для отладки
        Log.d(TAG, "Binding event: " + event.getEventId() + ", Title: " + event.getTitle() +
                ", StartDate: " + event.getStartDate() + ", EndDate: " + event.getEndDate() +
                ", Address: " + event.getAddress() + ", coverImage: " + event.getCoverImage());

        // Заголовок
        holder.tvEventTitle.setText(event.getTitle() != null ? event.getTitle() : "Без названия");

        // Изображение
        if (event.getCoverImage() != null && !event.getCoverImage().isEmpty()) {
            Log.d(TAG, "Loading image: " + event.getCoverImage());
            Glide.with(context)
                    .load(event.getCoverImage())
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image)
                    .into(holder.imvEventContent);
        } else {
            Log.w(TAG, "Cover image is null or empty for event " + event.getEventId());
            holder.imvEventContent.setImageResource(R.drawable.default_image);
        }

        // Количество участников
        holder.tvParticipants.setText(event.getCurrentParticipants() + " / " + event.getMaxParticipants() + " участников");

        // Дата и время
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d • HH:mm", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Minsk"));
        String eventDetails = dateFormat.format(event.getStartDate().toDate());
        holder.tvDateTime.setText(eventDetails + " (EUROPE/Minsk)");

        // Местоположение (уже отображается отдельно)
        holder.tvLocation.setText(event.getAddress() != null && !event.getAddress().isEmpty() ? event.getAddress() : "Местоположение не указано");

        // Теги
        holder.chipGroupCategories.removeAllViews();
        if (event.getTags() != null && !event.getTags().isEmpty()) {
            for (String tag : event.getTags()) {
                Chip chip = new Chip(context);
                chip.setText(tag);
                chip.setEnabled(false);
                holder.chipGroupCategories.addView(chip);
            }
        } else {
            Chip chip = new Chip(context);
            chip.setText("Без категорий");
            chip.setEnabled(false);
            holder.chipGroupCategories.addView(chip);
        }

        // Обработка нажатий
        holder.itemView.setOnClickListener(v -> {
            if (onEventClickListener != null) {
                onEventClickListener.onEventClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView imvEventContent;
        TextView tvParticipants, tvEventTitle, tvDateTime, tvLocation;
        ChipGroup chipGroupCategories;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            imvEventContent = itemView.findViewById(R.id.imv_event_content);
            tvParticipants = itemView.findViewById(R.id.tv_participants);
            tvEventTitle = itemView.findViewById(R.id.tv_event_title);
            tvDateTime = itemView.findViewById(R.id.tv_date_time);
            tvLocation = itemView.findViewById(R.id.tv_location);
            chipGroupCategories = itemView.findViewById(R.id.chip_group_categories);
        }
    }
}