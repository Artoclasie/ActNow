package com.example.actnow.Adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.actnow.Fragments.AnotherProfileFragment;
import com.example.actnow.Models.UserProfile;
import com.example.actnow.R;

import java.util.List;

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ParticipantViewHolder> {

    private List<UserProfile> participants;
    private int visibleCount = Integer.MAX_VALUE; // По умолчанию показываем всех

    public ParticipantAdapter(List<UserProfile> participants) {
        this.participants = participants;
    }

    public void setVisibleCount(int visibleCount) {
        this.visibleCount = visibleCount;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ParticipantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_participant, parent, false);
        return new ParticipantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantViewHolder holder, int position) {
        if (position >= visibleCount) return;
        UserProfile participant = participants.get(position);
        holder.tvVolunteerName.setText(participant.getName());
        holder.tvVolunteerCity.setText(participant.getCity() != null ? participant.getCity() : "Не указан");

        if (participant.getProfileImage() != null && !participant.getProfileImage().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(participant.getProfileImage())
                    .circleCrop()
                    .into(holder.ivOrganizerPhoto);
        } else {
            holder.ivOrganizerPhoto.setImageResource(R.drawable.default_profile_picture);
        }

        // Устанавливаем слушатель клика
        holder.itemView.setOnClickListener(v -> {
            AnotherProfileFragment fragment = new AnotherProfileFragment();
            Bundle args = new Bundle();
            args.putString("userId", participant.getUserId()); // Предполагается, что UserProfile имеет userId
            fragment.setArguments(args);
            if (holder.itemView.getContext() instanceof androidx.fragment.app.FragmentActivity) {
                androidx.fragment.app.FragmentTransaction transaction =
                        ((androidx.fragment.app.FragmentActivity) holder.itemView.getContext())
                                .getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return Math.min(visibleCount, participants.size());
    }

    static class ParticipantViewHolder extends RecyclerView.ViewHolder {
        ImageView ivOrganizerPhoto;
        TextView tvVolunteerName;
        TextView tvVolunteerCity;

        public ParticipantViewHolder(@NonNull View itemView) {
            super(itemView);
            ivOrganizerPhoto = itemView.findViewById(R.id.iv_organizer_photo);
            tvVolunteerName = itemView.findViewById(R.id.tv_volunteer_name);
            tvVolunteerCity = itemView.findViewById(R.id.tv_volunteer_city);
        }
    }
}