package com.example.actnow.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actnow.Fragments.EventDetailFragment;
import com.example.actnow.Models.ReviewModel;
import com.example.actnow.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<ReviewModel> reviewList;
    private Context context;
    private FragmentManager fragmentManager;

    public ReviewAdapter(List<ReviewModel> reviewList, Context context, FragmentManager fragmentManager) {
        this.reviewList = reviewList;
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reviewcontent, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReviewModel review = reviewList.get(position);
        holder.tvEventTitle.setText(review.getEventTitle() != null ? review.getEventTitle() : "Не указано");

        if (review.getCreatedAt() != null) {
            holder.tvDateTime.setText(formatDateTime(review.getCreatedAt().toDate()));
        } else {
            holder.tvDateTime.setText("Дата не указана");
        }
        setStarRating(holder, review.getRating());
        holder.tvBottomText.setText(review.getContent() != null ? review.getContent() : "Без текста");

        holder.itemView.setOnClickListener(v -> {
            EventDetailFragment fragment = new EventDetailFragment();
            Bundle args = new Bundle();
            args.putString("eventId", review.getEventId());
            fragment.setArguments(args);
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventTitle, tvDateTime, tvBottomText;
        ImageView star1, star2, star3, star4, star5;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventTitle = itemView.findViewById(R.id.tv_event_title);
            tvDateTime = itemView.findViewById(R.id.tv_date_time);
            tvBottomText = itemView.findViewById(R.id.tv_bottom_text);
            star1 = itemView.findViewById(R.id.star1);
            star2 = itemView.findViewById(R.id.star2);
            star3 = itemView.findViewById(R.id.star3);
            star4 = itemView.findViewById(R.id.star4);
            star5 = itemView.findViewById(R.id.star5);
        }
    }

    private String formatDateTime(Date date) {
        if (date == null) return "Дата не указана";
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d • HH:mm (z)", new Locale("ru"));
        return sdf.format(date);
    }

    private void setStarRating(ViewHolder holder, float rating) {
        int stars = (int) Math.floor(rating);
        boolean hasHalfStar = rating - stars >= 0.5f;

        holder.star1.setImageResource(stars >= 1 ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        holder.star2.setImageResource(stars >= 2 ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        holder.star3.setImageResource(stars >= 3 ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        holder.star4.setImageResource(stars >= 4 ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        holder.star5.setImageResource(stars >= 5 ? R.drawable.ic_star_filled : (hasHalfStar && stars == 4 ? R.drawable.ic_star_half : R.drawable.ic_star_outline));
    }
}