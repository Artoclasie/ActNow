package com.example.actnow.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.actnow.Models.UserProfile;
import com.example.actnow.R;
import java.util.List;

public class VolunteerAdapter extends RecyclerView.Adapter<VolunteerAdapter.VolunteerViewHolder> {
    private final List<UserProfile> volunteers;
    private Context context;
    private final OnVolunteerClickListener clickListener;

    public interface OnVolunteerClickListener {
        void onVolunteerClick(UserProfile user);
    }

    public VolunteerAdapter(List<UserProfile> volunteers, OnVolunteerClickListener clickListener) {
        this.volunteers = volunteers;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public VolunteerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_volunteers, parent, false);
        return new VolunteerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VolunteerViewHolder holder, int position) {
        UserProfile user = volunteers.get(position);
        holder.tvVolunteerName.setText(user.getName() != null ? user.getName() : "Без имени");
        holder.tvVolunteerCity.setText(user.getCity() != null ? user.getCity() : "Не указан город");

        String avatarUrl = user.getProfileImage();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.default_profile_picture)
                    .error(R.drawable.default_profile_picture)
                    .circleCrop()
                    .into(holder.ivVolunteerPhoto);
        } else {
            holder.ivVolunteerPhoto.setImageResource(R.drawable.default_profile_picture);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onVolunteerClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return volunteers.size();
    }

    static class VolunteerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivVolunteerPhoto;
        TextView tvVolunteerName;
        TextView tvVolunteerCity;

        VolunteerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivVolunteerPhoto = itemView.findViewById(R.id.iv_organizer_photo);
            tvVolunteerName = itemView.findViewById(R.id.tv_volunteer_name);
            tvVolunteerCity = itemView.findViewById(R.id.tv_volunteer_city);
        }
    }
}