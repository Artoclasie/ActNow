package com.example.actnow.Adapters;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UpcomingEventAdapter extends RecyclerView.Adapter<UpcomingEventAdapter.UpcomingEventViewHolder> {

    private List<EventModel> upcomingEvents;

    public UpcomingEventAdapter(List<EventModel> upcomingEvents) {
        this.upcomingEvents = upcomingEvents;
    }

    @NonNull
    @Override
    public UpcomingEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_upcoming_event, parent, false);
        return new UpcomingEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UpcomingEventViewHolder holder, int position) {
        EventModel event = upcomingEvents.get(position);

        holder.tvEventTitle.setText(event.getTitle());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        Date startDate = event.getStartDate().toDate();
        Date endDate = event.getEndDate().toDate();

        holder.tvEventDate.setText(dateFormat.format(startDate));
        holder.tvEventTime.setText(String.format("%s - %s",
                timeFormat.format(startDate),
                timeFormat.format(endDate)));

        if (event.getImages() != null && !event.getImages().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(event.getImages())
                    .into(holder.ivEventImage);
        } else {
            holder.ivEventImage.setImageResource(R.drawable.profiles);
        }
    }

    @Override
    public int getItemCount() {
        return upcomingEvents.size();
    }

    static class UpcomingEventViewHolder extends RecyclerView.ViewHolder {
        ImageView ivEventImage;
        TextView tvEventTitle, tvEventDate, tvEventTime;

        public UpcomingEventViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEventImage = itemView.findViewById(R.id.iv_event_image);
            tvEventTitle = itemView.findViewById(R.id.tv_event_title);
            tvEventDate = itemView.findViewById(R.id.tv_event_date);
            tvEventTime = itemView.findViewById(R.id.tv_event_time);
        }
    }
}