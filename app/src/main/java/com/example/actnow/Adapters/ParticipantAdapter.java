package com.example.actnow.Adapters;

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

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ParticipantViewHolder> {

    private List<UserProfile> participants;

    public ParticipantAdapter(List<UserProfile> participants) {
        this.participants = participants;
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
        UserProfile participant = participants.get(position);
        holder.tvParticipantName.setText(participant.getName());

        if (participant.getProfileImage() != null && !participant.getProfileImage().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(participant.getProfileImage())
                    .circleCrop()
                    .into(holder.ivParticipantPhoto);
        } else {
            holder.ivParticipantPhoto.setImageResource(R.drawable.default_profile_picture);
        }
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    static class ParticipantViewHolder extends RecyclerView.ViewHolder {
        ImageView ivParticipantPhoto;
        TextView tvParticipantName;

        public ParticipantViewHolder(@NonNull View itemView) {
            super(itemView);
            ivParticipantPhoto = itemView.findViewById(R.id.iv_participant_photo);
            tvParticipantName = itemView.findViewById(R.id.tv_participant_name);
        }
    }
}