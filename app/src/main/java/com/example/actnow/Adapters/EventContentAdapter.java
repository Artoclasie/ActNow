package com.example.actnow.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.actnow.Fragments.EventDetailFragment;
import com.example.actnow.Models.EventModel;
import com.example.actnow.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class EventContentAdapter extends RecyclerView.Adapter<EventContentAdapter.ViewHolder> {

    private List<EventModel> eventList;
    private final Context context;

    public EventContentAdapter(List<EventModel> eventList, Context context) {
        this.eventList = eventList != null ? new ArrayList<>(eventList) : new ArrayList<>();
        if (this.eventList != null) {
            Collections.reverse(this.eventList);
        }
        this.context = context;
    }

    public void updateEvents(List<EventModel> newEvents) {
        this.eventList = new ArrayList<>(newEvents);
        Collections.reverse(this.eventList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.eventcontent, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EventModel event = eventList.get(position);

        // Установка заголовка
        holder.tvEventTitle.setText(event.getTitle() != null ? event.getTitle() : "Без названия");

        // Количество участников
        holder.tvParticipants.setText(event.getCurrentParticipants() + " / " + event.getMaxParticipants() + " участников");

        // Форматирование даты и места проведения
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d • HH:mm", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Minsk"));
        String eventDetails = dateFormat.format(event.getStartDate().toDate());

        String address = event.getAddress();
        if (address != null && !address.isEmpty()) {
            eventDetails += " • " + address;
        }
        holder.tvDateTime.setText(eventDetails + " (EUROPE/MINSK)");

        // Местоположение
        holder.tvLocation.setText(address != null && !address.isEmpty() ? address : "Местоположение не указано");

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

        // Загрузка изображения
        if (event.getCoverImage() != null && !event.getCoverImage().isEmpty()) {
            Glide.with(context)
                    .load(event.getCoverImage())
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image)
                    .into(holder.imvEventContent);
        } else {
            holder.imvEventContent.setImageResource(R.drawable.default_image);
        }

        // Обработка клика
        holder.itemView.setOnClickListener(v -> openEventDetails(event));
    }

    private void openEventDetails(EventModel event) {
        EventDetailFragment fragment = new EventDetailFragment();
        Bundle args = new Bundle();

        // Основная информация
        args.putString("eventId", event.getEventId());
        args.putString("title", event.getTitle());
        args.putString("description", event.getDescription());
        args.putString("coverImage", event.getCoverImage());

        // Место проведения (только адрес, без onlineLink)
        args.putString("address", event.getAddress());

        // Даты
        args.putLong("startDate", event.getStartDate().toDate().getTime());
        args.putLong("endDate", event.getEndDate().toDate().getTime());

        // Дополнительная информация
        args.putInt("maxParticipants", event.getMaxParticipants());
        args.putInt("currentParticipants", event.getCurrentParticipants());

        fragment.setArguments(args);

        if (context instanceof FragmentActivity) {
            FragmentTransaction transaction = ((FragmentActivity) context)
                    .getSupportFragmentManager()
                    .beginTransaction();

            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack("event_detail");
            transaction.commit();
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imvEventContent;
        final TextView tvParticipants, tvEventTitle, tvDateTime, tvLocation;
        final ChipGroup chipGroupCategories;

        public ViewHolder(@NonNull View itemView) {
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