package com.example.actnow.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.actnow.Models.EventReportModel;
import com.example.actnow.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EventReportAdapter extends RecyclerView.Adapter<EventReportAdapter.EventReportViewHolder> {
    private List<EventReportModel> eventReports;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));

    public EventReportAdapter(List<EventReportModel> eventReports) {
        this.eventReports = eventReports;
    }

    @NonNull
    @Override
    public EventReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_report, parent, false);
        return new EventReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventReportViewHolder holder, int position) {
        EventReportModel report = eventReports.get(position);
        holder.bind(report);
    }

    @Override
    public int getItemCount() {
        return eventReports.size();
    }

    static class EventReportViewHolder extends RecyclerView.ViewHolder {
        private TextView tvEventName;
        private TextView tvEventDate;
        private TextView tvAttendance;
        private TextView tvRating;
        private TextView tvProfit;
        private TextView tvSummary;
        private ImageView ivEventPhoto;

        public EventReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tv_event_name);
            tvEventDate = itemView.findViewById(R.id.tv_event_date);
            tvAttendance = itemView.findViewById(R.id.tv_attendance);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvProfit = itemView.findViewById(R.id.tv_profit);
            tvSummary = itemView.findViewById(R.id.tv_summary);
            ivEventPhoto = itemView.findViewById(R.id.iv_event_photo);
        }

        public void bind(EventReportModel report) {
            tvEventName.setText(report.getEventName());
            tvEventDate.setText(new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"))
                    .format(report.getEventDate()));
            tvAttendance.setText(String.format("Посещаемость: %d/%d (%.1f%%)",
                    report.getTotalAttended(),
                    report.getTotalParticipants(),
                    report.getAttendanceRate()));
            tvRating.setText(String.format("Рейтинг: %.1f/5.0", report.getAverageRating()));
            tvProfit.setText(String.format("Прибыль: %.2f ₽", report.getProfit()));
            tvSummary.setText(report.getSummary());

            if (report.getPhotos() != null && !report.getPhotos().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(report.getPhotos().get(0))
                        .into(ivEventPhoto);
            }
        }
    }
} 